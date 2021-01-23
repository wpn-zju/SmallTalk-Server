package com.smalltalknow.service.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"DuplicateBranchesInSwitch", "unused"})
public class JsonObject {
    private static class Index {
        private int value;
        private final int length;

        public Index(int value, int length) {
            this.value = value;
            this.length = length;
        }

        public int get() {
            return value;
        }

        public int saveGet() throws JsonParseException {
            if (value >= length) {
                throw new JsonParseException("Parse Error - Index overflow.");
            } else {
                return value;
            }
        }

        public void increment() throws JsonParseException {
            plus(1);
        }

        public void plus(int addend) throws JsonParseException {
            if (value + addend > length) {
                throw new JsonParseException("Parse Error - Index overflow.");
            } else {
                value += addend;
            }
        }
    }

    private static class JsonParseException extends Exception {
        public JsonParseException() {
            super();
        }

        public JsonParseException(String message) {
            super(message);
        }
    }

    private final JsonType jsonType;
    private final Object object;

    public JsonObject() {
        jsonType = JsonType.Null;
        object = null;
    }

    public JsonObject(boolean value) {
        jsonType = JsonType.Boolean;
        object = value;
    }

    public JsonObject(int value) {
        this(new JsonNumber(String.valueOf(value)));
    }

    public JsonObject(long value) {
        this(new JsonNumber(String.valueOf(value)));
    }

    public JsonObject(float value) {
        this(new JsonNumber(String.valueOf(value)));
    }

    public JsonObject(double value) {
        this(new JsonNumber(String.valueOf(value)));
    }

    public JsonObject(JsonNumber value) {
        jsonType = JsonType.Number;
        object = value;
    }

    public JsonObject(String value) {
        jsonType = JsonType.String;
        object = value;
    }

    public JsonObject(List<JsonObject> list) {
        jsonType = JsonType.Array;
        object = list;
    }

    public JsonObject(Map<String, JsonObject> kvMap) {
        jsonType = JsonType.Object;
        object = kvMap;
    }

    public JsonObject(JsonObject that) {
        this.jsonType = that.jsonType;

        switch (this.jsonType) {
            case Null:
                this.object = null;
                break;
            case Boolean:
                this.object = that.getBool();
                break;
            case Number:
                this.object = new JsonNumber(that.getNumber());
                break;
            case String:
                this.object = that.getString();
                break;
            case Array:
                this.object = new ArrayList<>(that.getList());
                break;
            case Object:
                this.object = new LinkedHashMap<>(that.getObject());
                break;
            default:
                throw new JsonIOException("Copy Error - Invalid JSON type.");
        }
    }

    public static JsonObject create(String input) {
        try {
            Index index = new Index(0, input.length());
            JsonObject result = parseValue(input, index);
            if (index.get() == input.length()) {
                return result;
            } else {
                throw new JsonParseException(buildErrorMessage("Redundant component", input, index.get()));
            }
        } catch (JsonParseException e) {
            throw new JsonIOException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    public StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        appendWithThis(sb);
        return sb;
    }

    public JsonType getJsonType() {
        return jsonType;
    }

    public boolean getBool() {
        if (jsonType != JsonType.Boolean) {
            throw new JsonCastException(JsonType.Boolean, jsonType);
        }

        return (boolean) object;
    }

    public int getInt() {
        return getNumber().intValue();
    }

    public long getLong() {
        return getNumber().longValue();
    }

    public float getFloat() {
        return getNumber().floatValue();
    }

    public double getDouble() {
        return getNumber().doubleValue();
    }

    public JsonNumber getNumber() {
        if (jsonType != JsonType.Number) {
            throw new JsonCastException(JsonType.Number, jsonType);
        }

        return (JsonNumber) object;
    }

    public String getString() {
        if (jsonType != JsonType.String) {
            throw new JsonCastException(JsonType.String, jsonType);
        }

        return (String) object;
    }

    @SuppressWarnings("unchecked")
    public List<JsonObject> getList() {
        if (jsonType != JsonType.Array) {
            throw new JsonCastException(JsonType.Array, jsonType);
        }

        return (List<JsonObject>) object;
    }

    @SuppressWarnings("unchecked")
    public Map<String, JsonObject> getObject() {
        if (jsonType != JsonType.Object) {
            throw new JsonCastException(JsonType.Object, jsonType);
        }

        return (Map<String, JsonObject>) object;
    }

    public boolean containsKey(String key) {
        if (jsonType != JsonType.Object) {
            throw new JsonCastException(JsonType.Object, jsonType);
        }

        return getObject().containsKey(key);
    }

    public JsonObject get(int index) {
        return getList().get(index);
    }

    public JsonObject get(String index) {
        return getObject().get(index);
    }

    public void add(JsonObject value) {
        getList().add(value);
    }

    public void put(String key, JsonObject value) {
        getObject().put(key, value);
    }

    private JsonObject(JsonType jsonType, Object object) {
        this.jsonType = jsonType;
        this.object = object;
    }

    private static JsonObject parseValue(String input, Index index) throws JsonParseException {
        readWhitespace(input, index);
        JsonObject result = parseValueBody(input, index);
        readWhitespace(input, index);
        return result;
    }

    private static void readWhitespace(String input, Index index) throws JsonParseException {
        if (index.get() == input.length()) { return; }
        while (input.charAt(index.saveGet()) == ' ' ||
                input.charAt(index.saveGet()) == '\n' ||
                input.charAt(index.saveGet()) == '\r' ||
                input.charAt(index.saveGet()) == '\t') {
            index.increment();
            if (index.get() == input.length()) { return; }
        }
    }

    private static JsonObject parseValueBody(String input, Index index) throws JsonParseException {
        switch (input.charAt(index.saveGet())) {
            case 'n':
                return new JsonObject(JsonType.Null, parseNull(input, index));
            case 't':
                return new JsonObject(JsonType.Boolean, parseBoolean(input, index));
            case 'f':
                return new JsonObject(JsonType.Boolean, parseBoolean(input, index));
            case '-':
                return new JsonObject(JsonType.Number, parseNumber(input, index));
            case '"':
                return new JsonObject(JsonType.String, parseString(input, index));
            case '[':
                return new JsonObject(JsonType.Array, parseList(input, index));
            case '{':
                return new JsonObject(JsonType.Object, parseObject(input, index));
            default:
                if (input.charAt(index.saveGet()) >= '0' && input.charAt(index.saveGet()) <= '9') {
                    return new JsonObject(JsonType.Number, parseNumber(input, index));
                } else {
                    throw new JsonParseException(buildErrorMessage("Read type error", input, index.get()));
                }
        }
    }

    private static Object parseNull(String input, Index index) throws JsonParseException {
        if (input.charAt(index.saveGet()) != 'n') throw new JsonParseException(
                buildErrorMessage("Invalid null node", input, index.get()));
        index.increment();
        if (input.charAt(index.saveGet()) != 'u') throw new JsonParseException(
                buildErrorMessage("Invalid null node", input, index.get()));
        index.increment();
        if (input.charAt(index.saveGet()) != 'l') throw new JsonParseException(
                buildErrorMessage("Invalid null node", input, index.get()));
        index.increment();
        if (input.charAt(index.saveGet()) != 'l') throw new JsonParseException(
                buildErrorMessage("Invalid null node", input, index.get()));
        index.increment();
        return null;
    }

    private static boolean parseBoolean(String input, Index index) throws JsonParseException {
        boolean result = input.charAt(index.saveGet()) == 't';
        if (result) {
            if (input.charAt(index.saveGet()) != 't') throw new JsonParseException(
                    buildErrorMessage("Invalid true node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'r') throw new JsonParseException(
                    buildErrorMessage("Invalid true node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'u') throw new JsonParseException(
                    buildErrorMessage("Invalid true node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'e') throw new JsonParseException(
                    buildErrorMessage("Invalid true node", input, index.get()));
        } else {
            if (input.charAt(index.saveGet()) != 'f') throw new JsonParseException(
                    buildErrorMessage("Invalid false node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'a') throw new JsonParseException(
                    buildErrorMessage("Invalid false node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'l') throw new JsonParseException(
                    buildErrorMessage("Invalid false node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 's') throw new JsonParseException(
                    buildErrorMessage("Invalid false node", input, index.get()));
            index.increment();
            if (input.charAt(index.saveGet()) != 'e') throw new JsonParseException(
                    buildErrorMessage("Invalid false node", input, index.get()));
        }
        index.increment();
        return result;
    }

    private static JsonNumber parseNumber(String input, Index index) throws JsonParseException {
        int startIndex = index.get();
        int currentIndex = startIndex;

        StringBuilder sb = new StringBuilder();

        // Integer Part
        // Leading zeros are allowed
        if (input.charAt(currentIndex) == '-') {
            sb.append(input.charAt(currentIndex++));
        }

        if (currentIndex < input.length()
                && input.charAt(currentIndex) >= '0'
                && input.charAt(currentIndex) <= '9') {
            while (currentIndex < input.length()
                    && input.charAt(currentIndex) >= '0'
                    && input.charAt(currentIndex) <= '9') {
                sb.append(input.charAt(currentIndex++));
            }
        } else {
            throw new JsonParseException(buildErrorMessage("Incomplete number", input, currentIndex));
        }

        // Fraction Part
        if (currentIndex < input.length()
                && input.charAt(currentIndex) == '.') {
            sb.append(input.charAt(currentIndex++));
            if (currentIndex < input.length()
                    && input.charAt(currentIndex) >= '0'
                    && input.charAt(currentIndex) <= '9') {
                while (currentIndex < input.length()
                        && input.charAt(currentIndex) >= '0'
                        && input.charAt(currentIndex) <= '9') {
                    sb.append(input.charAt(currentIndex++));
                }
            } else {
                throw new JsonParseException(buildErrorMessage("Incomplete fraction", input, currentIndex));
            }
        }

        // Exponent Part
        if (currentIndex < input.length()
                && (input.charAt(currentIndex) == 'e' || input.charAt(currentIndex) == 'E')) {
            sb.append(input.charAt(currentIndex++));
            if (input.charAt(currentIndex) == '+') {
                sb.append(input.charAt(currentIndex++));
            } else if (input.charAt(currentIndex) == '-') {
                sb.append(input.charAt(currentIndex++));
            }
            if (currentIndex < input.length()
                    && input.charAt(currentIndex) >= '0'
                    && input.charAt(currentIndex) <= '9') {
                while (currentIndex < input.length()
                        && input.charAt(currentIndex) >= '0'
                        && input.charAt(currentIndex) <= '9') {
                    sb.append(input.charAt(currentIndex++));
                }
            } else {
                throw new JsonParseException(buildErrorMessage("Incomplete exponent", input, currentIndex));
            }
        }

        index.plus(currentIndex - startIndex);

        return new JsonNumber(sb.toString());
    }

    private static char hexToUChar(char input) throws JsonParseException {
        if (input >= 'A' && input <= 'F') {
            return (char) (input - 'A' + 10);
        } else if (input >= 'a' && input <= 'f') {
            return (char) (input - 'a' + 10);
        } else if (input >= '0' && input <= '9') {
            return (char) (input - '0');
        } else {
            throw new JsonParseException();
        }
    }

    private static String parseString(String input, Index index) throws JsonParseException {
        if (input.charAt(index.saveGet()) != '"') {
            throw new JsonParseException(buildErrorMessage("Invalid string", input, index.get()));
        }
        index.increment();
        StringBuilder sb = new StringBuilder();
        while (input.charAt(index.saveGet()) != '"') {
            if (input.charAt(index.saveGet()) == '\\') {
                index.increment();
                switch (input.charAt(index.saveGet())) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    // Front Slash Rules
                    // - Front slash will NOT be escaped in serialization.
                    // - However, an escaped front slash is acceptable '\/' in deserialization.
                    // - The behavior is the same as most third-party JSON libs like Jackson and Gson.
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u': {
                        try {
                            index.increment();
                            char h1 = hexToUChar(input.charAt(index.saveGet()));
                            index.increment();
                            char h2 = hexToUChar(input.charAt(index.saveGet()));
                            index.increment();
                            char h3 = hexToUChar(input.charAt(index.saveGet()));
                            index.increment();
                            char h4 = hexToUChar(input.charAt(index.saveGet()));
                            sb.append((char) ((h1 << 12) + (h2 << 8) + (h3 << 4) + h4));
                        } catch (JsonParseException e) {
                            throw new JsonParseException(
                                    buildErrorMessage("Invalid Unicode escaped character", input, index.get()));
                        }
                        break;
                    }
                    default:
                        throw new JsonParseException(
                                buildErrorMessage("Invalid escaped character", input, index.get()));
                }
            } else {
                sb.append(input.charAt(index.saveGet()));
            }
            index.increment();
        }
        index.increment();
        return sb.toString();
    }

    private static List<JsonObject> parseList(String input, Index index) throws JsonParseException {
        if (input.charAt(index.saveGet()) != '[') {
            throw new JsonParseException(buildErrorMessage("Invalid list", input, index.get()));
        }
        index.increment();
        List<JsonObject> list = new ArrayList<>();
        readWhitespace(input, index);
        if (input.charAt(index.saveGet()) != ']') {
            while (true) {
                list.add(parseValue(input, index));
                if (input.charAt(index.saveGet()) == ']') {
                    break;
                } else if (input.charAt(index.saveGet()) == ',') {
                    index.increment();
                } else {
                    throw new JsonParseException(
                            buildErrorMessage("Missing comma in list", input, index.get()));
                }
            }
        }
        index.increment();
        return list;
    }

    private static Map<String, JsonObject> parseObject(String input, Index index) throws JsonParseException {
        if (input.charAt(index.saveGet()) != '{') {
            throw new JsonParseException(buildErrorMessage("Invalid object", input, index.get()));
        }
        index.increment();
        Map<String, JsonObject> map = new LinkedHashMap<>();
        readWhitespace(input, index);
        if (input.charAt(index.saveGet()) != '}') {
            while (true) {
                readWhitespace(input, index);
                String key = parseString(input, index);
                readWhitespace(input, index);
                if (input.charAt(index.saveGet()) != ':') {
                    throw new JsonParseException(
                            buildErrorMessage("Missing colon in object", input, index.get()));
                }
                index.increment();
                map.put(key, parseValue(input, index));
                if (input.charAt(index.saveGet()) == '}') {
                    break;
                } else if (input.charAt(index.saveGet()) == ',') {
                    index.increment();
                } else {
                    throw new JsonParseException(
                            buildErrorMessage("Missing comma in object", input, index.get()));
                }
            }
        }
        index.increment();
        return map;
    }

    private static String buildErrorMessage(String errorType, String source, int position)
    {
        int nearStart = position >= 3 ? position - 3 : 0;
        int nearEnd = position + 3 < source.length() ? position + 3 : source.length() - 1;
        return String.format("Parse Error - %s at position %d near \"%s\", source = \"%s\".",
                errorType, position, source.substring(nearStart, nearEnd + 1), source);
    }

    private static void appendStringEscaped(StringBuilder sb, String input) {
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
    }

    private void appendWithThis(StringBuilder sb) {
        switch (jsonType) {
            case Null:
                sb.append("null");
                break;
            case Boolean:
                sb.append(getBool());
                break;
            case Number:
                sb.append(getNumber());
                break;
            case String:
                sb.append('"');
                appendStringEscaped(sb, getString());
                sb.append('"');
                break;
            case Array:
                sb.append('[');
                getList().forEach(e -> {
                    e.appendWithThis(sb);
                    sb.append(',');
                });
                if (getList().size() > 0) sb.setLength(sb.length() - 1);
                sb.append(']');
                break;
            case Object:
                sb.append('{');
                getObject().forEach((key, value) -> {
                    sb.append('"');
                    appendStringEscaped(sb, key);
                    sb.append('"').append(':');
                    value.appendWithThis(sb);
                    sb.append(',');
                });
                if (getObject().size() > 0) sb.setLength(sb.length() - 1);
                sb.append('}');
                break;
        }
    }
}
