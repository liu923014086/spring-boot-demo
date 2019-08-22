//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xkcoding.cache.ehcache.cachefactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.distribution.CacheReplicator;
import net.sf.ehcache.distribution.RemoteCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMISynchronousCacheReplicatorSelfDefine implements CacheReplicator {
    private static final Logger LOG = LoggerFactory.getLogger(RMISynchronousCacheReplicatorSelfDefine.class.getName());
    protected Status status;
    protected final boolean replicatePuts;
    protected boolean replicatePutsViaCopy;
    protected final boolean replicateUpdates;
    protected final boolean replicateUpdatesViaCopy;
    protected final boolean replicateRemovals;

    public RMISynchronousCacheReplicatorSelfDefine(boolean replicatePuts, boolean replicatePutsViaCopy, boolean replicateUpdates, boolean replicateUpdatesViaCopy, boolean replicateRemovals) {
        this.replicatePuts = replicatePuts;
        this.replicatePutsViaCopy = replicatePutsViaCopy;
        this.replicateUpdates = replicateUpdates;
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
        this.replicateRemovals = replicateRemovals;
        this.status = Status.STATUS_ALIVE;
    }
    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicatePuts) {
                if (!element.isSerializable()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Object with key " + element.getObjectKey() + " is not Serializable and cannot be replicated");
                    }

                } else {
                    if (this.replicatePutsViaCopy) {
                        replicatePutNotification(cache, element);
                    } else {
                        replicateRemovalNotification(cache, (Serializable)element.getObjectKey());
                    }

                }
            }
        }
    }

    protected static void replicatePutNotification(Ehcache cache, Element element) throws RemoteCacheException {
        List cachePeers = listRemoteCachePeers(cache);
        Iterator var3 = cachePeers.iterator();

        while(var3.hasNext()) {
            Object cachePeer1 = var3.next();
            CachePeer cachePeer = (CachePeer)cachePeer1;

            try {
                cachePeer.put(element);
            } catch (Throwable var7) {
                LOG.error("Exception on replication of putNotification. " + var7.getMessage() + ". Continuing...", var7);
            }
        }

    }
  @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicateUpdates) {
                if (this.replicateUpdatesViaCopy) {
                    if (!element.isSerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Object with key " + element.getObjectKey() + " is not Serializable and cannot be updated via copy");
                        }

                        return;
                    }

                    replicatePutNotification(cache, element);
                } else {
                    if (!element.isKeySerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
                        }

                        return;
                    }

                    replicateRemovalNotification(cache, (Serializable)element.getObjectKey());
                }

            }
        }
    }
  @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicateRemovals) {
                if (!element.isKeySerializable()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
                    }

                } else {
                    replicateRemovalNotification(cache, (Serializable)element.getObjectKey());
                }
            }
        }
    }

    protected static void replicateRemovalNotification(Ehcache cache, Serializable key) throws RemoteCacheException {
        List cachePeers = listRemoteCachePeers(cache);
        Iterator var3 = cachePeers.iterator();

        while(var3.hasNext()) {
            Object cachePeer1 = var3.next();
            CachePeer cachePeer = (CachePeer)cachePeer1;

            try {
                cachePeer.remove(key);
            } catch (Throwable var7) {
                LOG.error("Exception on replication of removeNotification. " + var7.getMessage() + ". Continuing...", var7);
            }
        }

    }
  @Override
    public final void notifyElementExpired(Ehcache cache, Element element) {
    }
  @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
    }
  @Override
    public void notifyRemoveAll(Ehcache cache) {
        if (!this.notAlive()) {
            if (this.replicateRemovals) {
                this.replicateRemoveAllNotification(cache);
            }
        }
    }

    protected void replicateRemoveAllNotification(Ehcache cache) {
        List cachePeers = listRemoteCachePeers(cache);
        Iterator var3 = cachePeers.iterator();

        while(var3.hasNext()) {
            Object cachePeer1 = var3.next();
            CachePeer cachePeer = (CachePeer)cachePeer1;

            try {
                cachePeer.removeAll();
            } catch (Throwable var7) {
                LOG.error("Exception on replication of removeAllNotification. " + var7.getMessage() + ". Continuing...", var7);
            }
        }

    }

    static List<CachePeer> listRemoteCachePeers(Ehcache cache) {
        CacheManagerPeerProvider provider = cache.getCacheManager().getCacheManagerPeerProvider("RMI");
        return provider.listRemoteCachePeers(cache);
    }
  @Override
    public final boolean isReplicateUpdatesViaCopy() {
        return this.replicateUpdatesViaCopy;
    }
  @Override
    public final boolean notAlive() {
        return !this.alive();
    }
  @Override
    public final boolean alive() {
        return this.status == null ? false : this.status.equals(Status.STATUS_ALIVE);
    }
  @Override
    public void dispose() {
        this.status = Status.STATUS_SHUTDOWN;
    }
  @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new RMISynchronousCacheReplicatorSelfDefine(this.replicatePuts, this.replicatePutsViaCopy, this.replicateUpdates, this.replicateUpdatesViaCopy, this.replicateRemovals);
    }
}
