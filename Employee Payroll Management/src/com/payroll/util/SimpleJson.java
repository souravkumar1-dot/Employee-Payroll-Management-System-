package com.payroll.util;

import java.util.*;

/**
 * Lightweight JSON parser and formatter with zero external dependencies.
 */
public class SimpleJson {

    // --- Serialization Helpers ---

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Object[]) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    // --- Parsing Helpers ---

    private final String src;
    private int pos = 0;

    public SimpleJson(String src) {
        this.src = src != null ? src.trim() : "";
    }

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        return new SimpleJson(json).parseValue();
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"' || c == '\'') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        throw new RuntimeException("Unexpected character at " + pos + ": " + c);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // skip '{'
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == '}') {
            pos++;
            return map;
        }
        while (pos < src.length()) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != ':') {
                throw new RuntimeException("Expected ':' at position " + pos);
            }
            pos++; // skip ':'
            Object val = parseValue();
            map.put(key, val);
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') {
                pos++;
                continue;
            } else if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                break;
            } else {
                throw new RuntimeException("Expected ',' or '}' at position " + pos);
            }
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        pos++; // skip '['
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == ']') {
            pos++;
            return list;
        }
        while (pos < src.length()) {
            Object val = parseValue();
            list.add(val);
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') {
                pos++;
                continue;
            } else if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                break;
            } else {
                throw new RuntimeException("Expected ',' or ']' at position " + pos);
            }
        }
        return list;
    }

    private String parseString() {
        skipWhitespace();
        char quote = src.charAt(pos);
        pos++; // skip quote
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == quote) {
                return sb.toString();
            }
            if (c == '\\') {
                if (pos >= src.length()) break;
                char next = src.charAt(pos++);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\'': sb.append('\''); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 <= src.length()) {
                            String hex = src.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        break;
                    default:
                        sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Boolean parseBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        } else if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new RuntimeException("Invalid boolean at position " + pos);
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid null at position " + pos);
    }

    private Number parseNumber() {
        int start = pos;
        if (src.charAt(pos) == '-') pos++;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
            if ((src.charAt(pos) == '+' || src.charAt(pos) == '-') && src.charAt(pos - 1) != 'e' && src.charAt(pos - 1) != 'E') {
                break;
            }
            pos++;
        }
        String numStr = src.substring(start, pos);
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.parseDouble(numStr);
        } else {
            long l = Long.parseLong(numStr);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return l;
        }
    }
}
