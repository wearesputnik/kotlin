/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import java.io.Serializable
import java.util.*

typealias CommonArgumentCacheIdType = Int
typealias ClasspathArgumentCacheIdType = Array<Int>

interface CachedArgsInfo : Serializable {
    val currentCommonArgumentsCacheIds: Array<CommonArgumentCacheIdType>
    val currentClasspathArgumentsCacheIds: Array<ClasspathArgumentCacheIdType>
    val defaultCommonArgumentsCacheIds: Array<CommonArgumentCacheIdType>
    val defaultClasspathArgumentsCacheIds: Array<ClasspathArgumentCacheIdType>
    val dependencyClasspathCacheIds: Array<ClasspathArgumentCacheIdType>
}

data class CachedArgsInfoImpl(
    override val currentCommonArgumentsCacheIds: Array<CommonArgumentCacheIdType>,
    override val currentClasspathArgumentsCacheIds: Array<ClasspathArgumentCacheIdType>,
    override val defaultCommonArgumentsCacheIds: Array<CommonArgumentCacheIdType>,
    override val defaultClasspathArgumentsCacheIds: Array<ClasspathArgumentCacheIdType>,
    override val dependencyClasspathCacheIds: Array<ClasspathArgumentCacheIdType>
) : CachedArgsInfo {
    constructor(cachedArgsInfo: CachedArgsInfo) : this(
        arrayOf(*cachedArgsInfo.currentCommonArgumentsCacheIds),
        arrayOf(*cachedArgsInfo.currentClasspathArgumentsCacheIds),
        arrayOf(*cachedArgsInfo.defaultCommonArgumentsCacheIds),
        arrayOf(*cachedArgsInfo.defaultClasspathArgumentsCacheIds),
        arrayOf(*cachedArgsInfo.dependencyClasspathCacheIds)
    )
}

typealias CachedCompilerArgumentBySourceSet = Map<String, CachedArgsInfo>

/**
 * Creates deep copy in order to avoid holding links to Proxy objects created by gradle tooling api
 */
fun CachedCompilerArgumentBySourceSet.deepCopy(): CachedCompilerArgumentBySourceSet {
    val result = HashMap<String, CachedArgsInfo>()
    this.forEach { key, value -> result[key] = CachedArgsInfoImpl(value) }
    return result
}

data class CompilerArgumentsCachingMapper(
    val cacheIndexToCompilerArguments: MutableMap<Int, String> = mutableMapOf(),
    val compilerArgumentToCacheIndex: MutableMap<String, Int> = mutableMapOf()
) : Serializable