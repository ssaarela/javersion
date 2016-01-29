# Javersion [![Build Status](https://travis-ci.org/ssaarela/javersion.svg?branch=master)](https://travis-ci.org/ssaarela/javersion) [![Coverage Status](https://coveralls.io/repos/ssaarela/javersion/badge.svg?branch=master)](https://coveralls.io/r/ssaarela/javersion?branch=master)

Javersion is a data versioning toolkit for Java. It's easy to use, highly modular and 
extensible. Anything that can be mapped to `Map<K, V>` with simple immutable key and value
can be versioned - that includes e.g. Plain Old Java Objects and JSON.  

Think of your favourite VCS (e.g. git, mercurial or svn) to get an idea of what kind of versioning 
Javersion supports:  

* Concurrent editing of a shared resource
* Merging concurrent updates
* Conflict detection with automatic resolving strategies
* Branching - published vs workspace
* All versions accessible, viewing and reverting to older versions
* Customizable version metadata: who did what exactly and when?

Where as source control systems are meant for versioning files and directory structures
that represent some data, Javersion versions data itself.
Difference should be clear if you think of for example 
reformatting JSON or reordering columns of a CSV file. 
Even if it's serialized representation might be very different, it doesn't change actual data.
Instead of getting conflicting lines of text, in Javersion, you get conflicting property values.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
### Table of Contents 

- [Introduction](#introduction)
  - [Why Should You Use It?](#why-should-you-use-it)
  - [Why Should You Not Use it?](#why-should-you-not-use-it)
  - [How It Works?](#how-it-works)
  - [Deleting Data?](#deleting-data)
  - [Null Handling](#null-handling)
- [Getting Started With Java Objects](#getting-started-with-java-objects)
- [Version Persistence](#version-persistence)
  - [Searching](#searching)
  - [Documented-Oriented JDBC Persistence](#documented-oriented-jdbc-persistence)
    - [Publishing Versions](#publishing-versions)
    - [DocumentVersionStore](#documentversionstore)
    - [EntityVersionStore](#entityversionstore)
- [Core Classes](#core-classes)
  - [Version](#version)
  - [VersionGraph](#versiongraph)
  - [VersionNode](#versionnode)
  - [Merge](#merge)
  - [Diff](#diff)
  - [Revision](#revision)
  - [PropertyPath](#propertypath)
  - [Schema](#schema)
- [Object Mapping](#object-mapping)
  - [Nulls](#nulls)
  - [Lists](#lists)
  - [Maps](#maps)
  - [Sets](#sets)
  - [Objects and Polymorphism](#objects-and-polymorphism)
  - [References](#references)
  - [Custom ValueTypes](#custom-valuetypes)
- [Modules](#modules)
  - [Core](#core)
  - [Object](#object)
  - [Spring JDBC](#spring-jdbc)
  - [Util](#util)
  - [Reflect](#reflect)
  - [Path](#path)
  - [JSON](#json)
- [Release Versioning](#release-versioning)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Introduction

## Why Should You Use It?

Because your users/customer/PO wants it.

* They want undo and they don't get it, why it's so hard
  to implement it for a multi-user web application. 

* They want to see who has edited, what exactly and when.
  Instead we offer them creationDate, createdBy, lastModified, modifiedBy 
  and an action log file accessible only by administrator. 

* They want to have a playground for changes before they go live - and they want
  to be able to make small changes to the live data while preparing something larger
  in their playground.
 
* They want to have templates for their products and they want those
  products to change when the template changes but not where that piece of information
  is manually edited. 

* They want to import updated data from an external system while still being able 
  to edit it locally - and not loose those edits when next import occurs.

* They want to combine data maintained in separate systems, not 
  to specify primary master for each piece of information and worry about unidirectional 
  data flow.

...they just don't know it's called versioning.

With versioning enabled system, concurrent editing of shared resources is safe without
need for optimistic or pessimistic locking. Updates can be merged and possible conflicts
handled with grace.

## Why Should You Not Use it?

* You don't have aforementioned requirements.

* Your data is immutable by nature or, once created, only it's state changes in a strictly 
  controlled manner. For example financial transactions or action log events.

* Your data is not structured or semi-structured by nature. E.g. Your data is binary,
  mostly free-text or it's most natural representation is actually textual, like program files.
  
* You have a large set of strongly interconnected data and you cannot split it into 
  smaller mostly independent parts that can be viewed as e.g. JSON or XML documents.


## How It Works?

As with any VCS, first you checkout some branch, then modify it and when done, commit changes.

Javersion´s core model is an in-memory data-structure. You decide yourself if and 
how you want to persist the versions.

Unlike most version control systems, Javersion doesn't prevent you from committing 
conflicting changes. Conflicting versions may be stored and resolved later. 
You may, however, decide yourself not to actually persist a version if there's a conflicting 
change. 

When reading versioned data, it's by default always a merge - from one or more branches.
You get possible conflicts as merge metadata with automatic conflict resolution 
applied to the merged data. 
You also get one or more revisions that identify versions from which the data was merged.
If there's only one revision, then no actual merge has occurred. 

**The biggest change to basic interaction logic is that when updating data, you need to know 
the revisions on which the update is based on.**

This is crucial: without knowing how data has been changed, one cannot merge it safely. 
However when the starting point of an update is known and immutable, 
one can easily calculate the actual change (diff). 

In a user interface, it is natural to first load the latest version of data, then 
edit it and lastly save it. Make sure that when loading it, 
you get the revisions metadata along with the data, 
and when saving it, send also those revisions back unchanged. 

With import-type interface where you can't keep track of the revisions in the external system,
use a branch in which to import. Use other branches for local changes or other 
sources of data, and merge those when reading. 

## Deleting Data?

Thou shall not delete data! - You overwrite it with nulls.

## Null Handling

Null have a special meaning, which is that given key has been removed (i.e. tombstone). 
Keys with null values should be treated as if they didn't exists in the first place. 
Null keys are not allowed. In some rare cases where null is actually to be treated as a value, 
e.g. Map with null values, one may use `Persistent.NULL` in a custom `ValueType`.


# Getting Started With Java Objects

As you can parse most any data into Java Objects, it's a good starting point.

1. Design your domain class(es) so that all relevant data can be reached from a single root.
    * Thinking of something that can be serialized to JSON helps.
    * No need for getters and setters - Javersion uses fields directly.
      * Private and final fields are OK.
      * Transient and `@VersionIgnore` fields are skipped.
      * Java Bean properties may be versioned with `@VersionProperty` annotated getter method with matchin setter.
    * Default (no-args) or `@VersionCreator` or `@JsonCreator` -annotated constructor.
      * Constructor parameters need to be named with `javac -parameters`, `@Param` or `@JsonProperty`.
    
1. Annotate your classes with `@Versionable` or use `TypeMappings.builder()` to configure
   how your domain model is to be serialized.

1. Create an `ObjectSerializer` instance for your class. It's immutable and thread-safe
   so you might just as well save it in a final static field.
```java
static final ObjectSerializer MY_OBJECT_SERIALIZER = new ObjectSerializer(MyObject.class);
```

1. Construct your object and use `ObjectVersionManager` (not thread-safe!) 
   to create a version of if: 
```java
ObjectVersionManager versionManager = new ObjectVersionManager(MY_OBJECT_SERIALIZER);
ObjectVersion version = versionManager.buildVersion(myObject)
        .branch("master")
        .build();
saveVersion(version);
```
1. Read and update your object
```java
// Load
ObjectVersionManager versionManger = new ObjectVersionManager(MY_OBJECT_SERIALIZER)
    .init(loadVersionGraph());
MergeObject merge = versionManager.mergeBranches("master");
myObject = merge.getObject();
myObject.revisions = merge.getMergeHeads();
```
```java
// Modify myObject
...
```
```java
// Save 
ObjectVersionManager versionManger = new ObjectVersionManager(MY_OBJECT_SERIALIZER)
    .init(loadVersionGraph());
ObjectVersion version = versionManager.buildVersion(myObject)
        .parents(myObject.revisions)
        .branch("master")
        .build();
saveVersion(version);
```
Getting ObjectVersionManager can (should) be generalized for create, update and read, as
it can be initialized with an empty version graph.

1. Setup `EntityVersionStoreJdbc`, `DocumentVersionStoreJdbc` or create your own 
   persistence for versions.

# Version Persistence

Version is relatively simple data model that is easy to persist. Feel free to rollup your own 
persistence to meet your requirements or choose one of Javersion's.

While versions have a generic key, value and metadata types, you'll get a long way with just a few 
concrete alternatives.

ObjectSerializer uses `PropertyPath` for keys and mostly JSON compatible set of objects for values
(see `org.javersion.core.Persistent.Type`):

* null
* Object - immutable wrapper for type alias (e.g. classes simple name, or Map for generic object)
* Array - a constant for arrays
* String
* Boolean
* Numbers: Long, Double and BigDecimal.

Nested structures are split into nested property paths with values from the list above.

Simplest relational model for this consists of three tables: `version`, `version_parent` and `version_property`. 

Javersion contains two SQL-based persistence strategies: `DocumentVersionStoreJdbc` and `EntityVersionStoreJdbc`.
Both are optimized for use with cache and for synchronizing to external systems. 

## Searching

Versions with their changesets and DAG-inheritance model are hardly ideal - or even usable - for searches.
Instead you should index relevant data separately. You probably want your latest "master"-version to be searchable,
but what about other branches or older versions?

Good (tried-out) options include:
* Using search engine like Lucene or Elastic Search.
* Creating a search-optimized relational model along version tables and updating it at the same time 
  when inserting new versions. 

## Documented-Oriented JDBC Persistence

Both Javersion's persistence strategies, `DocumentVersionStore` and `EntityVersionStore` are document-oriented.
All versions belong to a "document" and all documents have their own version history.

Versions are stored in three tables:
* `version`: `doc_id`, `revision`, `branch`, `type`.
* `version_parent`: `revision`, `parent_revision`.
* `version_property`: `revision`, `path`, value consisting of `type` and `str` or `nbr`. 

In addition a few shared configuration tables are needed: 
* `repository` containing a row per repository that can be locked for publish. 
* `version_type` - allowed values for `version.type` column. 

Both strategies guarantee that visible versions are strictly ordered. You can always get
changes and only changes that have occurred after a given revision. In an external system that needs to 
synchronize it's data to Javersion, it's enough to keep track of the last revision
read and fetch changes since that. Fetching updates is fast and returns an empty list if 
there is none. Thus for example cluster safe cache is trivial to implement as you can
keep TTL very short and check for updates often. 

Both strategies have in common: 

* Keys are of type PropertyPath.
* Values are expected to be of aforementioned types - although custom types may be supported by an extension.
* Document identifier -type is customizable (i.e. generic).
* Document metadata -type is customizable.
* Single-valued properties can be configured to be persisted in `version`-table columns instead of the generic `version_properties` table.
* Versioning tables may have any names but they must follow strategy specific schema - 
  a set of versioning tables is called a repository and applications may have multiple repositories (like collections in MongoDB).
* There must be a row in `repository` table for each repository.

Guaranteeing the order of updates requires locking. The two persistence strategies differ 
mainly on how this ordering is achieved, what is locked and when, and when versions become 
visible.

### Publishing Versions

Calling publish assigns repository-wide ordinals to versions. 
Publishing acquires a repository wide lock so that only one process
is allowed to publish at a given time. Publishing can, however, be run
asynchronously and each call to publish processes all versions that were inserted since last publish.
Also new versions may be inserted concurrently while publishing. 

*Publishing should be called in a separate transaction from actual inserts. Inserting new versions
and publishing in the same transaction severely limits concurrency and may end up in deadlock.*

### DocumentVersionStore

DocumentVersionStore allows fully concurrent inserting of versions, but requires 
publishing in a separate transaction to assign version ordinals,
before they become visible at all. 

Use e.g. a transaction commit hook and an asynchronous queue to publish changes.

_This strategy is suitable if you aim for high concurrency and insertion performance with low integrity requirements._

Beware that there is always a (time) gap between when version is inserted and when it becomes visible. 
If you index your data in database in the same transaction in which it's inserted, your searches may 
match data that is not yet visible. 

### EntityVersionStore

EntityVersionStore requires that all versions refer to an "entity table". It's a table with 
document id as a primary key. Different entities may be updated concurrently but
updates to a given entity are serialized. In result entity-specific versions 
(accessed by single id) are ordered and visible immediately after transaction commits.
Bulk fetching entity versions - especially updates, however, still requires publishing first. 

When inserting new versions one must first select relevant entity table rows for update.
As there strictly cannot be concurrent updates to given entity the version order can be guaranteed
immediately.

With this strategy also data integrity can be guaranteed. You may validate all aspects of your data
before committing it: 

* Does current latest (persisted) version allow updates? 
* Is the result of merge still valid?
* Are there conflicts that should block adding a new version?

_This strategy is suitable when you need strong control over integrity of your data._ 

You may also safely update the entity table and use it for searches. 


# Core Classes

## Version

`Version<K, V, M>` is an immutable base class for different types of versions. 
It's generic parameters are
  * K for key type
  * V for value type
  * M for custom metadata type

Versions consist of 

* revision - a unique UUID-like id of the version, e.g. 02M7GKAK7J000-AEV5TWAWKQHRV
* branch - name of the branch, just a string 
* parentRevisions - a set of revisions on which this version is based on
* changeset - changed property values, `Map<K, V>`
* type - VersionType enum required for some more advanced use-cases
* meta - what ever metadata you need 

As coding with references to multiple generic types can get quite tedious, 
this class is designed to be extended by concrete implementations,
like `ObjectVersion<M>`.

Versions are constructed with `Version.builder()` which is a fluent
builder that is also designed to be easily extended with e.g. `ObjectVersion.builder()`.

## VersionGraph

VersionGraph is the core data structure of Javersion, but 
is not intended to be used directly as it has a hellish
generic signature. It's meant to be used via a subclass like 
`ObjectVersionGraph<M>` where `M` is custom metadata type.

VersionGraph is a persistent data structure that contains object 
model of Versions, i.e. VersionNodes. It can

* Merge individual revisions and branches.
* Efficiently return any version or merge of branches effective at any version.
* Optimize graph by discarding unnecessary versions while keeping 
  effective state of desired versions intact. 
* Build a new VersionGraph from added Versions.  

## VersionNode

`VersionNode` is a fully built snapshot view of a version. It contains

* All Version data including a verified changeset. 
* Merged properties - a PersistentHashMap built on parent versions' properties.
* All merged revisions - a PersistentHashSet built on parent versions' data structure. 
* Conflicts - a MultiMap of unresolved conflicts.
* Heads of all branches at the time when version was added.
* Link to previous version to be able to iterate over versions in reverse addition order. 

## Merge

`Merge` is the result of merging branches or specific revisions. Javersion supports two kind of merge strategies:

1. Merging specified versions where later Revision wins in conflicts (the other value is accessible via conflicts).

1. Merging branches where the order of branches to be merged defines default conflict resolution.
   In case there are multiple heads in a branch, those are first merged using former strategy. 

`VersionNode` itself is also a Merge object. It merges it's parents and applies it's own changeset on top of that.  

## Diff

`Diff` is a utility class containing static diff-methods. The resulting map will contain all
keys that have different value in the latter map - and all keys not present in the former 
but not in the latter map will have null as value in the result. 

## Revision

Custom time-based GUID. The main differences to standard UUID are

* Time-part is straight forward 48-bit from current time plus 16-bit sequence number 
  for Revisions created withing the same millisecond.
* Unsigned comparison of Revisions.
* Custom serialization that is lexically comparable.
* Serialization is based on Crockford variant of Base32 instead of Hex.

Rationale behind this is that while security/randomness Revisions is not that 
important for versioning, time-based comparison for conflict resolution is.

## PropertyPath 
* Root: (empty string) 
* Properties: `property`
* Indexed lists: `list[0]`
* Maps: `map["key"]`
* Nested paths: `list[0].map["key"].property`
* Schema path
    * Any index: `list[]`
    * Any property: `.*`
    * Any key: `map{}` (matches also properties)
    * Any: `*` (matches all)
* PropertyPath is a list of NodeIds that identify a path in Object model

## Schema 
* Simple recursive, possibly cyclic object model with a value and children by NodeId
* E.g. 
```java
// Pseudocode example of a Schema of TreeNode with name and list of children
Schema string = new Schema(String.class);
Schema treeNode = new Schema(TreeNode.class);
Schema listOfTreeNodes = new Schema(List.class);
treeNode.children["name"] = string;
treeNode.children["children"] = listOfTreeNodes;
listOfTreeNodes.children["<any-index>"] = treeNode;
```

# Object Mapping

## Properties

By default Javersion processes all non-transient fields. Fields may be ignored also with `@VersionIgnore` annotation. 

Simple Java Bean compatible properties (get/is/set) can be versioned with `@VersionProperty` on a getter with
matching setter. 

Property name used in versioning defaults to field's or property's name. Default name can be
overridden with `@VersionProperty("versionableName")`. 

`@VersionProperty` annotated getter overrides a field with
a name that matches it's _versionable name_. Therefore if property name is overridden, one may
have to ignore the field separately.

## Constructors

Javersion uses default (no-args) constructor by default. Other constructor or static method 
may be used by annotating it with `@VersionCreator` or Jackson's `@JsonCreator`. 

When other than default constructor is used, Javersion needs to know _versionable_ property names
that are bound to constructor parameters. Parameter names can be defined by

  * compiling with `javac -parameters` option allows using source code parameter names
  * `@Param("versionableName")` annotation
  * `@JsonProperty("versionableName")` annotation

## Nulls

If a property has null value in changeset, it is skipped in binding. Thus setting a property to null,
reverts it back to what ever is set to it in object initializer. This applies also to primitive fields:
nulls are skipped and the field is left to it's default value.

In some rare cases where `ValueType` needs to support null, it may use `Persistent.NULL` as a null-marker
that isn't equal to non-existing keys. `MapType` is one such special case as it supports null values.

## Lists

List elements are mapped by index. Beware that if you remove from the beginning of the list, all subsequent 
elements will change! Nulls are allowed, but trailing nulls are truncated. For example saving and loading a list with
```
[null, 1, null, null]
```
will result in 
```
[null, 1]
```

## Maps 

Javersion supports `Map` (HashMap), `SortedMap` and `NavigableMap` (both TreeMap). 
You should use these interfaces instead of concrete classes in your domain model. 

Map supports all scalar keys, i.e. key `ValueType` needs to implement `ScalarType`.
As [ReferenceType](#references) is actually a `ScalarType`, any identifiable object
may be used as a key.

Null keys are not supported, but values may be null (as of version 0.10.0).

## Sets

Javersion supports `Set` (HashSet), `SortedSet` and `NavigableSet` (both via TreeSet). 
You should use these interfaces instead of concrete classes in your domain model.

Sets are a kind of a special case of maps and require an identifying key,
i.e. element `ValueType`´ must implement `IdentifiableType`.
Unlike lists, changeset of removing an element from a Set only affects 
that particular element. 

A set of primitives, Strings and other scalar values can be used as such, but
complex objects require either an identifier property (field or getter) 
that is annotated with `@Id`. An identifier may be non-versionable
(read-only) by annotating a getter without matching setter. 

An alternative to `@Id` annotation is `@SetKey` on element type, field or getter.
SetKey defines either property names used for (composite) key or 
a `Function` that is used to convert elements to identifying keys.
SetKey's value refers to versionable properties used for element keys by their 
versionable name.  

However Set's elements are identified, the versionable identity should match
equals/hashCode identity of the element.

Null elements are not supported.

## Objects and Polymorphism

Objects should have default no-args constructor. They can be mapped with `TypeMappings.builder()` or with
`@Versionable` annotation.

You can define polymorphic classes with `@Versionable(subclasses=...}` (or `@JsonSubTypes`) on 
the root class, or use `TypeMappings.builder`: 

```
TypeMappings.builder()
    .withClass(Pet.class, "Pet")
    .havingSubClass(Dog.class, "Doggy")
    .havingSubClass(Cat.class, "Kitty")
    .build();
```
Second (String) parameter defines an alias for the type that is mapped to given class when reading data.
Default alias for a class is it's simple name.

Sub classes should not have properties with same name unless they are also of same type. 

## References

Objects with an `@Id` property (field or getter+setter) may be replaced with a reference by id in serialization.
This allows serializing and versioning complex object graphs, not just trees like with JSON. 
These graphs may even even contain cycles.

A nice side-effect of using references is
that resulting paths are of fixed length even for deeply nested structures. 
This may be used for example in relational persistence to define foreign key 
constraint for allowed paths by serializing schema path and keys/indices in separate columns.

In order to configure references one must configure a target path for that kind of objects:
either `@Versionable(targetPath="allPets")` or `TypeMappings.builder().withClass(Pet.class).asReferenceForPath("allPets")`.

For example
```java
Dog mother = new Dog();
mother.id = "mother";
Dog puppy = new Dog();
puppy.id = "puppy";
puppy.mother = mother;
Owner owner = new Owner();
owner.pets = asList(mother, puppy);
petSerializer.toPropertyMap(owner);
```
will result in 
```
(root) = Owner
pets = Array
pets[0] = mother
pets[1] = puppy
allPets["mother"] = Dog
allPets["mother"].id = mother
allPets["puppy"] = Dog
allPets["puppy"].id = puppy
allPets["puppy"].mother = mother
```
And if there were `Map<String, Pet> allPets` in Owner, it would contain all those pets by id.

## Custom Type Mapping and Value Types

If you have a class that Javersion's basic type mappings can't handle properly, 
you need to register a custom `TypeMapping` for it using `TypeMappings.builder()`.

`TypeMapping` is a simple interface that

1. Checks if it applies to a given property (map), type (K or V of a Map) or path (PropertyPath).
2. Provides a ValueType for it.
3. Using reflection describes recursively all sub paths (properties, indexes or keys) it can have. 

`ValueType` again is a simple interface that

1. Serializes given object to keys of type PropertyPath and _values returned by nested ValueTypes_.
2. Can instantiate and 
3. bind given object from PropertyPaths and values.

Instantiation creates the object from the simple mapped value. For scalars that's all there is to it,
but complex objects need to also bind nested values to object properties, map keys or collection elements.

For an object to be usable as identifier in a Set, it needs to implement `IdentifiableType` -interface. 
It extends ValueType with a method that converts value to `NodeId` that is 
essentially a wrapper for either long "index" or String key that can be part of a PropertyPath.

For an object to be usable as a key in a Map, it needs to implement `ScalarType` that can 
also convert NodeIds back to object. 

### Basic Components with String-constructor

Simple components that have a String-constructor and matching toString, may be registered
using 

```TypeMappings.Builder.withMapping(new ToStringMapping(MyStringComponent.class))```

`ToStringMapping` also allows matching sub classes of the given class with `boolean matchSubClasses`-parameter, 
but beware that it does not support polymorphism! If your property is of type `MySuperStringComponent` then 
that's what you're going to get out event if you assign `MySubStringComponent` to it. 

### Delegate Mapping

`@VersionValue` (or `@JsonValue`) can be used to mark a no-args instance method with non-void return type as a 
delegate for versioning. It requires a matching `@VersionCreate` constructor or static method for reading
versions back to given object. 

### Configurable Annotations Search Path

Javersion's TypeMappings checks if Jackson is in found in classpath and in that cases uses Jackson's annotations
as secondary mapping annotations. Javersion's own annotations can always be used to override Jackson's annotations. 
As a fallback Javersion uses basic reflection, e.g. javac -parameters for parameters and Class.getSimpleName() 
for alias. However, this search path can be configured using `TypeMappings.withMappingResolvers`
and one may implement a custom `MappingResolver`. 

# Modules

## Core 
* Generic versioning of Map<K, V> 
* Immutable in-memory data structures
* Requires immutable (scalar) K and V

## Object
* Conversion from POJOs to Map<PropertyPath, Object> and back
* Helper classes for object versioning

## Spring JDBC
* Spring + Querydsl SQL based persistence for versions

## Util
* Persistent data structures.

## Reflect
* Simplified reflection: TypeDescriptor, FieldDescriptor, MethodDescriptor, ConstructorDescriptor, ParameterDescriptor and BeanProperty.
* Uses Guava's TypeToken to resolve all resolvable generic bindings.

## Path
* Model of Java/JavaScript compliant paths
* Schema for validating - or guiding the reading of - paths 

## JSON
* JSON to Map<PropertyPath, Object> to JSON mapping (PoC-level)


# Release Versioning

Javersion follows [Semantic Versioning][http://semver.org/] guidelines. 
As current version is still 0.x the API is guaranteed to change.
