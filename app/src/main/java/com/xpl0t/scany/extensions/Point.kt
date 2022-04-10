package com.xpl0t.scany.extensions

import org.opencv.core.Point

fun Point.multiply(value: Double): Point {
    return Point(x * value, y * value)
}

data class Quadrangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomRight: Point,
    val bottomLeft: Point
)

/**
 * Sort quadrangle edge points.
 *
 * The list of points must have a length of 4.
 */
fun Iterable<Point>.toQuadrangleMatrix(): Quadrangle {
    if (count() != 4) {
        throw IllegalArgumentException("Iterable must have exactly 4 items.")
    }

    val sortedBySum = sortedBy { it.x + it.y }
    val sortedByDiff = sortedBy { it.y - it.x }

    return Quadrangle(
        sortedBySum[0], sortedByDiff[0],
        sortedBySum[3], sortedByDiff[3]
    )
}
