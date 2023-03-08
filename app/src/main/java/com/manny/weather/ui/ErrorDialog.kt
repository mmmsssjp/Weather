package com.manny.weather.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manny.weather.viewmodel.WeatherViewModel

@Composable
internal fun ErrorDialog(viewModel: WeatherViewModel) {
    if (viewModel.errorMessage.value == null) return
    val prompt = viewModel.errorMessage.value ?: return

    AlertDialog(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(6.dp),
        shape = MaterialTheme.shapes.large,
        title = { Text("Error") },
        text = { Text(prompt) },
        onDismissRequest = {
            viewModel.clearErrorMessage()
        },
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Button(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    onClick = {
                        viewModel.clearErrorMessage()
                    }
                ) {
                    Text(text = "Ok")
                }
            }
        },
    )
}
