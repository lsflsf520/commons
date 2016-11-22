package com.yisi.stiku.rpc.test;

/*import java.util.Iterator;

 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;

 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.Serializer;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;

 *//**
 * @author shangfeng
 *
 */
/*
public class SolrDocumentListSerializer extends Serializer<SolrDocumentList> {

@Override
public void write(Kryo kryo, Output output, SolrDocumentList object) {

	output.writeLong(object.getNumFound());
	output.writeLong(object.getStart());
	output.writeFloat(object.getMaxScore());

	output.writeInt(object.size());
	Iterator<SolrDocument> docItr = object.iterator();
	while (docItr.hasNext()) {
		kryo.writeObjectOrNull(output, docItr.next(), SolrDocument.class);
	}

	output.flush();
}

@Override
public SolrDocumentList read(Kryo kryo, Input input, Class<SolrDocumentList> type) {

	SolrDocumentList docList = new SolrDocumentList();
	docList.setNumFound(input.readLong());
	docList.setStart(input.readLong());
	docList.setMaxScore(input.readFloat());

	int size = input.readInt();
	for (int index = 0; index < size; index++) {
		docList.add(kryo.readObjectOrNull(input, SolrDocument.class));
	}

	return docList;
}

}*/
