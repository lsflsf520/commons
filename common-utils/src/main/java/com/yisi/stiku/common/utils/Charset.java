package com.yisi.stiku.common.utils;


public enum Charset
{
  ISO("ISO-8859-1", 1), 
  GBK("GBK", 2), 
  UTF8("UTF-8", 3);

  private String name;
  private int bytes;

  private Charset(String name, int bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  public String getName() {
    return this.name;
  }

  public int getBytes() {
    return this.bytes;
  }
}