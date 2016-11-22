package com.yisi.stiku.rpc.test;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * @author shangfeng
 *
 */
public class Person {

	private String name;
	@Tag(1)
	private int weight;

	@Optional("height")
	private int height;

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	// public int getAge() {
	//
	// return age;
	// }
	//
	// public void setAge(int age) {
	//
	// this.age = age;
	// }

	public int getWeight() {

		return weight;
	}

	public void setWeight(int weight) {

		this.weight = weight;
	}

	public int getHeight() {

		return height;
	}

	public void setHeight(int height) {

		this.height = height;
	}

}
