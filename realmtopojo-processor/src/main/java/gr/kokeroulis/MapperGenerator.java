package com.kokeroulis;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

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

    public TypeSpec generateClass(AnnotatedClass annotatedClass) {

        pojoClassName = annotatedClass.annotatedClassName;
        realmObjectClassName = REALM + annotatedClass.annotatedClassName;

        TypeSpec.Builder builder =  classBuilder(REALM + "MapperToPojo")
            .addModifiers(PUBLIC, FINAL);

        MethodSpec.Builder from = generateFromRealm();

        for (Variable variable : annotatedClass.variables) {
            String name = variable.variableName;
            from.addStatement("j.$N = r.$N()", name, GeneratorUtils.toGetter(name));
        }

        from.addStatement("return j");
        builder = builder.addMethod(from.build());

        return builder.build();
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
