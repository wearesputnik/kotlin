/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import java.io.File
import java.io.Serializable
import java.util.HashMap

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

interface IArgumentsCache<T> : Serializable {
    fun cacheArgument(argument: String): T
    fun obtainCacheIndex(argument: String): T?
    fun selectArgument(index: T): String?
    fun clearCache()
    val lastIndex: Int
}

class CommonCompilerArgumentCache(initialKey: Int = 0) : IArgumentsCache<CommonArgumentCacheIdType> {
    private var currentIndex = initialKey

    private val idToCompilerArgumentMap = mutableMapOf<CommonArgumentCacheIdType, String>()
    private val compilerArgumentToIdMap = mutableMapOf<String, CommonArgumentCacheIdType>()

    override fun cacheArgument(argument: String): CommonArgumentCacheIdType = compilerArgumentToIdMap[argument] ?: run {
        idToCompilerArgumentMap += currentIndex to argument
        compilerArgumentToIdMap += argument to currentIndex
        val result = currentIndex
        currentIndex++
        result
    }

    override fun obtainCacheIndex(argument: String): Int? = compilerArgumentToIdMap[argument]

    override fun selectArgument(index: Int): String? = idToCompilerArgumentMap[index]

    override fun clearCache() {
        idToCompilerArgumentMap.clear()
        compilerArgumentToIdMap.clear()
    }

    override val lastIndex: Int
        get() = currentIndex
}

class ClasspathCompilerArgumentCache(initialKey: Int = 0) : IArgumentsCache<ClasspathArgumentCacheIdType> {
    private var currentIndex = initialKey

    override val lastIndex: Int
        get() = currentIndex

    private val idToCompilerArgumentMap = mutableMapOf<CommonArgumentCacheIdType, String>()
    private val compilerArgumentToIdMap = mutableMapOf<String, CommonArgumentCacheIdType>()

    override fun cacheArgument(argument: String): ClasspathArgumentCacheIdType =
        argument.split(File.pathSeparator).map {
            compilerArgumentToIdMap[it] ?: run {
                idToCompilerArgumentMap += currentIndex to argument
                compilerArgumentToIdMap += argument to currentIndex
                val result = currentIndex
                currentIndex++
                result
            }
        }.toTypedArray()

    override fun obtainCacheIndex(argument: String): ClasspathArgumentCacheIdType? =
        argument.split(File.pathSeparator).mapNotNull { compilerArgumentToIdMap[it] }.toTypedArray().takeIf { it.isNotEmpty() }

    override fun selectArgument(index: ClasspathArgumentCacheIdType): String =
        index.mapNotNull { idToCompilerArgumentMap[it] }.joinToString(separator = File.pathSeparator)

    override fun clearCache() {
        idToCompilerArgumentMap.clear()
        compilerArgumentToIdMap.clear()
    }
}