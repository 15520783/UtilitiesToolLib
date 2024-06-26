/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package UtilitiesToolLib.module.TemporaryStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.dropwizard.util.DataSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Student {
  private String id;
  private String name;
  private int age;

  public String toString() {
    return id + ":" + name + ":" + String.valueOf(age);
  }
}

class TemporaryStorageTest {
  @Test
  void someLibraryMethodReturnsTrue() {
    TemporaryStorage<String, Student> tempStorage =
        new TemporaryStorage<String, Student>(DataSize.megabytes(1).toBytes(), true) {};
    List<String> keys = new ArrayList<String>();
    List<Student> beforeList = new ArrayList<Student>();
    List<Student> afterList = new ArrayList<Student>();
    try {
      for (int i = 0; i < 100000; i++) {
        Student target = new Student(("0000" + i), "Member " + i, 18);
        String key = UUID.randomUUID().toString();
        tempStorage.add(key, new Student(("0000" + i), "Member " + i, 18));
        keys.add(key);
        beforeList.add(target);
      }
      afterList = keys.stream().map(key -> tempStorage.get(key)).collect(Collectors.toList());
    } catch (Exception e) {
      // ignore
    } finally {
      tempStorage.close();
    }
    for (int i = 0; i < 100000; i++) {
      assertEquals(beforeList.get(i).toString(), afterList.get(i).toString());
    }
  }
}
