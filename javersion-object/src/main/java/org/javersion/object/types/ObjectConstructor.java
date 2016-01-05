/*
 * Copyright 2016 Samppa Saarela
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
package org.javersion.object.types;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import org.javersion.reflect.ConstructorDescriptor;
import org.javersion.reflect.ParameterDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ObjectConstructor {

    public final ConstructorDescriptor constructor;

    public final Set<String> parameters;

    public ObjectConstructor(TypeDescriptor type) {
        this(type.getDefaultConstructor(), ImmutableList.of());
    }

    public ObjectConstructor(ConstructorDescriptor constructor) {
        this(constructor, constructor.getParameters().stream().map(ParameterDescriptor::getName).collect(toList()));
    }

    public ObjectConstructor(ConstructorDescriptor constructor, List<String> parameters) {
        this(constructor, ImmutableSet.copyOf(parameters));
    }

    public ObjectConstructor(ConstructorDescriptor constructor, ImmutableSet<String> parameters) {
        this.constructor = Check.notNull(constructor, "constructor");
        this.parameters = ImmutableSet.copyOf(parameters);
    }

    public Object[] newParametersArray() {
        return new Object[parameters.size()];
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public Set<String> getParameters() {
        return parameters;
    }

    public Object newInstance(Object[] params) {
        return constructor.newInstance(params);
    }

    public boolean hasParameter(String name) {
        return parameters.contains(name);
    }
}
