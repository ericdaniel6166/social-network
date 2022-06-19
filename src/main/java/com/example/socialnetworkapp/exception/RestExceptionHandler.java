package com.example.socialnetworkapp.exception;

import com.example.socialnetworkapp.dto.ErrorDetail;
import com.example.socialnetworkapp.dto.ErrorResponseDTO;
import com.example.socialnetworkapp.dto.ValidationErrorDetail;
import com.example.socialnetworkapp.enums.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    //TODO operationId

    private ResponseEntity<Object> buildResponseExceptionEntity(ErrorResponseDTO errorResponseDTO) {
        return new ResponseEntity<>(errorResponseDTO, errorResponseDTO.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleConstraintViolationException(MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {
        //TODO get message from master_error_message from database
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorCode.VALIDATION_ERROR.name(), null, httpServletRequest, null);

        List<ErrorDetail> errorDetails = e.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> mapFieldErrorToErrorDetail((FieldError) error))
                .collect(Collectors.toList());

        errorResponseDTO.setErrorDetails(errorDetails);

        return buildResponseExceptionEntity(errorResponseDTO);
    }

    private ErrorDetail mapFieldErrorToErrorDetail(FieldError fieldError) {
        return new ValidationErrorDetail(fieldError.getObjectName(), fieldError.getField(),
                fieldError.getRejectedValue(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException e, HttpServletRequest httpServletRequest) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getStatus(), e.getStatus().name(),
                e.getReason(), httpServletRequest, null);
        return buildResponseExceptionEntity(errorResponseDTO);
    }

    @ExceptionHandler(SocialNetworkAppException.class)
    public ResponseEntity<Object> handleSocialNetworkAppException(SocialNetworkAppException e, HttpServletRequest httpServletRequest) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getHttpStatus(), e.getError(),
                e.getMessage(), httpServletRequest, e.getErrorDetails());

        return buildResponseExceptionEntity(errorResponseDTO);
    }

}
