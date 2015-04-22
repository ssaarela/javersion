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
package org.javersion.json.web;

import static org.javersion.object.Persistent.GENERIC_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javersion.core.Revision;
import org.javersion.core.VersionProperty;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;

import com.google.common.collect.Multimap;

@Versionable(alias = GENERIC_TYPE)
public class VersionMetadata {
    public String _id;
    public List<Revision> _revs;
    public Map<PropertyPath, Collection<VersionProperty<Object>>> _conflicts;

    public VersionMetadata() {}

    public VersionMetadata(String _id, Set<Revision> _revs, Multimap<PropertyPath, VersionProperty<Object>> conflicts) {
        this._id = _id;
        this._revs = _revs.isEmpty() ? null : new ArrayList<>(_revs);
        this._conflicts = conflicts.isEmpty() ? null : conflicts.asMap();
    }
}
