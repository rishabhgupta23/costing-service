package com.jubeiwato.costing_service.constants;

public enum DateFormat {
    yyyyMMdd_HHmmss("yyyyMMdd_HHmmss");

    private final String format;
    DateFormat(String format) { this.format = format; }
    public String getFormat() { return format; }
}
