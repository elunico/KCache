package com.tom.caching

import java.nio.file.Path
import kotlin.jvm.Throws
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Represents the configuration parameters of a [Cache] object.
 */
data class CacheConfiguration<ID, T>(val maxAge: Duration = 1.days)

/**
 * Represents the result of caching an item using [Cache]
 * [ID] should be the type that the [Cache] can use to identify items so that the cache encounters equal objects of type
 * [ID], it returns the cached data
 * [T] is the type of the data being cached.
 * This is frequently Strings being cached using the FileSystem and a [FileSystemCache] class exists for convenience
 */
data class CacheResult<ID>(val identifier: ID, val overwrote: Boolean)

/**
 * Represents an exceptional Cache problem
 */
class CacheException(cause: String): Exception(cause)

/**
 * Represents an unrecoverable Cache problem that means the [Cache] instance is useless
 */
class CacheError(cause: String): Exception(cause)

/**
 * Extremely general interface for implementing Caching of objects of type [T]. A [Cache] type can accept and
 * identifier and content and perform some sort of caching (in memory, on disk, etc.) using [cacheItem] and then
 * retrieve that data later when calling [getItem].
 *
 * [ID] should be the type that the [Cache] can use to identify items so that the cache encounters equal objects of type
 * [ID], it returns the cached data
 * [T] is the type of the data being cached.
 * This is frequently Strings being cached using the FileSystem and a [FileSystemCache] class exists for convenience
 *
 * The cache functions of the [ID] type and values therein so conflicting objects of this type for
 * different content will cause inconsistencies in the cache. Ensure that there are sufficiently many values for the
 * type used as [ID]
 *
 * Both [ID] and [T] must be compatible with the method of caching. Since the interface is so general, the type bound
 * of [Serializable] is not imposed on the types, since implementation may not require it, however, in general the types
 * must be compatible with whatever method of caching they use.
 */
interface Cache<ID, T> {
    /**
     * Return this Cache's configuration settings
     */
    val configuration: CacheConfiguration<ID, T>

    /**
     * Cache the [content] using the [identifier] provided
     * Can throw [CacheException] if caching fails or [CacheError] if caching is impossible or the class is unusable
     * Implementers do not need to throw either exception type
     * @see [CacheResult]
     */
    @Throws(CacheException::class)
    fun cacheItem(identifier: ID, content: T): CacheResult<ID>

    /**
     * Retrieve the content associated with the given [identifier] or null if no such identifier was cached
     * If retrieval fails [CacheException] can be thrown but SHOULD NOT BE thrown for successful attempts at retreival
     * that do not yield extant cache values
     */
    @Throws(CacheException::class)
    fun getItem(identifier: ID): T?

    /**
     * Return the current size of the cache or null if the given implementation does not support size querying
     */
    val size: Int?

    /**
     * Clear the cache. Can throw [CacheException] if clearing fails
     */
    @Throws(CacheException::class)
    fun clear()
}