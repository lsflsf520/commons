package com.yisi.stiku.rpc.container.spring;

import org.springframework.context.support.AbstractApplicationContext;

import com.yisi.stiku.rpc.container.Context;


public final class SpringContext implements Context<AbstractApplicationContext> {
	
	private final AbstractApplicationContext applicationContext;
	
	public SpringContext(final AbstractApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public AbstractApplicationContext get() {
		return applicationContext;
	}

}
