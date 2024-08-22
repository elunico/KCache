package com.tom.caching

import java.io.File

/**
 * Implementation of [StringCachedDataSource] that retrieves data from files on the System and identifies them using
 * [FileIdentifier]
 */
class FileCachedDataSource(cache: Cache<FileIdentifier, String>) : StringCachedDataSource<FileIdentifier>(cache) {
    override fun fetchSupplier(identifier: FileIdentifier): String {
        return File(identifier.id).readText()
    }
}