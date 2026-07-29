import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../application/admin_vacations_controller.dart';
import '../domain/admin_vacation_models.dart';

/// Bandeja de aprobación de vacaciones (COMPANY_ADMIN / HR_ADMIN). Espejo móvil de
/// la bandeja del portal web: filtra por estado y resuelve (aprobar/rechazar) con nota.
class AdminVacationsScreen extends ConsumerWidget {
  const AdminVacationsScreen({super.key});

  static const _filters = <(String, String)>[
    ('PENDING', 'Pendientes'),
    ('APPROVED', 'Aprobadas'),
    ('REJECTED', 'Rechazadas'),
    ('', 'Todas'),
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(adminVacationsControllerProvider);
    final controller = ref.read(adminVacationsControllerProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: const Text('Aprobar vacaciones')),
      body: Column(
        children: [
          _FilterBar(
            selected: state.statusFilter,
            filters: _filters,
            onSelected: controller.setStatus,
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: controller.load,
              child: _Body(state: state, controller: controller),
            ),
          ),
        ],
      ),
    );
  }
}

class _FilterBar extends StatelessWidget {
  const _FilterBar({required this.selected, required this.filters, required this.onSelected});

  final String selected;
  final List<(String, String)> filters;
  final ValueChanged<String> onSelected;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          for (final f in filters) ...[
            ChoiceChip(
              label: Text(f.$2),
              selected: selected == f.$1,
              onSelected: (_) => onSelected(f.$1),
            ),
            const SizedBox(width: 8),
          ],
        ],
      ),
    );
  }
}

class _Body extends StatelessWidget {
  const _Body({required this.state, required this.controller});

  final AdminVacationsState state;
  final AdminVacationsController controller;

  @override
  Widget build(BuildContext context) {
    if (state.loading) {
      return const Center(child: Padding(padding: EdgeInsets.all(32), child: CircularProgressIndicator()));
    }
    if (state.error != null && state.items.isEmpty) {
      return _messageList(child: Text(state.error!, textAlign: TextAlign.center));
    }
    if (state.items.isEmpty) {
      return _messageList(child: _empty(context));
    }

    return ListView.builder(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
      itemCount: state.items.length + (state.hasMore ? 1 : 0),
      itemBuilder: (context, index) {
        if (index >= state.items.length) {
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 12),
            child: Center(
              child: state.loadingMore
                  ? const CircularProgressIndicator()
                  : OutlinedButton(onPressed: controller.loadMore, child: const Text('Cargar más')),
            ),
          );
        }
        return _RequestCard(
          request: state.items[index],
          onTap: () => _openDetail(context, state.items[index], controller),
        );
      },
    );
  }

  // La lista debe poder desplazarse para que RefreshIndicator funcione aun cuando está vacía.
  Widget _messageList({required Widget child}) => ListView(
        padding: const EdgeInsets.fromLTRB(24, 80, 24, 24),
        children: [Center(child: child)],
      );

  Widget _empty(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Icon(Icons.beach_access_outlined, size: 44, color: theme.colorScheme.onSurfaceVariant),
        const SizedBox(height: 12),
        Text('No hay solicitudes para mostrar.', style: theme.textTheme.bodyMedium),
      ],
    );
  }
}

class _RequestCard extends StatelessWidget {
  const _RequestCard({required this.request, required this.onTap});

  final AdminVacationRequest request;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = statusStyle(request.status, theme);
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: CircleAvatar(
          backgroundColor: s.$2.withValues(alpha: 0.16),
          child: Icon(Icons.beach_access, color: s.$2, size: 20),
        ),
        title: Text(request.displayName, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (request.employeeCode != null && request.employeeCode!.isNotEmpty)
              Text(request.employeeCode!, style: theme.textTheme.bodySmall),
            const SizedBox(height: 2),
            Text('${fmtDate(request.startDate)} – ${fmtDate(request.endDate)} · '
                '${request.days} ${request.days == 1 ? 'día' : 'días'}'),
            const SizedBox(height: 6),
            _StatusPill(status: request.status),
          ],
        ),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({required this.status});
  final String status;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = statusStyle(status, theme);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: s.$2.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(999)),
      child: Text(s.$1, style: theme.textTheme.labelSmall?.copyWith(color: s.$2, fontWeight: FontWeight.w700)),
    );
  }
}

Future<void> _openDetail(
  BuildContext context,
  AdminVacationRequest request,
  AdminVacationsController controller,
) async {
  await showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    showDragHandle: true,
    builder: (_) => _ResolveSheet(request: request, controller: controller),
  );
}

/// Hoja de detalle + resolución. Para PENDING muestra nota + botones Aprobar/Rechazar;
/// para el resto es solo lectura.
class _ResolveSheet extends StatefulWidget {
  const _ResolveSheet({required this.request, required this.controller});

  final AdminVacationRequest request;
  final AdminVacationsController controller;

  @override
  State<_ResolveSheet> createState() => _ResolveSheetState();
}

class _ResolveSheetState extends State<_ResolveSheet> {
  final _note = TextEditingController();
  bool _saving = false;

  @override
  void dispose() {
    _note.dispose();
    super.dispose();
  }

  Future<void> _resolve(bool approve) async {
    setState(() => _saving = true);
    final ok = await widget.controller.resolve(
      widget.request,
      approve: approve,
      note: _note.text.trim(),
    );
    if (!mounted) return;
    if (ok) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(approve ? 'Solicitud aprobada.' : 'Solicitud rechazada.')),
      );
    } else {
      setState(() => _saving = false);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No se pudo resolver la solicitud.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final r = widget.request;
    final bottom = MediaQuery.of(context).viewInsets.bottom;
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 4, 20, 20 + bottom),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(r.displayName, style: theme.textTheme.titleLarge),
          if (r.employeeCode != null && r.employeeCode!.isNotEmpty)
            Text(r.employeeCode!, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
          const SizedBox(height: 16),
          _detailRow(theme, 'Periodo', '${fmtDate(r.startDate)} – ${fmtDate(r.endDate)}'),
          _detailRow(theme, 'Días solicitados', '${r.days}'),
          if (r.reason != null && r.reason!.isNotEmpty) _detailRow(theme, 'Motivo', r.reason!),
          _detailRow(theme, 'Estado', statusStyle(r.status, theme).$1),
          if (r.resolvedBy != null && r.resolvedBy!.isNotEmpty) _detailRow(theme, 'Resuelto por', r.resolvedBy!),
          if (r.resolutionNote != null && r.resolutionNote!.isNotEmpty)
            _detailRow(theme, 'Nota', r.resolutionNote!),
          if (r.isPending) ...[
            const SizedBox(height: 16),
            TextField(
              controller: _note,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Nota para el colaborador (opcional)',
                alignLabelWithHint: true,
              ),
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _saving ? null : () => _resolve(false),
                    icon: const Icon(Icons.close),
                    label: const Text('Rechazar'),
                    style: OutlinedButton.styleFrom(foregroundColor: theme.colorScheme.error),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton.icon(
                    onPressed: _saving ? null : () => _resolve(true),
                    icon: _saving
                        ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Icon(Icons.check),
                    label: const Text('Aprobar'),
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _detailRow(ThemeData theme, String label, String value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 130,
              child: Text(label, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            ),
            Expanded(child: Text(value, style: theme.textTheme.bodyMedium)),
          ],
        ),
      );
}

/// Estilo (etiqueta, color) por estado — mismo criterio que la pantalla del colaborador.
(String, Color) statusStyle(String status, ThemeData theme) {
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

/// Formatea una fecha ISO (yyyy-MM-dd...) a dd/MM; devuelve el original si no aplica.
String fmtDate(String iso) {
  if (iso.length < 10) return iso;
  final p = iso.substring(0, 10).split('-');
  return p.length == 3 ? '${p[2]}/${p[1]}' : iso;
}
