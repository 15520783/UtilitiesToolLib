package UtilitiesToolLib.module.TemporaryStorage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Temporary partition storage
 * 
 * @since PBI_6659
 */
@Slf4j
@Getter
public abstract class TemporaryPartition<K, T> implements Closeable {

  /** Data alignment size in byte */
  private static final int DATA_ALIGNMENT_SIZE = 4;

  /** Default buffer size for reading/writing data */
  private static final int DEFAULT_BUFFER_SIZE = 8192;

  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

  private ObjectMapper objectMapper = new ObjectMapper();

  /** The unique id of partition */
  private String uuid;

  /** Data map */
  private Map<K, T> dataMap;

  /** Flag indicates whether data has been persisted into hard disk */
  private boolean persisted;

  /** Flag indicates whether data should be compress before writing into disk */
  private boolean compress;

  /** The total size of all data in byte */
  private long size;

  private Class<K> clazzKey;

  private Class<T> clazzValue;

  /**
   * Create temporary partition storage
   */
  public TemporaryPartition() {
    this(false);
  }

  /**
   * Create temporary partition storage
   * 
   * @param compress if true data will be compressed before writing to disk
   */
  @SuppressWarnings("unchecked")
  public TemporaryPartition(boolean compress) {
    this.uuid = UUID.randomUUID().toString();
    this.dataMap = new LinkedHashMap<>();
    this.compress = compress;
    final ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
    clazzKey = (Class<K>) type.getActualTypeArguments()[0];
    clazzValue = (Class<T>) type.getActualTypeArguments()[1];
  }

  /**
   * Create temporary partition storage
   * 
   * @param compress if true data will be compressed before writing to disk
   */
  @SuppressWarnings("unchecked")
  public TemporaryPartition(boolean compress, TemporaryStorage<?, ?> temporaryStorage) {
    this.uuid = UUID.randomUUID().toString();
    this.dataMap = new LinkedHashMap<>();
    this.compress = compress;
    final ParameterizedType type = (ParameterizedType) temporaryStorage.getClass().getGenericSuperclass();
    clazzKey = (Class<K>) type.getActualTypeArguments()[0];
    clazzValue = (Class<T>) type.getActualTypeArguments()[1];
  }

  /**
   * Close all resources after used
   */
  @Override
  public void close() {
    File storageFile = getStorageFile();

    if (storageFile.exists() && storageFile.isFile()) {
      if (storageFile.delete()) {
        log.info("TemporaryPartition: {} temporary file has been removed successfully", uuid);
      }
    }

    dataMap.clear();
    persisted = false;
  }

  /***
   * Get value by string
   * 
   * @param value
   * @return
   * @throws JsonProcessingException
   */
  private String getValueByString(Object value) throws JsonProcessingException {
    return (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
  }

  /**
   * Add data with specific key into partition
   * 
   * @param key Integer
   * @param value String
   * @throws Exception
   */
  public void add(final K key, final T value) throws Exception {
    int dataLength = StorageUtil.getBytesCount(getValueByString(value));
    this.size += dataLength;
    dataMap.put(key, value);
  }

  /**
   * Get data associated with the specific key
   * 
   * @param key K
   * @return T
   */
  public T get(final K key) {
    return dataMap.get(key);
  }

  /**
   * Check whether value associated with specific key is existed or not
   * 
   * @param key K
   * @return boolean
   */
  public boolean containsKey(final K key) {
    return dataMap.containsKey(key);
  }

  /**
   * Returns a Set view of the keys contained in this partition.
   * 
   * @return Set<K>
   */
  public Set<K> keySet() {
    return dataMap.keySet();
  }

  /**
   * Save data into disk and clean up memory using by data
   * 
   * @throws IOException
   */
  public void save() throws IOException {
    Set<Entry<K, T>> entrySet = dataMap.entrySet();

    if (!isPersisted()) {
      this.persisted = true;

      File storageFile = getStorageFile();

      FileOutputStream fos = null;
      OutputStream bos = null;

      try {
        fos = new FileOutputStream(storageFile, false);
        bos = compress ? new GZIPOutputStream(fos, DEFAULT_BUFFER_SIZE) : new BufferedOutputStream(fos);
        // Write header
        // 4 bytes: total key set
        // headers:
        // - 4 bytes: length of key
        // - 4 bytes: length of data
        // - 4 bytes: key value
        // Using 4 byte for total key in set
        bos.write(StorageUtil.convertIntToByteArray(entrySet.size()));

        for (Entry<K, T> entry : entrySet) {
          final K key = entry.getKey();
          int keyLength = StorageUtil.getBytesCount(getValueByString(key));
          int dataLength = StorageUtil.getBytesCount(getValueByString(dataMap.get(key)));
          bos.write(StorageUtil.convertIntToByteArray(keyLength));
          bos.write(StorageUtil.convertIntToByteArray(dataLength));
          bos.write(getValueByString(key).getBytes(StorageUtil.UTF8));
        }

        bos.flush();

        // Write data:
        // - n bytes: data byte array
        for (Entry<K, T> entry : entrySet) {
          byte[] dataArray = getValueByString(entry.getValue()).getBytes(StorageUtil.UTF8);

          bos.write(dataArray);
        }

        bos.flush();
        log.info("TemporaryPartition: {} temporary file has been created successfully", uuid);
      } finally {
        StorageUtil.closeQuietly(bos);
        StorageUtil.closeQuietly(fos);
      }
    }
    // Clean up memory using by data
    for (Entry<K, T> entry : entrySet) {
      if (entry.getValue() != null) {

        entry.setValue(null);
      }
    }
  }

  /**
   * Load data from disk into memory
   * 
   * @throws FileNotFoundException
   * @throws IOException
   * @throws DataFormatException
   */
  @SuppressWarnings("unchecked")
  public void load() throws FileNotFoundException, IOException {
    if (isPersisted()) {
      File storageFile = getStorageFile();

      FileInputStream fis = null;
      InputStream bis = null;

      try {
        fis = new FileInputStream(storageFile);
        bis = compress ? new GZIPInputStream(fis, DEFAULT_BUFFER_SIZE) : new BufferedInputStream(fis);
        // Read total key set
        byte[] totalCountArray = bis.readNBytes(DATA_ALIGNMENT_SIZE);
        int total = StorageUtil.convertByteArrayToInt(totalCountArray);
        Map<K, Integer> dataLengthMap = new LinkedHashMap<>();

        // Read all key value along side data length in byte
        for (int i = 0; i < total; i++) {
          byte[] headerArray = bis.readNBytes(DATA_ALIGNMENT_SIZE);
          int keyLength = StorageUtil.convertByteArrayToInt(headerArray);

          headerArray = bis.readNBytes(DATA_ALIGNMENT_SIZE);
          int dataLength = StorageUtil.convertByteArrayToInt(headerArray);

          headerArray = bis.readNBytes(keyLength);
          String strKey = StorageUtil.convertByteArrayToString(headerArray);

          if (clazzKey.equals(String.class)) {
            dataLengthMap.put((K) strKey, dataLength);
          } else {
            dataLengthMap.put(objectMapper.readValue(strKey, clazzKey), dataLength);
          }
        }

        // Clear the existing map
        dataMap.clear();

        for (Entry<K, Integer> entry : dataLengthMap.entrySet()) {
          final K key = entry.getKey();
          // Get real data in byte
          int dataLength = dataLengthMap.get(key);
          // If compressed size is zero, it means data was not compressed at all, using data length instead
          byte[] dataArray = bis.readNBytes(dataLength);

          if (dataArray.length == dataLength) {
            if (clazzValue.equals(String.class)) {
              dataMap.put(key, (T) StorageUtil.convertByteArrayToString(dataArray));
            } else {
              dataMap.put(key, objectMapper.readValue(StorageUtil.convertByteArrayToString(dataArray), clazzValue));
            }
          }
        }
      } finally {
        StorageUtil.closeQuietly(bis);
        StorageUtil.closeQuietly(fis);
      }
    }
  }

  /**
   * Get temporary file path on disk
   * 
   * @return File
   */
  public File getStorageFile() {
    return Path.of(TEMP_DIR, uuid + ".tsp").toFile();
  }
}
