package com.xkcoding.cache.ehcache.cachefactory;

import java.util.Properties;

import net.sf.ehcache.distribution.RMIAsynchronousCacheReplicator;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import net.sf.ehcache.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMICacheReplicatorSelfDefineFactory extends CacheEventListenerFactory {
  protected static final int DEFAULT_ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS = 1000;
  protected static final int DEFAULT_ASYNCHRONOUS_REPLICATION_MAXIMUM_BATCH_SIZE = 1000;
  private static final Logger LOG = LoggerFactory.getLogger(net.sf.ehcache.distribution.RMICacheReplicatorFactory.class.getName());
  private static final String REPLICATE_PUTS = "replicatePuts";
  private static final String REPLICATE_PUTS_VIA_COPY = "replicatePutsViaCopy";
  private static final String REPLICATE_UPDATES = "replicateUpdates";
  private static final String REPLICATE_UPDATES_VIA_COPY = "replicateUpdatesViaCopy";
  private static final String REPLICATE_REMOVALS = "replicateRemovals";
  private static final String REPLICATE_ASYNCHRONOUSLY = "replicateAsynchronously";
  private static final String ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS = "asynchronousReplicationIntervalMillis";
  private static final String ASYNCHRONOUS_REPLICATION_MAXIMUM_BATCH_SIZE = "asynchronousReplicationMaximumBatchSize";
  private static final int MINIMUM_REASONABLE_INTERVAL = 10;

  public RMICacheReplicatorSelfDefineFactory() {
  }

  @Override
  public final CacheEventListener createCacheEventListener(Properties properties) {
    boolean replicatePuts = this.extractReplicatePuts(properties);
    boolean replicatePutsViaCopy = this.extractReplicatePutsViaCopy(properties);
    boolean replicateUpdates = this.extractReplicateUpdates(properties);
    boolean replicateUpdatesViaCopy = this.extractReplicateUpdatesViaCopy(properties);
    boolean replicateRemovals = this.extractReplicateRemovals(properties);
    boolean replicateAsynchronously = this.extractReplicateAsynchronously(properties);
    int replicationIntervalMillis = this.extractReplicationIntervalMilis(properties);
    int maximumBatchSize = this.extractMaximumBatchSize(properties);
    return (CacheEventListener)(replicateAsynchronously ? new RMIAsynchronousCacheReplicatorSelfDefine(replicatePuts, replicatePutsViaCopy, replicateUpdates, replicateUpdatesViaCopy, replicateRemovals, replicationIntervalMillis, maximumBatchSize) : new RMISynchronousCacheReplicator(replicatePuts, replicatePutsViaCopy, replicateUpdates, replicateUpdatesViaCopy, replicateRemovals));
  }

  protected int extractReplicationIntervalMilis(Properties properties) {
    String asynchronousReplicationIntervalMillisString = PropertyUtil.extractAndLogProperty("asynchronousReplicationIntervalMillis", properties);
    int asynchronousReplicationIntervalMillis;
    if (asynchronousReplicationIntervalMillisString != null) {
      try {
        int asynchronousReplicationIntervalMillisCandidate = Integer.parseInt(asynchronousReplicationIntervalMillisString);
        if (asynchronousReplicationIntervalMillisCandidate < 10) {
          LOG.debug("Trying to set the asynchronousReplicationIntervalMillis to an unreasonable number. Using the default instead.");
          asynchronousReplicationIntervalMillis = 1000;
        } else {
          asynchronousReplicationIntervalMillis = asynchronousReplicationIntervalMillisCandidate;
        }
      } catch (NumberFormatException var5) {
        LOG.warn("Number format exception trying to set asynchronousReplicationIntervalMillis. Using the default instead. String value was: '" + asynchronousReplicationIntervalMillisString + "'");
        asynchronousReplicationIntervalMillis = 1000;
      }
    } else {
      asynchronousReplicationIntervalMillis = 1000;
    }

    return asynchronousReplicationIntervalMillis;
  }

  protected int extractMaximumBatchSize(Properties properties) {
    String maximumBatchSizeString = PropertyUtil.extractAndLogProperty("asynchronousReplicationMaximumBatchSize", properties);
    if (maximumBatchSizeString == null) {
      return 1000;
    } else {
      try {
        return Integer.parseInt(maximumBatchSizeString);
      } catch (NumberFormatException var4) {
        LOG.warn("Number format exception trying to set maximumBatchSize. Using the default instead. String value was: '" + maximumBatchSizeString + "'");
        return 1000;
      }
    }
  }

  protected boolean extractReplicateAsynchronously(Properties properties) {
    String replicateAsynchronouslyString = PropertyUtil.extractAndLogProperty("replicateAsynchronously", properties);
    boolean replicateAsynchronously;
    if (replicateAsynchronouslyString != null) {
      replicateAsynchronously = PropertyUtil.parseBoolean(replicateAsynchronouslyString);
    } else {
      replicateAsynchronously = true;
    }

    return replicateAsynchronously;
  }

  protected boolean extractReplicateRemovals(Properties properties) {
    String replicateRemovalsString = PropertyUtil.extractAndLogProperty("replicateRemovals", properties);
    boolean replicateRemovals;
    if (replicateRemovalsString != null) {
      replicateRemovals = PropertyUtil.parseBoolean(replicateRemovalsString);
    } else {
      replicateRemovals = true;
    }

    return replicateRemovals;
  }

  protected boolean extractReplicateUpdatesViaCopy(Properties properties) {
    String replicateUpdatesViaCopyString = PropertyUtil.extractAndLogProperty("replicateUpdatesViaCopy", properties);
    boolean replicateUpdatesViaCopy;
    if (replicateUpdatesViaCopyString != null) {
      replicateUpdatesViaCopy = PropertyUtil.parseBoolean(replicateUpdatesViaCopyString);
    } else {
      replicateUpdatesViaCopy = true;
    }

    return replicateUpdatesViaCopy;
  }

  protected boolean extractReplicatePutsViaCopy(Properties properties) {
    String replicatePutsViaCopyString = PropertyUtil.extractAndLogProperty("replicatePutsViaCopy", properties);
    boolean replicatePutsViaCopy;
    if (replicatePutsViaCopyString != null) {
      replicatePutsViaCopy = PropertyUtil.parseBoolean(replicatePutsViaCopyString);
    } else {
      replicatePutsViaCopy = true;
    }

    return replicatePutsViaCopy;
  }

  protected boolean extractReplicateUpdates(Properties properties) {
    String replicateUpdatesString = PropertyUtil.extractAndLogProperty("replicateUpdates", properties);
    boolean replicateUpdates;
    if (replicateUpdatesString != null) {
      replicateUpdates = PropertyUtil.parseBoolean(replicateUpdatesString);
    } else {
      replicateUpdates = true;
    }

    return replicateUpdates;
  }

  protected boolean extractReplicatePuts(Properties properties) {
    String replicatePutsString = PropertyUtil.extractAndLogProperty("replicatePuts", properties);
    boolean replicatePuts;
    if (replicatePutsString != null) {
      replicatePuts = PropertyUtil.parseBoolean(replicatePutsString);
    } else {
      replicatePuts = true;
    }

    return replicatePuts;
  }
}
