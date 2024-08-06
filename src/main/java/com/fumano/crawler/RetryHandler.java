package com.fumano.crawler;

import com.fumano.crawler.exception.CrawlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryHandler {

    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);
    private static final int tries = 3;

    public static <T> T get(Method<T> method) throws CrawlerException {
        for (int i = 1; i <= tries; i++) {
            try {
                return method.get();
            } catch (Exception e) {
                logger.error("try %d: %s".formatted(i, e.getMessage()));
            }
        }
        throw new CrawlerException("retryhandler tried method %d times, limit reached");
    }

    public interface Method<T> {
        T get();
    }
}
