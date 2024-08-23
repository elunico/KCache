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
 * Important: the callback methods [onCacheRetrieve] and [onDataFetched] should *ONLY* be called from [getData] based
 * on the return value of the respective data getting methods. DO NOT CALL [onCacheRetrieve] in [tryFromCache] nor
 * [onDataFetched] in [fetchFreshData]. This is done to maintain consist behavior with [DataSource]
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
     * Do not call [onCacheRetrieve] in this method. That method should only be called by [getData]
     *
     * @throws CacheException if [Cache.getItem] does
     */
    @Throws(CacheException::class)
    fun tryFromCache(source: S): T?

    /**
     * Retrieves the data from their original source, returning null if it fails to retrieve the data
     *
     * Implementers should implement this method and [tryFromCache]. Callers should use [getData]
     *
     * Do not call [onDataFetched] in this method. It should be called only in [getData]
     */
    fun fetchFreshData(source: S): T?

    /**
     * Called if the cache returns a cached value with that retrieved value
     */
    fun onCacheRetrieve(cachedValue: T)

    /**
     * Callers should use this method to retrieve data from the DataSource. It automatically handles first
     * [tryFromCache] to attempt to load the data from cached values and then will use [fetchFreshData] if necessary
     * if the cache does not return data
     *
     * @throws CacheException if [tryFromCache] does
     */
    @Throws(CacheException::class)
    override fun getData(source: S): T? {
        val cachedValue = tryFromCache(source)
        if (cachedValue != null) {
            onCacheRetrieve(cachedValue)
            return cachedValue
        }

        val freshData = fetchFreshData(source)
        if (freshData != null) {
            onDataFetched(freshData)
            cache.cacheItem(source, freshData)
        }

        return freshData
    }
}