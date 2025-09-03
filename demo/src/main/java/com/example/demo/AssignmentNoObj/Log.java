package com.example.demo.AssignmentNoObj;

import java.time.LocalDateTime;

public class Log {
    private LocalDateTime timestamp;
    private LogLevel level;
    private String service;
    private String message;

    public Log() {
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getLevel() {
        return this.level.toString();
    }

    public String getService() {
        return this.service;
    }

    public String getMessage() {
        return this.message;
    }
}
