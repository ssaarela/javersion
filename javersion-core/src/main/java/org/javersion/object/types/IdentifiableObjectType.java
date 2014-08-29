/*
 * Copyright 2014 Samppa Saarela
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

import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

public class IdentifiableObjectType<O> extends ObjectType<O> implements IdentifiableType {

    private final FieldDescriptor idField;
    
    private final IdentifiableType idType;
    
    public IdentifiableObjectType(Class<O> rootType, Iterable<TypeDescriptor> types, FieldDescriptor idField, IdentifiableType idType) {
        super(rootType, types);
        this.idField = idField;
        this.idType = idType;
    }
    
    @Override
    public String toString(Object object) {
        return idType.toString(idField.get(object));
    }

}
