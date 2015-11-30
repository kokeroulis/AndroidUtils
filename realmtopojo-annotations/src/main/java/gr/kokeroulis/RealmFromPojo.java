package com.kokeroulis;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

// Target only classes
@Target(value = TYPE)
public @interface RealmFromPojo
{}