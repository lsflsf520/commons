package com.yisi.stiku.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.yisi.stiku.conf.BaseConfig;
import com.yisi.stiku.rpc.serial.kryo.KryoSerialization;
import com.yisi.stiku.rpc.serial.kryo.KyroFactory;

@Component
public class KryoPool {
	
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
	
	private KyroFactory kyroFactory;
	
	private int maxTotal;
	
	private int minIdle;
	
	private long maxWaitMillis;
	
	private long minEvictableIdleTimeMillis;
	
	@PostConstruct
	public void init() {
		maxTotal = Integer.valueOf(BaseConfig.getValue("serialize.kryo.pool.maxTotal", "10"));
		minIdle = Integer.valueOf(BaseConfig.getValue("serialize.kryo.pool.minIdle", "2"));
		maxWaitMillis = Integer.valueOf(BaseConfig.getValue("serialize.kryo.pool.maxWaitMillis", "10"));
		minEvictableIdleTimeMillis = Integer.valueOf(BaseConfig.getValue("serialize.kryo.pool.minEvictableIdleTimeMillis", "600000"));
		
		kyroFactory = new KyroFactory(maxTotal, minIdle, maxWaitMillis, minEvictableIdleTimeMillis);
	}
	
	public void encode(final ByteBuf out, final Object message) throws IOException {
		ByteBufOutputStream bout = new ByteBufOutputStream(out);
		bout.write(LENGTH_PLACEHOLDER);
		KryoSerialization kryoSerialization = new KryoSerialization(kyroFactory);
		kryoSerialization.serialize(bout, message);
	}
	
	public Object decode(final ByteBuf in) throws IOException {
		KryoSerialization kryoSerialization = new KryoSerialization(kyroFactory);
		return kryoSerialization.deserialize(new ByteBufInputStream(in));
	}
}
