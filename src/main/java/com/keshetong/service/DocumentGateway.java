package com.keshetong.service;

public interface DocumentGateway {

    ParsedDocumentResult parseDocument(String filePath);

    ExportedDocumentResult exportReportDocx(String topic, String artifactType, String title, String content);

    record ParsedDocumentResult(String title, String markdown, String textContent, String fileType) {
    }

    record ExportedDocumentResult(String fileName, String filePath, String downloadUrl) {
    }
}
