package com.ujigu.secure.common.bean;

import java.io.Serializable;

public abstract class ShardBaseEntity<PK extends Serializable, shardKey extends Serializable> extends BaseEntity<PK> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	abstract protected shardKey getShardKey();

}
