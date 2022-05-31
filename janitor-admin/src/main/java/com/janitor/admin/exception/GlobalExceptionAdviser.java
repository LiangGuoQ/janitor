package com.janitor.admin.exception;

import com.janitor.common.base.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * ClassName GlobalExceptionAdviser
 * Description 全局异常通知处理器
 *
 * @author 曦逆
 * Date 2022/5/31 9:12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdviser {

    /**
     * 统一业务异常
     *
     * @param e 业务异常
     * @return 返回异常响应体
     */
    @ExceptionHandler(JanitorAdminException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Result handleBusinessException(JanitorAdminException e) {
        log.error("there has occurred an janitor admin exception:", e);
        return Result.fail(e.getErrorCode(), e.getMessage());
    }

    /**
     * spring servlet找不到对应的handler时抛出的异常
     *
     * @param e 异常
     * @return 返回异常响应体
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error(HttpStatus.NOT_FOUND.getReasonPhrase(), e);
        return Result.fail(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    /**
     * 405 - Method Not Allowed
     * 带有@ResponseStatus注解的异常类会被ResponseStatusExceptionResolver 解析
     *
     * @param e 异常
     * @return 返回异常响应体
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), e);
        return Result.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
    }

    /**
     * 非业务异常,程序无法处理,直接返回前端500错误码
     *
     * @param th 异常
     * @return 返回异常响应体
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleUnknownException(Throwable th) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), th);
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
}
