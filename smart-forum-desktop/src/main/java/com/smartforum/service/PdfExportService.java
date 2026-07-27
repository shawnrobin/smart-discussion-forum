package com.smartforum.service;

import com.smartforum.model.Message;
import com.smartforum.model.Topic;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Requirement #6: "these [topic threads] can be exported to PDF by anyone."
 *
 * Renders a topic (title + body) and its full threaded reply list into a
 * real, standalone PDF file — no server round-trip required once the topic
 * and messages have been loaded, so this also works offline from cache.
 */
public class PdfExportService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float USABLE_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    /**
     * Builds the PDF and writes it to disk.
     * @return the file that was written
     */
    public static File export(Topic topic, List<Message> messages, File destination) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont italic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            Cursor cursor = new Cursor(doc);

            // ── Title ──────────────────────────────────────────
            cursor.write(topic.title != null ? topic.title : "Untitled discussion", bold, 18);
            cursor.gap(6);
            String meta = "Posted by " + (topic.user != null ? topic.user.name : "Unknown")
                    + (topic.createdAt != null ? "  ·  " + topic.createdAt : "");
            cursor.write(meta, italic, 10);
            cursor.gap(14);

            if (topic.description != null && !topic.description.isBlank()) {
                cursor.writeWrapped(topic.description, regular, 12);
                cursor.gap(16);
            }

            cursor.rule();
            cursor.gap(14);
            cursor.write("Replies (" + messages.size() + ")", bold, 13);
            cursor.gap(10);

            if (messages.isEmpty()) {
                cursor.write("No replies yet.", italic, 11);
            }

            for (Message m : messages) {
                String indent = "    ".repeat(Math.max(0, m.depth));
                String author = (m.user != null ? m.user.name : "Unknown")
                        + (m.isAnswer ? "  [Marked as answer]" : "");
                cursor.write(indent + author, bold, 11);
                if (m.createdAt != null) cursor.write(indent + m.createdAt, italic, 9);
                cursor.writeWrapped(indent + (m.content != null ? m.content : ""), regular, 11);
                cursor.gap(10);
            }

            cursor.close();
            doc.save(destination);
            return destination;
        }
    }

    /** Handles page breaks and text placement so callers just "write" lines. */
    private static class Cursor {
        private final PDDocument doc;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        Cursor(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        void gap(float amount) throws IOException {
            y -= amount;
            ensureRoom(0);
        }

        void rule() throws IOException {
            ensureRoom(2);
            stream.setLineWidth(0.5f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(PAGE_WIDTH - MARGIN, y);
            stream.stroke();
        }

        void write(String text, PDFont font, float size) throws IOException {
            if (text == null) return;
            ensureRoom(size + 4);
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(sanitize(text));
            stream.endText();
            y -= (size + 6);
        }

        /** Wraps long paragraphs to fit within the usable page width. */
        void writeWrapped(String text, PDFont font, float size) throws IOException {
            if (text == null || text.isBlank()) return;
            for (String paragraphLine : text.split("\n")) {
                List<String> lines = wrap(paragraphLine, font, size);
                for (String line : lines) {
                    write(line, font, size);
                }
            }
        }

        private List<String> wrap(String text, PDFont font, float size) throws IOException {
            java.util.List<String> lines = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : text.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                float width = font.getStringWidth(sanitize(candidate)) / 1000 * size;
                if (width > USABLE_WIDTH && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            if (!current.isEmpty()) lines.add(current.toString());
            if (lines.isEmpty()) lines.add("");
            return lines;
        }

        private void ensureRoom(float needed) throws IOException {
            if (y - needed < MARGIN) newPage();
        }

        /** PDFBox's base fonts only support WinAnsi — strip anything outside it. */
        private String sanitize(String text) {
            return text.replaceAll("[^\\x00-\\xFF]", "?");
        }

        void close() throws IOException {
            stream.close();
        }
    }
}
