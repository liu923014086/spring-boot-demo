package com.xkcoding.cache.ehcache.cachefactory;

import net.sf.ehcache.distribution.RmiEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventHandlerImpl implements CacheEventHandler {
  private static final Logger LOG = LoggerFactory.getLogger(CacheEventHandlerImpl.class);
  @Override
  public void handleEvent(RmiEventMessage eventMessage) {
    LOG.info("get cache status========={}",eventMessage.getType().name());
  }
}
