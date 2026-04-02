package com.infilect.assignment.dto;

import java.util.List;

public class UploadResponse {
    private String fileId;
    private String filename;
    private String status;
    private int totalRows;
    private int successRows;
    private int failureRows;
    private List<RowError> errors;
    private long processingTimeMs;
    private int totalErrorCount;
    private boolean errorsTruncated;

    public UploadResponse() {}

    public UploadResponse(String fileId, String filename, String status, int totalRows,
                         int successRows, int failureRows, List<RowError> errors, long processingTimeMs) {
        this.fileId = fileId;
        this.filename = filename;
        this.status = status;
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failureRows = failureRows;
        this.errors = errors;
        this.processingTimeMs = processingTimeMs;
        this.totalErrorCount = errors != null ? errors.size() : 0;
        this.errorsTruncated = false;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessRows() { return successRows; }
    public void setSuccessRows(int successRows) { this.successRows = successRows; }

    public int getFailureRows() { return failureRows; }
    public void setFailureRows(int failureRows) { this.failureRows = failureRows; }

    public List<RowError> getErrors() { return errors; }
    public void setErrors(List<RowError> errors) { this.errors = errors; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public int getTotalErrorCount() { return totalErrorCount; }
    public void setTotalErrorCount(int totalErrorCount) { this.totalErrorCount = totalErrorCount; }

    public boolean isErrorsTruncated() { return errorsTruncated; }
    public void setErrorsTruncated(boolean errorsTruncated) { this.errorsTruncated = errorsTruncated; }
}
