package dev.benedek.sael.activities

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dev.benedek.sael.services.PlaybackService
import dev.benedek.sael.ui.LocalIsLandscape
import dev.benedek.sael.ui.Main
import dev.benedek.sael.ui.theme.SaelTheme
import dev.benedek.sael.viewmodels.MainViewModel
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    //enum class SheetValue { Collapsed, PartiallyExpanded, Expanded }
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    // by viewModels() in activity and = viewModel in Composable context
    private val viewModel: MainViewModel by viewModels()


    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanTracks(this) // Already granted, safe to scan
        } else {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    viewModel.scanTracks(this)
                }
            }
            launcher.launch(permission)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()


        setContent {
            val windowInfo = LocalWindowInfo.current
            val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height




            SaelTheme() {
                CompositionLocalProvider(
                    LocalIsLandscape provides isLandscape,
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
                viewModel.mediaController = controller
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