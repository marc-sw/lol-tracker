package com.fumano.crawler.exception;

public class MissingConfigFieldException extends Exception {
    public MissingConfigFieldException(String fieldName, String filepath) {
        super("missing field '%s' in %s".formatted(fieldName, filepath));
    }
}
