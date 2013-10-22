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

import java.util.Collection;
import java.util.Set;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public abstract class ObjectTypeBuilder<R, B extends ObjectTypeBuilder<R, B>> {
    
    protected String alias;
    
    protected Set<Class<? extends R>> classes = Sets.newHashSet();

    protected IdMapper<R> idMapper;
    
    public ObjectTypeBuilder(Class<R> root) {
        classes.add(Check.notNull(root, "root"));
        alias = Check.notNull(root.getCanonicalName(), "root.getCanonicalName()");
    }
    
    @SafeVarargs
    public final B havingSubClasses(Class<? extends R>... subClasses) {
        return havingSubClasses(ImmutableList.copyOf(subClasses));
    }

    public final B havingSubClasses(Collection<Class<? extends R>> subClasses) {
        classes.addAll(subClasses);
        return self();
    }
    
    public B havingIdMapper(IdMapper<R> idMapper) {
        Check.notNull(idMapper, "idMapper");
        this.idMapper = idMapper;
        return self();
    }

    public B havingAlias(String alias) {
        Check.notNullOrEmpty(alias, "alias");
        this.alias = alias;
        return self();
    }
    
    protected abstract B self();
    
}
