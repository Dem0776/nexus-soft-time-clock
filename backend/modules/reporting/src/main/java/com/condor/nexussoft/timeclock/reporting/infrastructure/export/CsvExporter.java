package com.condor.nexussoft.timeclock.reporting.infrastructure.export;

import com.condor.nexussoft.timeclock.reporting.application.ReportRow;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExporter {

    public byte[] export(List<ReportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", ReportColumns.HEADERS)).append("\r\n");
        for (ReportRow r : rows) {
            String[] cells = ReportColumns.toCells(r);
            for (int i = 0; i < cells.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(escape(cells[i]));
            }
            sb.append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
