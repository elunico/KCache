package com.tom.caching

import java.io.File

/**
 * Implementation of [StringCachedDataSource] that retrieves data from files on the System and identifies them using
 * [FileIdentifier]
 */
open class FileCachedDataSource(cache: Cache<FileIdentifier, String>) : StringCachedDataSource<FileIdentifier>(cache) {
    override fun fetchSupplier(identifier: FileIdentifier): String {
        return File(identifier.id).readText()
    }

    override fun onCacheRetrieve(cachedValue: String) {
    }

    override fun onDataFetched(item: String) {
    }
}