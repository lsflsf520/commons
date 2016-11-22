package com.yisi.stiku.rpc.container.spring;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yisi.stiku.rpc.container.Container;

public final class SpringContainer implements Container {
	
	public static final String CONFIG_FILE = "classpath:META-INF/spring/internal/root/applicationContext.xml";
	
	private SpringContext springContext;
	
	@Override
	public void start() {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILE);
		springContext = new SpringContext(context);
		context.start();
	}
	
	@Override
	public void stop() {
		if (null != springContext && null != springContext.get()) {
			springContext.get().close();
			springContext = null;
		}
	}
	
	@Override
	public SpringContext getContext() {
		return springContext;
	}
}
