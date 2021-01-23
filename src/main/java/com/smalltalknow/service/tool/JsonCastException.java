package com.smalltalknow.service.tool;

public class JsonCastException extends ClassCastException {
    public JsonCastException(JsonType casted, JsonType actual) {
        super(String.format("Invalid JSON type cast, expected %s, actual %s.", casted, actual));
    }
}
