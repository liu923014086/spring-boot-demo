package net.sf.ehcache.distribution;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.xkcoding.cache.ehcache.cachefactory.rmireceivecache.CacheRemoteEventHandler;
import com.xkcoding.cache.ehcache.cachefactory.rmireceivecache.RemoteEnum;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RmiEventMessage.RmiEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 覆盖原有的加入接收cache变化的逻辑
 */
public class RMICachePeer extends UnicastRemoteObject implements CachePeer, Remote {
    private static final Logger LOG = LoggerFactory.getLogger(RMICachePeer.class.getName());
    private final String hostname;
    private final Integer rmiRegistryPort;
    private Integer remoteObjectPort;
    private final Ehcache cache;
  private CacheRemoteEventHandler cacheRemoteEventHandler;

    public RMICachePeer(Ehcache cache, String hostName, Integer rmiRegistryPort, Integer remoteObjectPort, Integer socketTimeoutMillis) throws RemoteException {
        super(remoteObjectPort.intValue(), new ConfigurableRMIClientSocketFactory(socketTimeoutMillis), ConfigurableRMIClientSocketFactory.getConfiguredRMISocketFactory());
        this.remoteObjectPort = remoteObjectPort;
        this.hostname = hostName;
        this.rmiRegistryPort = rmiRegistryPort;
        this.cache = cache;
      //spi实现注入
      ServiceLoader<CacheRemoteEventHandler> cacheRemoteEventHandlers = ServiceLoader.load(CacheRemoteEventHandler.class);
      this.cacheRemoteEventHandler = cacheRemoteEventHandlers.iterator().next();
    }
    @Override
    public final String getUrl() {
        return "//" + (this.hostname.contains(":") ? "[" + this.hostname + "]" : this.hostname) + ":" + this.rmiRegistryPort + "/" + this.cache.getName();
    }
  @Override
    public final String getUrlBase() {
        return "//" + (this.hostname.contains(":") ? "[" + this.hostname + "]" : this.hostname) + ":" + this.rmiRegistryPort;
    }
  @Override
    public List getKeys() throws RemoteException {
        List keys = this.cache.getKeys();
        return (List)(keys instanceof Serializable ? keys : new ArrayList(keys));
    }
  @Override
    public Element getQuiet(Serializable key) throws RemoteException {
        return this.cache.getQuiet(key);
    }
  @Override
    public List getElements(List keys) throws RemoteException {
        if (keys == null) {
            return new ArrayList();
        } else {
            List elements = new ArrayList();

            for(int i = 0; i < keys.size(); ++i) {
                Serializable key = (Serializable)keys.get(i);
                Element element = this.cache.getQuiet(key);
                if (element != null) {
                    elements.add(element);
                }
            }

            return elements;
        }
    }
  @Override
    public void put(Element element) throws RemoteException, IllegalArgumentException, IllegalStateException {
        this.cache.put(element, true);
    cacheRemoteEventHandler.handler(RemoteEnum.PUT);
        if (LOG.isDebugEnabled()) {
            LOG.debug("RMICachePeer for cache " + this.cache.getName() + ": remote put received. Element is: " + element);
        }

    }
  @Override
    public boolean remove(Serializable key) throws RemoteException, IllegalStateException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RMICachePeer for cache " + this.cache.getName() + ": remote remove received for key: " + key);
        }
    cacheRemoteEventHandler.handler(RemoteEnum.REMOVE);
        return this.cache.remove(key, true);
    }
  @Override
    public void removeAll() throws RemoteException, IllegalStateException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RMICachePeer for cache " + this.cache.getName() + ": remote removeAll received");
        }
    cacheRemoteEventHandler.handler(RemoteEnum.REMOVE_ALL);
        this.cache.removeAll(true);
    }
  @Override
    public void send(List eventMessages) throws RemoteException {
        for(int i = 0; i < eventMessages.size(); ++i) {
            RmiEventMessage eventMessage = (RmiEventMessage)eventMessages.get(i);
            if (eventMessage.getType() == RmiEventType.PUT) {
                this.put(eventMessage.getElement());
            } else if (eventMessage.getType() == RmiEventType.REMOVE) {
                this.remove(eventMessage.getSerializableKey());
            } else if (eventMessage.getType() == RmiEventType.REMOVE_ALL) {
                this.removeAll();
            } else {
                LOG.error("Unknown event: " + eventMessage);
            }
        }

    }
  @Override
    public final String getName() throws RemoteException {
        return this.cache.getName();
    }
  @Override
    public final String getGuid() throws RemoteException {
        return this.cache.getGuid();
    }

    final Ehcache getBoundCacheInstance() {
        return this.cache;
    }
  @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("URL: ");
        buffer.append(this.getUrl());
        buffer.append(" Remote Object Port: ");
        buffer.append(this.remoteObjectPort);
        return buffer.toString();
    }
}
