/*
 * Copyright (C) 2015 Antonis Tsiapaliokas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.kokeroulis;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;

final class RealmObjectGenerator {

    public static final String REALM = "Realm";
    private final String packageName;
    private TypeSpec.Builder builder;
    private TypeName type;

    public RealmObjectGenerator(String packageName) {
        this.packageName = packageName;
    }

    public TypeSpec generateClass(AnnotatedClass annotatedClass) {
        ClassName realmObjectClass = get("io.realm", "RealmObject");
        builder =  classBuilder(REALM + annotatedClass.annotatedClassName)
                .superclass(realmObjectClass)
                .addModifiers(PUBLIC);
        for (Variable variable : annotatedClass.variables) {
            type = GeneratorUtils.getType(get(variable.type), packageName);
            /*if (variable.type.toString().equals("java.lang.String") || get(variable.type).isPrimitive()) {
                type = get(variable.type);

            } else {
                int packageSize = packageName.length() + 1;
                int classSize = type.toString().length();
                String name = type.toString().substring(packageSize, classSize);
                type = get(packageName, REALM + GeneratorUtils.upperFirstLater(name));
            }*/
            builder = addVariable(variable.variableName, variable.type);
        }

        return builder.build();
    }


    private TypeSpec.Builder addVariable(String variableName, TypeMirror variableType) {
        builder = addGetter(variableName, variableType);
        builder = addSetter(variableName, variableType);
        return builder.addField(type,
                                                       variableName, Modifier.PRIVATE);
    }

    private TypeSpec.Builder addGetter(String variableName, TypeMirror variableType) {
        return builder.addMethod(
            MethodSpec.methodBuilder(GeneratorUtils.toGetter(variableName))
            .addModifiers(Modifier.PUBLIC)
            .returns(type)
            .addStatement("return $N", variableName)
            .build()
        );
    }

    private TypeSpec.Builder addSetter(String variableName, TypeMirror variableType) {
        return builder.addMethod(
            MethodSpec.methodBuilder(GeneratorUtils.toSetter(variableName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, variableName)
                .addStatement("this.$N = $N", variableName, variableName)
                .build()
        );
    }
}
