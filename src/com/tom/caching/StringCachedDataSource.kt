package com.tom.caching

/**
 * Simple implementation of [CachedDataSource] that handles general caching String data using a generic Identifier of type
 * [ID]
 *
 * The implementer of this class needs to only implement the [fetchSupplier] method which can often be done in one line.
 * It must return the freshly fetched String that corresponds to the given [String] identifier
 */
abstract class StringCachedDataSource<ID>(override val cache: Cache<ID, String>) :
    CachedDataSource<ID, String> {
    override fun tryFromCache(source: ID, onRetrieve: (item: String) -> Unit): String? {
        val cachedItem = cache.getItem(source)
        if (cachedItem != null) {
            onRetrieve(cachedItem)
            return cachedItem
        }
        return null
    }

    /**
     * Return the String corresponding to the [identifier] from the actual source of data. Usually implementable in
     * 1 line
     * @see [FileCachedDataSource] for an example
     */
    abstract fun fetchSupplier(identifier: ID): String

    override fun fetchFreshData(source: ID, onRetrieve: (item: String) -> Unit): String {
        val data = fetchSupplier(source)
        cache.cacheItem(source, data)
        onRetrieve(data)
        return data
    }
}

