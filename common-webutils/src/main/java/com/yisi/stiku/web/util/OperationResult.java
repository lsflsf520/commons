/**
 * Copyright (c) 2012
 */
package com.yisi.stiku.web.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 用于Object到JSON序列化的对象结构体定义
 */
@JsonInclude(Include.NON_NULL)
public class OperationResult {

    /** 标识操作成功与否 */
    public static enum OperResultType {
        success,
        warning,
        failure,
        NOT_LOGIN,
        ACCESS_DENIED,
        SYS_ERROR,
        confirm
    }

    /** 返回success或failure操作标识 */
    private String type;
    /** 国际化处理的返回JSON消息正文 */
    private String message;
    /** 补充的数据 */
    private Object userdata;
    
    public static OperationResult buildResult(OperResultType resultType, String message, Object userdata) {
        return new OperationResult(resultType, message, userdata);
    }
    
    public static OperationResult buildResult(OperResultType resultType, String message) {
        return buildResult(resultType, message, null);
    }
    
    public static OperationResult buildResult(OperResultType resultType) {
        return buildResult(resultType, null);
    }

    public static OperationResult buildSuccessResult(String message, Object userdata) {
        return buildResult(OperResultType.success, message, userdata);
    }

    public static OperationResult buildSuccessResult(String message) {
        return buildResult(OperResultType.success, message);
    }

    public static OperationResult buildWarningResult(String message, Object userdata) {
        return buildResult(OperResultType.warning, message, userdata);
    }

    public static OperationResult buildFailureResult(String message) {
        return buildResult(OperResultType.failure, message);
    }

    public static OperationResult buildFailureResult(String message, Object userdata) {
        return buildResult(OperResultType.failure, message, userdata);
    }
    
    public static OperationResult buildConfirmResult(String message, Object userdata) {
        return buildResult(OperResultType.confirm, message, userdata);
    }

    public OperationResult(OperResultType type, String message) {
        this.type = type.name();
        this.message = message;
    }

    public OperationResult(OperResultType type, String message, Object userdata) {
        this.type = type.name();
        this.message = message;
        this.userdata = userdata;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public Object getUserdata() {
        return userdata;
    }

    public void setUserdata(Object userdata) {
        this.userdata = userdata;
    }
}
