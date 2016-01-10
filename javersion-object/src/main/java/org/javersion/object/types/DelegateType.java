/*
 * Copyright 2016 Samppa Saarela
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
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.MethodDescriptor;
import org.javersion.reflect.StaticExecutable;

public class DelegateType implements ValueType {

    public static DelegateType of(MethodDescriptor valueMethod, StaticExecutable creator, ValueType valueType) {
        if (valueType instanceof ScalarType) {
            return new Scalar(valueMethod, creator, (ScalarType) valueType);
        }
        if (valueType instanceof IdentifiableType) {
            return new Identifiable(valueMethod, creator, (IdentifiableType) valueType);
        }
        return new DelegateType(valueMethod, creator, valueType);
    }

    final MethodDescriptor valueMethod;
    final StaticExecutable creator;
    final ValueType valueType;

    private DelegateType(MethodDescriptor valueMethod, StaticExecutable creator, ValueType valueType) {
        this.valueMethod = valueMethod;
        this.creator = creator;
        this.valueType = valueType;
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        Object delegate = valueType.instantiate(propertyTree, value, context);
        valueType.bind(propertyTree, delegate, context);
        return fromDelegate(delegate);
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        Object delegate = toDelegate(object);
        valueType.serialize(path, delegate, context);
    }

    Object fromDelegate(Object delegate) {
        return creator.invokeStatic(delegate);
    }

    Object toDelegate(Object object) {
        return valueMethod.invoke(object);
    }

    private static class Identifiable extends DelegateType implements IdentifiableType {

        private Identifiable(MethodDescriptor valueMethod, StaticExecutable creator, IdentifiableType valueType) {
            super(valueMethod, creator, valueType);
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext writeContext) {
            Object delegate = toDelegate(object);
            return ((IdentifiableType) valueType).toNodeId(delegate, writeContext);
        }

        @Override
        public boolean isReference() {
            return valueType.isReference();
        }
    }

    private static class Scalar extends Identifiable implements ScalarType {

        private Scalar(MethodDescriptor valueMethod, StaticExecutable creator, ScalarType valueType) {
            super(valueMethod, creator, valueType);
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) throws Exception {
            Object delegate = ((ScalarType) valueType).fromNodeId(nodeId, context);
            return fromDelegate(delegate);
        }
    }
}
