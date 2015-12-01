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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.JavaFile.builder;
import static gr.kokeroulis.Utils.getPackageName;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class RealmFromPojoProcessor extends AbstractProcessor {

    private static final String ANNOTATION = "@" + RealmFromPojo.class.getSimpleName();

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(RealmFromPojo.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ArrayList<AnnotatedClass> annotatedClasses = new ArrayList<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RealmFromPojo.class)) {
            // Our annotation is defined with @Target(value=TYPE). Therefore, we can assume that
            // this annotatedElement is a TypeElement.
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            if (!isValidClass(annotatedClass)) {
                return true;
            }


            try {
                annotatedClasses.add(buildAnnotatedClass(annotatedClass));
            } catch (NoPackageNameException | IOException e) {
                String message = String.format("Couldn't process class %s: %s", annotatedClass,
                        e.getMessage());
                messager.printMessage(ERROR, message, annotatedElement);
            }
        }
        try {
            generate(annotatedClasses);
        } catch (Exception e) {
            messager.printMessage(ERROR, "Couldn't generate class: " + e.getMessage());
        }
        return true;
    }

    private boolean isValidClass(TypeElement annotatedClass) {

        if (!ClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (ClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        return true;
    }

    private AnnotatedClass buildAnnotatedClass(TypeElement annotatedClass)
            throws NoPackageNameException, IOException {
        List<Variable> variables = new ArrayList<>();
        for (Element element : annotatedClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement)) {
                continue;
            }

            VariableElement variableElement = (VariableElement) element;
            String variableName = variableElement.getSimpleName().toString();
            TypeMirror type = variableElement.asType();
            variables.add(new Variable(variableName, type));
        }
        return new AnnotatedClass(annotatedClass , variables);
    }

    private void generate(List<AnnotatedClass> annos) throws Exception {
        if (annos.size() == 0) {
            return;
        }
        String packageName = getPackageName(processingEnv.getElementUtils(),
                annos.get(0).typeElement);

        for (AnnotatedClass annotatedClass : annos) {
            // Generate Realm object
            RealmObjectGenerator realmObject = new RealmObjectGenerator(packageName);
            TypeSpec generatedClass = realmObject.generateClass(annotatedClass);
            JavaFile javaFile = builder(packageName, generatedClass).build();
            javaFile.writeTo(processingEnv.getFiler());
        }

        // generate RealmMapperToPojo
        MapperGenerator mapper = new MapperGenerator(packageName);
        TypeSpec mapperClass = mapper.generateClass(annos);
        JavaFile mapperFile = builder(packageName, mapperClass).build();
        mapperFile.writeTo(processingEnv.getFiler());
    }
}

