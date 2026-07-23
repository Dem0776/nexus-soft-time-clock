import { AttendanceReport } from './report.models';

/**
 * Generador de datos mock para desarrollo y modo demo (cuando el backend no responde).
 * Produce distribuciones realistas: mayoría con buen cumplimiento y algunos casos límite.
 */

const FIRST_NAMES = [
  'Ana', 'Luis', 'María', 'Carlos', 'Sofía', 'Jorge', 'Lucía', 'Miguel', 'Elena', 'Diego',
  'Valeria', 'Andrés', 'Camila', 'Fernando', 'Paula', 'Ricardo', 'Daniela', 'Sergio', 'Gabriela', 'Pablo',
];
const LAST_NAMES = [
  'García', 'Rodríguez', 'Martínez', 'López', 'Pérez', 'González', 'Sánchez', 'Ramírez', 'Torres', 'Flores',
  'Rivera', 'Gómez', 'Díaz', 'Cruz', 'Morales', 'Ortiz', 'Gutiérrez', 'Chávez', 'Ramos', 'Vargas',
];
const WORK_CENTERS = ['Sucursal Centro', 'Planta Norte', 'Corporativo', 'Sucursal Sur', 'Centro de Distribución'];

function pick<T>(arr: T[], i: number): T {
  return arr[i % arr.length];
}

function randInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function round1(n: number): number {
  return Math.round(n * 10) / 10;
}

/** Genera `count` filas de reporte con variedad de cumplimiento, faltas y retardos. */
export function generateMockReport(count = 60): AttendanceReport[] {
  const rows: AttendanceReport[] = [];
  for (let i = 0; i < count; i++) {
    const expectedDays = randInt(18, 23);
    // La mayoría asiste bien; algunos casos con cumplimiento medio/bajo.
    const bucket = Math.random();
    const attendedRatio = bucket < 0.65 ? randInt(95, 100) / 100 : bucket < 0.85 ? randInt(80, 94) / 100 : randInt(55, 79) / 100;
    const attendedDays = Math.min(expectedDays, Math.round(expectedDays * attendedRatio));

    const missing = expectedDays - attendedDays;
    const justifiedAbsences = randInt(0, Math.max(0, Math.min(missing, 2)));
    const unjustifiedAbsences = Math.max(0, missing - justifiedAbsences);
    const lateArrivals = Math.random() < 0.5 ? 0 : randInt(1, 6);

    const workedHours = round1(attendedDays * randInt(70, 90) / 10); // ~7-9 h/día
    const overtimeHours = Math.random() < 0.4 ? round1(randInt(0, 120) / 10) : 0;
    const totalHours = round1(workedHours + overtimeHours);
    const active = Math.random() < 0.85;
    const compliancePercentage = expectedDays > 0 ? round1((attendedDays * 100) / expectedDays) : 0;

    rows.push({
      employeeNumber: `E-${String(1000 + i)}`,
      employeeName: `${pick(FIRST_NAMES, i * 7 + 3)} ${pick(LAST_NAMES, i * 3 + 1)}`,
      workCenter: pick(WORK_CENTERS, i * 2 + i),
      expectedDays,
      attendedDays,
      justifiedAbsences,
      unjustifiedAbsences,
      lateArrivals,
      workedHours,
      overtimeHours,
      totalHours,
      active,
      compliancePercentage,
    });
  }
  return rows;
}
