package com.ujigu.secure.web.filter.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.ujigu.secure.web.util.WebUtils;

/**
** @author Administrator
** @version 2017年11月2日上午9:55:13
** @Description
*/
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper{
	
	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws UnsupportedEncodingException {
		super(request);
		body = WebUtils.getRequestJsonString(request).getBytes("utf-8");
	}
	
	public BufferedReader getReader() throws IOException {  
        return new BufferedReader(new InputStreamReader(getInputStream()));  
    }  
  
    @Override  
    public ServletInputStream getInputStream() throws IOException {  
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);  
        return new ServletInputStream() {  
  
            @Override  
            public int read() throws IOException {  
                return bais.read();  
            }

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// TODO Auto-generated method stub
				
			}  
        };  
    }  

	private final byte[] body;

}
