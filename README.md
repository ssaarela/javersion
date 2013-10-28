javersion
======

TOBE: Data Versioning Library for Java


Non-Blocking Persistent Collections for Java
------

Based on Clojure's persistent hash array mapped trie -algorithm, here's same for Java apps:

* [PeristentMap](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentMap.java) - persistent trie
* [PersistentSet](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/PersistentSet.java) - persistent trie
* [AtomicMap](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/AtomicMap.java) - java.util.Map implementation using AtomicReference for PersistentMap
* [AtomicSet](https://github.com/ssaarela/javersion/blob/master/javersion-core/src/main/java/org/javersion/util/AtomicSet.java) - java.util.Set impelementation using AtomicReference for PersistentSet
