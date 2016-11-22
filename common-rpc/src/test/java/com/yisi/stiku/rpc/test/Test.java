package com.yisi.stiku.rpc.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.yisi.stiku.rpc.serial.kryo.KryoSerialization;
import com.yisi.stiku.rpc.serial.kryo.KyroFactory;

public class Test {

	public static void main(String[] args) throws IOException {

		// serializePrim();

		serializeSolrDoc();

	}

	private static void serializePrim() throws IOException {

		KryoSerialization serial = new KryoSerialization(new KyroFactory());

		FileOutputStream out = new FileOutputStream(new File("D:/msg.txt"));

		serial.serialize(out, 1);
		out.flush();
		out.close();

		FileInputStream in = new FileInputStream(new File("D:/msg.txt"));
		int result = (int) serial.deserialize(in);
		System.out.println(result);
	}

	private static void serializeSolrDoc() throws IOException {

		KryoSerialization serial = new KryoSerialization(new KyroFactory());

		FileOutputStream out = new FileOutputStream(new File("D:/msg.txt"));
		Response res = new Response();

		/*
		 * SolrDocumentList docList = new SolrDocumentList(); SolrDocument doc =
		 * new SolrDocument(); doc.setField("name", "鹤顶红"); doc.setField("type",
		 * "毒药"); docList.add(doc);
		 */

		SDL sdl = new SDL();
		sdl.setMaxScore(1.0f);
		sdl.setNumFound(1124);
		sdl.setStart(2);

		/*
		 * docList.setMaxScore(1.0f); docList.setNumFound(1124);
		 * docList.setStart(3);
		 * 
		 * res.set_results(docList);
		 */
		res.setSdl(sdl);

		serial.serialize(out, res);
		out.flush();
		out.close();

		FileInputStream in = new FileInputStream(new File("D:/msg.txt"));
		Response dres = (Response) serial.deserialize(in);
		// System.out.println(dres.get_results().toString());
	}

}
