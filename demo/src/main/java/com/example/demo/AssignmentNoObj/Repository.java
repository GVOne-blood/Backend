package com.example.demo.AssignmentNoObj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Repository {

    public Repository() {
    }

    public List<String> getAllLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    public boolean checkMessage(String line, String keyword) {
        if (keyword == null) return true;
        int temp = line.lastIndexOf("- ");
        return line.substring(temp + 2).toLowerCase().contains(keyword.toLowerCase());
    }

    public boolean checkLogLevel(String line, String level) {
        if (level == null) return true;
        String levelPattern = level.toUpperCase();
        return line.contains(levelPattern);
    }

    public boolean checkTimestamp(String line, LocalDateTime from, LocalDateTime to, Pattern TIMESTAMP_PATTERN, DateTimeFormatter DATETIME_FORMAT) {
        if (from == null || to == null) return true;
        Matcher matcher = TIMESTAMP_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), DATETIME_FORMAT);
                return !timestamp.isBefore(from) && !timestamp.isAfter(to);
            } catch (Exception e) {
                // Bỏ qua lỗi parse
            }
        }
        return false;
    }

    public boolean isEndGame(String line, String END_GAME) {
        return END_GAME.equals(line);
    }

    public boolean isValidLogLine(String line) {
        return line != null && !line.trim().isEmpty();
    }
}