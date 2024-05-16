package UtilitiesToolLib.common.jackson.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import UtilitiesToolLib.common.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Decrypt data by AES algorithm
 * 
 * @author thaint
 *
 */
@Slf4j
public class DecryptionDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

  private Class<?> targetClass;

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
    // Find here the targetClass to be deserialized
    String targetClassName = ctxt.getContextualType().toCanonical();
    try {
      targetClass = Class.forName(targetClassName);
    } catch (ClassNotFoundException e) {
      log.error("Failed to get target class", e.getMessage());
    }
    return this;
  }

  @Override
  public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String decryptData = EncryptionUtil.decrypt(jsonParser.getValueAsString());
      if (targetClass.equals(String.class)) {
        return decryptData;
      }
      return objectMapper.readValue(decryptData, targetClass);
    } catch (Exception e) {
      log.error("Failed to deserialize", e.getMessage());
      return null;
    }
  }

}
