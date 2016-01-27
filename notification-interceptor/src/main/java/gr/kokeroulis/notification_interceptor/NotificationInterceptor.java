/*
 * Copyright (C) 2016 Antonis Tsiapaliokas
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
package gr.kokeroulis.notification_interceptor;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NotificationInterceptor {
    private NotificationInterceptor() {
        throw new RuntimeException("No ops here!");
    }

    private static final Map<Class<?>, NotificationReceiver> sCached = new LinkedHashMap<>();

    public static void bind(Fragment fragment) {
        bind(fragment.getActivity());
    }

    public static void bind(Activity target) {
        bind((Context) target);
    }

    public static void bind(Context target) {
        Class<?> targetClass = target.getClass();

        NotificationReceiver receiver = sCached.get(targetClass);
        if (receiver != null) {
            target.registerReceiver(receiver.receiver, filterFromString(receiver.category));
        } else {
            NotificationReceiver receiverForActivity = generateReceiverFromClass(target);
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(target);
            manager.registerReceiver(receiverForActivity.receiver,
                filterFromString(receiverForActivity.category));
            sCached.put(targetClass, receiverForActivity);
        }
    }

    public static void unBind(Fragment fragment) {
        bind(fragment.getActivity());
    }

    public static void unBind(Activity target) {
        bind((Context) target);
    }

    public static void unBind(Context target) {
        Class<?> targetClass = target.getClass();

        NotificationReceiver receiver = sCached.get(targetClass);
        if (receiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(target);
            manager.unregisterReceiver(receiver.receiver);
        }
    }

    private static NotificationReceiver
    generateReceiverFromClass(final Context target) {
        final Class<?> targetClass = target.getClass();
        for (Method method : targetClass.getDeclaredMethods()) {
            if (isAnnotated(method, NotificationIntercept.class) && methodIsValid(method)) {
                NotificationIntercept intercept = method.getAnnotation(NotificationIntercept.class);
                final String category = intercept.category();
                final BroadcastReceiver broadcastReceiver = generateReceiver(target, method);
                return new NotificationReceiver(broadcastReceiver, category);
            }
        }

        throw new RuntimeException("Unable to find annotated method"
                + " for class " + targetClass.getSimpleName());
    }

    private static BroadcastReceiver generateReceiver(final Context target, final Method m) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    m.invoke(target, context, intent);
                    abortBroadcast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static boolean methodIsValid(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException(method.getName() + " must not be static");
        } else if (!Modifier.isPublic(method.getModifiers())) {
            throw new RuntimeException(method.getName() + " must be either public");
        } else if (method.getParameterTypes().length > 2
            || !methodParametersAreValid(method.getParameterTypes())) {
            throw new RuntimeException(method.getName()
                + " must have (Context context, Intent intent) as parameters "
                + method.getParameterTypes().length);
        }

        return true;
    }

    private static boolean methodParametersAreValid(Class<?>[] paremeters) {
        final String intentClassName = Intent.class.getCanonicalName();
        final String contextClassName = Context.class.getCanonicalName();
        if (!paremeters[0].getCanonicalName().equals(contextClassName)) {
            throw new RuntimeException(paremeters[0].getName() + " is not a context");
        } else if (!paremeters[1].getCanonicalName().equals(intentClassName)) {
            throw new RuntimeException(paremeters[1].getName() + " is not an intent");
        }

        return true;
    }

    private static boolean isAnnotated(Method method, Class<? extends Annotation> annotation) {
        Annotation a = method.getAnnotation(annotation);
        return method.getAnnotation(annotation) != null;
    }

    private static IntentFilter filterFromString(final String category) {
        return new IntentFilter(category);
    }
}
