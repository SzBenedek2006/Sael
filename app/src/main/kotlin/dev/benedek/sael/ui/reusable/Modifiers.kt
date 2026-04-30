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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp
import dev.benedek.sael.ui.miniPlayerContentHeight

fun Modifier.animatePlayerElement(
    progress: Float,
    miniSize: Float?,
    expandedSize: Float?,
    x: Float,
    y: Float
): Modifier = this.composed {
    var absoluteOffset by remember { mutableStateOf(Offset.Zero) }
    var elementSize by remember { mutableStateOf(IntSize.Zero) }

    this.onGloballyPositioned { coordinates ->
        if (progress == 1f)
        absoluteOffset = coordinates.positionInParent()
        Log.d(this.toString(), coordinates.positionInRoot().toString())
    }.onSizeChanged{ size ->
        elementSize = size
    }.graphicsLayer {
        // if expanded size is null, then scale factor is 1
        if (expandedSize != null && miniSize != null) {
            val scale = lerp(expandedSize / miniSize, 1f, progress) // scale -> how many times the original
            scaleX = scale
            scaleY = scale
        }

        // The image's 0;0 coordinates are used, instead of the middle
        transformOrigin = TransformOrigin(0f, 0f)

        var startX = 0f
        var startY = 0f

        // Actually the start will be the expanded size
        if (expandedSize != null) {
            startX = x - expandedSize / 2f
            startY = y - expandedSize / 2f
        } else {
            startX = x - elementSize.width / 2f - absoluteOffset.x
            startY = y - elementSize.height / 2f - absoluteOffset.y
        }


        Log.d(this.toString(), "$startX, $startY")


        translationX = lerp(startX, 0f, progress)
        translationY = lerp(startY, 0f, progress)
    }
}