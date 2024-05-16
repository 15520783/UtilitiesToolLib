package UtilitiesToolLib.common.jackson.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import UtilitiesToolLib.common.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Encrypt data to cipher text by AES algorithm
 * 
 * @author thaint
 *
 */
@Slf4j
public class EncryptionSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    if (value == null) {
      jgen.writeNull();
    } else {
      try {
        if (value instanceof String) {
          jgen.writeString(EncryptionUtil.encrypt((String) value));
        } else {
          jgen.writeString(EncryptionUtil.encrypt(mapper.writeValueAsString(value)));
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

}
