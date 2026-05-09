package dev.benedek.sael.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import dev.benedek.sael.R
import dev.benedek.sael.ui.reusable.FlatEdgeLinearProgressIndicator
import dev.benedek.sael.ui.reusable.disableTouch
import dev.benedek.sael.ui.theme.SaelTheme
import kotlin.math.pow

@Stable
object MiniPlayerDimensions {
    val height = Dp(70f)
    val cornerRadius = Dp(12f)
}
val LocalIsLandscape = compositionLocalOf { false }

var navBarHeightDp by mutableStateOf(0.dp)

var sheetY by mutableFloatStateOf(0f)





// I still don't understand why Compose doesn't have accessible properties like expanded ratio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetExpandedRatio(scaffoldState: BottomSheetScaffoldState, navbarHeight: Dp): Float {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val navbarHeightPx = with(density) { navbarHeight.toPx() }
    val playerHeightPx = with(density) { MiniPlayerDimensions.height.toPx() }
    val miniStateHeight = windowInfo.containerSize.height - navbarHeightPx - playerHeightPx


    val progress by remember(scaffoldState, miniStateHeight) {
        derivedStateOf {
            try {
                val currentOffset = scaffoldState.bottomSheetState.requireOffset()
                (currentOffset / miniStateHeight).coerceIn(0f, 1f)
            } catch (e: IllegalStateException) {
                1f
            }

        }
    }
    return progress
}

@Composable
fun getCurrentSheetCornerRadius(progress: Float): Dp {
    return lerp(0.dp, MiniPlayerDimensions.cornerRadius, progress.pow(1/4f))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    sheetY = with(LocalDensity.current) { navBarHeightDp.toPx() + MiniPlayerDimensions.height.toPx() }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )
    val density = LocalDensity.current
    val progress = bottomSheetExpandedRatio(scaffoldState, navBarHeightDp)
    val sheetCornerRadius = getCurrentSheetCornerRadius(progress)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
                sheetPeekHeight = navBarHeightDp + MiniPlayerDimensions.height,
            topBar = {
                TopAppBar(
                    title = { Text("Sael") }
                )
            },
            sheetContent = {
                PlayerSheet(progress)
            },
            sheetShape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            sheetDragHandle = {
                // Already drawn inside Content(), don't draw here
            },
            sheetMaxWidth = Float.POSITIVE_INFINITY.dp
        ) { paddingValues ->
            Content(paddingValues)
        }

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates -> // Access the coordinates of starting pos of the bar to get the height
                    navBarHeightDp = with(density) { coordinates.size.height.toDp() }
                    Log.d("navBarHeightDp", navBarHeightDp.toString())
                }
                .graphicsLayer {
                    translationY = lerp(size.height, 0f, progress)
                }
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Outlined.LibraryMusic,
                        contentDescription = "Music Library"
                    )
                },
                selected = true,
                onClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(paddingValues: PaddingValues) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Main",
                fontSize = 100.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheet(openFraction: Float, modifier: Modifier = Modifier) {
    val compositeLandscape = LocalIsLandscape.current
    val isPortrait = !compositeLandscape

    Box {

        Player(openFraction, modifier)
        val width = 32.dp
        val height = 4.dp

        MiniPlayer(openFraction, isPortrait)


        // Drag handle
        val topPaddingDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            Modifier
                .matchParentSize()
                .padding(top = lerp(topPaddingDp, 0.dp, openFraction)),
            Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp) // private DragHandleVerticalPadding = 22.dp
                ,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Box(Modifier.size(width = width, height = height))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Player(openFraction: Float, modifier: Modifier = Modifier) {
    val compositeLandscape = LocalIsLandscape.current
    val isPortrait = !compositeLandscape

    val outerPadding = 24.dp
    val innerPadding = 16.dp

    Box(modifier.fillMaxSize()) {
        if (isPortrait) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(outerPadding).safeDrawingPadding()
                    .alpha((1 - openFraction).pow(2))
            ) {
                PlayerImage(
                    openFraction,
                    Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                        .padding(bottom = innerPadding)
                )
                PlayerControls(
                    openFraction,
                    Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                        .padding(top = innerPadding)
                )
            }
        } else {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(outerPadding).safeDrawingPadding()
                    .alpha((1 - openFraction).pow(2))
            ) {
                PlayerImage(
                    openFraction,
                    Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
                        .padding(end = innerPadding)

                )
                PlayerControls(
                    openFraction,
                    Modifier
                        .weight(2 / 3f)
                        .fillMaxHeight()
                        .padding(start = innerPadding)
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(openFraction: Float, isPortrait: Boolean) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().alpha(openFraction.pow(50))) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            CoverImage(R.drawable.test, Modifier.size(MiniPlayerDimensions.height - ProgressIndicatorDefaults.LinearTrackStopIndicatorSize))
            TitleArtist(Modifier.weight(1f).padding(start = 12.dp))
            IconButton(
                { Toast.makeText(context, "click", Toast.LENGTH_SHORT).show() },
                enabled = openFraction > 0.1f
            ) {
                Icon(Icons.Outlined.FavoriteBorder, null)
            }
            if (isPortrait) {
                IconButton({}, Modifier.padding(6.dp).size(32.dp), enabled = openFraction > 0.1f) {
                    Icon(Icons.Outlined.PlayArrow, null, Modifier.fillMaxSize())
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton({}, enabled = openFraction > 0.1f) {
                        Icon(Icons.Outlined.SkipPrevious, null)
                    }
                    IconButton({}, enabled = openFraction > 0.1f) {
                        Icon(Icons.Outlined.PlayArrow, null)
                    }
                    IconButton({}, enabled = openFraction > 0.1f) {
                        Icon(Icons.Outlined.SkipPrevious, null)
                    }
                }
            }
        }
        FlatEdgeLinearProgressIndicator({0.5f}, Modifier.fillMaxWidth()) // TODO: Custom implementation with square edges.
    }
}

@Composable
fun PlayerImage(openFraction: Float, modifier: Modifier = Modifier) {
    Box(modifier, Alignment.Center) {
        Box(
            Modifier
                .aspectRatio(1f)
                .fillMaxSize()
        ) {
            CoverImage(
                R.drawable.test,
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(MiniPlayerDimensions.cornerRadius))
            )
        }
    }
}

@Composable
fun CoverImage(@DrawableRes id: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, id)

    when(drawable) {
        is VectorDrawable,
        is VectorDrawableCompat -> {
            Image(
                imageVector = ImageVector.vectorResource(id),
                contentDescription = "icon",
                contentScale = ContentScale.FillBounds,
                modifier = modifier
            )
        }
        is BitmapDrawable -> {
            Image(
                ImageBitmap.imageResource(id),
                contentDescription = "icon",
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.High,
                modifier = modifier
            )
        }
    }

}

@Composable
fun PlayerControls(openFraction: Float, modifier: Modifier = Modifier) {
    Column(
        modifier,
        Arrangement.Bottom,
        Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding( top = 48.dp))
        Column(Modifier.fillMaxWidth().weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) {
                TitleArtist(Modifier.weight(1f))
                Row(Modifier, Arrangement.End) {
                    IconButton(
                        onClick = {/*TODO()*/ }
                    ) {
                        Icon(Icons.Outlined.MoreVert, null)
                    }
                    IconButton(
                        onClick = {/*TODO()*/ }
                    ) {
                        Icon(Icons.Outlined.Speed, null)
                    }
                    IconButton(
                        onClick = {/*TODO()*/ }
                    ) {
                        Icon(Icons.Outlined.FavoriteBorder, null)
                    }
                }

            }

            @OptIn(ExperimentalMaterial3Api::class)
            Column(Modifier.fillMaxWidth()) {
                val sliderState = SliderState(0.5f, 0)
                Slider(
                    state = sliderState,
                    track = {
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            drawStopIndicator = null
                        )
                    }
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("0:00")
                    Text("12:00")
                }
            }
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,) {
                IconButton(
                    onClick = {/*TODO()*/},
                    modifier = Modifier.padding(6.dp).size(48.dp),
                    enabled = openFraction < 0.8f
                ) {
                    Icon(Icons.Rounded.SkipPrevious, null, Modifier.fillMaxSize())
                }
                IconButton(
                    onClick = {/*TODO()*/ },
                    modifier = Modifier.padding(6.dp).size(72.dp)
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, Modifier.fillMaxSize())
                }
                IconButton(
                    onClick = {/*TODO()*/ },
                    modifier = Modifier.padding(6.dp).size(48.dp),
                    enabled = openFraction < 0.8f
                ) {
                    Icon(Icons.Rounded.SkipNext, null, Modifier.fillMaxSize())
                }
            }
        }


        Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {/*TODO()*/ }
            ) {
                Icon(Icons.Outlined.Menu, null)
            }
            IconButton(
                onClick = {/*TODO()*/ }
            ) {
                Icon(Icons.Outlined.Lyrics, null)
            }
        }
    }
}


@Composable
fun TitleArtist(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        var typography = MaterialTheme.typography.bodyLarge
        Text(
            text = "Title",
            style = typography,
            fontWeight = FontWeight.Bold,

            )
        typography = MaterialTheme.typography.bodySmall
        Text(
            text = "Artist",
            style = typography,
            fontWeight = typography.fontWeight,
            color = LocalContentColor.current.copy(0.7f)
        )
    }
}




@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MainPreview() {
    SaelTheme {
        Main()
    }
}


//@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
//@Composable
//fun PlayerSheetPreview() {
//    SaelTheme {
//        Surface() {
//            PlayerSheet(0.0f)
//        }
//    }
//}



@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlayerPreview() {
    SaelTheme {
        Surface() {
            Player(0.0f)
        }
    }
}