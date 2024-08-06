package com.fumano.crawler.exception;

public class InvalidConfigFieldException extends Exception {
    public InvalidConfigFieldException(String key, String value) {
        super("invalid value '%s' for config field '%s'".formatted(value, key));
    }
}
