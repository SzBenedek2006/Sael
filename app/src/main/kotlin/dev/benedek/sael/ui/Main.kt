package dev.benedek.sael.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import dev.benedek.sael.R
import dev.benedek.sael.ui.theme.SaelTheme
import kotlin.math.pow

val miniPlayerContentHeight = 70.dp
val miniPlayerHeight = miniPlayerContentHeight // TODO: set perfect height
val sheetCornerRadius = 12.dp // 24.dp is the default in materialtheme


// I still don't understand why Compose doesn't have accessible properties like expanded ratio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getCurrentSheetCornerRadius(scaffoldState: BottomSheetScaffoldState, navbarHeight: Dp): Dp {
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
    return lerp(0.dp, sheetCornerRadius, progress.pow(1/4f))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )
    val density = LocalDensity.current
    var navBarHeightDp by remember { mutableStateOf(0.dp) }
    val sheetCornerRadius = getCurrentSheetCornerRadius(scaffoldState, navBarHeightDp)
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
                Player()
            },
            sheetShape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            sheetDragHandle = {
                // Already drawn inside Content(), don't draw here
            }
        ) { paddingValues ->
            Content(paddingValues)
        }
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
fun Player() {
    MiniPlayer()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer() {
    Box() {
        val width = 32.dp
        val height = 4.dp


        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painterResource(R.drawable.ic_launcher_background),
                contentDescription = "icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(miniPlayerContentHeight)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Title")
                Text("Artist")
            }
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({/*TODO()*/}) {
                    Icon(Icons.Outlined.PlayArrow, null)
                }
                IconButton({/*TODO()*/}) {
                    Icon(Icons.Outlined.Pause, null)
                }
            }
        }
        Box(
            Modifier.matchParentSize(),
            Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp) // private DragHandleVerticalPadding = 22.dp
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