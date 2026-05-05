package dev.benedek.sael.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import dev.benedek.sael.R
import dev.benedek.sael.ui.reusable.ElementAnchor
import dev.benedek.sael.ui.reusable.animateElement
import dev.benedek.sael.ui.theme.SaelTheme
import kotlin.math.pow

val miniPlayerContentHeight = 70.dp
val miniPlayerHeight = miniPlayerContentHeight // TODO: set perfect height
val sheetCornerRadius = 12.dp // 24.dp is the default in materialtheme
var compositeLandscape = false


// I still don't understand why Compose doesn't have accessible properties like expanded ratio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetExpandedRatio(scaffoldState: BottomSheetScaffoldState, navbarHeight: Dp): Float {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val navbarHeightPx = with(density) { navbarHeight.toPx() }
    val playerHeightPx = with(density) { miniPlayerHeight.toPx() }
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
    return lerp(0.dp, sheetCornerRadius, progress.pow(1/4f))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val windowInfo = LocalWindowInfo.current
    compositeLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )
    val density = LocalDensity.current
    var navBarHeightDp by remember { mutableStateOf(0.dp) }
    val progress = bottomSheetExpandedRatio(scaffoldState, navBarHeightDp)
    val sheetCornerRadius = getCurrentSheetCornerRadius(progress)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
                sheetPeekHeight = navBarHeightDp + miniPlayerHeight,
            topBar = {
                TopAppBar(
                    title = { Text("Sael") }
                )
            },
            sheetContent = {
                Player(progress)
            },
            sheetShape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            sheetDragHandle = {
                // Already drawn inside Content(), don't draw here
            },
            sheetMaxWidth = Float.POSITIVE_INFINITY.dp
        ) { paddingValues ->
            Content(paddingValues)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .animateElement(
                    progress,
                    null,
                    null,
                    null,
                    false,
                    null,
                    with(density) { navBarHeightDp.toPx() } + windowInfo.containerSize.height / 2f
                )
        ) {
            NavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coordinates -> // Access the coordinates of starting pos of the bar to get the height
                        navBarHeightDp = with(density) { coordinates.size.height.toDp() }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(paddingValues: PaddingValues) {
    Surface(
    ) {
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
fun Player(progress: Float) {
    Box {
        val width = 32.dp
        val height = 4.dp
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current


        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            val coverArtX = if (compositeLandscape) windowInfo.containerSize.width * 1 / 4f else windowInfo.containerSize.width / 2f
            val coverArtY = if (compositeLandscape) windowInfo.containerSize.height / 2f else windowInfo.containerSize.height * 1 / 3f
            val height = if (compositeLandscape) windowInfo.containerSize.height * (4/7f) else windowInfo.containerSize.width * (3/4f)
            Box(
                modifier = Modifier
                    .size(miniPlayerContentHeight)
                    .animateElement(
                        progress,
                        with(density) { miniPlayerContentHeight.toPx() }, // it's 1:1 so height is ok
                        height,
                        null,
                        false,
                        coverArtX,
                        coverArtY
                    )
            ) {
                Image(
                    painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(miniPlayerContentHeight)
                )
            }

            val titleX = if (compositeLandscape) windowInfo.containerSize.width * 1 / 2f else windowInfo.containerSize.width * 1 / 8f
            val titleY = if (compositeLandscape) windowInfo.containerSize.height * 3 / 10f else windowInfo.containerSize.height * 6 / 11f

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(miniPlayerHeight)
                    .animateElement(
                        progress,
                        null,
                        null,
                        2f,
                        false,
                        titleX,
                        titleY,
                        ElementAnchor.Left
                    )//.border(1.dp, Color.Black)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        // TODO: Does this cause unrealistically large lag or not?
                        .padding(start = lerp(0.dp, 10.dp, progress), top = 10.dp, end = 10.dp, bottom = 10.dp)
                ) {
                    var typography = MaterialTheme.typography.bodyLarge
                    Text(
                        text = "Title",
                        modifier = Modifier,
                        style = typography,
                        fontWeight = FontWeight.Bold,

                        )
                    typography = MaterialTheme.typography.bodySmall
                    Text(
                        text = "Artist",
                        modifier = Modifier,
                        style = typography,
                        fontWeight = typography.fontWeight,
                        color = LocalContentColor.current.copy(0.7f)
                    )
                }
            }

            val controlsX = if (compositeLandscape) windowInfo.containerSize.width * 3 / 4f else windowInfo.containerSize.width * 1 / 2f
            val controlsY = if (compositeLandscape) windowInfo.containerSize.height * 7 / 10f else windowInfo.containerSize.height * 4 / 5f




            Box(modifier = Modifier
                .offset(40.dp) // The size of an IconButton maybe (source: debugger)
                .padding(10.dp)
                .animateElement(
                    progress,
                    null,
                    null,
                    null,
                    true,
                    controlsX,
                    controlsY
                )) {
                IconButton(
                    onClick = {/*TODO()*/},
                    modifier = Modifier.graphicsLayer { alpha = (progress).pow(5f) },
                    enabled = progress > 0.2f
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, null)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Implement please", Toast.LENGTH_SHORT)
                                .show()
                        },
                        modifier = Modifier.graphicsLayer { alpha = (1 - progress).pow(5f) },
                        enabled = progress < 0.8f
                        ) {
                        Icon(Icons.Outlined.SkipPrevious, null)
                    }
                    IconButton(
                        onClick = {/*TODO()*/},
                        Modifier.animateElement(
                            progress,
                            null,
                            null,
                            2f,
                            true,
                            null,
                            null
                        )
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, Modifier.scale(1.4f))
                    }
                    IconButton(
                        onClick = {/*TODO()*/},
                        modifier = Modifier.graphicsLayer { alpha = (1 - progress).pow(5f) },
                        enabled = progress < 0.8f
                        ) {
                        Icon(Icons.Outlined.SkipNext, null)
                    }
                }
            }
        }


        val sliderX = if (compositeLandscape) windowInfo.containerSize.width * 3 / 4f else 0
        val sliderY = if (compositeLandscape) windowInfo.containerSize.height * 7 / 10f else windowInfo.containerSize.height * 7 / 11f
        val sliderHorizontalPadding = with(density) { (windowInfo.containerSize.width * (1/8f)).toDp() }

        val sliderState = SliderState()
        // Slider + info
        Box(
            Modifier
                .offset {
                    IntOffset(sliderX.toInt(), sliderY.toInt())
                }

        ) {
            Column() {
                Slider(
                    state = sliderState,
                    modifier = Modifier.padding(horizontal = sliderHorizontalPadding)
                )
                Row() {

                }
            }
        }

        val topPaddingDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            Modifier
                .matchParentSize()
                .padding(top = lerp(topPaddingDp, 0.dp, progress))
            ,
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
fun MiniPlayer() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayer() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Mao")
    }
}



@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MainPreview() {
    SaelTheme {
        Main()
    }
}


@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlayerPreview() {
    SaelTheme {
        Surface() {
            Player(0.0f)
        }
    }
}