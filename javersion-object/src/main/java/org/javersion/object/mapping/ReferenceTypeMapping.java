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

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ReferenceType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.Root;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class ReferenceTypeMapping implements TypeMapping {

    private final PropertyPath targetPath;

    private final ObjectTypeMapping<?> objectTypeMapping;

    public ReferenceTypeMapping(PropertyPath targetPath, ObjectTypeMapping<?> objectTypeMapping) {
        Check.that(targetPath != null && !targetPath.isRoot(), "targetPath should not be null or root");
        this.targetPath = targetPath;
        this.objectTypeMapping = Check.notNull(objectTypeMapping, "objectTypeMapping");
    }

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        return objectTypeMapping.applies(targetPath, typeContext) &&
                (path == null || isReferencePath(targetPath, path));
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return describeReference(type, targetPath, context);
    }

    public static boolean isReferencePath(PropertyPath targetPath, PropertyPath path) {
        return path instanceof Root
                || path instanceof SubPath
                && !targetPath.equals(((SubPath) path).parent);
    }

    public static ValueType describeReference(TypeDescriptor type, PropertyPath targetPath, DescribeContext context) {
        TypeContext typeContext = new TypeContext(type);
        context.describeAsync(targetPath.anyIndex(), typeContext);
        IdentifiableType identifiableType = (IdentifiableType) context.describeNow(targetPath.anyKey(), typeContext);
        return new ReferenceType(identifiableType, targetPath);
    }
}
