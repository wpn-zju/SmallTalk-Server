package com.smalltalknow.service.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"DuplicateBranchesInSwitch", "unused"})
public class JsonObject {
    static class Index {
        private int value;
        private int length;

        public Index(int value, int length) {
            this.value = value;
            this.length = length;
        }

        public int intValue() {
            return value;
        }

        public void increment() {
            plus(1);
        }

        public void plus(int addend) {
            if (value + addend > length) {
                throw new JsonIOException();
            } else {
                value = value + addend;
            }
        }
    }

    private JsonType jsonType;
    private Object object;

    public JsonObject() {
        jsonType = JsonType.Null;
        object = null;
    }

    public JsonObject(boolean value) {
        jsonType = JsonType.Boolean;
        object = value;
    }

    public JsonObject(int value) {
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

    public JsonObject(JsonType jsonType, Object object) {
        this.jsonType = jsonType;
        this.object = object;
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
                this.object = that.getInt();
                break;
            case String:
                this.object = that.getString();
                break;
            case Array:
                this.object = new ArrayList<>(that.getList());
                break;
            case Object:
                this.object = new HashMap<>(that.getObject());
                break;
            case Invalid:
                this.object = null;
                break;
            default:
                this.object = null;
                break;
        }
    }

    public static JsonObject create(String input) {
        Index index = new Index(0, input.length());

        switch (readType(input, index)) {
            case Null:
                return new JsonObject();
            case Boolean:
                return new JsonObject(JsonType.Boolean, booleanParser(input, index));
            case Number:
                return new JsonObject(JsonType.Number, numberParser(input, index));
            case String:
                return new JsonObject(JsonType.String, stringParser(input, index));
            case Array:
                return new JsonObject(JsonType.Array, listParser(input, index));
            case Object:
                return new JsonObject(JsonType.Object, objectParser(input, index));
            case Invalid:
                return new JsonObject();
            default:
                return new JsonObject();
        }
    }

    private static JsonType readType(String input, Index index) {
        while (input.charAt(index.intValue()) == ' ' ||
                input.charAt(index.intValue()) == '\r' ||
                input.charAt(index.intValue()) == '\n' ||
                input.charAt(index.intValue()) == '\t') index.increment();

        switch (input.charAt(index.intValue())) {
            case '"':
                return JsonType.String;
            case 't':
                return JsonType.Boolean;
            case 'f':
                return JsonType.Boolean;
            case '[':
                return JsonType.Array;
            case '{':
                return JsonType.Object;
            case 'n':
                return JsonType.Null;
            case '-':
                return JsonType.Number;
            default:
                return input.charAt(index.intValue()) >= '0' && input.charAt(index.intValue()) <= '9' ? JsonType.Number : JsonType.Invalid;
        }    
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (jsonType) {
            case Invalid:
                sb.append("null");
                break;
            case Null:
                sb.append("null");
                break;
            case Boolean:
                sb.append(getBool());
                break;
            case Number:
                sb.append(getInt());
                break;
            case String:
                sb.append(String.format("\"%s\"", getString()));
                break;
            case Array:
                sb.append('[');
                getList().forEach(e -> sb.append(String.format("%s,", e.toString())));
                if (getList().size() > 0) sb.setLength(sb.length() - 1);
                sb.append(']');
                break;
            case Object:
                sb.append('{');
                getObject().forEach((key, value) -> sb.append(String.format("\"%s\":%s,", key, value.toString())));
                if (getObject().size() > 0) sb.setLength(sb.length() - 1);
                sb.append('}');
                break;
        }

        return sb.toString();
    }

    public JsonType getJSONType() {
        return jsonType;
    }

    public boolean getBool() {
        if (jsonType != JsonType.Boolean) {
            throw new ClassCastException();
        }

        return (boolean) object;
    }

    public int getInt() {
        if (jsonType != JsonType.Number) {
            throw new ClassCastException();
        }

        return (int) object;
    }

    public String getString() {
        if (jsonType != JsonType.String) {
            throw new ClassCastException();
        }

        return (String) object;
    }


    @SuppressWarnings("unchecked")
    public List<JsonObject> getList() {
        if (jsonType != JsonType.Array) {
            throw new ClassCastException();
        }

        return (List<JsonObject>) object;
    }

    public boolean containsKey(String key) {
        if (jsonType != JsonType.Object) {
            throw new ClassCastException();
        }

        return getObject().containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, JsonObject> getObject() {
        if (jsonType != JsonType.Object) {
            throw new ClassCastException();
        }

        return (Map<String, JsonObject>) object;
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

    private static Object nullParser(String input, Index index) {
        index.plus("null".length());

        return null;
    }

    private static int numberParser(String input, Index index) {
        boolean neg = input.charAt(index.intValue()) == '-';

        if (neg) { index.increment(); }

        int ret = 0;

        while (input.charAt(index.intValue()) <= '9' && input.charAt(index.intValue()) >= '0') {
            ret *= 10;
            ret += input.charAt(index.intValue()) - '0';
            index.increment();
        }

        if (neg) { ret = -ret; }

        return ret;
    }

    private static boolean booleanParser(String input, Index index) {
        boolean ret = input.charAt(index.intValue()) == 't';

        index.plus(ret ? "true".length() : "false".length());

        return ret;
    }

    private static String stringParser(String input, Index index) {
        StringBuilder ret = new StringBuilder();

        index.increment();

        while (input.charAt(index.intValue()) != '"') {
            // Escape Characters
            if (input.charAt(index.intValue()) == '\\') {
                ret.append('\\');
                index.increment();
            }
            ret.append(input.charAt(index.intValue()));
            index.increment();
        }

        index.increment();

        return ret.toString();
    }

    private static List<JsonObject> listParser(String input, Index index) {
        List<JsonObject> list = new ArrayList<>();

        index.increment();

        while (input.charAt(index.intValue()) != ']') {
            switch (input.charAt(index.intValue())) {
                case ' ':
                    index.increment();
                    break;
                case '\r':
                    index.increment();
                    break;
                case '\n':
                    index.increment();
                    break;
                case '\t':
                    index.increment();
                    break;
                case ',':
                    index.increment();
                    break;
                case '[':
                    list.add(new JsonObject(JsonType.Array, listParser(input, index)));
                    break;
                case '{':
                    list.add(new JsonObject(JsonType.Object, objectParser(input, index)));
                    break;
                case 't':
                    list.add(new JsonObject(JsonType.Boolean, booleanParser(input, index)));
                    break;
                case 'f':
                    list.add(new JsonObject(JsonType.Boolean, booleanParser(input, index)));
                    break;
                case '"':
                    list.add(new JsonObject(JsonType.String, stringParser(input, index)));
                    break;
                case 'n':
                    list.add(new JsonObject(JsonType.Null, nullParser(input, index)));
                    break;
                case '-':
                    list.add(new JsonObject(JsonType.Number, numberParser(input, index)));
                    break;
                default:
                    list.add(new JsonObject(JsonType.Number, numberParser(input, index)));
                    break;
            }
        }

        index.increment();

        return list;
    }

    private static Map<String, JsonObject> objectParser(String input, Index index) {
        Map<String, JsonObject> map = new HashMap<>();

        index.increment();

        int flag = 0;
        String column = "";
        while (input.charAt(index.intValue()) != '}') {
            if (flag == 1) {
                switch (input.charAt(index.intValue())) {
                    case ' ':
                        index.increment();
                        break;
                    case '\r':
                        index.increment();
                        break;
                    case '\n':
                        index.increment();
                        break;
                    case '\t':
                        index.increment();
                        break;
                    case ',':
                        index.increment();
                        break;
                    case ':':
                        index.increment();
                        break;
                    case '[':
                        map.put(column, new JsonObject(JsonType.Array, listParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case '{':
                        map.put(column, new JsonObject(JsonType.Object, objectParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case 't':
                        map.put(column, new JsonObject(JsonType.Boolean, booleanParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case 'f':
                        map.put(column, new JsonObject(JsonType.Boolean, booleanParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case '"':
                        map.put(column, new JsonObject(JsonType.String, stringParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case 'n':
                        map.put(column, new JsonObject(JsonType.Null, nullParser(input, index)));
                        flag = 0; column = "";
                        break;
                    case '-':
                        map.put(column, new JsonObject(JsonType.Number, numberParser(input, index)));
                        flag = 0; column = "";
                        break;
                    default:
                        map.put(column, new JsonObject(JsonType.Number, numberParser(input, index)));
                        flag = 0; column = "";
                        break;
                }
            }
            else {
                if (input.charAt(index.intValue()) == '"') {
                    column = stringParser(input, index);
                    flag = 1;
                }
                else {
                    index.increment();
                }
            }
        }

        index.increment();

        return map;
    }
}
