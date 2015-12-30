/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.object;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used on a Java Bean property getter method (isX/getX), marks it to be versioned.
 *
 * Used on a field, allows redefining property name used in versioning.
 */
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@Documented
public @interface VersionProperty {
    /**
     * Property name used in versioning, defaults to field/property name.
     * Name must conform to Java identifier syntax.
     */
    String value() default "";
}
