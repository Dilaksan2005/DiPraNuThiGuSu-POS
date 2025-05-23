package org.example.cafepos;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.IOException;
import java.util.List;

public class PDFGeneratorUserManagement {

    private static final PDType1Font HEADER_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDType1Font BODY_FONT = PDType1Font.HELVETICA;
    private static final float TITLE_FONT_SIZE = 18f;
    private static final float HEADER_FONT_SIZE = 12f;
    private static final float BODY_FONT_SIZE = 10f;
    private static final float MARGIN = 50f;
    private static final float ROW_HEIGHT = 20f;

    public static void generateUserReport(List<EmployeeData> users, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                // Add café logo
                addCafeLogo(document, contentStream, page);

                // Report title
                addReportTitle(contentStream, "CAFE POS - User Management Report");

                // Table setup
                float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
                float yStart = 700f;

                // Draw table headers
                drawTableHeader(contentStream, MARGIN, yStart, tableWidth);

                // Draw table rows
                float yPosition = yStart - ROW_HEIGHT;
                boolean needsNewPage = false;

                for (int i = 0; i < users.size(); i++) {
                    if (yPosition < 100f) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750f;
                        drawTableHeader(contentStream, MARGIN, yPosition + ROW_HEIGHT, tableWidth);
                        needsNewPage = true;
                    }

                    if (needsNewPage) {
                        yPosition -= ROW_HEIGHT;
                        needsNewPage = false;
                    }

                    // Alternate row color
                    if (i % 2 == 0) {
                        contentStream.setNonStrokingColor(245, 245, 245);
                        contentStream.addRect(MARGIN, yPosition - ROW_HEIGHT + 2, tableWidth, ROW_HEIGHT);
                        contentStream.fill();
                        contentStream.setNonStrokingColor(0, 0, 0);
                    }

                    drawTableRow(contentStream, MARGIN, yPosition, tableWidth, users.get(i), i); // Added i as last parameter
                    yPosition -= ROW_HEIGHT;
                }

                // Footer
                addFooter(contentStream, page, "Generated by Café POS System");
            } finally {
                contentStream.close();
            }

            document.save(filePath);
        }
    }

    private static void addCafeLogo(PDDocument document, PDPageContentStream contentStream, PDPage page)
            throws IOException {
        try {
            PDImageXObject logo = PDImageXObject.createFromFile(
                    "src/main/resources/images/cafe_logo.png", document);
            float logoWidth = 100;
            float logoHeight = 50;
            float x = page.getMediaBox().getWidth() - MARGIN - logoWidth;
            contentStream.drawImage(logo, x, 750, logoWidth, logoHeight);
        } catch (IOException e) {
            System.err.println("Logo not found, proceeding without it");
        }
    }

    private static void addReportTitle(PDPageContentStream contentStream, String title)
            throws IOException {
        contentStream.beginText();
        contentStream.setFont(HEADER_FONT, TITLE_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, 750);
        contentStream.showText(title);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(BODY_FONT, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, 730);
        contentStream.showText("Generated on: " + java.time.LocalDate.now());
        contentStream.endText();
    }

    private static void drawTableHeader(PDPageContentStream contentStream,
                                        float x, float y, float width) throws IOException {
        float[] columnWidths = {width * 0.15f, width * 0.35f, width * 0.3f, width * 0.2f};
        String[] headers = {"ID", "Username", "Role", "Status"};

        // Header background
        contentStream.setNonStrokingColor(58, 71, 80);
        contentStream.addRect(x, y - ROW_HEIGHT, width, ROW_HEIGHT);
        contentStream.fill();

        // Header text
        contentStream.beginText();
        contentStream.setFont(HEADER_FONT, HEADER_FONT_SIZE);
        contentStream.setNonStrokingColor(255, 255, 255); // White text

        float textX = x + 10f; // Initial padding
        for (int i = 0; i < headers.length; i++) {
            contentStream.newLineAtOffset(textX - (i == 0 ? x : textX), y - 15);
            contentStream.showText(headers[i]);
            textX += columnWidths[i];
        }
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
    }

    private static void drawTableRow(PDPageContentStream contentStream,
                                     float x, float y, float width,
                                     EmployeeData user, int rowIndex) throws IOException {
        float[] columnWidths = {width * 0.15f, width * 0.35f, width * 0.3f, width * 0.2f};
        String[] values = {
                String.format("%4d", user.getId()),  // Right-aligned ID
                user.getUsername(),
                user.getRole(),
                "Active"
        };

        // Draw row background (alternating colors)
        if (rowIndex % 2 == 0) {
            contentStream.setNonStrokingColor(245, 245, 245);
            contentStream.addRect(x, y - ROW_HEIGHT, width, ROW_HEIGHT);
            contentStream.fill();
        }
        contentStream.setNonStrokingColor(0, 0, 0); // Black text

        // Draw column borders
        contentStream.setLineWidth(0.3f);
        contentStream.setStrokingColor(200, 200, 200);
        float borderX = x;
        for (float cWidth : columnWidths) {
            contentStream.moveTo(borderX, y);
            contentStream.lineTo(borderX, y - ROW_HEIGHT);
            borderX += cWidth;
        }
        contentStream.stroke();

        // Draw text content with precise alignment
        float textX = x + 5f;
        contentStream.beginText();
        contentStream.setFont(BODY_FONT, BODY_FONT_SIZE);

        // ID (right-aligned)
        float idWidth = BODY_FONT.getStringWidth(values[0]) / 1000f * BODY_FONT_SIZE;
        contentStream.newLineAtOffset(textX + columnWidths[0] - idWidth - 5f, y - 15);
        contentStream.showText(values[0]);

        // Username (left-aligned)
        contentStream.newLineAtOffset(-(columnWidths[0] - idWidth - 5f) + columnWidths[0] + 5f, 0);
        contentStream.showText(values[1]);

        // Role (left-aligned)
        contentStream.newLineAtOffset(columnWidths[1], 0);
        contentStream.showText(values[2]);

        // Status (centered)
        float statusWidth = BODY_FONT.getStringWidth(values[3]) / 1000f * BODY_FONT_SIZE;
        contentStream.newLineAtOffset(columnWidths[2] + (columnWidths[3] - statusWidth)/2 - 5f, 0);
        contentStream.showText(values[3]);

        contentStream.endText();
    }

    private static void addFooter(PDPageContentStream contentStream,
                                  PDPage page, String footerText) throws IOException {
        contentStream.beginText();
        contentStream.setFont(BODY_FONT, 8f);
        contentStream.newLineAtOffset(MARGIN, 30);
        contentStream.showText(footerText);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(BODY_FONT, 8f);
        contentStream.newLineAtOffset(page.getMediaBox().getWidth() - MARGIN - 30, 30);
        contentStream.showText("Page 1");
        contentStream.endText();
    }
}