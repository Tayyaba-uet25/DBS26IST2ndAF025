package hms.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates business-level PDF reports (tabular) using OpenPDF and then
 * opens them in the system default viewer.
 * SOFTWARE CLASS #6
 */
public final class PdfReportGenerator {

    private static final Color HEADER_BG = new Color(33, 97, 140);
    private static final Color HEADER_FG = Color.WHITE;
    private static final Color ROW_ALT   = new Color(235, 241, 247);

    private PdfReportGenerator() { }

    /**
     * @param fileName  output file name (saved under reports/)
     * @param title     report title
     * @param subtitle  parameter line (e.g. date range) - may be null
     * @param columns   table headers
     * @param rows      data rows (each same length as columns)
     * @return the absolute path of the created PDF
     */
    public static String buildTableReport(String fileName, String title, String subtitle,
                                          String[] columns, List<String[]> rows) {
        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File out = new File(dir, fileName);

        Document doc = new Document(PageSize.A4, 36, 36, 42, 42);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Font hospitalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(20, 60, 90));
            Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY);
            Font subFont       = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font metaFont      = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            Paragraph hospital = new Paragraph("City Care Hospital", hospitalFont);
            hospital.setAlignment(Element.ALIGN_CENTER);
            doc.add(hospital);

            Paragraph addr = new Paragraph("Hospital Management System  -  Lahore, Pakistan", subFont);
            addr.setAlignment(Element.ALIGN_CENTER);
            addr.setSpacingAfter(8);
            doc.add(addr);

            Paragraph t = new Paragraph(title, titleFont);
            t.setAlignment(Element.ALIGN_CENTER);
            doc.add(t);

            if (subtitle != null && !subtitle.isEmpty()) {
                Paragraph s = new Paragraph(subtitle, subFont);
                s.setAlignment(Element.ALIGN_CENTER);
                doc.add(s);
            }

            String when = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Paragraph meta = new Paragraph("Generated: " + when
                    + "   |   By: " + SessionManager.currentUserName()
                    + "   |   Records: " + rows.size(), metaFont);
            meta.setAlignment(Element.ALIGN_RIGHT);
            meta.setSpacingAfter(10);
            doc.add(meta);

            PdfPTable table = new PdfPTable(columns.length);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, HEADER_FG);
            for (String col : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headFont));
                cell.setBackgroundColor(HEADER_BG);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            int r = 0;
            for (String[] row : rows) {
                boolean alt = (r++ % 2 == 1);
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, cellFont));
                    cell.setPadding(4);
                    if (alt) {
                        cell.setBackgroundColor(ROW_ALT);
                    }
                    table.addCell(cell);
                }
            }
            if (rows.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No records found.", cellFont));
                empty.setColspan(columns.length);
                empty.setPadding(10);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
            }
            doc.add(table);

            Paragraph footer = new Paragraph("\nThis is a system generated report.", metaFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            AppLogger.info("Report generated: " + out.getAbsolutePath());
        } catch (Exception e) {
            if (doc.isOpen()) {
                doc.close();
            }
            AppLogger.error("Failed to build report " + fileName, e);
            throw new RuntimeException("Could not create PDF report: " + e.getMessage(), e);
        }

        openFile(out);
        return out.getAbsolutePath();
    }

    private static void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            AppLogger.error("Could not auto-open report " + file.getName(), e);
        }
    }
}
