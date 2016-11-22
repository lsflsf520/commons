package com.yisi.stiku.rpc.container;

public interface Container {
	
	void start();
	
	void stop();
	
	Context<?> getContext();
}
