package com.xkcoding.cache.ehcache.cachefactory;

import net.sf.ehcache.distribution.RmiEventMessage;

public interface CacheEventHandler {
    void handleEvent(RmiEventMessage eventMessage);
}
