package com.example.back_end.service;

import com.example.back_end.entity.Quiz;
import com.example.back_end.entity.QuizQuestion;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.QuizQuestionRepository;
import com.example.back_end.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizImportService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final R2StorageService storageService;

    private static class MultipartFileImpl implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public MultipartFileImpl(byte[] content, String name, String originalFilename, String contentType) {
            this.content = content;
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content == null || content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() throws IOException { return content; }
        @Override public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }

    @Transactional
    public void importQuizQuestionsFromZip(Long quizId, MultipartFile zipFile) throws Exception {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Map<String, byte[]> fileMap = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                
                String name = entry.getName();
                String filename = new File(name).getName();
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                fileMap.put(filename.toLowerCase(), bos.toByteArray());
                fileMap.put(name.toLowerCase(), bos.toByteArray());
                zis.closeEntry();
            }
        }

        String excelKey = null;
        for (String filename : fileMap.keySet()) {
            if (filename.endsWith(".xlsx")) {
                excelKey = filename;
                break;
            }
        }

        if (excelKey == null) {
            throw new IllegalArgumentException("Không tìm thấy file Excel (.xlsx) nào trong gói ZIP.");
        }

        byte[] excelBytes = fileMap.get(excelKey);
        List<QuizQuestion> questions = new ArrayList<>();
        
        List<QuizQuestion> existingQuestions = quizQuestionRepository.findByQuizId(quizId);
        int nextOrder = existingQuestions.size() + 1;

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Bỏ qua dòng tiêu đề (Header row)
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                Cell questionCell = row.getCell(2);
                if (questionCell == null || getCellStringValue(questionCell).isBlank()) {
                    continue;
                }

                String section = getCellStringValue(row.getCell(1)).toUpperCase().trim();
                if (section.isBlank()) {
                    section = "READING";
                }
                String questionText = getCellStringValue(row.getCell(2));
                String correctAnswer = getCellStringValue(row.getCell(3));
                
                List<String> wrongAnswers = new ArrayList<>();
                for (int i = 4; i <= 6; i++) {
                    String wa = getCellStringValue(row.getCell(i));
                    if (!wa.isBlank()) {
                        wrongAnswers.add(wa);
                    }
                }

                String audioFile = getCellStringValue(row.getCell(7));
                String imageFile = getCellStringValue(row.getCell(8));
                
                String pointsStr = getCellStringValue(row.getCell(9));
                BigDecimal points = BigDecimal.valueOf(2.00);
                if (!pointsStr.isBlank()) {
                    try {
                        points = new BigDecimal(pointsStr);
                    } catch (Exception e) {
                        log.warn("Lỗi parse điểm: {}", pointsStr, e);
                    }
                }

                String explanation = getCellStringValue(row.getCell(10));
                
                String audioUrl = null;
                String audioSource = "TTS";
                if (!audioFile.isBlank()) {
                    byte[] audioBytes = findFileInMap(fileMap, audioFile);
                    if (audioBytes != null) {
                        String contentType = "audio/mpeg";
                        if (audioFile.toLowerCase().endsWith(".mp4")) {
                            contentType = "video/mp4";
                        }
                        MultipartFile impl = new MultipartFileImpl(audioBytes, "file", audioFile, contentType);
                        String key = storageService.uploadFile(impl, "audio");
                        audioUrl = "/api/files/download/" + key;
                        audioSource = "UPLOAD";
                    }
                }

                String imageUrl = null;
                if (!imageFile.isBlank()) {
                    byte[] imageBytes = findFileInMap(fileMap, imageFile);
                    if (imageBytes != null) {
                        String contentType = "image/png";
                        if (imageFile.toLowerCase().endsWith(".jpg") || imageFile.toLowerCase().endsWith(".jpeg")) {
                            contentType = "image/jpeg";
                        } else if (imageFile.toLowerCase().endsWith(".gif")) {
                            contentType = "image/gif";
                        }
                        MultipartFile impl = new MultipartFileImpl(imageBytes, "file", imageFile, contentType);
                        String key = storageService.uploadFile(impl, "images");
                        imageUrl = "/api/files/download/" + key;
                    }
                }

                QuizQuestion q = QuizQuestion.builder()
                        .quiz(quiz)
                        .section(section)
                        .questionText(questionText)
                        .questionType("MULTIPLE_CHOICE")
                        .correctAnswer(correctAnswer)
                        .wrongAnswers(wrongAnswers)
                        .audioUrl(audioUrl)
                        .audioSource(audioSource)
                        .imageUrl(imageUrl)
                        .points(points)
                        .explanation(explanation)
                        .order(nextOrder++)
                        .build();

                questions.add(q);
            }
        }

        if (!questions.isEmpty()) {
            quizQuestionRepository.saveAll(questions);
        }
    }

    private byte[] findFileInMap(Map<String, byte[]> fileMap, String fileName) {
        String lower = fileName.toLowerCase().trim();
        if (fileMap.containsKey(lower)) {
            return fileMap.get(lower);
        }
        String base = new File(fileName).getName().toLowerCase().trim();
        if (fileMap.containsKey(base)) {
            return fileMap.get(base);
        }
        return null;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (type == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toString();
            }
            double val = cell.getNumericCellValue();
            if (val == (long) val) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        } else if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception e) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return "";
    }
}
