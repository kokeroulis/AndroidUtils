package com.kokeroulis;

import java.util.List;

import javax.lang.model.element.TypeElement;

class AnnotatedClass {
    public final String annotatedClassName;
    public final List<Variable> variables;
    public final TypeElement typeElement;

    public AnnotatedClass(TypeElement typeElement, List<Variable> variables) {
        this.annotatedClassName = typeElement.getSimpleName().toString();
        this.variables = variables;
        this.typeElement = typeElement;
    }
}
