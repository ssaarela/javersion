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
package org.javersion.object;

import static com.google.common.base.Strings.isNullOrEmpty;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class VersionableReferenceTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, ElementDescriptor elementDescriptor) {
        Versionable versionable = elementDescriptor.typeDescriptor.getAnnotation(Versionable.class);
        return versionable != null 
                && !isNullOrEmpty(versionable.byReferenceAlias())
                && ReferenceTypeMapping.isReferencePath(versionable.byReferenceAlias(), path);
    }

    @Override
    public ValueType describe(DescribeContext context) {
        TypeDescriptor typeDescriptor = context.getCurrentType();
        Versionable versionable = typeDescriptor.getAnnotation(Versionable.class);
        return ReferenceTypeMapping.describeReference(versionable.byReferenceAlias(), context);
    }

}
