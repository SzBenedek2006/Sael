package dev.benedek.sael.ui.reusable

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp

enum class ElementAnchor { Left, Center, Right }

fun Modifier.animateElement(
    progress: Float,
    originalWidth: Float?,
    expandedWidth: Float?,
    scalingFactor: Float?,
    scaleFromCenter: Boolean = false,
    x: Float?,
    y: Float?,
    anchor: ElementAnchor = ElementAnchor.Center
): Modifier = this.composed {
    var absoluteOffset by remember { mutableStateOf(Offset.Zero) }
    var elementSize by remember { mutableStateOf(IntSize.Zero) }

    this.onGloballyPositioned { coordinates ->
        if (progress == 1f) {
            absoluteOffset = coordinates.positionInParent()
            Log.d(this.toString(), coordinates.positionInRoot().toString())
        }
    }.onSizeChanged{ size ->
        if (progress == 1f) {
            elementSize = size
        }
    }.graphicsLayer {

        // if expanded size is null, then scale factor is 1
        if (expandedWidth != null) {
            val scale = lerp(expandedWidth / (originalWidth ?: elementSize.width.toFloat()), 1f, progress) // scale -> how many times the original
            scaleX = scale
            scaleY = scale
        } else if (scalingFactor != null) {
            val scale = lerp(scalingFactor, 1f, progress) // scale -> how many times the original
            scaleX = scale
            scaleY = scale
        }

        // The image's 0;0 coordinates are used, instead of the middle
        if (!scaleFromCenter) transformOrigin = TransformOrigin(0f, 0f)

        if (x == null && y == null) return@graphicsLayer

        var startX = 0f
        var startY = 0f

        val _x = x ?: 0f
        val _y = y ?: 0f

        val pivotFactor = when (anchor) {
            ElementAnchor.Left -> 0f
            ElementAnchor.Center -> 0.5f
            ElementAnchor.Right -> 1f
        }


        // Actually the start will be the expanded size
        if (expandedWidth != null) {
            startX = _x - expandedWidth * pivotFactor
            startY = _y - expandedWidth / 2f
        } else {
            startX = _x - elementSize.width * pivotFactor - absoluteOffset.x
            startY = _y - elementSize.height / 2f - absoluteOffset.y
        }


        Log.d(this.toString(), "$startX, $startY")


        x?.let { translationX = lerp(startX, 0f, progress) }
        y?.let { translationY = lerp(startY, 0f, progress) }

    }
}