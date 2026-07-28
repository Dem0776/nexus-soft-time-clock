import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nexus_time_clock/src/features/home/presentation/home_screen.dart';

void main() {
  testWidgets('HomeScreen muestra el título, los accesos y permite cerrar sesión', (tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(home: HomeScreen()),
      ),
    );

    expect(find.text('Nexus Soft Time Clock'), findsOneWidget);
    expect(find.text('Registrar asistencia'), findsOneWidget);
    expect(find.text('Mi perfil'), findsOneWidget);
    expect(find.text('Mis vacaciones'), findsOneWidget);
    expect(find.byIcon(Icons.schedule), findsOneWidget);
    expect(find.byIcon(Icons.logout), findsOneWidget);
  });
}
