package com.abranlezama.ecommercerestfulapi.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.time.LocalDateTime.now;

@Builder
@Data
public class HttpResponse {

    @Builder.Default
    private String timeStamp = now().toString();
    private int statusCode;
    private String status;
    private String message;
    private Map<String, Object> result;
    private String error;
    private String errorMessage;
    private String stackTrace;
    private List<UserInputValidationError> validationErrors;

    public void addValidationError(String field, String message) {
        if (Objects.isNull(validationErrors)) this.validationErrors = new ArrayList<>();
        validationErrors.add(new UserInputValidationError(field, message));


    }

    private record UserInputValidationError(String field, String message){}
}
