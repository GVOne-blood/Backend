package com.theblood.springfood.chat.service.rag;

// TODO: Uncomment when Apache POI is needed for Word document processing
// import org.apache.poi.xwpf.usermodel.XWPFDocument;
// import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
// import java.util.List;

/**
 * Utility for extracting text from various document formats.
 * Supports: .txt (Plain text)
 * TODO: Add .docx (Word) support when needed
 */
@Component
public class DocumentTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractor.class);

    /**
     * Extract text from Word document (.docx)
     * TODO: Implement when Apache POI is needed
     *
     * @param inputStream Word document input stream
     * @return Extracted text content
     * @throws IOException if reading fails
     */
    public String extractFromWord(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Word document extraction not yet implemented. Use PDF or TXT files.");
        
        /* TODO: Uncomment when Apache POI is needed
        log.debug("Extracting text from Word document");

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                String paragraphText = paragraph.getText();
                if (paragraphText != null && !paragraphText.isBlank()) {
                    text.append(paragraphText).append("\n");
                }
            }

            String extractedText = text.toString().trim();
            log.debug("Extracted {} characters from Word document", extractedText.length());
            return extractedText;
        }
        */
    }

    /**
     * Extract text from plain text file
     *
     * @param inputStream Text file input stream
     * @return File content as string
     * @throws IOException if reading fails
     */
    public String extractFromText(InputStream inputStream) throws IOException {
        log.debug("Reading plain text file");
        return new String(inputStream.readAllBytes());
    }

    /**
     * Detect and extract text based on file extension
     *
     * @param inputStream File input stream
     * @param filename    Original filename (for extension detection)
     * @return Extracted text
     * @throws IOException if reading fails or format not supported
     */
    public String extractText(InputStream inputStream, String filename) throws IOException {
        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            // case "docx" -> extractFromWord(inputStream); // TODO: Enable when Apache POI is implemented
            case "txt" -> extractFromText(inputStream);
            default -> throw new IllegalArgumentException(
                "Unsupported file format: " + extension + ". Supported: .txt (Word .docx coming soon)"
            );
        };
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
