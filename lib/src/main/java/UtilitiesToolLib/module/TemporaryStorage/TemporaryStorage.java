package UtilitiesToolLib.module.TemporaryStorage;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Temporary storage utility class <br/>
 * It keeps a small amount of data on memory, others will be persisted into hard disk
 * 
 */
@Slf4j
public abstract class TemporaryStorage<K, T> implements Closeable {

  /** Active partition */
  private TemporaryPartition<K, T> activePartition;

  /** Persisted partitions */
  private List<TemporaryPartition<K, T>> persistedPartitions;

  /** Limit size of each partition in byte */
  private long limitSize;

  /** Flag indicates whether data should be compress before writing into disk */
  private boolean compress;

  protected TemporaryStorage(long limitSize, boolean compress) {
    this.limitSize = limitSize;
    this.compress = compress;
    activePartition = createNewPartition();
    persistedPartitions = new ArrayList<>();
  }

  /**
   * Close all resources after used
   */
  @Override
  public void close() {
    activePartition.close();

    for (TemporaryPartition<K, T> partition : persistedPartitions) {
      partition.close();
    }
  }

  /**
   * Add data with specific key into partition
   * 
   * @param key Integer
   * @param value String
   * @throws Exception
   */
  public void add(final K key, final T value) throws Exception {
    // If current size of partition is exceeding limit
    // Try to save it into disk then create brand new for storing data
    if (activePartition.getSize() >= limitSize) {
      try {
        activePartition.save();
        if (persistedPartitions.indexOf(activePartition) < 0) {
          persistedPartitions.add(activePartition);
        }

        activePartition = createNewPartition();
      } catch (IOException e) {
        log.error("Temporary storage save paritition: an error has been occurred", e);
      }
    }

    activePartition.add(key, value);
  }

  /**
   * Get data associated with the specific key
   * 
   * @param key T
   * @return String
   */
  public T get(final K key) {
    if (key == null) {
      throw new IllegalArgumentException("Key can not be null");
    }
    if (activePartition.containsKey(key)) {
      return activePartition.get(key);
    }
    try {
      TemporaryPartition<K, T> activeCandidate = null;

      for (TemporaryPartition<K, T> persistedPartition : persistedPartitions) {
        if (persistedPartition.containsKey(key)) {
          activeCandidate = persistedPartition;
          break;
        }
      }
      if (activeCandidate != null) {
        log.info("Temporary storage: swap {} for {}", activePartition.getUuid(), activeCandidate.getUuid());
        // Load data of active candidate from disk into memory
        activeCandidate.load();
        // Save data of active partition from memory into disk
        activePartition.save();

        // Swap current active partition and candidate
        persistedPartitions.remove(activeCandidate);

        if (persistedPartitions.indexOf(activePartition) < 0) {
          persistedPartitions.add(activePartition);
        }
        activePartition = activeCandidate;

        return activePartition.get(key);
      }
    } catch (IOException e) {
      log.error("Temporary storage load paritition: an error has been occurred", e);
    }

    return null;
  }

  /**
   * Check whether value associated with specific key is existed or not
   * 
   * @param key K
   * @return boolean
   */
  public boolean containsKey(final K key) {
    if (activePartition.containsKey(key)) {
      return true;
    }
    for (TemporaryPartition<K, T> persistedPartition : persistedPartitions) {
      if (persistedPartition.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create new partition for storing data
   * 
   * @return TemporaryPartition
   */
  private TemporaryPartition<K, T> createNewPartition() {
    return new TemporaryPartition<K, T>(this.compress, this) {};
  }
}
