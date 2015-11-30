package com.kokeroulis;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public class GeneratorUtils {
    public static TypeName getType(TypeMirror type) {
        return TypeName.get(type);
    }

    public static String toGetter(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static String toSetter(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
