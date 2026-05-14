@file:OptIn(ExperimentalFoundationApi::class)

package dev.benedek.sael.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.benedek.sael.data.model.Track
import dev.benedek.sael.ui.reusable.FlatEdgeLinearProgressIndicator
import dev.benedek.sael.ui.theme.SaelTheme
import dev.benedek.sael.utils.formatDuration
import dev.benedek.sael.viewmodels.MainViewModel
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import kotlin.math.pow

@Stable
object MiniPlayerDimensions {
    val height = Dp(70f)
    val cornerRadius = Dp(12f)
}
val LocalIsLandscape = compositionLocalOf { false }



// I still don't understand why Compose doesn't have accessible properties like expanded ratio.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun bottomSheetExpandedRatio(
    scaffoldState: BottomSheetScaffoldState<SheetValue>,
    navbarHeight: Dp
): () -> Float {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val navbarHeightPx = with(density) { navbarHeight.toPx() }
    val playerHeightPx = with(density) { MiniPlayerDimensions.height.toPx() }
    val miniStateHeight = windowInfo.containerSize.height - navbarHeightPx - playerHeightPx


    return remember(scaffoldState, miniStateHeight) {
        {
            try {
                val currentOffset = scaffoldState.sheetState.requireOffset()
                (currentOffset / miniStateHeight).coerceIn(0f, 1f)
            } catch (e: IllegalStateException) {
                1f
            }

        }
    }
}

@Composable
fun getCurrentSheetCornerRadius(progress: () -> Float): Dp {
    return lerp(0.dp, MiniPlayerDimensions.cornerRadius, progress().pow(1/4f))
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Main() {
    val viewModel: MainViewModel = viewModel()
    val bottomWindowInset = with(LocalDensity.current) { WindowInsetsCompat.toWindowInsetsCompat(LocalView.current.rootWindowInsets!!).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom.toDp() }

    var isDismissable by remember { mutableStateOf(true) }
    val density = LocalDensity.current


    val bottomSheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        defineValues = {
            // Bottom sheet height is 100 dp.
            if (isDismissable) SheetValue.Hidden at height(0)
            // Bottom sheet offset is 60%, meaning it takes 40% of the screen.
            SheetValue.PartiallyExpanded at height(navigationBarHeight + bottomWindowInset + MiniPlayerDimensions.height)
            // Bottom sheet height is equal to its content height.
            SheetValue.Expanded at height(100)
        },
        // The default { 0f } values are trash, IDK why they did that.
        positionalThreshold = {distance -> distance * 0.5f},
        velocityThreshold = {with(density) {150.dp.toPx()} },
    )



    LaunchedEffect(navigationBarHeight, bottomWindowInset, bottomSheetState.currentValue) {
        isDismissable = bottomSheetState.currentValue != SheetValue.Expanded
        bottomSheetState.refreshValues()
        if (bottomSheetState.currentValue == SheetValue.Hidden) viewModel.stop()
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        sheetState = bottomSheetState
    )
    val progress = bottomSheetExpandedRatio(scaffoldState, navigationBarHeight + bottomWindowInset)
    val sheetCornerRadius = getCurrentSheetCornerRadius(progress)
    val context = LocalContext.current

    val tracks by viewModel.tracks.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.scanTracks(context)
    }

    // If the currently playing track is dismissed from somewhere else.
    LaunchedEffect(viewModel.track) {
        if (viewModel.track == null) scaffoldState.sheetState.animateTo(SheetValue.Hidden)
        else {
            if (scaffoldState.sheetState.currentValue == SheetValue.Hidden) {
                scaffoldState.sheetState.animateTo(SheetValue.PartiallyExpanded)
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            //sheetPeekHeight = navBarHeightDp + MiniPlayerDimensions.height,
            topBar = {
                TopAppBar(
                    title = { Text("Sael") }
                )
            },
            sheetContent = {
                PlayerSheet(
                    progress,
                    Modifier,
                    viewModel.track,
                    viewModel::isPlaying,
                    viewModel::pause,
                    viewModel::resume,
                    viewModel::stop,
                    viewModel::currentPositionMs
                )
            },
            sheetShape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            sheetDragHandle = {
                // Already drawn inside Content(), don't draw here
            },
            sheetMaxWidth = Float.POSITIVE_INFINITY.dp
        ) { paddingValues ->
            TrackList(paddingValues, tracks, { track -> viewModel.playTrack(track) }, {/*TODO*/})
        }

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = lerp(size.height, 0f, progress())
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
fun TrackList(paddingValues: PaddingValues, trackList: List<Track>, onItemClick: (Track) -> Unit, onFavoriteClick: (Track) -> Unit) {

    Surface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(trackList) { track: Track ->
                TrackListItem(track, { onItemClick(track) }, {/*TODO*/})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListItem(track: Track, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {

            AsyncImage(
                track.artworkUri,
                "Cover",
                Modifier.size(MiniPlayerDimensions.height)
            )

            TitleArtist(Modifier
                .weight(1f)
                .padding(start = 12.dp), track)
            IconButton(
                onFavoriteClick
            ) {
                Icon(Icons.Outlined.FavoriteBorder, null)
            }

        }
        // FlatEdgeLinearProgressIndicator({0.5f}, Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheet(
    openFraction: () -> Float,
    modifier: Modifier = Modifier,
    track: Track?,
    isPlaying: () -> Boolean,
    pause: () -> Unit,
    play: () -> Unit,
    stop: () -> Unit,
    currentPosition: () -> Long // In Milliseconds
) {

    //val viewModel: MainViewModel = viewModel()
    var displayedTrack by remember {
        mutableStateOf(track)
    }
    if (track != null) displayedTrack = track

    val compositeLandscape = LocalIsLandscape.current
    val isPortrait = !compositeLandscape

    Box(Modifier.fillMaxSize()) {

        val width = 32.dp
        val height = 4.dp

        displayedTrack?.let {
            MiniPlayer(
                openFraction,
                isPortrait,
                it, // track if not null
                isPlaying,
                pause,
                play,
                currentPosition
            )
            Player(openFraction, Modifier, it)
        }


        // Drag handle
        val topPaddingDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            Modifier
                .matchParentSize()
                .padding(top = lerp(topPaddingDp, 0.dp, openFraction())),
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
fun Player(openFraction: () -> Float, modifier: Modifier = Modifier, track: Track) {
    val compositeLandscape = LocalIsLandscape.current
    val isPortrait = !compositeLandscape

    val outerPadding = 24.dp
    val innerPadding = 16.dp

    Box(modifier.fillMaxSize()) {
        if (isPortrait) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .safeDrawingPadding()
                    .alpha((1 - openFraction()).pow(2))
            ) {
                PlayerImage(
                    Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                        .padding(bottom = innerPadding),
                    track
                )
                PlayerControls(
                    openFraction,
                    Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                        .padding(top = innerPadding),
                    track
                )
            }
        } else {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .safeDrawingPadding()
                    .alpha((1 - openFraction()).pow(2))
            ) {
                PlayerImage(
                    Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
                        .padding(end = innerPadding),
                    track
                )
                PlayerControls(
                    openFraction,
                    Modifier
                        .weight(2 / 3f)
                        .fillMaxHeight()
                        .padding(start = innerPadding),
                    track
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    openFraction: () -> Float,
    isPortrait: Boolean,
    track: Track,
    isPlaying: () -> Boolean,
    pause: () -> Unit,
    play: () -> Unit,
    currentPosition: () -> Long // In Milliseconds
) {


    val context = LocalContext.current
    Column(Modifier
        .fillMaxWidth()
        .alpha(openFraction().pow(50))) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            AsyncImage(track.artworkUri, null, Modifier.size(MiniPlayerDimensions.height - ProgressIndicatorDefaults.LinearTrackStopIndicatorSize))
            TitleArtist(Modifier
                .weight(1f)
                .padding(start = 12.dp), track)
            IconButton(
                { Toast.makeText(context, "click", Toast.LENGTH_SHORT).show() },
                enabled = openFraction() > 0.1f
            ) {
                Icon(Icons.Outlined.FavoriteBorder, null)
            }
            if (isPortrait) {
                IconButton({ if (isPlaying()) pause() else play() }) {
                    if (isPlaying())
                        Icon(Icons.Outlined.Pause, null)
                    else
                        Icon(Icons.Outlined.PlayArrow, null)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton({}, enabled = openFraction() > 0.1f) {
                        Icon(Icons.Outlined.SkipPrevious, null)
                    }
                    IconButton({ if (isPlaying()) pause() else play() }) {
                        if (isPlaying())
                            Icon(Icons.Outlined.Pause, null)
                        else
                            Icon(Icons.Outlined.PlayArrow, null)
                    }
                    IconButton({}, enabled = openFraction() > 0.1f) {
                        Icon(Icons.Outlined.SkipPrevious, null)
                    }
                }
            }
        }


        val displayProgress = if (track.duration > 0) (currentPosition().toFloat() / track.duration).coerceIn(0f, 1f) else 0f




        FlatEdgeLinearProgressIndicator({displayProgress}, Modifier.fillMaxWidth())
    }
}

@Composable
fun PlayerImage(modifier: Modifier = Modifier, track: Track) {
    Box(modifier, Alignment.Center) {
        Box(
            Modifier
                .aspectRatio(1f)
                .fillMaxSize()
        ) {
            AsyncImage(
                track.artworkUri,
                "Cover",
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(MiniPlayerDimensions.cornerRadius))
            )
        }
    }
}

@Composable
fun PlayerControls(openFraction: () -> Float, modifier: Modifier = Modifier, track: Track) {

    val viewModel: MainViewModel = viewModel()


    Column(
        modifier,
        Arrangement.Bottom,
        Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding( top = 48.dp))
        Column(
            Modifier.fillMaxWidth().weight(1f),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                TitleArtist(Modifier.weight(1f), track)
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


                var dragProgress by remember { mutableFloatStateOf(Float.NaN) }
                val currentPosition = viewModel.currentPositionMs

                val displayProgress = if (!dragProgress.isNaN()) {
                    dragProgress
                } else {
                    if (track.duration > 0) (currentPosition.toFloat() / track.duration).coerceIn(0f, 1f) else 0f
                }

                // Determine what text to show based on the drag state
                val displayTimeMs = if (!dragProgress.isNaN()) {
                    (dragProgress * track.duration).toLong()
                } else {
                    currentPosition
                }


                Slider(
                    value = displayProgress,
                    onValueChange = { newValue ->
                        dragProgress = newValue
                    },
                    onValueChangeFinished = {
                        val seekTarget = (dragProgress * track.duration).toLong()
                        viewModel.seekTo(seekTarget)
                        dragProgress = Float.NaN
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState,
                            drawStopIndicator = null
                        )
                    }
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(formatDuration(displayTimeMs))
                    Text(formatDuration(track.duration))
                }
            }
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,) {
                IconButton(
                    onClick = {/*TODO()*/},
                    modifier = Modifier
                        .padding(6.dp)
                        .size(48.dp),
                    enabled = openFraction() < 0.8f
                ) {
                    Icon(Icons.Rounded.SkipPrevious, null, Modifier.fillMaxSize())
                }
                IconButton(
                    onClick = {
                        if (viewModel.isPlaying) viewModel.pause()
                        else viewModel.resume()
                              },
                    modifier = Modifier
                        .padding(6.dp)
                        .size(72.dp)
                ) {
                    if (viewModel.isPlaying)
                        Icon(Icons.Outlined.Pause, null, Modifier.fillMaxSize())
                    else
                        Icon(Icons.Outlined.PlayArrow, null, Modifier.fillMaxSize())
                }
                IconButton(
                    onClick = {/*TODO()*/ },
                    modifier = Modifier
                        .padding(6.dp)
                        .size(48.dp),
                    enabled = openFraction() < 0.8f
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
fun TitleArtist(modifier: Modifier = Modifier, track: Track) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        var typography = MaterialTheme.typography.bodyLarge
        Text(
            text = track.title ?: "",
            style = typography,
            fontWeight = FontWeight.Bold,

            )
        typography = MaterialTheme.typography.bodySmall
        Text(
            text = track.artist ?: "",
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

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MiniPlayerPreview() {
    SaelTheme() {
        MiniPlayer(
            { 1f },
            true, Track(
            id = 10,
            title = "Title",
            artist = "Artist",
            album = "Album",
            duration = 10L,
            trackNumber = 1,
            year = 10,
            path = "",
            mimeType = "",
            bitrate = 10,
            contentUri = "".toUri(),
            artworkUri = "".toUri(),
            lrcPath = null
            ),
            {true},
            {},
            {},
            {5L}
        )
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlayerPreview() {
    SaelTheme {
        Surface() {
            Player(
                { 0.0f }, Modifier, Track(
                id = 10,
                title = "Title",
                artist = "Artist",
                album = "Album",
                duration = 10,
                trackNumber = 1,
                year = 10,
                path = "",
                mimeType = "",
                bitrate = 10,
                contentUri = "".toUri(),
                artworkUri = "".toUri(),
                lrcPath = null
            ))
        }
    }
}