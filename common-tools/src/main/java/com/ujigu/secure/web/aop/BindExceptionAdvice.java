package com.ujigu.secure.web.aop;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 使用表单校验框架@valid校验时，在方法没有指定对应的BindingResult的情况下，将会抛出BindException异常
 * 该类是用来捕获这个异常用的
 * @author lsf
 *
 */

@ControllerAdvice
@ResponseBody
public class BindExceptionAdvice {

	/**
     * 400 - BindException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {BindException.class})
    public ResultModel handleBindException(BindException e, WebRequest request, HttpServletResponse response) throws BindException{
    	if(!WebUtils.isAjax(request)){
    		throw e; //若不是ajax请求，则将异常直接抛出
    	}
        LogUtils.warn("参数验证失败，%s", e.getMessage());
        ResultModel resultModel = new ResultModel("ILLEGAL_PARAM", "参数不正确");
        BindingResult result = e.getBindingResult();
//        StringBuffer sb = new StringBuffer();
        for (ObjectError error : result.getAllErrors()) {
            String code = error.getCode();
            if(error instanceof FieldError){
            	code = ((FieldError)error).getField();
            }
            
            String errorMsg = error.getDefaultMessage();
//            String message = String.format("%s:%s", field, code);
//            sb.append(message);
            
            resultModel.addExtraInfo(code, errorMsg);
        }
       
        return resultModel;

    }
	
}
