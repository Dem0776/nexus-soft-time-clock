import 'package:flutter/material.dart';

/// Semilla de color de la marca (azul Nexus). A partir de ella Material 3 deriva
/// el [ColorScheme] completo para claro/oscuro. Los tokens de componentes
/// (campos, botones, tarjetas, appbars) se definen en [_themeFor] para dar una
/// apariencia consistente y profesional en toda la app.
const Color _seed = Color(0xFF1565C0);

/// Radios de esquina usados en toda la UI para mantener una forma coherente.
const double _radiusField = 12;
const double _radiusButton = 12;
const double _radiusCard = 16;

ThemeData buildLightTheme() => _themeFor(
      ColorScheme.fromSeed(seedColor: _seed, brightness: Brightness.light),
    );

ThemeData buildDarkTheme() => _themeFor(
      ColorScheme.fromSeed(seedColor: _seed, brightness: Brightness.dark),
    );

/// Construye el tema Material 3 a partir de un [ColorScheme], aplicando los
/// tokens de componentes compartidos entre claro y oscuro. Solo estética: no
/// altera comportamiento de widgets.
ThemeData _themeFor(ColorScheme scheme) {
  final base = ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: scheme.surface,
    visualDensity: VisualDensity.adaptivePlatformDensity,
  );

  final buttonShape = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(_radiusButton),
  );
  const buttonTextStyle = TextStyle(
    fontSize: 15,
    fontWeight: FontWeight.w600,
    letterSpacing: 0.2,
  );

  return base.copyWith(
    // Jerarquía tipográfica más marcada para títulos.
    textTheme: base.textTheme.copyWith(
      headlineSmall: base.textTheme.headlineSmall?.copyWith(
        fontWeight: FontWeight.w700,
        letterSpacing: -0.2,
      ),
      titleLarge: base.textTheme.titleLarge?.copyWith(
        fontWeight: FontWeight.w600,
      ),
      titleMedium: base.textTheme.titleMedium?.copyWith(
        fontWeight: FontWeight.w600,
      ),
    ),

    appBarTheme: AppBarTheme(
      centerTitle: true,
      elevation: 0,
      scrolledUnderElevation: 2,
      backgroundColor: scheme.surface,
      foregroundColor: scheme.onSurface,
      surfaceTintColor: scheme.surfaceTint,
      titleTextStyle: base.textTheme.titleLarge?.copyWith(
        fontWeight: FontWeight.w600,
        color: scheme.onSurface,
      ),
    ),

    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: scheme.surfaceContainerHighest.withValues(alpha: 0.4),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      prefixIconColor: scheme.onSurfaceVariant,
      floatingLabelStyle: TextStyle(color: scheme.primary),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radiusField),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radiusField),
        borderSide: BorderSide(color: scheme.outlineVariant),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radiusField),
        borderSide: BorderSide(color: scheme.primary, width: 1.5),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radiusField),
        borderSide: BorderSide(color: scheme.error),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radiusField),
        borderSide: BorderSide(color: scheme.error, width: 1.5),
      ),
    ),

    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size.fromHeight(52),
        shape: buttonShape,
        textStyle: buttonTextStyle,
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        minimumSize: const Size.fromHeight(52),
        shape: buttonShape,
        textStyle: buttonTextStyle,
        side: BorderSide(color: scheme.outline),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        shape: buttonShape,
        textStyle: buttonTextStyle,
      ),
    ),

    cardTheme: CardThemeData(
      elevation: 0,
      margin: EdgeInsets.zero,
      color: scheme.surfaceContainerLow,
      surfaceTintColor: scheme.surfaceTint,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(_radiusCard),
      ),
    ),

    snackBarTheme: SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(_radiusField),
      ),
    ),

    dividerTheme: DividerThemeData(
      color: scheme.outlineVariant,
      space: 1,
      thickness: 1,
    ),
  );
}
