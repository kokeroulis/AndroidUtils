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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class MapperGenerator {
    /*
     * Reference for how the mapper works!
     * public final class RealmMapperToPojo {
     *  public static Person fromRealm(RealmPerson r) {
     *      Person j = new Person();
     *      j.id = r.getId();
     *      j.name = r.getName();
     *      j.address = r.getAddress();
     *      j.emailObject = RealmMapperToPojo.fromRealm(r.getEmailObject());
     *      List<Email> customEmailList = new ArrayList<Email>();
     *      for (int i=0; i < r.getCustomEmail().size(); i++) {
     *          customEmailList.add(RealmMapperToPojo.fromRealm(r.getCustomEmail().get(i)));
     *      }
     *      j.customEmail = customEmailList;
     *      return j;
     *   }
     *
     *  public static Email fromRealm(RealmEmail r) {
     *      Email j = new Email();
     *      j.emailAddress = r.getEmailAddress();
     *      j.title = r.getTitle();
     *      j.subject = r.getSubject();
     *      return j;
     *   }
     * }
     */

    private final String REALM = "Realm";
    private final String FROM_REALM = "fromRealm";
    private final String TO_REALM = "toRealm";
    private String pojoClassName;
    private String realmObjectClassName;
    private String packageName;
    private TypeSpec.Builder builder;

    public MapperGenerator(String packageName) {
        this.packageName = packageName;
    }

    public TypeSpec generateClass(List<AnnotatedClass> annos) {
        builder = classBuilder(REALM + "MapperToPojo")
                  .addModifiers(PUBLIC, FINAL);
        for (AnnotatedClass annotatedClass : annos) {
           generateMapper(annotatedClass);
        }

        return builder.build();
    }

    private void generateMapper(AnnotatedClass annotatedClass) {
        pojoClassName = annotatedClass.annotatedClassName;
        realmObjectClassName = REALM + annotatedClass.annotatedClassName;

        MethodSpec.Builder from = generateFromRealm();
        generateEqualFieldsFrom(from, annotatedClass);

        MethodSpec.Builder to = generateToRealm();
        generateEqualFieldsTo(to, annotatedClass);

        from.addStatement("return j");
        to.addStatement("return r");
        builder.addMethod(from.build());
        builder.addMethod(to.build());
    }

    private MethodSpec.Builder generateEqualFieldsFrom(MethodSpec.Builder from, AnnotatedClass annotatedClass) {
        for (Variable variable : annotatedClass.variables) {
            String name = variable.variableName;
            if (GeneratorUtils.isPojo(get(variable.type))) {
                from.addStatement("j.$N = r.$N()", name, GeneratorUtils.toGetter(name));
            } else if (!GeneratorUtils.isList(variable.type)){
                from.addStatement("j.$N = RealmMapperToPojo.fromRealm(r.$N())", name, GeneratorUtils.toGetter(name));
            } else {
                from = generateEqualListFrom(from, variable);
            }
        }

        return from;
    }

    private MethodSpec.Builder generateEqualListFrom(MethodSpec.Builder from, Variable variable) {
        // Classes and object types
        TypeName pojoType = GeneratorUtils.getListParameterizedTypeName(variable.type);
        TypeName listType = ParameterizedTypeName.get(get(List.class), pojoType);
        TypeName arrayListType = ParameterizedTypeName.get(get(ArrayList.class), pojoType);

        // Strings for generation
        final String pojoListName = variable.variableName + "List";
        final String realmObjectName = GeneratorUtils.toGetter(variable.variableName);
        final String listEqualStatement = "$N.add(RealmMapperToPojo.fromRealm(r.$N().get(i)))";
        final String forLoop = "for (int i=0; i < r.$N().size(); i++)";
        final String saveList = "j.$N = $N";

        // Generate
        from.addStatement("$T $N = new $T()", listType, pojoListName, arrayListType);
        from.beginControlFlow(forLoop, realmObjectName);
        from.addStatement(listEqualStatement, pojoListName, realmObjectName);
        from.endControlFlow();
        from.addStatement(saveList, variable.variableName, pojoListName);
        return from;
    }

    private MethodSpec.Builder generateFromRealm() {
        ClassName pojoType = get(packageName, pojoClassName);
        ClassName realmType = get(packageName, realmObjectClassName);
        return MethodSpec.methodBuilder(FROM_REALM)
            .addModifiers(PUBLIC, STATIC)
            .addParameter(realmType, "r")
            .addStatement("$T j = new $T()", pojoType, pojoType)
            .returns(pojoType);
    }

    private MethodSpec.Builder generateToRealm() {
        ClassName pojoType = get(packageName, pojoClassName);
        ClassName realmType = get(packageName, realmObjectClassName);
        return MethodSpec.methodBuilder(TO_REALM)
            .addModifiers(PUBLIC, STATIC)
            .addParameter(pojoType, "j")
            .addStatement("$T r = new $T()", realmType, realmType)
            .returns(realmType);
    }

    private MethodSpec.Builder generateEqualFieldsTo(MethodSpec.Builder to, AnnotatedClass annotatedClass) {
        for (Variable variable : annotatedClass.variables) {
            String name = variable.variableName;
            if (GeneratorUtils.isPojo(get(variable.type))) {
                to.addStatement("r.$N(j.$N)", GeneratorUtils.toSetter(name), name);
            } else if (!GeneratorUtils.isList(variable.type)){
                to.addStatement("j.$N = RealmMapperToPojo.fromRealm(r.$N())", name, GeneratorUtils.toGetter(name));
            } else {
                to = generateEqualListTo(to, variable);
            }
        }

        return to;
    }

    private MethodSpec.Builder generateEqualListTo(MethodSpec.Builder to, Variable variable) {
        // Classes and object types
        TypeName pojoType = GeneratorUtils.getListParameterizedTypeName(variable.type);
        TypeName realmType = GeneratorUtils.getType(pojoType, packageName);
        ClassName realmListClass = get("io.realm", "RealmList");
        TypeName realmListType = ParameterizedTypeName.get(realmListClass, realmType);

        // Strings for generation
        final String pojoListName = variable.variableName;
        final String listEqualStatement = "$N.add(RealmMapperToPojo.toRealm(j.$N.get(i)))";
        final String forLoop = "for (int i=0; i < j.$N.size(); i++)";
        final String saveList = "r.$N($N)";

        // Generate
        to.addStatement("$T $N = new $T()", realmListType, pojoListName, realmListType);
        to.beginControlFlow(forLoop, pojoListName);
        to.addStatement(listEqualStatement, pojoListName, pojoListName);
        to.endControlFlow();
        to.addStatement(saveList, GeneratorUtils.toSetter(variable.variableName), pojoListName);
        return to;
    }
}
