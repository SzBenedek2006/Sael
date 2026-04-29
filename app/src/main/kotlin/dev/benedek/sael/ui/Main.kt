package dev.benedek.sael.ui

import androidx.appcompat.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.benedek.sael.ui.theme.SaelTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sael") }
            )
        }
    ) { paddingValues ->
        Content(paddingValues)
    }
}

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


@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MainPreview() {
    SaelTheme {
        Main()
    }
}