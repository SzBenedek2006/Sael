package dev.benedek.sael.activities

import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.benedek.sael.services.PlaybackService
import dev.benedek.sael.ui.LocalBottomSheetState
import dev.benedek.sael.ui.LocalIsLandscape
import dev.benedek.sael.ui.Main
import dev.benedek.sael.ui.theme.SaelTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            val windowInfo = LocalWindowInfo.current
            val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
            val scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.PartiallyExpanded,
                    skipHiddenState = false
                )
            )


            SaelTheme() {
                CompositionLocalProvider(
                    LocalIsLandscape provides isLandscape,
                    LocalBottomSheetState provides scaffoldState
                ) {
                    Main()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Create a SessionToken
        val sessionToken = SessionToken(
            this,
            ComponentName(this, PlaybackService::class.java)
        )

        // Create a MediaController
        controllerFuture = MediaController
            .Builder(this, sessionToken)
            // setListener currently does nothing. TODO: implement if needed
            .setListener(
                object : MediaController.Listener {
                    override fun onCustomCommand(
                        controller: MediaController,
                        command: SessionCommand,
                        args: Bundle
                    ): ListenableFuture<SessionResult> {
                        // Handle custom command. TODO
                        return super.onCustomCommand(controller, command, args)
                    }

                    override fun onDisconnected(controller: MediaController) {
                        // Handle disconnection TODO
                    }
                }
            )
            .buildAsync()

        controllerFuture.addListener(
            {
                // MediaController is available here with controllerFuture.get()
                controller = controllerFuture.get()
            },
            ContextCompat.getMainExecutor(this),
        )



    }

    override fun onStop() {
        super.onStop()
        controller = null
        MediaController.releaseFuture(controllerFuture)
    }
}