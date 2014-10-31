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
package org.javersion.core;

import org.javersion.util.Check;

public class BranchAndRevision implements Comparable<BranchAndRevision>{

    public static BranchAndRevision min(String branch) {
        return new BranchAndRevision(branch, Revision.MIN_VALUE);
    }

    public static BranchAndRevision max(String branch) {
        return new BranchAndRevision(branch, Revision.MAX_VALUE);
    }


    public final String branch;

    public final Revision revision;

    public BranchAndRevision(VersionNode<?, ?, ?> versionNode) {
        this(versionNode.getBranch(), versionNode.getRevision());
    }

    private BranchAndRevision(String branch, Revision revision) {
        this.branch = Check.notNullOrEmpty(branch, "branch");
        this.revision = revision;
    }

    @Override
    public int hashCode() {
        return revision.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BranchAndRevision) {
            BranchAndRevision other = (BranchAndRevision) obj;
            return branch.equals(other.branch) && revision == other.revision;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(BranchAndRevision other) {
        int cmpr = branch.compareTo(other.branch);
        if (cmpr == 0) {
            return revision.compareTo(other.revision);
        }
        return cmpr;
    }

}
