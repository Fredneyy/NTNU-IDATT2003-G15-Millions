package ntnu.idatt2003.group15.utilities;

import ntnu.idatt2003.group15.model.exceptions.FileReaderException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny dependency-free JSON parser.
 *
 * <p>Returns an Object tree where objects become {@link LinkedHashMap}, arrays
 * become {@link ArrayList}, numbers become {@link BigDecimal}, strings/booleans
 * become their natural Java types, and JSON {@code null} becomes Java {@code null}.
 * Good enough for reading the save files written by {@link SaveGameUtil}.
 */
public final class JsonParser {

  private final String src;
  private int pos;

  private JsonParser(String src) {
    this.src = src;
  }

  public static Object parse(String src) {
    JsonParser p = new JsonParser(src);
    p.skipWs();
    Object value = p.readValue();
    p.skipWs();
    if (p.pos != src.length()) {
      throw new IllegalArgumentException(
          "The JSON file has extra content after the end of the document (at position " + p.pos
              + "). It may be corrupted or contain more than one top-level value.");
    }
    return value;
  }

  private Object readValue() {
    skipWs();
    if (pos >= src.length()) throw err("The JSON file ended before a value was found. It looks incomplete or truncated");
    char c = src.charAt(pos);
    return switch (c) {
      case '{' -> readObject();
      case '[' -> readArray();
      case '"' -> readString();
      case 't', 'f' -> readBoolean();
      case 'n' -> readNull();
      default -> readNumber();
    };
  }

  private Map<String, Object> readObject() {
    expect('{');
    Map<String, Object> out = new LinkedHashMap<>();
    skipWs();
    if (peek() == '}') { pos++; return out; }
    while (true) {
      skipWs();
      String key = readString();
      skipWs();
      expect(':');
      Object value = readValue();
      out.put(key, value);
      skipWs();
      char c = src.charAt(pos++);
      if (c == ',') continue;
      if (c == '}') return out;
      throw err("The JSON object is malformed. Expected a ',' or '}' here, but found '" + c + "'");
    }
  }

  private List<Object> readArray() {
    expect('[');
    List<Object> out = new ArrayList<>();
    skipWs();
    if (peek() == ']') { pos++; return out; }
    while (true) {
      out.add(readValue());
      skipWs();
      char c = src.charAt(pos++);
      if (c == ',') continue;
      if (c == ']') return out;
      throw err("The JSON array is malformed. Expected a ',' or ']' here, but found '" + c + "'");
    }
  }

  private String readString() {
    expect('"');
    StringBuilder sb = new StringBuilder();
    while (pos < src.length()) {
      char c = src.charAt(pos++);
      if (c == '"') return sb.toString();
      if (c == '\\') {
        if (pos >= src.length()) throw err("The JSON string ends with a stray backslash. The escape sequence is incomplete");
        char esc = src.charAt(pos++);
        switch (esc) {
          case '"', '\\', '/' -> sb.append(esc);
          case 'b' -> sb.append('\b');
          case 'f' -> sb.append('\f');
          case 'n' -> sb.append('\n');
          case 'r' -> sb.append('\r');
          case 't' -> sb.append('\t');
          case 'u' -> {
            if (pos + 4 > src.length()) throw err("The JSON has an incomplete unicode escape. \\u must be followed by exactly 4 hex digits");
            sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
            pos += 4;
          }
          default -> throw err("The JSON string contains an unknown escape sequence '\\" + esc + "'");
        }
      } else {
        sb.append(c);
      }
    }
    throw err("A JSON string is missing its closing quote. The file may be truncated");
  }

  private Boolean readBoolean() {
    if (src.startsWith("true", pos))  { pos += 4; return Boolean.TRUE; }
    if (src.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
    throw err("The JSON is malformed. Expected 'true' or 'false' here");
  }

  private Object readNull() {
    if (src.startsWith("null", pos)) { pos += 4; return null; }
    throw err("The JSON is malformed. Expected 'null' here");
  }

  private BigDecimal readNumber() {
    int start = pos;
    if (peek() == '-') pos++;
    while (pos < src.length() && "0123456789.eE+-".indexOf(src.charAt(pos)) >= 0) pos++;
    if (start == pos) throw err("The JSON is malformed. Expected a number here");
    return new BigDecimal(src.substring(start, pos));
  }

  private void skipWs() {
    while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
  }

  private void expect(char c) {
    if (pos >= src.length() || src.charAt(pos) != c) {
      throw err("The JSON is malformed. Expected '" + c + "' here");
    }
    pos++;
  }

  private char peek() {
    return pos < src.length() ? src.charAt(pos) : '\0';
  }

  private FileReaderException err(String msg) {
    return new FileReaderException(msg + " at position " + pos);
  }
}
