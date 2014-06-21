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
package org.javersion.object;

import java.util.Map;

import org.javersion.path.PropertyPath;

public class ObjectSerializer<B> {

    private final SchemaRoot schemaRoot;
    
    public ObjectSerializer(Class<B> clazz) {
        this.schemaRoot = DescribeContext.DEFAULT.describeSchema(clazz);
    }
    
    public ObjectSerializer(Class<B> clazz, TypeMappings typeMappings) {
        this.schemaRoot = new DescribeContext(typeMappings).describeSchema(clazz);
    }

    public Map<PropertyPath, Object> write(B object) {
        return new WriteContext(schemaRoot, object).toMap();
    }

    @SuppressWarnings("unchecked")
    public B read(Map<PropertyPath, Object> properties) {
        return (B) new ReadContext(schemaRoot, properties).getObject();
    }
}
