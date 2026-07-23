package com.condor.nexussoft.timeclock.reporting.infrastructure.web;

import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.reporting.application.AttendanceReportService;
import com.condor.nexussoft.timeclock.reporting.application.AttendanceSummaryRow;
import com.condor.nexussoft.timeclock.reporting.application.AttendanceSummaryService;
import com.condor.nexussoft.timeclock.reporting.application.ReportRow;
import com.condor.nexussoft.timeclock.reporting.infrastructure.export.CsvExporter;
import com.condor.nexussoft.timeclock.reporting.infrastructure.export.ExcelExporter;
import com.condor.nexussoft.timeclock.reporting.infrastructure.export.PdfExporter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Exportación de reportes de asistencia (RF-11) en CSV/Excel/PDF con filtros. Requiere {@code report:export}. */
@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAuthority('report:export')")
public class ReportController {

    private final AttendanceReportService report;
    private final AttendanceSummaryService summary;
    private final CsvExporter csv;
    private final ExcelExporter excel;
    private final PdfExporter pdf;

    public ReportController(AttendanceReportService report, AttendanceSummaryService summary,
                            CsvExporter csv, ExcelExporter excel, PdfExporter pdf) {
        this.report = report;
        this.summary = summary;
        this.csv = csv;
        this.excel = excel;
        this.pdf = pdf;
    }

    @GetMapping("/attendance")
    public ResponseEntity<byte[]> attendance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "csv") String format) {

        Instant fromInstant = from != null ? Instant.parse(from) : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant toInstant = to != null ? Instant.parse(to) : Instant.now();
        List<ReportRow> rows = report.rows(TenantContext.require(), fromInstant, toInstant, status);

        return switch (format.toLowerCase()) {
            case "xlsx", "excel" -> download(excel.export(rows), "asistencia.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            case "pdf" -> download(pdf.export(rows), "asistencia.pdf", MediaType.APPLICATION_PDF);
            default -> download(csv.export(rows), "asistencia.csv", MediaType.parseMediaType("text/csv"));
        };
    }

    /**
     * Reporte agregado por colaborador (JSON). La pantalla web filtra/ordena/exporta en el cliente;
     * {@code from}/{@code to} son fechas {@code yyyy-MM-dd} (por defecto, últimos 30 días).
     */
    @GetMapping("/attendance-summary")
    public List<AttendanceSummaryRow> attendanceSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate toDate = to != null && !to.isBlank() ? LocalDate.parse(to) : LocalDate.now();
        LocalDate fromDate = from != null && !from.isBlank() ? LocalDate.parse(from) : toDate.minusDays(29);
        return summary.summary(TenantContext.require(), fromDate, toDate);
    }

    private ResponseEntity<byte[]> download(byte[] body, String filename, MediaType type) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(type)
                .body(body);
    }
}
