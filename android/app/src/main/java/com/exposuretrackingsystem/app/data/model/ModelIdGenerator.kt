package com.exposuretrackingsystem.app.data.model

import java.util.concurrent.atomic.AtomicLong

internal object ModelIdGenerator {
    private val nextId = AtomicLong(1L)

    fun next(): Long = nextId.getAndIncrement()
}