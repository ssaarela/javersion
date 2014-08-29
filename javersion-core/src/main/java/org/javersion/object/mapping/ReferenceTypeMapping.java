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

    private static final String REFERENCES = "@REF@";
    
    private final String alias;
    
    private final Class<?> rootType;
    
    public ReferenceTypeMapping(Class<?> rootType, String alias) {
        this.rootType = Check.notNull(rootType, "rootType");
        this.alias = Check.notNullOrEmpty(alias, "alias");
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return rootType != null 
                && localTypeDescriptor.typeDescriptor.isSubTypeOf(rootType)
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
        IdentifiableType identifiableType = (IdentifiableType) context.describeNow(targetPath(alias).index(""), type);
        return new ReferenceType(identifiableType, targetPath(alias));
    }
}
