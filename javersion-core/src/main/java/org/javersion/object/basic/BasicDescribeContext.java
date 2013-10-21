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
package org.javersion.object.basic;

import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import org.javersion.object.DescribeContext;
import org.javersion.object.RootMapping;

public class BasicDescribeContext extends DescribeContext<Object> {
    
    public static final BasicDescribeContext DEFAULT = new BasicDescribeContext();
    
    public BasicDescribeContext() {
        this(new BasicValueTypes());
    }
    public BasicDescribeContext(BasicValueTypes valueTypes) {
        super(valueTypes);
    }
    
    public static RootMapping<Object> describe(Class<?> clazz) {
        return DEFAULT.describe(getTypeDescriptor(clazz));
    }

}
