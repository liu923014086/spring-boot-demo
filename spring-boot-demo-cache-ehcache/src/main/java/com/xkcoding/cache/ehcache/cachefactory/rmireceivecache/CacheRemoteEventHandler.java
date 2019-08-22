package com.xkcoding.cache.ehcache.cachefactory.rmireceivecache;


public interface CacheRemoteEventHandler {
  /**
   *处理远程传过来的cache变化
   */
  void handler(RemoteEnum remoteEnum);
}
