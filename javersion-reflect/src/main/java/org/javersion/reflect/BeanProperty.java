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
package org.javersion.reflect;

import org.javersion.util.Check;

public class BeanProperty implements Property {

    private final String name;

    private final MethodDescriptor readMethod;

    private final MethodDescriptor writeMethod;

    public BeanProperty(String name, MethodDescriptor readMethod, MethodDescriptor writeMethod) {
        this.name = Check.notNull(name, "name");
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;

        if (readMethod == null && writeMethod == null) {
            throw new IllegalArgumentException("Both readMethod and writeMethod cannot be null");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isReadable() {
        return readMethod != null;
    }

    public boolean isWritable() {
        return writeMethod != null;
    }

    public MethodDescriptor getReadMethod() {
        return readMethod;
    }

    public MethodDescriptor getWriteMethod() {
        return writeMethod;
    }

    public void set(Object bean, Object value) {
        writeMethod.invoke(bean, value);
    }

    public Object get(Object bean) {
        return readMethod.invoke(bean);
    }

    @Override
    public boolean isReadableFrom(TypeDescriptor typeDescriptor) {
        return readMethod != null && readMethod.applies(typeDescriptor);
    }

    @Override
    public boolean isWritableFrom(TypeDescriptor typeDescriptor) {
        return writeMethod != null && writeMethod.applies(typeDescriptor);
    }

    public TypeDescriptor getDeclaringType() {
        if (readMethod != null) {
            return readMethod.getDeclaringType();
        } else {
            return writeMethod.getDeclaringType();
        }
    }

    @Override
    public TypeDescriptor getType() {
        if (readMethod != null) {
            return readMethod.getReturnType();
        } else {
            return writeMethod.getParameters().get(0).getType();
        }
    }

}
