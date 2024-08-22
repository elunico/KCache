package com.tom.caching

/**
 * Used to differential the source of data. Either retrieved from a [Cache] or loaded directly ([FRESH])
 */
enum class LoadSource {
    CACHE, FRESH
}