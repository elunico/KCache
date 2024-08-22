package com.tom.caching

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
    /**
     * Gets data from a generic source of Data
     */
    fun getData(source: S): T?

    /**
     * Called if the data is successfully retrieved from [getData]
     */
    fun onDataFetched(item: T)
}