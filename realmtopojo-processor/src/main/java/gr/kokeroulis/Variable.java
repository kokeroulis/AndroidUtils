package com.kokeroulis;

import javax.lang.model.type.TypeMirror;

public class Variable {
    public final String variableName;
    public final TypeMirror type;

    public Variable(String variableName, TypeMirror type) {
        this.variableName = variableName;
        this.type = type;
    }
}
