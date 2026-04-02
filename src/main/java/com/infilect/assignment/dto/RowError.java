package com.infilect.assignment.dto;

public class RowError {
    private int rowNumber;
    private String column;
    private String reason;
    private String value;

    public RowError() {}

    public RowError(int rowNumber, String column, String reason, String value) {
        this.rowNumber = rowNumber;
        this.column = column;
        this.reason = reason;
        this.value = value;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
