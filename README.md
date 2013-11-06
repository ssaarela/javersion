Javersion
======

Javersion is a Work In Proggress for Data Versioning Library for Java. 
It's aim is to provide full-featured in-memory version control algorithms and data structures
with optional and customizable persistence for versions (changesets). 
Some use cases for Javersion are:

* Tracking who did, what exactly and when.
* Maintaining entity on multiple branches, e.g. workspace + public.
* Easy merging, e.g. always merge latest from public to workspace - but merge workspace into public only on demand.
  * Customizable merge strategy.
* Revert to older version.
* Safe concurrent editing, no matter how long it takes.
  * Conflicts are detected and may be resolved consciously.
* "Modify" active history while retaining all earlier verions:
  * Undo(!)
  * Fix errors.
  * Optimize active history by merging or removing unnecessary intermediate saves (e.g. all versions from workspace).
  * This is achieved in purely immutable way by defining a new root for the modified/following versions.

It's designed to be extensible at all levels, e.g. 

* Core makes no assumptions about what metadata versions shuold contain, it only knows of functional properties of versios 
(e.g. revision number, parents, diff of properties).
* Core algorithm works on Map making no assumptions about key or value type other than that
  * key needs to be immutable and implement equals/hashCode correctly and
  * value needs to be immutable - i.e. mutable structures need to be decomposed into immutable key/value-pairs.

On top of core, Javersion is going to provide Object versioning library that maps Java Objects to
path/value maps and then versions those. Similar path/value maps can easily be made from e.g. XML or JSON. 
And as e.g. Lucene's Document is essentially path/value map, it's trivial to use Lucene to index version snapshots!
Reading Lucene Document to object is again trivial by converting it to a version 
and then using object-to-version mapper to convert it back to object.

For persistence alternatives, at least SQL-based is on the list.


Non-Blocking Persistent Data Structures for Java
------

Efficient versioning calls for persistent data structures (as in Purely Functional Data Structures), but I was unable to find suitable from existing Java libraries - and I love to code - so I implemented my own. I suspect that these may be handy also on their own...

Inspired by Clojure's (i.e. Phil Bagwell's) persistent Hash Array Mapped Trie, here's some goodies for Java apps:

* [PeristentMap](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentMap.java) - persistent hash trie map 
* [PersistentSet](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentSet.java) - PersistentMap based set

Compared to Clojure's PersistentHashMap (v. 1.5.1), PersistentMap is idiomatic Java code, simpler implementation and faster - at least on my Mac. But I encourage you not to trust my word but see for your self. - And if you do, I'd very much like to hear what you think of these (e.g. Twitter: @ssaarela or bug reports in Github).
