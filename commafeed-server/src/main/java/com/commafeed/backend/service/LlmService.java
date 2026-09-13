package com.commafeed.backend.service;

public interface LlmService {

    String generate(String input, String prompt) throws LlmServiceException;
}
