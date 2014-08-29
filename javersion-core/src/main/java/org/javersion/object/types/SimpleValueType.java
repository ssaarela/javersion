/*
 * Copyright 2013 Samppa Saarela
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

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

public class SimpleValueType implements ValueType, IdentifiableType {
    
    public SimpleValueType() {
    }
    
    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return value;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object);
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }

    @Override
    public boolean isReference() {
        return false;
    }

}
