package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JsonParserTest {

  @Nested
  @DisplayName("Positive JsonParser Tests")
  class positiveJsonParserTests {

    @Test
    void parsesEmptyObject() {
      Object value = JsonParser.parse("{}");
      assertInstanceOf(Map.class, value);
      assertTrue(((Map<?, ?>) value).isEmpty());
    }

    @Test
    void parsesEmptyArray() {
      Object value = JsonParser.parse("[]");
      assertInstanceOf(List.class, value);
      assertTrue(((List<?>) value).isEmpty());
    }

    @Test
    void parsesObjectWithStringValue() {
      Object value = JsonParser.parse("{\"name\":\"Alice\"}");
      assertEquals("Alice", ((Map<?, ?>) value).get("name"));
    }

    @Test
    void parsesNumberAsBigDecimal() {
      Object value = JsonParser.parse("42.5");
      assertInstanceOf(BigDecimal.class, value);
      assertEquals(0, BigDecimal.valueOf(42.5).compareTo((BigDecimal) value));
    }

    @Test
    void parsesNegativeNumber() {
      Object value = JsonParser.parse("-17");
      assertEquals(0, BigDecimal.valueOf(-17).compareTo((BigDecimal) value));
    }

    @Test
    void parsesBooleanTrueAndFalse() {
      assertEquals(Boolean.TRUE, JsonParser.parse("true"));
      assertEquals(Boolean.FALSE, JsonParser.parse("false"));
    }

    @Test
    void parsesNull() {
      assertNull(JsonParser.parse("null"));
    }

    @Test
    void parsesArrayOfMixedTypes() {
      List<?> value = (List<?>) JsonParser.parse("[1, \"two\", true, null]");
      assertEquals(4, value.size());
      assertEquals(0, BigDecimal.ONE.compareTo((BigDecimal) value.get(0)));
      assertEquals("two", value.get(1));
      assertEquals(Boolean.TRUE, value.get(2));
      assertNull(value.get(3));
    }

    @Test
    void parsesNestedObjectInsideArray() {
      List<?> value = (List<?>) JsonParser.parse("[{\"k\":\"v\"}]");
      Map<?, ?> first = (Map<?, ?>) value.getFirst();
      assertEquals("v", first.get("k"));
    }

    @Test
    void parsesStringWithEscapedNewline() {
      Object value = JsonParser.parse("\"line1\\nline2\"");
      assertEquals("line1\nline2", value);
    }

    @Test
    void parsesStringWithUnicodeEscape() {
      Object value = JsonParser.parse("\"\\u00e6\"");
      assertEquals("æ", value);
    }

    @Test
    void skipsWhitespaceAroundValues() {
      Object value = JsonParser.parse("  {  \"k\"  :  1  }  ");
      assertEquals(0, BigDecimal.ONE.compareTo(
          (BigDecimal) ((Map<?, ?>) value).get("k")));
    }
  }

  @Nested
  @DisplayName("Negative JsonParser Tests")
  class negativeJsonParserTests {

    @Test
    void trailingContentThrows() {
      // Trailing-content check is the one path that still throws IllegalArgumentException;
      // every other parse error funnels through err() which produces FileReaderException.
      assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("1 garbage"));
    }

    @Test
    void unterminatedStringThrows() {
      assertThrows(FileReaderException.class, () -> JsonParser.parse("\"unterminated"));
    }

    @Test
    void badEscapeThrows() {
      assertThrows(FileReaderException.class, () -> JsonParser.parse("\"\\q\""));
    }

    @Test
    void unclosedObjectThrows() {
      assertThrows(RuntimeException.class, () -> JsonParser.parse("{\"k\":1"));
    }
  }
}
