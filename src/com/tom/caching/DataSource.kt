package com.tom.caching

import kotlin.jvm.Throws

/**
 * Represents a source of data
 *
 * Data can be loaded from any place. Memory, disk, network, etc. For expensive data retrieval implementations,
 * consider using [CachedDataSource] which allows for data to be cached
 *
 * @see [Cache]
 * @see [CachedDataSource]
 */
interface DataSource<S, T> {
    @Throws(Exception::class)
    fun getData(source: S, onRetrieve: (source: LoadSource, item: T) -> Unit): T?
}