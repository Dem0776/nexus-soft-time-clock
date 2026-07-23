import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

import { AttendanceReport } from './report.models';

/** Encabezados en español, en el orden de las columnas del reporte. */
const HEADERS: readonly string[] = [
  'N.º empleado',
  'Nombre',
  'Centro de trabajo',
  'Días esperados',
  'Días asistidos',
  'Faltas justificadas',
  'Faltas injustificadas',
  'Retardos',
  'Horas trabajadas',
  'Horas extra',
  'Horas totales',
  'Estado',
  '% Cumplimiento',
];

function toRow(r: AttendanceReport): (string | number)[] {
  return [
    r.employeeNumber,
    r.employeeName,
    r.workCenter,
    r.expectedDays,
    r.attendedDays,
    r.justifiedAbsences,
    r.unjustifiedAbsences,
    r.lateArrivals,
    r.workedHours,
    r.overtimeHours,
    r.totalHours,
    r.active ? 'Activo' : 'Inactivo',
    r.compliancePercentage,
  ];
}

function stamp(): string {
  return new Date().toISOString().slice(0, 10);
}

/** Exporta las filas dadas (ya filtradas) a un archivo .xlsx. */
export function exportToExcel(rows: AttendanceReport[]): void {
  const data: (string | number)[][] = [[...HEADERS], ...rows.map(toRow)];
  const sheet = XLSX.utils.aoa_to_sheet(data);
  const book = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(book, sheet, 'Asistencia');
  XLSX.writeFile(book, `reporte-asistencia-${stamp()}.xlsx`);
}

/** Exporta las filas dadas (ya filtradas) a un archivo .pdf horizontal. */
export function exportToPdf(rows: AttendanceReport[], range?: { from: string; to: string }): void {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });

  doc.setFontSize(14);
  doc.text('Reporte de asistencia por colaborador', 40, 40);
  doc.setFontSize(10);
  doc.setTextColor(120);
  const subtitle = range ? `Periodo: ${range.from} a ${range.to}  ·  ${rows.length} registros` : `${rows.length} registros`;
  doc.text(subtitle, 40, 58);

  autoTable(doc, {
    startY: 72,
    head: [HEADERS as string[]],
    body: rows.map(toRow),
    styles: { fontSize: 7, cellPadding: 3 },
    headStyles: { fillColor: [57, 73, 171], textColor: 255, fontStyle: 'bold' },
    alternateRowStyles: { fillColor: [244, 246, 251] },
    margin: { left: 40, right: 40 },
  });

  doc.save(`reporte-asistencia-${stamp()}.pdf`);
}
