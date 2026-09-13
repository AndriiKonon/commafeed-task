package com.commafeed.backend.service;

import java.io.Serial;

public class LlmServiceException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public enum Kind {
        UNAVAILABLE,
        UPSTREAM_FAILURE
    }

    private final Kind kind;

    public LlmServiceException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public LlmServiceException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
