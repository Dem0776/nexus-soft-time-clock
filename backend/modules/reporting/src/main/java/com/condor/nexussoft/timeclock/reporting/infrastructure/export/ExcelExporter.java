package com.condor.nexussoft.timeclock.reporting.infrastructure.export;

import com.condor.nexussoft.timeclock.reporting.application.ReportRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class ExcelExporter {

    public byte[] export(List<ReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Asistencia");

            Row header = sheet.createRow(0);
            for (int i = 0; i < ReportColumns.HEADERS.length; i++) {
                header.createCell(i).setCellValue(ReportColumns.HEADERS[i]);
            }

            int rowIdx = 1;
            for (ReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                String[] cells = ReportColumns.toCells(r);
                for (int i = 0; i < cells.length; i++) {
                    row.createCell(i).setCellValue(cells[i]);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel", e);
        }
    }
}
