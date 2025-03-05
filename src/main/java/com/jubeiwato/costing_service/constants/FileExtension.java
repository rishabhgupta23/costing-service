package com.jubeiwato.costing_service.constants;

public enum FileExtension {
    SPREADSHEET("xlsx");

    private final String value;
    FileExtension(String value) { this.value = value; }
    public String getValue() { return value; }
}
