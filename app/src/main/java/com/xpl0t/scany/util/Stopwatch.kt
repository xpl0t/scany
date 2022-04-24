package com.xpl0t.scany.util

import java.lang.System.currentTimeMillis

/**
 * Measure time.
 */
class Stopwatch {

    private var startTime: Long? = null

    /**
     * Start the stopwatch.
     */
    fun start() {
        startTime = currentTimeMillis()
    }

    /**
     * Stop the stopwatch & return the elapsed time in milliseconds.
     * If it has never been started, returns 0.
     *
     * @return Elapsed time in milliseconds, or 0 if it was never started.
     */
    fun stop(): Long {
        if (startTime == null) {
            return 0
        }

        return currentTimeMillis() - startTime!!
    }

}