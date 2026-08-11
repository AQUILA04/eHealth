package com.sih.tenant.exception;

import lombok.Getter;

@Getter
public class QuotaExceededException extends RuntimeException {
    private final String operation;
    private final String period;
    private final Long limit;
    private final Long current;

    public QuotaExceededException(String operation, String period, Long limit, Long current) {
        super("Quota exceeded for operation: " + operation);
        this.operation = operation;
        this.period = period;
        this.limit = limit;
        this.current = current;
    }
}
