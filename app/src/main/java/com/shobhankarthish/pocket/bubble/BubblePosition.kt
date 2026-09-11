package com.shobhankarthish.pocket.bubble

data class ScreenBox(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val usableLeft: Int get() = left
    val usableTop: Int get() = top
    val usableRight: Int get() = (width - right).coerceAtLeast(left)
    val usableBottom: Int get() = (height - bottom).coerceAtLeast(top)

    fun maxX(childWidth: Int): Int = (usableRight - childWidth).coerceAtLeast(usableLeft)
    fun maxY(childHeight: Int): Int = (usableBottom - childHeight).coerceAtLeast(usableTop)

    fun padded(pad: Int): ScreenBox = copy(
        left = left + pad,
        top = top + pad,
        right = right + pad,
        bottom = bottom + pad,
    )
}

data class BubblePlacement(
    val onLeft: Boolean,
    val yFraction: Float,
)

object BubblePosition {
    fun clamp(
        x: Int,
        y: Int,
        childW: Int,
        childH: Int,
        box: ScreenBox,
    ): Pair<Int, Int> {
        val nx = x.coerceIn(box.usableLeft, box.maxX(childW))
        val ny = y.coerceIn(box.usableTop, box.maxY(childH))
        return nx to ny
    }

    fun snapX(x: Int, childW: Int, box: ScreenBox): Pair<Int, Boolean> {
        val mid = x + childW / 2
        val onLeft = mid < box.width / 2
        val snapped = if (onLeft) box.usableLeft else box.maxX(childW)
        return snapped to onLeft
    }

    fun toPlacement(
        x: Int,
        y: Int,
        childW: Int,
        childH: Int,
        box: ScreenBox,
    ): BubblePlacement {
        val (_, onLeft) = snapX(x, childW, box)
        val minY = box.usableTop
        val span = (box.maxY(childH) - minY).coerceAtLeast(1)
        val yFraction = ((y - minY).toFloat() / span).coerceIn(0f, 1f)
        return BubblePlacement(onLeft, yFraction)
    }

    fun fromPlacement(
        placement: BubblePlacement,
        childW: Int,
        childH: Int,
        box: ScreenBox,
    ): Pair<Int, Int> {
        val x = if (placement.onLeft) box.usableLeft else box.maxX(childW)
        val minY = box.usableTop
        val y = minY + ((box.maxY(childH) - minY) * placement.yFraction.coerceIn(0f, 1f)).toInt()
        return clamp(x, y, childW, childH, box)
    }

    fun anchored(
        placement: BubblePlacement,
        childW: Int,
        childH: Int,
        box: ScreenBox,
    ): Pair<Int, Int> = fromPlacement(placement, childW, childH, box)
}
