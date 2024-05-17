package UtilitiesToolLib.module.TemporaryStorage;

import java.lang.reflect.ParameterizedType;

import lombok.Getter;

@Getter
public abstract class AbstractTemporaryStorage<K, V> {

  private Class<K> clazzKey;

  private Class<V> clazzValue;

  @SuppressWarnings("unchecked")
  protected AbstractTemporaryStorage() {
    ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
    clazzKey = (Class<K>) type.getActualTypeArguments()[0];
    clazzValue = (Class<V>) type.getActualTypeArguments()[1];
  }

  public AbstractTemporaryStorage(Class<K> clazzKey, Class<V> clazzValue) {
    this.clazzKey = clazzKey;
    this.clazzValue = clazzValue;
  }
}
