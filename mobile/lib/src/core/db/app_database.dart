import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

part 'app_database.g.dart';

/// Cola local de operaciones de asistencia creadas offline (offline-first, RF-21).
/// Cada operación se persiste ANTES de intentar enviarse; nunca se pierde por falta de red.
class PendingAttendanceOps extends Table {
  TextColumn get operationUuid => text()();
  TextColumn get payload => text()(); // JSON del RegisterAttendanceRequest
  TextColumn get status => text().withDefault(const Constant('PENDING'))(); // PENDING/SYNCED/REJECTED/ERROR
  IntColumn get attempts => integer().withDefault(const Constant(0))();
  TextColumn get lastError => text().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column<Object>> get primaryKey => {operationUuid};
}

@DriftDatabase(tables: [PendingAttendanceOps])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;

  Future<void> enqueue(String operationUuid, String payload) {
    return into(pendingAttendanceOps).insert(
      PendingAttendanceOpsCompanion.insert(operationUuid: operationUuid, payload: payload),
      mode: InsertMode.insertOrIgnore, // idempotente: no duplica el mismo UUID
    );
  }

  Future<List<PendingAttendanceOp>> pending() {
    return (select(pendingAttendanceOps)
          ..where((t) => t.status.equals('PENDING'))
          ..orderBy([(t) => OrderingTerm.asc(t.createdAt)]))
        .get();
  }

  Future<int> pendingCount() async {
    final rows = await (select(pendingAttendanceOps)..where((t) => t.status.equals('PENDING'))).get();
    return rows.length;
  }

  Future<void> markStatus(String operationUuid, String status, String? error) {
    return (update(pendingAttendanceOps)..where((t) => t.operationUuid.equals(operationUuid)))
        .write(PendingAttendanceOpsCompanion(
      status: Value(status),
      lastError: Value(error),
    ),);
  }

  Future<void> incrementAttempts(String operationUuid, String error) {
    return customUpdate(
      'UPDATE pending_attendance_ops SET attempts = attempts + 1, last_error = ? WHERE operation_uuid = ?',
      variables: [Variable(error), Variable(operationUuid)],
      updates: {pendingAttendanceOps},
    );
  }
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dir = await getApplicationDocumentsDirectory();
    final file = File(p.join(dir.path, 'nexus_time_clock.sqlite'));
    return NativeDatabase.createInBackground(file);
  });
}

final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});
