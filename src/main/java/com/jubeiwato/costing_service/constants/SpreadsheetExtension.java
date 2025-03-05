package com.jubeiwato.costing_service.constants;

public enum SpreadsheetExtension {
    xlsx("xlsx");

    private final String value;
    SpreadsheetExtension(String value) { this.value = value; }
    public String getValue() { 
        return value;
     }

}
