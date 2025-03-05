package com.jubeiwato.costing_service.constants;

public enum SpreadsheetExtention {
    xlsx("xlsx");

    private final String value;
    SpreadsheetExtention(String value) { this.value = value; }
    public String getValue() { return value; }
}
