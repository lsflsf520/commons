package com.yisi.stiku.web.exception;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.yisi.stiku.web.util.AuthContextHolder;


public class ExceptionLogger {

    private static final Integer PAD_SIZE = 30;

    private static final Logger logger = LoggerFactory.getLogger("lab.s2jh.errors");

    public static String logForHttpRequest(Throwable e, HttpServletRequest request) {


        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
        }
        if (requestUri == null) {
            requestUri = request.getRequestURI();
        }
        String userId = AuthContextHolder.getAuthUserPin();

        String rand = DateFormatUtils.format(new java.util.Date(), "yyyyMMddHHmmss")
                + RandomStringUtils.randomNumeric(3);
        boolean skipLog = false;
        String errorTitle = "ERR" + rand + ": ";
        String errorMessage = errorTitle + "系统运行错误，请联系管理员！";
        if (e != null) {
            errorMessage = errorTitle + e.getClass().getName() + ":" + e.getMessage();
//           if (e instanceof BusinessException) {
//                errorMessage = e.getMessage();
//                skipLog = true;
//            } else if (e instanceof com.yisi.stiku.core.exception.BaseRuntimeException) {
//                errorMessage = errorTitle + e.getMessage();
//            } else if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
//                org.springframework.dao.DataIntegrityViolationException dive = (org.springframework.dao.DataIntegrityViolationException) e;
//                if (dive.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
//                    org.hibernate.exception.ConstraintViolationException cve = (org.hibernate.exception.ConstraintViolationException) dive
//                            .getCause();
//                    if (cve.getCause() instanceof java.sql.SQLException) {
//                        java.sql.SQLException sqle = (java.sql.SQLException) cve.getCause();
//                        String sqlMessage = sqle.getMessage();
//                        if (sqlMessage != null && (sqlMessage.indexOf("FK") > -1 || sqlMessage.startsWith("ORA-02292"))) {
//                            errorMessage = "该数据已被关联使用：" + sqlMessage;
//                            skipLog = true;
//                        } else if (sqlMessage != null
//                                && (sqlMessage.indexOf("Duplicate") > -1 || sqlMessage.indexOf("UNIQUE") > -1 || sqlMessage
//                                        .startsWith("ORA-02292"))) {
//                            errorMessage = "违反唯一性约束：" + sqlMessage;
//                            skipLog = true;
//                        }
//                    }
//                }
//            }

            if (!skipLog) {

                if (StringUtils.isNotBlank(userId)) {
                    MDC.put("USER_ID", userId);
                }
                String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                MDC.put("LOG_DATETIME", time);

                StringBuilder sb = new StringBuilder();
                String xForwardedFor = request.getHeader("x-forwarded-for");
                sb.append(StringUtils.rightPad("\n HTTP Request Logon User PIN", PAD_SIZE) + ":" + userId);
                sb.append(StringUtils.rightPad("\n HTTP Request RemoteAddr", PAD_SIZE) + ":" + request.getRemoteAddr());
                sb.append(StringUtils.rightPad("\n HTTP Request RemoteHost", PAD_SIZE) + ":" + request.getRemoteHost());
                sb.append(StringUtils.rightPad("\n HTTP Request x-forwarded-for", PAD_SIZE) + ":" + xForwardedFor);
                sb.append(StringUtils.rightPad("\n HTTP Request Method", PAD_SIZE) + ":" + request.getMethod());
                sb.append(StringUtils.rightPad("\n HTTP Request URI", PAD_SIZE) + ":" + requestUri);
                sb.append(StringUtils.rightPad("\n HTTP Request Query String", PAD_SIZE) + ":"
                        + request.getQueryString());
                if (logger.isDebugEnabled()) {
                    sb.append("\nHTTP Request Parameter List : ");
                    Enumeration<?> paramNames = request.getParameterNames();
                    while (paramNames.hasMoreElements()) {
                        String paramName = (String) paramNames.nextElement();
                        String paramValue = StringUtils.join(request.getParameterValues(paramName), ",");
                        if (paramValue != null && paramValue.length() > 100) {
                            sb.append("\n - " + paramName + "=" + paramValue.substring(0, 100) + "...");
                        } else {
                            sb.append("\n - " + paramName + "=" + paramValue);
                        }
                    }

                    sb.append("\nRequest Header Data:");
                    Enumeration<?> headerNames = request.getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String headerName = (String) headerNames.nextElement();
                        sb.append("\n - " + headerName + "=" + request.getHeader(headerName));
                    }

                    sb.append("\nRequest Attribute Data:");
                    Enumeration<?> attrNames = request.getAttributeNames();
                    while (attrNames.hasMoreElements()) {
                        String attrName = (String) attrNames.nextElement();
                        Object attr = request.getAttribute(attrName);
                        if (attr != null && attr.toString().length() > 100) {
                            sb.append("\n - " + attrName + "=" + attr.toString().substring(0, 100) + "...");
                        } else {
                            sb.append("\n - " + attrName + "=" + attr);
                        }
                    }

                    sb.append("\nSession Attribute Data:");
                    HttpSession session = request.getSession();
                    Enumeration<?> sessionAttrNames = session.getAttributeNames();
                    while (sessionAttrNames.hasMoreElements()) {
                        String attrName = (String) sessionAttrNames.nextElement();
                        Object attr = session.getAttribute(attrName);
                        if (attr != null && attr.toString().length() > 100) {
                            sb.append("\n - " + attrName + "=" + attr.toString().substring(0, 100) + "...");
                        } else {
                            sb.append("\n - " + attrName + "=" + attr);
                        }
                    }
                }

                MDC.put("WEB_DATA", sb.toString());

                logger.error(errorMessage, e);

                MDC.clear();
            }
        } else {
            if (request.getAttribute("SPRING_SECURITY_403_EXCEPTION") != null) {
                errorMessage = "未授权访问, URL: " + requestUri;
                logger.warn("Access Denied: user=" + userId + ", url=" + requestUri);
            }
        }

        return errorMessage;
    }
}
