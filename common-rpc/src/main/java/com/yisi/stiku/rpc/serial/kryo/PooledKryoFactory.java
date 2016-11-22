package com.yisi.stiku.rpc.serial.kryo;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.rpc.bean.RpcConstants;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.SubListSerializers;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

/**
 * 
 * @author shangfeng
 *
 */
final class PooledKryoFactory extends BasePooledObjectFactory<Kryo> {

	private final static Logger LOG = LoggerFactory.getLogger(PooledKryoFactory.class);

	@Override
	public Kryo create() throws Exception {

		return createKryo();
	}

	@Override
	public PooledObject<Kryo> wrap(Kryo kryo) {

		return new DefaultPooledObject<Kryo>(kryo);
	}

	private Kryo createKryo() {

		Kryo kryo = new KryoReflectionFactorySupport() {

			@Override
			public Serializer<?> getDefaultSerializer(@SuppressWarnings("rawtypes") final Class clazz) {

				if (EnumMap.class.isAssignableFrom(clazz)) {
					return new EnumMapSerializer();
				}
				if (SubListSerializers.ArrayListSubListSerializer.canSerialize(clazz)
						|| SubListSerializers.JavaUtilSubListSerializer.canSerialize(clazz)) {
					return SubListSerializers.createFor(clazz);
				}
				return super.getDefaultSerializer(clazz);
			}
		};

		kryo.register(Arrays.asList("").getClass(), new
				ArraysAsListSerializer());

		Iterator<String> itr = ConfigOnZk.getKeys(RpcConstants.KRYO_SERIALIZER_CONFIG_ZK_NODE);
		while (itr != null && itr.hasNext()) {
			String aimClazzStr = itr.next();
			String serializerClazzStr = ConfigOnZk.getValue(RpcConstants.KRYO_SERIALIZER_CONFIG_ZK_NODE, aimClazzStr);
			if (StringUtils.isNotBlank(aimClazzStr) && StringUtils.isNotBlank(serializerClazzStr)) {
				try {
					Class<?> aimClazz = Class.forName(aimClazzStr);
					Class<?> serializerClazz = Class.forName(serializerClazzStr);

					Serializer<?> serializer = (Serializer) serializerClazz.newInstance();
					kryo.register(aimClazz, serializer);
				} catch (ClassNotFoundException ex) {
					LOG.warn("ClassNotFoundException: entity class '" + aimClazzStr + "' or serializer class '"
							+ serializerClazzStr + "' not found.");
				} catch (InstantiationException e) {
					LOG.warn("InstantiationException: entity class '" + aimClazzStr + "' or serializer class '"
							+ serializerClazzStr + "' init error.");
				} catch (IllegalAccessException e) {
					LOG.warn("IllegalAccessException: entity class '" + aimClazzStr + "' or serializer class '"
							+ serializerClazzStr + "' access illegal.");
				}
			}
		}
		// kryo.register(SolrDocumentList.class, new
		// SolrDocumentListSerializer());
		UnmodifiableCollectionsSerializer.registerSerializers(kryo);
		return kryo;
	}
}
