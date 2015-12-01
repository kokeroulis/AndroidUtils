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
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class MapperGenerator {
    /*
     * Reference for how the mapper works!
     * RealmMapperToPojo.fromRealm(RealmPeople r) {
     *
     *  People p = new People();
     *  //for each variable!!
     *  p.name = r.getName();
     * }
     */

    private final String REALM = "Realm";
    private final String FROM_REALM = "fromRealm";
    private String pojoClassName;
    private String realmObjectClassName;
    private String packageName;

    public MapperGenerator(String packageName) {
        this.packageName = packageName;
    }

    public TypeSpec generateClass(List<AnnotatedClass> annos) {
        TypeSpec.Builder builder = null;
        for (AnnotatedClass annotatedClass : annos) {
            builder = generateMapper(annotatedClass);
        }

        return builder.build();
    }

    private TypeSpec.Builder generateMapper(AnnotatedClass annotatedClass) {
        pojoClassName = annotatedClass.annotatedClassName;
        realmObjectClassName = REALM + annotatedClass.annotatedClassName;

        TypeSpec.Builder builder =  classBuilder(REALM + "MapperToPojo")
            .addModifiers(PUBLIC, FINAL);

        MethodSpec.Builder from = generateFromRealm();

        for (Variable variable : annotatedClass.variables) {
            String name = variable.variableName;
            if (GeneratorUtils.isPojo(get(variable.type))) {
                from.addStatement("j.$N = r.$N()", name, GeneratorUtils.toGetter(name));
            } else {
                from.addStatement("j.$N = RealmMapperToPojo.fromRealm(r.$N())", name, GeneratorUtils.toGetter(name));
            }
        }

        from.addStatement("return j");
        builder = builder.addMethod(from.build());

        return builder;
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
}
