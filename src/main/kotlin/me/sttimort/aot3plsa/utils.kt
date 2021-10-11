package me.sttimort.aot3plsa

import java.io.File
import kotlin.random.Random

fun getFileOrThrow(path: String) =
    File(path).takeIf { it.isFile } ?: throw IllegalArgumentException("Can't read file $path")

fun randomDoubleNoZero() = generateSequence { Random.nextDouble() }.first { it > 0 }