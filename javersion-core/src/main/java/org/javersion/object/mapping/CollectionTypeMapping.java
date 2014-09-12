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
package org.javersion.object.mapping;

import java.util.Collection;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.CollectionType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class CollectionTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return localTypeDescriptor.typeDescriptor.getRawType().equals(Collection.class);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor collectionType, DescribeContext context) {
        TypeDescriptor elementType = collectionType.resolveGenericParameter(Collection.class, 0);
        context.describeComponent(path.index(""), collectionType, elementType);
        return new CollectionType();
    }

}
