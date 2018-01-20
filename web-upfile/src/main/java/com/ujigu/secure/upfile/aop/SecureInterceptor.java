package com.ujigu.secure.upfile.aop;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ujigu.secure.upfile.bean.ImgConstant;
import com.ujigu.secure.upfile.bean.ImgErrorCode;

/**
 * 
 * @author lsf
 *
 */
public class SecureInterceptor extends HandlerInterceptorAdapter{
	
	private final static Logger LOG = LoggerFactory.getLogger(SecureInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		/*Map<String, Object> paramMap = ParamUtil.extractParams(request);
		if(paramMap == null || paramMap.size() <=0){
			return false;
		}
		
		paramMap.remove(paramMap.get(ImgConstant.UP_FILE_ELEM_NAME_KEY));
		paramMap.remove("sign");
		paramMap.remove("primUri");
		paramMap.remove("cutpx");
		paramMap.remove("Filename");
		paramMap.remove("Upload");
		paramMap.remove("localUrl");
		paramMap.remove("callback");
		paramMap.remove("_");
		paramMap.remove(ImgConstant.PRIM_URI_SIGN_KEY);
        Set<String> keyset = paramMap.keySet();
        for(String key : keyset){
        	if(key.startsWith("ig")){
        		paramMap.remove(key);
        	}
        }
        
		String primStr = ParamUtil.joinParams(paramMap);
		String secCode = ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SECURE_PATH, ImgConstant.SECURE_PROP_KEY);
		
		String mysign = EncryptTools.EncryptByMD5(primStr + secCode);
		LOG.debug(LOG.isDebugEnabled() ? "primStr:" + primStr + ", img.secure.code:" + secCode + ",mysign:" + mysign + ",sign:" + request.getParameter("sign") : null);
		
		if(!mysign.equals(request.getParameter("sign"))){
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.SECURE_CHECK_NO_PASS);
			WebUtils.writeJsonByObj(resultMap, response, request);
			
			return false;
		}*/
		
		return true;
	}
	
}
