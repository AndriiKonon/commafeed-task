package com.commafeed.frontend.model.request;

import lombok.Data;

@Data
public class GenerateAlternativeRequest {

    private String target;
    private String prompt;
}
