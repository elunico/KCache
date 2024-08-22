package com.tom.caching

import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.file.Paths
import java.time.Instant
import kotlin.time.DurationUnit

/**
 * Partial implementation of [Cache] for use with the filesystem. Use of this abstract class involves a few things
 * 1. Implement the [onItemRetrieval] method to handle cache retrievals
 * 2. Use String identifiers with the inline class [FileIdentifier] - In general this cache is going to cache each
 *    content item given to it as a separate file in a single directory. This means, like any user of [Cache] the
 *    strings given for [getItem] and [cacheItem] in the first argument must be unique to each piece of content being
 *    cached. In order to function, the strings given as ID do not directly translate to filenames, but are used to
 *    build them and must be unique
 * 3. Have permission to write in the directory named by [cacheStore]
 */
open class FileSystemCache<ID>(
    override val configuration: CacheConfiguration<ID, String> = CacheConfiguration(),
    cacheRoot: File = Paths.get(".cache").toFile()
) : Cache<ID, String> where ID : FileIdentifier {

    companion object {
        @JvmStatic
        private var currentInstanceNumber: Int = 1
            get() {
                synchronized(FileSystemCache::class.java) {
                    field++
                    return field
                }
            }
    }

    private val cacheInstanceID: String = "FileSystemCache-Impl$currentInstanceNumber"

    /**
     * Returns the storage location of the cache in an immutable way
     */
    val storageLocation get() = cacheStoreDirectory.absolutePath

    private val cacheStoreDirectory: File = Paths.get(cacheRoot.absolutePath, cacheInstanceID).toFile()

    init {
        if (!cacheStoreDirectory.exists()) {
            cacheStoreDirectory.mkdirs()
        }
        if (!cacheStoreDirectory.isDirectory) {
            throw IOException("Cache Store location [$cacheStoreDirectory] is not a directory path")
        }
    }

    /**
     * Protects filesystem from illegal characters
     */
    protected fun String.escapedForFilesystemPath(): String {
        return URLEncoder.encode(this, "utf-8").replace("%", "+")
    }

    /**
     * Determines the filepath for a given filename identifier String.
     */
    protected fun fileFromName(identifier: ID): File {
        return Paths.get(cacheStoreDirectory.absolutePath, identifier.id.escapedForFilesystemPath())
            .toAbsolutePath().toFile()
    }

    // can be overridden but does not require implementation
    open fun onItemRetrieval(source: LoadSource, id: String, content: String?) {}
    // abstract fun identify(content: T): ID // inherited from Cache

    /**
     * Caches the file [content] using [filename] as an identifier
     * @see [Cache.cacheItem]
     */
    final override fun cacheItem(filename: ID, content: String): CacheResult<ID> {
        val file = fileFromName(filename)
        val result = CacheResult(filename, file.exists())
        file.writeText(content)
        return result
    }

    /**
     * Attempts to retrieve the content associated with [filename] id in the cache
     * @see [Cache.getItem]
     */
    final override fun getItem(filename: ID): String? {
        return try {
            val file = fileFromName(filename)
            if (!file.exists()) {
                null
            } else {
                val now = Instant.now().toEpochMilli()
                val lastRightMillis = file.lastModified()
                val maximum = configuration.maxAge.toLong(DurationUnit.MILLISECONDS)
                if ((now - lastRightMillis) >= maximum) {
                    null
                } else {
                    file.readText().also {
                        onItemRetrieval(LoadSource.CACHE, filename.id, it)
                    }
                }
            }
        } catch (i: IOException) {
            null
        }
    }

    override val size: Int?
        get() = null

    final override fun clear() {
        for (item in cacheStoreDirectory.listFiles() ?: throw CacheException("Could not list directory to clear")) {
            item.deleteRecursively()
        }
    }
}

