import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../data/profile_repository.dart';
import '../domain/my_profile.dart';

/// Mi perfil (solo lectura): datos personales del colaborador. Para cambios, RR.HH.
class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  static const _genders = {
    'FEMALE': 'Femenino', 'MALE': 'Masculino', 'OTHER': 'Otro', 'UNDISCLOSED': 'Prefiere no decir',
  };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final async = ref.watch(myProfileProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mi perfil')),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => _ErrorView(onRetry: () => ref.invalidate(myProfileProvider)),
        data: (p) => RefreshIndicator(
          onRefresh: () async => ref.invalidate(myProfileProvider),
          child: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _Header(profile: p),
              const SizedBox(height: 20),
              _Section(title: 'Datos personales', children: [
                _Row(icon: Icons.badge_outlined, label: 'Código de empleado', value: p.employeeCode),
                _Row(icon: Icons.mail_outline, label: 'Correo', value: p.email),
                _Row(icon: Icons.cake_outlined, label: 'Fecha de nacimiento', value: _fmt(p.birthDate)),
                _Row(icon: Icons.wc_outlined, label: 'Género', value: _genders[p.gender]),
                _Row(icon: Icons.call_outlined, label: 'Teléfono', value: p.phone),
                _Row(icon: Icons.place_outlined, label: 'Dirección', value: p.address),
              ]),
              const SizedBox(height: 16),
              _Section(title: 'Datos de empresa', children: [
                _Row(icon: Icons.event_available_outlined, label: 'Fecha de ingreso', value: _fmt(p.hireDate)),
              ]),
              const SizedBox(height: 16),
              _Section(title: 'Contacto de emergencia', children: [
                _Row(icon: Icons.person_outline, label: 'Nombre', value: p.emergencyContactName),
                _Row(icon: Icons.phone_outlined, label: 'Teléfono', value: p.emergencyContactPhone),
              ]),
              const SizedBox(height: 20),
              OutlinedButton.icon(
                onPressed: () => context.pushNamed('changePassword'),
                icon: const Icon(Icons.lock_reset),
                label: const Text('Cambiar contraseña'),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Icon(Icons.lock_outline, size: 16, color: theme.colorScheme.onSurfaceVariant),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Información de solo lectura. Para actualizar tus datos, contacta a Recursos Humanos.',
                      style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  static String? _fmt(String? iso) {
    if (iso == null || iso.length < 10) return iso;
    final p = iso.substring(0, 10).split('-');
    return p.length == 3 ? '${p[2]}/${p[1]}/${p[0]}' : iso;
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.profile});
  final MyProfile profile;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final name = (profile.fullName ?? '').trim();
    final initials = name.isEmpty
        ? '?'
        : name.split(RegExp(r'\s+')).take(2).map((s) => s.isEmpty ? '' : s[0]).join().toUpperCase();
    return Column(
      children: [
        CircleAvatar(
          radius: 44,
          backgroundColor: theme.colorScheme.primaryContainer,
          child: Text(initials,
              style: theme.textTheme.headlineSmall?.copyWith(color: theme.colorScheme.onPrimaryContainer)),
        ),
        const SizedBox(height: 14),
        Text(name.isEmpty ? 'Colaborador' : name,
            style: theme.textTheme.titleLarge, textAlign: TextAlign.center),
        if (profile.email != null)
          Text(profile.email!, style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
      ],
    );
  }
}

class _Section extends StatelessWidget {
  const _Section({required this.title, required this.children});
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 8),
          child: Text(title.toUpperCase(),
              style: theme.textTheme.labelMedium?.copyWith(
                  color: theme.colorScheme.primary, fontWeight: FontWeight.w700, letterSpacing: 0.6)),
        ),
        Card(child: Padding(padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4), child: Column(children: children))),
      ],
    );
  }
}

class _Row extends StatelessWidget {
  const _Row({required this.icon, required this.label, required this.value});
  final IconData icon;
  final String label;
  final String? value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ListTile(
      dense: true,
      leading: Icon(icon, color: theme.colorScheme.onSurfaceVariant),
      title: Text(label, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
      subtitle: Text(
        (value == null || value!.trim().isEmpty) ? '—' : value!,
        style: theme.textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w600),
      ),
    );
  }
}

class _ErrorView extends StatelessWidget {
  const _ErrorView({required this.onRetry});
  final VoidCallback onRetry;
  @override
  Widget build(BuildContext context) => Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.error_outline, size: 40),
          const SizedBox(height: 12),
          const Text('No se pudo cargar tu perfil.'),
          const SizedBox(height: 12),
          FilledButton(onPressed: onRetry, child: const Text('Reintentar')),
        ]),
      );
}
