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
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ReferenceType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.Root;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class ReferenceTypeMapping implements TypeMapping {

    private static final String REFERENCES = "$REF";

    private final String alias;

    private final ObjectTypeMapping<?> objectTypeMapping;

    public ReferenceTypeMapping(String alias, ObjectTypeMapping<?> objectTypeMapping) {
        // FIXME: Customizable targetPath
        this.alias = Check.notNullOrEmpty(alias, "alias");
        this.objectTypeMapping = Check.notNull(objectTypeMapping, "objectTypeMapping");
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return objectTypeMapping.applies(path, localTypeDescriptor)
                && isReferencePath(alias, path);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return describeReference(path, type, alias, context);
    }

    public static boolean isReferencePath(String alias, PropertyPath path) {
        return path instanceof Root
                || path instanceof SubPath
                && !targetPath(alias).equals(((SubPath) path).parent);
    }

    private static SubPath targetPath(String alias) {
        return PropertyPath.ROOT.property(REFERENCES).property(alias);
    }

    public static ValueType describeReference(PropertyPath path, TypeDescriptor type, String alias, DescribeContext context) {
        SubPath targetPath = targetPath(alias);
        context.describeAsync(targetPath.anyIndex(), type);
        IdentifiableType identifiableType = (IdentifiableType) context.describeNow(targetPath.anyKey(), type);
        return new ReferenceType(identifiableType, targetPath);
    }
}
