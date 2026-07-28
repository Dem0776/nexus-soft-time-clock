import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/vacation_repository.dart';
import '../domain/vacation_models.dart';

/// Mis vacaciones: días que me corresponden según mi antigüedad, mis solicitudes y
/// la opción de solicitar nuevas.
class VacationsScreen extends ConsumerWidget {
  const VacationsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final summary = ref.watch(vacationSummaryProvider);
    final requests = ref.watch(myVacationsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mis vacaciones')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openRequest(context, ref),
        icon: const Icon(Icons.add),
        label: const Text('Solicitar'),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(vacationSummaryProvider);
          ref.invalidate(myVacationsProvider);
        },
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 96),
          children: [
            summary.when(
              loading: () => const Card(child: SizedBox(height: 150, child: Center(child: CircularProgressIndicator()))),
              error: (e, _) => const Card(child: Padding(padding: EdgeInsets.all(20), child: Text('No se pudo cargar tu saldo de vacaciones.'))),
              data: (s) => _SummaryCard(summary: s),
            ),
            const SizedBox(height: 24),
            Text('Mis solicitudes', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            requests.when(
              loading: () => const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator())),
              error: (e, _) => const Text('No se pudieron cargar tus solicitudes.'),
              data: (list) => list.isEmpty
                  ? _empty(context)
                  : Column(children: [for (final r in list) _RequestTile(request: r)]),
            ),
          ],
        ),
      ),
    );
  }

  Widget _empty(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 16),
        child: Column(children: [
          Icon(Icons.beach_access_outlined, size: 40, color: theme.colorScheme.onSurfaceVariant),
          const SizedBox(height: 10),
          Text('Aún no has solicitado vacaciones.', style: theme.textTheme.bodyMedium),
        ]),
      ),
    );
  }

  Future<void> _openRequest(BuildContext context, WidgetRef ref) async {
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => const _RequestSheet(),
    );
    if (ok == true) {
      ref.invalidate(vacationSummaryProvider);
      ref.invalidate(myVacationsProvider);
    }
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({required this.summary});
  final VacationSummary summary;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              Icon(Icons.wb_sunny_outlined, color: theme.colorScheme.primary),
              const SizedBox(width: 8),
              Text('${summary.yearsOfService} ${summary.yearsOfService == 1 ? 'año' : 'años'} en la empresa',
                  style: theme.textTheme.titleMedium),
            ]),
            const SizedBox(height: 6),
            Text('Te corresponden ${summary.entitledDays} días de vacaciones al año según tu antigüedad.',
                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            const SizedBox(height: 18),
            Row(children: [
              _Stat(value: summary.availableDays, label: 'Disponibles', color: theme.colorScheme.primary),
              _Stat(value: summary.takenDays, label: 'Tomados', color: theme.colorScheme.onSurfaceVariant),
              _Stat(value: summary.pendingDays, label: 'Pendientes', color: Colors.orange.shade700),
            ]),
          ],
        ),
      ),
    );
  }
}

class _Stat extends StatelessWidget {
  const _Stat({required this.value, required this.label, required this.color});
  final int value;
  final String label;
  final Color color;
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Expanded(
      child: Column(children: [
        Text('$value', style: theme.textTheme.headlineMedium?.copyWith(color: color, fontWeight: FontWeight.w800)),
        Text(label, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
      ]),
    );
  }
}

class _RequestTile extends StatelessWidget {
  const _RequestTile({required this.request});
  final MyVacationRequest request;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = _statusStyle(request.status, theme);
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        leading: CircleAvatar(backgroundColor: s.$2.withValues(alpha: 0.16), child: Icon(Icons.beach_access, color: s.$2, size: 20)),
        title: Text('${_fmt(request.startDate)} – ${_fmt(request.endDate)}', style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text('${request.days} ${request.days == 1 ? 'día' : 'días'}${request.reason != null ? ' · ${request.reason}' : ''}'),
        trailing: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
          decoration: BoxDecoration(color: s.$2.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(999)),
          child: Text(s.$1, style: theme.textTheme.labelSmall?.copyWith(color: s.$2, fontWeight: FontWeight.w700)),
        ),
      ),
    );
  }

  static (String, Color) _statusStyle(String status, ThemeData theme) {
    switch (status) {
      case 'APPROVED':
        return ('Aprobada', Colors.green.shade600);
      case 'REJECTED':
        return ('Rechazada', theme.colorScheme.error);
      case 'CANCELLED':
        return ('Cancelada', theme.colorScheme.onSurfaceVariant);
      default:
        return ('Pendiente', Colors.orange.shade700);
    }
  }

  static String _fmt(String iso) {
    if (iso.length < 10) return iso;
    final p = iso.substring(0, 10).split('-');
    return p.length == 3 ? '${p[2]}/${p[1]}' : iso;
  }
}

/// Hoja inferior para capturar una nueva solicitud (fechas + motivo).
class _RequestSheet extends ConsumerStatefulWidget {
  const _RequestSheet();
  @override
  ConsumerState<_RequestSheet> createState() => _RequestSheetState();
}

class _RequestSheetState extends ConsumerState<_RequestSheet> {
  DateTime? _start;
  DateTime? _end;
  final _reason = TextEditingController();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _reason.dispose();
    super.dispose();
  }

  Future<void> _pick(bool isStart) async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: (isStart ? _start : _end) ?? now,
      firstDate: now.subtract(const Duration(days: 1)),
      lastDate: DateTime(now.year + 2),
    );
    if (picked != null) {
      setState(() {
        if (isStart) {
          _start = picked;
          if (_end != null && _end!.isBefore(picked)) _end = picked;
        } else {
          _end = picked;
        }
      });
    }
  }

  String _iso(DateTime d) =>
      '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  Future<void> _submit() async {
    if (_start == null || _end == null) {
      setState(() => _error = 'Elige las fechas de inicio y fin.');
      return;
    }
    if (_end!.isBefore(_start!)) {
      setState(() => _error = 'La fecha fin no puede ser anterior al inicio.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await ref.read(vacationRepositoryProvider).request(
            startDate: _iso(_start!),
            endDate: _iso(_end!),
            reason: _reason.text.trim(),
          );
      if (mounted) Navigator.of(context).pop(true);
    } catch (_) {
      setState(() {
        _saving = false;
        _error = 'No se pudo registrar la solicitud (revisa traslapes con otra solicitud).';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bottom = MediaQuery.of(context).viewInsets.bottom;
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 4, 20, 20 + bottom),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Solicitar vacaciones', style: theme.textTheme.titleLarge),
          const SizedBox(height: 4),
          Text('Los días se calculan según la política de la empresa.',
              style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
          const SizedBox(height: 16),
          Row(children: [
            Expanded(child: _DateField(label: 'Desde', value: _start, onTap: () => _pick(true))),
            const SizedBox(width: 12),
            Expanded(child: _DateField(label: 'Hasta', value: _end, onTap: () => _pick(false))),
          ]),
          const SizedBox(height: 12),
          TextField(
            controller: _reason,
            decoration: const InputDecoration(labelText: 'Motivo (opcional)', prefixIcon: Icon(Icons.notes_outlined)),
          ),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Text(_error!, style: TextStyle(color: theme.colorScheme.error)),
          ],
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: FilledButton.icon(
              onPressed: _saving ? null : _submit,
              icon: _saving
                  ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.check),
              label: const Text('Enviar solicitud'),
            ),
          ),
        ],
      ),
    );
  }
}

class _DateField extends StatelessWidget {
  const _DateField({required this.label, required this.value, required this.onTap});
  final String label;
  final DateTime? value;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final text = value == null
        ? 'Elegir'
        : '${value!.day.toString().padLeft(2, '0')}/${value!.month.toString().padLeft(2, '0')}/${value!.year}';
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: InputDecorator(
        decoration: InputDecoration(labelText: label, prefixIcon: const Icon(Icons.calendar_today_outlined)),
        child: Text(text),
      ),
    );
  }
}
