package com.xkcoding.cache.ehcache.cachefactory.rmireceivecache;

public enum  RemoteEnum {
  PUT("put"),
  REMOVE("remove"),
  REMOVE_ALL("remove_all");

  String type;

  RemoteEnum(){}
  RemoteEnum(String type){
    this.type = type;
  }
}
