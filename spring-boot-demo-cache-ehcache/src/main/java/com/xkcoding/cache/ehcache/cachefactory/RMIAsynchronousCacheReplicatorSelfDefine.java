//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xkcoding.cache.ehcache.cachefactory;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.rmi.UnmarshalException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.distribution.EventMessage;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import net.sf.ehcache.distribution.RmiEventMessage;
import net.sf.ehcache.distribution.RmiEventMessage.RmiEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMIAsynchronousCacheReplicatorSelfDefine extends RMISynchronousCacheReplicator {
    private static final Logger LOG = LoggerFactory.getLogger(RMIAsynchronousCacheReplicatorSelfDefine.class.getName());
    private final Thread replicationThread = new RMIAsynchronousCacheReplicatorSelfDefine.ReplicationThread();
    private final int replicationInterval;
    private final int maximumBatchSize;
    private final Queue<Object> replicationQueue = new ConcurrentLinkedQueue();
    private CacheEventHandler cacheEventHandler;

    public RMIAsynchronousCacheReplicatorSelfDefine(boolean replicatePuts, boolean replicatePutsViaCopy, boolean replicateUpdates, boolean replicateUpdatesViaCopy, boolean replicateRemovals, int replicationInterval, int maximumBatchSize) {
        super(replicatePuts, replicatePutsViaCopy, replicateUpdates, replicateUpdatesViaCopy, replicateRemovals);
        this.replicationInterval = replicationInterval;
        this.maximumBatchSize = maximumBatchSize;
        this.status = Status.STATUS_ALIVE;
        this.replicationThread.start();
      ServiceLoader<CacheEventHandler> cacheEventHandlers = ServiceLoader.load(CacheEventHandler.class);
      //默认取第一个
      cacheEventHandler = cacheEventHandlers.iterator().next();
    }


    private void replicationThreadMain() {
        while(true) {
            if (this.alive() && this.replicationQueue != null && this.replicationQueue.isEmpty()) {
                try {
                    Thread.sleep((long)this.replicationInterval);
                } catch (InterruptedException var2) {
                    LOG.debug("Spool Thread interrupted.");
                    return;
                }
            } else {
                if (this.notAlive()) {
                    return;
                }

                try {
                    this.writeReplicationQueue();
                } catch (Throwable var3) {
                    LOG.error("Exception on flushing of replication queue: " + var3.getMessage() + ". Continuing...", var3);
                }
            }
        }
    }
  @Override
    public final void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicatePuts) {
                if (this.replicatePutsViaCopy) {
                    if (!element.isSerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Object with key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
                        }

                        return;
                    }

                    this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.PUT, (Serializable)null, element));
                } else {
                    if (!element.isKeySerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Object with key " + element.getObjectKey() + " does not have a Serializable key and cannot be replicated via invalidate.");
                        }

                        return;
                    }

                    this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.REMOVE, element.getKey(), (Element)null));
                }

            }
        }
    }
  @Override
    public final void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicateUpdates) {
                if (this.replicateUpdatesViaCopy) {
                    if (!element.isSerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Object with key " + element.getObjectKey() + " is not Serializable and cannot be updated via copy.");
                        }

                        return;
                    }

                    this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.PUT, (Serializable)null, element));
                } else {
                    if (!element.isKeySerializable()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Object with key " + element.getObjectKey() + " does not have a Serializable key and cannot be replicated via invalidate.");
                        }

                        return;
                    }

                    this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.REMOVE, element.getKey(), (Element)null));
                }

            }
        }
    }
  @Override
    public final void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (!this.notAlive()) {
            if (this.replicateRemovals) {
                if (!element.isKeySerializable()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
                    }

                } else {
                    this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.REMOVE, element.getKey(), (Element)null));
                }
            }
        }
    }
  @Override
    public void notifyRemoveAll(Ehcache cache) {
        if (!this.notAlive()) {
            if (this.replicateRemovals) {
                this.addToReplicationQueue(new RmiEventMessage(cache, RmiEventType.REMOVE_ALL, (Serializable)null, (Element)null));
            }
        }
    }

    protected void addToReplicationQueue(RmiEventMessage eventMessage) {
      cacheEventHandler.handleEvent(eventMessage);
        if (!this.replicationThread.isAlive()) {
            LOG.error("CacheEventMessages cannot be added to the replication queue because the replication thread has died.");
        } else {
            switch(eventMessage.getType()) {
            case PUT:
                this.replicationQueue.add(new SoftReference(eventMessage));
                break;
            default:
                this.replicationQueue.add(eventMessage);
            }
        }

    }

    private void writeReplicationQueue() {
        List<EventMessage> eventMessages = this.extractEventMessages(this.maximumBatchSize);
        if (!eventMessages.isEmpty()) {
            Iterator var2 = RMISynchronousCacheReplicatorSelfDefine.listRemoteCachePeers(((EventMessage)eventMessages.get(0)).getEhcache()).iterator();

            while(var2.hasNext()) {
                CachePeer cachePeer = (CachePeer)var2.next();

                try {
                    cachePeer.send(eventMessages);
                } catch (UnmarshalException var6) {
                    String message = var6.getMessage();
                    if (!message.contains("Read time out") && !message.contains("Read timed out")) {
                        LOG.debug("Unable to send message to remote peer.  Message was: " + message);
                    } else {
                        LOG.warn("Unable to send message to remote peer due to socket read timeout. Consider increasing the socketTimeoutMillis setting in the cacheManagerPeerListenerFactory. Message was: " + message);
                    }
                } catch (Throwable var7) {
                    LOG.warn("Unable to send message to remote peer.  Message was: " + var7.getMessage(), var7);
                }
            }
        }

    }

    private void flushReplicationQueue() {
        while(!this.replicationQueue.isEmpty()) {
            this.writeReplicationQueue();
        }

    }

    private List<EventMessage> extractEventMessages(int limit) {
        List<EventMessage> list = new ArrayList(Math.min(this.replicationQueue.size(), limit));
        int droppedMessages = 0;

        while(list.size() < limit) {
            Object polled = this.replicationQueue.poll();
            if (polled == null) {
                break;
            }

            if (polled instanceof EventMessage) {
                list.add((EventMessage)polled);
            } else {
                EventMessage message = (EventMessage)((SoftReference)polled).get();
                if (message == null) {
                    ++droppedMessages;
                } else {
                    list.add(message);
                }
            }
        }

        if (droppedMessages > 0) {
            LOG.warn(droppedMessages + " messages were discarded on replicate due to reclamation of SoftReferences by the VM. Consider increasing the maximum heap size and/or setting the starting heap size to a higher value.");
        }

        return list;
    }
  @Override
    public final void dispose() {
        this.status = Status.STATUS_SHUTDOWN;
        this.flushReplicationQueue();
    }
  @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new RMIAsynchronousCacheReplicatorSelfDefine(this.replicatePuts, this.replicatePutsViaCopy, this.replicateUpdates, this.replicateUpdatesViaCopy, this.replicateRemovals, this.replicationInterval, this.maximumBatchSize);
    }

    private final class ReplicationThread extends Thread {
        public ReplicationThread() {
            super("Replication Thread");
            this.setDaemon(true);
            this.setPriority(5);
        }
        @Override
        public final void run() {
            RMIAsynchronousCacheReplicatorSelfDefine.this.replicationThreadMain();
        }
    }
}
