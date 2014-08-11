Javersion
======

Javersion is a Work In Progress for Data Versioning Library for Java.
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
  * Undo
  * Fix errors
  * Optimize active history by merging or removing unnecessary intermediate saves (e.g. all versions from workspace).
  * This is achieved in purely immutable way by defining a new root for the modified/following versions.

It's designed to be extensible at all levels, e.g.

* Core makes no assumptions about what metadata versions should contain, it only knows of functional properties of versions
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

Implementation utilizes immutability and non-blocking data structures / algorithms as much as possible, so
VersionGraphs are trivial and efficient to cache even in a cluster.


Non-Blocking Persistent Data Structures for Java
------

Efficient versioning requires persistent data structures (i.e. Purely Functional Data Structures).

Inspired by Clojure's (i.e. Phil Bagwell's) persistent Hash Array Mapped Trie:

* [PeristentHashMap](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentHashMap.java)
* [PersistentHashSet](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentHashSet.java)

And persistent red-black tree:

* [PersistentTreeMap](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentTreeMap.java)
* [PersistentTreeSet](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentTreeSet.java)


TODO
----
* Efficient persistent Map/Set and SortedMap/SortedSet for VersionGraph
  * ~~Optimize PersitentSet via abstract HAMT base class for Map and Set that doesn't require Entry.value.~~ DONE
  * ~~Try 64-bit mode for PersistentMap and if it's good, sniff JVM bitness or make it configurable~~
    * Didn't seem to provide any improvement
  * ~~ArrayNode-optimization~~ DONE
    * Bitmapped node is converted to ArrayNode when it's full (32) - there's no need to remap anything then.
  * ~~Efficient PersistedSortedSet~~ DONE
    * AbstractRedBlackTree implements persistent red black tree algorithm with PersistentSortedMap/Set sub classes for Map/Set
    * Uses UpdateContext mechanism for efficient bulk updates. Bulk updates (e.g. addAll) are done reusing same nodes.
  * ~~Interfaces for persistent and mutable, hash and sorted Sets and Maps~~ DONE
  * Improve tests by common base class for maps and sets
  * Improve test (and mutation) coverage
* Object/version binding
  * ~~Read (possibly cyclic) objects form VersionGraph~~ DONE
  * ~~Support for Set, Map, List~~ DONE
  * ...
* MAC-verified token for parent revs for easier updating
* JSON versioning
* ...
