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

import com.squareup.javapoet.TypeName;

import static com.squareup.javapoet.ClassName.get;

public class GeneratorUtils {
    public static TypeName getType(TypeName type, String packageName) {
        if (isPojo(type)) {
            return type;
        } else {
            int packageSize = packageName.length() + 1;
            int classSize = type.toString().length();
            String name = type.toString().substring(packageSize, classSize);
            type = get(packageName, "Realm" + GeneratorUtils.upperFirstLater(name));
            return type;
        }
    }

    public static String toGetter(String fieldName) {
        return "get" + upperFirstLater(fieldName);
    }

    public static String toSetter(String fieldName) {
        return "set" + upperFirstLater(fieldName);
    }

    public static String upperFirstLater(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static boolean isPojo(TypeName type) {
        return type.toString().equals("java.lang.String") || type.isPrimitive();
    }
}
