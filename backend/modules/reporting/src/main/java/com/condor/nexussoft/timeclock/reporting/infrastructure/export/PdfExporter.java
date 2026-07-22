package com.condor.nexussoft.timeclock.reporting.infrastructure.export;

import com.condor.nexussoft.timeclock.reporting.application.ReportRow;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class PdfExporter {

    public byte[] export(List<ReportRow> rows) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Reporte de asistencia — Nexus Soft Time Clock"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(ReportColumns.HEADERS.length);
            for (String header : ReportColumns.HEADERS) {
                table.addCell(header);
            }
            for (ReportRow r : rows) {
                for (String cell : ReportColumns.toCells(r)) {
                    table.addCell(cell);
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }
}
