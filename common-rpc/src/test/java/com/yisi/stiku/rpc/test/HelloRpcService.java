package com.yisi.stiku.rpc.test;


public interface HelloRpcService {

	String sayHello(String name, int count);

	// int save(String key);
	// boolean save(TblSysMenu sysmenu);

	Person getPerson();

	Car showCar();

	Response query(int start);

}
