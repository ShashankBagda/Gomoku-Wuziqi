package com.goody.nus.se.gomoku.web.base.exception;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

/**
 * Global exception handler
 *
 * @author haotian
 * @version 1.0, 2025/10/06
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    /**
     * Handle null pointer exception
     *
     * @param e NullPointerException
     * @return error response
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResult<?>> npeException(NullPointerException e) {
        log.warn("NPE", e);
        ApiResult<?> result = ApiResult.failed(ErrorCodeEnum.UNKNOWN_ERROR);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle HTTP method not supported exception
     *
     * @param e HttpRequestMethodNotSupportedException
     * @return error response
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<?> methodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        log.debug("Http method not allowed.");
        return ApiResult.failed(ErrorCodeEnum.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle resource not found exception
     *
     * @param e NoResourceFoundException
     * @return error response
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<?> noResourceFoundException(NoResourceFoundException e) {
        log.debug("Resource not found: {}", e.getResourcePath());
        String errorMsg = "Path not found: " + e.getResourcePath();
        return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST.getErrorCode(), errorMsg);
    }

    /**
     * Handle unknown exception
     *
     * @param e Exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResult<?>> unknownException(Exception e) {
        log.warn("Exception", e);
        ApiResult<?> result = ApiResult.failed(ErrorCodeEnum.UNKNOWN_ERROR);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle async request timeout exception
     *
     * @param e       AsyncRequestTimeoutException
     * @param request HttpServletRequest
     * @return error response
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResult<?>> asyncRequestTimeoutException(AsyncRequestTimeoutException e, HttpServletRequest request) {
        if (log.isInfoEnabled()) {
            log.info("Async request timeout: {}", request.getRequestURI());
        }
        return new ResponseEntity<>(ApiResult.failed(ErrorCodeEnum.TIMEOUT),
                HttpStatus.REQUEST_TIMEOUT);
    }

    /**
     * Handle business exception
     *
     * @param e BizException
     * @return error response
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> businessException(BizException e) {
        ErrorCodeEnum errorCode = e.getErrorCode();
        return ApiResult.failed(errorCode);
    }

    /**
     * Handle rejected execution exception
     *
     * @param e RejectedExecutionException
     * @return error response
     */
    @ExceptionHandler(RejectedExecutionException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> rejectedExecutionException(RejectedExecutionException e) {
        log.warn("Thread submission has been rejected, {}", e.getMessage());
        return ApiResult.failed(ErrorCodeEnum.UNKNOWN_ERROR);
    }

    /**
     * Handle form parameter validation exception
     * Triggered when @Valid or @Validated fails on form submissions (application/x-www-form-urlencoded)
     *
     * @param e BindException
     * @return error response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResult<?> bindException(BindException e) {
        log.info("Form parameter validation failed: {}", e.getBindingResult());
        String errorMsg = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST.getErrorCode(), errorMsg);
    }

    /**
     * Handle JSON parameter validation exception
     * Triggered when @Valid or @Validated fails on @RequestBody (application/json)
     * Validates annotations like @NotNull, @Min, @Max, @Size, @Email, etc.
     *
     * @param e MethodArgumentNotValidException
     * @return error response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("JSON parameter validation failed: {}", e.getBindingResult());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST.getErrorCode(), errorMsg);
    }

    /**
     * Handle controller method parameter validation exception
     * Triggered when validation annotations are used directly on @RequestParam or @PathVariable
     * Requires @Validated on the controller class
     *
     * @param e ConstraintViolationException
     * @return error response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<?> constraintViolationException(ConstraintViolationException e) {
        log.info("Method parameter validation failed: {}", e.getMessage());
        String errorMsg = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return fieldName + ": " + violation.getMessage();
                })
                .collect(Collectors.joining("; "));
        return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST.getErrorCode(), errorMsg);
    }

    /**
     * Handle HTTP message not readable exception
     *
     * @param e HttpMessageNotReadableException
     * @return error response
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.debug("Need request json body");
        return ApiResult.failed(15, "Unsupported Media Type");
    }
}
