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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.javersion.object.mapping.ReferenceTypeMapping.isReferencePath;

import java.util.Optional;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.Versionable;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class VersionableReferenceTypeMapping implements TypeMapping {

    @Override
    public boolean applies(Optional<PropertyPath> path, LocalTypeDescriptor localTypeDescriptor) {
        TypeDescriptor type = localTypeDescriptor.typeDescriptor;
        Versionable versionable = type.getAnnotation(Versionable.class);
        return versionable != null
                && !isNullOrEmpty(versionable.targetPath())
                && (!path.isPresent() || isReferencePath(getTargetPath(versionable, type), path.get()));
    }

    private PropertyPath getTargetPath(Versionable versionable, TypeDescriptor type) {
        return PropertyPath.parse(versionable.targetPath());
    }

    @Override
    public ValueType describe(Optional<PropertyPath> path, TypeDescriptor type, DescribeContext context) {
        Versionable versionable = type.getAnnotation(Versionable.class);
        return ReferenceTypeMapping.describeReference(type, getTargetPath(versionable, type), context);
    }

}
