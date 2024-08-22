package com.tom.caching

/**
 * Represents a source of data with caching support
 *
 * In general, implementers should implement [tryFromCache] and [fetchFreshData] but callers should only call [getData]
 *
 * data is first attempted to be loaded from a cache using [tryFromCache]. If the data
 * exists in the cache, and is otherwise valid, it is returned from the cache and [LoadSource] where applicable,
 * is set to [LoadSource.CACHE]
 *
 * The attempt to return data from the cache cannot return cached data because it does not exist, then data is loaded
 * using [fetchFreshData]. If this fails and returns null, then the CachedDataSource [getData] will also return null
 *
 * @see [Cache]
 * @see [DataSource]
 */
interface CachedDataSource<S, T> : DataSource<S, T> {
    val cache: Cache<S, T>

    /**
     * Retrieves the data from [cache] if [cache] is not null and if [Cache.getItem] returns non-null
     *
     * Implementers should implement this method and [fetchFreshData]. Callers should use [getData]
     *
     * @throws CacheException if [Cache.getItem] does
     */
    @Throws(CacheException::class)
    fun tryFromCache(source: S, onRetrieve: (item: T) -> Unit): T?

    /**
     * Retrieves the data from their original source, returning null if it fails to retrieve the data
     *
     * Implementers should implement this method and [tryFromCache]. Callers should use [getData]
     */
    fun fetchFreshData(source: S, onRetrieve: (item: T) -> Unit): T?

    /**
     * Callers should use this method to retrieve data from the DataSource. It automatically handlers first
     * [tryFromCache] to attempt to load the data from cached values and then will use [fetchFreshData] if necessary
     * if the cache does not return data
     *
     * @throws CacheException if [tryFromCache] does
     */
    @Throws(CacheException::class)
    override fun getData(source: S, onRetrieve: (source: LoadSource, item: T) -> Unit): T? {
        val cachedValue = tryFromCache(source) { onRetrieve(LoadSource.CACHE, it) }
        if (cachedValue != null)
            return cachedValue

        val freshData = fetchFreshData(source) { onRetrieve(LoadSource.FRESH, it) }
        if (freshData != null)
            cache.cacheItem(source, freshData)

        return freshData
    }
}