package com.yisi.stiku.rpc.serial.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.yisi.stiku.rpc.serial.Serialization;

public final class KryoSerialization implements Serialization {

	private final KyroFactory kyroFactory;

	public KryoSerialization(final KyroFactory kyroFactory) {

		this.kyroFactory = kyroFactory;
	}

	@Override
	public void serialize(final OutputStream out, final Object message) throws IOException {

		Kryo kryo = null;
		Output output = null;
		try {
			kryo = kyroFactory.getKryo();
			output = new Output(out);
			kryo.writeClassAndObject(output, message);
		} finally {
			if (output != null) {
				output.close();
			}
			if (kryo != null) {
				kyroFactory.returnKryo(kryo);
			}
		}
	}

	@Override
	public Object deserialize(final InputStream in) throws IOException {

		Kryo kryo = null;
		Input input = null;
		Object result = null;
		try {
			kryo = kyroFactory.getKryo();
			input = new Input(in);
			result = kryo.readClassAndObject(input);
		} finally {
			if (input != null) {
				input.close();
			}
			if (kryo != null) {
				kyroFactory.returnKryo(kryo);
			}
		}
		return result;
	}
}
