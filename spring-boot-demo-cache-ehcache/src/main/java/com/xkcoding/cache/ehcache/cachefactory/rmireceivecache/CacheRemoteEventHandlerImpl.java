package com.xkcoding.cache.ehcache.cachefactory.rmireceivecache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRemoteEventHandlerImpl implements CacheRemoteEventHandler {
  private static final Logger LOG = LoggerFactory.getLogger(CacheRemoteEventHandlerImpl.class);
  @Override
  public void handler(RemoteEnum remoteEnum) {
    LOG.info("xxxxxxxxxx  get remote cache changes {}",remoteEnum.type);

  }
}
