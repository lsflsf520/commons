package com.yisi.stiku.rpc.test;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

/**
 * @author shangfeng
 *
 */
public class Car {

	private String pinpai;
	private int width;
	private int height = 10;
	@Optional("length")
	private int length;

	public String getPinpai() {

		return pinpai;
	}

	public void setPinpai(String pinpai) {

		this.pinpai = pinpai;
	}

	public int getWidth() {

		return width;
	}

	public void setWidth(int width) {

		this.width = width;
	}

	public int getHeight() {

		return height;
	}

	public void setHeight(int height) {

		this.height = height;
	}

	public int getLength() {

		return length;
	}

	public void setLength(int length) {

		this.length = length;
	}

}
