# KCache

This is meant to be a highly general, customizable caching library for Kotlin (and the Java platform in general).

The two major general interfaces are `Cache<ID, T>` and `CachedDataSource<S, T>`. `CachedDataSource<S, T>` is an
interface
that allows users to retrieve data from any source with caching to any cache. `Cache<ID, T>` takes content and
identifiers
and stores the data to be retrieved later. These two separate interfaces allow for great flexibility in the ability to
get and cache data.

In addition to the main interfaces, `StringCachedDataSource<ID>` is a general partial implementation for storing String
content in cache by any identification method. The class itself is an instance of `CachedDataSource<S, T>` which
requires
a `Cache<ID, String>` instance but implements most of the general code that involves caching and retrieving and only
requires the implementer to implement `StringCachedDataSource<ID>.fetchSupplier(id: ID) -> String` which just simply
returns new data when it is requested when not found in the cache.

### Example use

```kotlin
class MyDataSource : StringCachedDataSource<String>(/**cache**/) {
    override fun fetchSupplier(id: String): String {
        return File(id).readText()
    }
}

class MyOtherDataSource : StringCachedDataSource<String>(/**cache**/) {
    override fun fetchSupplier(id: String): String {
        return URL(id).readAllBytes()
    }
}
```

In addition to the main interfaces, `FileCachedDataSource<ID>` exists which retrieves files from the File system with
caching. This is not an abstract class and needs to be created with any ID type conforming to `FileIdentifier` which
takes returns a unique file identifier to use as the file name on disk for the cached items. This class is meant to
work with `FileSystemCache` as its cache to simplify implementation

### Example Use

```kotlin
data class MixedData(val type: String, val url: String) : FileIdentifier {
    override val id: String
        get() = "$type-$url"
}

fun main() {
    val source = FileCachedDataSource(FileSystemCache())
    source.getData(source)
}

```