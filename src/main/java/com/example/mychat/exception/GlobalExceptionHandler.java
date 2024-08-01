package com.example.mychat.exception;

import com.example.mychat.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public CommonResult<String> handleException(MyCustomException e) {
        log.error("捕获到自定义异常：{}", e.getMessage());

        int code = e.getErrorCode();
        return CommonResult.error(code, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult<String> handleException(Exception e) {
        log.error("捕获到全局异常：{}", e.getMessage());

        return CommonResult.error(500, e.getMessage());
    }
}
