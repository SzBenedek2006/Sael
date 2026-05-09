package dev.benedek.sael.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalWindowInfo
import dev.benedek.sael.ui.LocalIsLandscape
import dev.benedek.sael.ui.Main
import dev.benedek.sael.ui.theme.SaelTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}