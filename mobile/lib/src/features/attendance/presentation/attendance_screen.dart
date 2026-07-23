import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/services/biometric_service.dart';
import '../application/attendance_controller.dart';
import '../data/event_type_service.dart';
import '../domain/qr_token.dart';
import 'qr_scanner_screen.dart';

/// Pantalla de registro de asistencia (offline-first). El empleado elige Entrada/Salida
/// y escanea el QR firmado del centro con la cámara: de ahí se obtiene el token crudo y
/// se deriva el centro (workSiteId). No hay entrada manual de datos.
class AttendanceScreen extends ConsumerStatefulWidget {
  const AttendanceScreen({super.key});

  @override
  ConsumerState<AttendanceScreen> createState() => _AttendanceScreenState();
}

class _AttendanceScreenState extends ConsumerState<AttendanceScreen> {
  Future<void> _register(String eventType) async {
    // 1) Abrir la cámara y esperar el QR (o null si el usuario cancela).
    final raw = await Navigator.of(context).push<String>(
      MaterialPageRoute(builder: (_) => const QrScannerScreen()),
    );
    if (!mounted || raw == null) {
      return;
    }

    // 2) Derivar el centro del token firmado; si el QR no es reconocible, avisar.
    final workSiteId = workSiteIdFromQrToken(raw);
    if (workSiteId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('QR no válido.')),
      );
      return;
    }

    // 3) Autenticación biométrica local (HU-14). Es oportunista: el backend rechaza si el
    //    centro la exige y no fue exitosa. Si el dispositivo no la soporta, devuelve false.
    final biometricOk = await ref
        .read(biometricServiceProvider)
        .authenticate('Confirma tu identidad para registrar tu asistencia');
    if (!mounted) {
      return;
    }

    // 4) Registrar (GPS → cola local → sincronización) con el token crudo.
    await ref.read(attendanceControllerProvider.notifier).register(
          eventType: eventType,
          workSiteId: workSiteId,
          qrToken: raw,
          biometricVerified: biometricOk,
        );
    final message = ref.read(attendanceControllerProvider).message;
    if (mounted && message != null) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(attendanceControllerProvider);

    final theme = Theme.of(context);
    final scheme = theme.colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Registrar asistencia'),
        actions: [
          IconButton(
            icon: Badge(label: Text('${state.pendingCount}'), child: const Icon(Icons.sync)),
            tooltip: 'Sincronizar pendientes',
            onPressed: state.busy ? null : () => ref.read(attendanceControllerProvider.notifier).syncNow(),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 480),
          child: Column(
            children: [
              Card(
                color: scheme.primaryContainer,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      Icon(Icons.qr_code_2, size: 32, color: scheme.onPrimaryContainer),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Text(
                          'Toca Entrada o Salida y escanea el QR del centro con la cámara.',
                          style: theme.textTheme.bodyMedium?.copyWith(color: scheme.onPrimaryContainer),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 20),
              SizedBox(
                height: 4,
                child: state.busy
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: const LinearProgressIndicator(),
                      )
                    : null,
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: FilledButton.icon(
                      onPressed: state.busy ? null : () => _register('ENTRADA'),
                      icon: const Icon(Icons.login),
                      label: const Text('Entrada'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FilledButton.tonalIcon(
                      onPressed: state.busy ? null : () => _register('SALIDA'),
                      icon: const Icon(Icons.logout),
                      label: const Text('Salida'),
                    ),
                  ),
                ],
              ),
              _IntermediateEvents(busy: state.busy, onRegister: _register),
              const SizedBox(height: 24),
              _PendingStatus(count: state.pendingCount, theme: theme, scheme: scheme),
            ],
          ),
        ),
      ),
    );
  }
}

/// Botones dinámicos de eventos intermedios (descanso, cambio de sitio) según los tipos
/// habilitados por la empresa (HU-12 CA1). Si no hay ninguno o la consulta falla, no muestra nada.
class _IntermediateEvents extends ConsumerWidget {
  const _IntermediateEvents({required this.busy, required this.onRegister});

  final bool busy;
  final Future<void> Function(String eventType) onRegister;

  IconData _iconFor(String eventType) => switch (eventType) {
        'INICIO_DESCANSO' => Icons.free_breakfast_outlined,
        'FIN_DESCANSO' => Icons.work_history_outlined,
        'CAMBIO_SITIO' => Icons.place_outlined,
        _ => Icons.more_horiz,
      };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final typesAsync = ref.watch(enabledIntermediateEventTypesProvider);
    return typesAsync.maybeWhen(
      data: (types) {
        if (types.isEmpty) {
          return const SizedBox.shrink();
        }
        return Padding(
          padding: const EdgeInsets.only(top: 12),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final t in types)
                OutlinedButton.icon(
                  onPressed: busy ? null : () => onRegister(t.eventType),
                  icon: Icon(_iconFor(t.eventType)),
                  label: Text(t.label),
                ),
            ],
          ),
        );
      },
      orElse: () => const SizedBox.shrink(),
    );
  }
}

/// Tarjeta de estado con el número de operaciones aún por sincronizar.
class _PendingStatus extends StatelessWidget {
  const _PendingStatus({required this.count, required this.theme, required this.scheme});

  final int count;
  final ThemeData theme;
  final ColorScheme scheme;

  @override
  Widget build(BuildContext context) {
    final synced = count == 0;
    return Card(
      color: scheme.surfaceContainerHighest.withValues(alpha: 0.4),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: [
            Icon(
              synced ? Icons.cloud_done_outlined : Icons.cloud_upload_outlined,
              color: synced ? scheme.primary : scheme.tertiary,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                'Operaciones pendientes de sincronizar',
                style: theme.textTheme.bodyMedium?.copyWith(color: scheme.onSurfaceVariant),
              ),
            ),
            Text(
              '$count',
              style: theme.textTheme.titleLarge?.copyWith(
                color: synced ? scheme.primary : scheme.tertiary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
