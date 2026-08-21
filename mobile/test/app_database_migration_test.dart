import 'dart:io';

import 'package:drift/drift.dart' show DatabaseConnection;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nexus_time_clock/src/core/db/app_database.dart';
import 'package:sqlite3/sqlite3.dart';

/// La cola local es offline-first: una marcación puede pasar horas esperando red. Al añadir las
/// columnas de evidencia (esquema v2), una app ya instalada debe seguir abriendo su base y
/// conservar lo pendiente. Sin MigrationStrategy la apertura falla y el registro se pierde.
void main() {
  late Directory tempDir;
  late File dbFile;

  setUp(() {
    tempDir = Directory.systemTemp.createTempSync('nexus_migration_test');
    dbFile = File('${tempDir.path}/nexus_time_clock.sqlite');
  });

  tearDown(() {
    if (tempDir.existsSync()) {
      tempDir.deleteSync(recursive: true);
    }
  });

  /// Deja en disco una base con el esquema exacto de la versión 1.
  void createV1Database() {
    final raw = sqlite3.open(dbFile.path);
    raw.execute(
      'CREATE TABLE pending_attendance_ops ('
      'operation_uuid TEXT NOT NULL PRIMARY KEY, '
      'payload TEXT NOT NULL, '
      "status TEXT NOT NULL DEFAULT 'PENDING', "
      'attempts INTEGER NOT NULL DEFAULT 0, '
      'last_error TEXT NULL, '
      'created_at INTEGER NOT NULL)',
    );
    raw.execute(
      "INSERT INTO pending_attendance_ops (operation_uuid, payload, created_at) "
      "VALUES ('op-1', '{\"eventType\":\"ENTRADA\"}', 0)",
    );
    raw.execute('PRAGMA user_version = 1');
    raw.dispose();
  }

  test('v1 → v2 conserva las operaciones pendientes y crea la caché de políticas', () async {
    createV1Database();

    final db = AppDatabase.forTesting(DatabaseConnection(NativeDatabase(dbFile)));

    // Abrir la base dispara onUpgrade.
    final pending = await db.pending();
    expect(pending, hasLength(1));
    expect(pending.single.operationUuid, 'op-1');

    // Las columnas nuevas existen y quedan vacías para las operaciones antiguas.
    expect(pending.single.evidencePath, null);
    expect(pending.single.evidenceKey, null);

    // La tabla nueva se creó y es usable.
    await db.saveSitePolicy('site-1', true, false, 50);
    expect((await db.sitePolicy('site-1'))?.requirePhoto, true);

    await db.close();
  });

  test('una operación nueva guarda la ruta de la foto pendiente', () async {
    final db = AppDatabase.forTesting(DatabaseConnection(NativeDatabase(dbFile)));

    await db.enqueue('op-2', '{}', evidencePath: '/fotos/op-2.jpg', evidenceSha256: 'abc');
    final row = await db.findByUuid('op-2');

    expect(row?.evidencePath, '/fotos/op-2.jpg');
    expect(row?.evidenceSha256, 'abc');

    // Tras subirla se anota la referencia que viajará en el payload.
    await db.markEvidenceUploaded('op-2', 'evidence', 't/x/y.jpg');
    final uploaded = await db.findByUuid('op-2');
    expect(uploaded?.evidenceBucket, 'evidence');
    expect(uploaded?.evidenceKey, 't/x/y.jpg');

    await db.close();
  });
}
