package com.abranlezama.ecommercerestfulapi.exception;

import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Objects;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TRACE = "trace";
    @Value("${custom.stacktrace.enabled}")
    private boolean printStackTrace;

//  Security exceptions
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleUserAuthenticationException(AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(ex, "Authentication failed. Contact customer service", UNAUTHORIZED, request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, FAILED_AUTHENTICATION, UNAUTHORIZED, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(ex, FAILED_AUTHENTICATION, UNAUTHORIZED, request);
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleAccountDisabledException(DisabledException ex, WebRequest request) {
        return buildErrorResponse(ex, ACCOUNT_DISABLED, UNAUTHORIZED, request);
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleAccountLockedException(LockedException ex, WebRequest request) {
        return buildErrorResponse(ex, ACCOUNT_LOCKED, UNAUTHORIZED, request);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, String errorMessage,
                                                      HttpStatus status, WebRequest request) {
        HttpResponse.HttpResponseBuilder httpResponseBuilder = HttpResponse.builder()
                .timeStamp(now().toString())
                .message(errorMessage)
                .status(status.getReasonPhrase())
                .statusCode(status.value());

        if (printStackTrace && isTraceOn(request)) {
            httpResponseBuilder.stackTrace(Arrays.toString(ex.getStackTrace()));
        }

        return ResponseEntity.status(status.value()).body(httpResponseBuilder.build());
    }

    private boolean isTraceOn(WebRequest request) {
        String[] value = request.getParameterValues(TRACE);
        return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
    }

}