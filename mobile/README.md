# Nexus Soft Time Clock — App móvil (Flutter)

App de empleados **offline-first**. Estructura **feature-first** con Clean Architecture por feature.

## Estructura

```
lib/
├── main.dart                 # entry point (ProviderScope)
├── l10n/                     # internacionalización (arb → gen-l10n)
└── src/
    ├── app/                  # App raíz, router (GoRouter), tema (Material 3)
    ├── core/                 # (próximas iteraciones) red Dio, storage seguro, errores
    └── features/
        └── home/
            └── presentation/ # HomeScreen (placeholder)
```

Cada feature seguirá `data / domain / presentation` (Clean Architecture) con Riverpod
para estado, Drift para persistencia local y Dio para red.

## Requisitos

- Flutter (última estable) — **no instalado en el entorno de generación**, verificar localmente.

## Comandos

```bash
flutter pub get
flutter gen-l10n           # genera AppLocalizations desde lib/l10n/*.arb
dart run build_runner build --delete-conflicting-outputs   # freezed/json/riverpod/drift
flutter analyze
flutter test
flutter run
```
