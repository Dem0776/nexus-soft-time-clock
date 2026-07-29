import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../auth/application/auth_controller.dart';

/// Pantalla principal tras iniciar sesión: accesos a registro de asistencia, perfil
/// y vacaciones. Diseño de tarjetas consistente con la marca (índigo).
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Nexus Soft Time Clock'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar sesión',
            onPressed: () => ref.read(authControllerProvider.notifier).logout(),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            // Encabezado destacado
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(22),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                gradient: LinearGradient(
                  colors: [theme.colorScheme.primary, theme.colorScheme.primary.withValues(alpha: 0.78)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(Icons.schedule, color: theme.colorScheme.onPrimary, size: 30),
                  const SizedBox(height: 12),
                  Text('Hola 👋',
                      style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.onPrimary.withValues(alpha: 0.9))),
                  Text('¿Qué quieres hacer hoy?',
                      style: theme.textTheme.headlineSmall?.copyWith(color: theme.colorScheme.onPrimary)),
                ],
              ),
            ),
            const SizedBox(height: 22),

            _ActionCard(
              icon: Icons.fingerprint,
              title: 'Registrar asistencia',
              subtitle: 'Entrada / salida con QR y GPS',
              highlighted: true,
              onTap: () => context.push('/attendance'),
            ),
            const SizedBox(height: 12),
            _ActionCard(
              icon: Icons.history,
              title: 'Mi historial',
              subtitle: 'Mi historial de asistencia',
              onTap: () => context.push('/attendance/history'),
            ),
            const SizedBox(height: 12),
            _ActionCard(
              icon: Icons.badge_outlined,
              title: 'Mi perfil',
              subtitle: 'Consulta tus datos personales',
              onTap: () => context.push('/profile'),
            ),
            const SizedBox(height: 12),
            _ActionCard(
              icon: Icons.beach_access_outlined,
              title: 'Mis vacaciones',
              subtitle: 'Días disponibles y solicitudes',
              onTap: () => context.push('/vacations'),
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
    this.highlighted = false,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final bool highlighted;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bg = highlighted ? theme.colorScheme.primaryContainer : theme.colorScheme.surfaceContainerLow;
    final fg = highlighted ? theme.colorScheme.onPrimaryContainer : theme.colorScheme.primary;
    return Card(
      color: bg,
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: fg.withValues(alpha: 0.14),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(icon, color: fg),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                    const SizedBox(height: 2),
                    Text(subtitle,
                        style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: theme.colorScheme.onSurfaceVariant),
            ],
          ),
        ),
      ),
    );
  }
}
