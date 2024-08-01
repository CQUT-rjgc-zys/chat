package com.example.mychat.exception;

import com.example.mychat.common.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MyCustomException extends RuntimeException {

    private int errorCode;

    public MyCustomException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MyCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public MyCustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

}
