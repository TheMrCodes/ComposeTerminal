package themrcodes.composeui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.InputStream
import java.io.OutputStream


@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@Composable
fun Terminal(
    inputStream: InputStream,
    outputStream: OutputStream,
) {
    val lines = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            channelFlow {
                while (true) {
                    // Continuous reader
                    val inputString = buildString {
                        runInterruptible {
                            val buffer = ByteArray(1024)
                            while (inputStream.available() > 0) {
                                val cnt = inputStream.read(buffer, 0, 1024)
                                append(String(buffer, 0, cnt))
                            }
                        }
                    }

                    if (inputString == "")
                        delay(100)
                    else
                        send(inputString)
                }
            }.collect {
                lines.addAll(it.split("\n"))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(10.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            SelectionContainer {
                Column {
                    val scrollState = remember { ScrollableState { it } }
                    LazyColumn(Modifier.scrollable(scrollState, Orientation.Vertical)) {
                        items(lines) {
                            Text(it)
                        }
                    }

                    val scope = rememberCoroutineScope()
                    var value by remember { mutableStateOf("") }
                    TextField(
                        value = value,
                        onValueChange = { value = it },
                        maxLines = 1,
                        modifier = Modifier.onKeyEvent {
                            if (!it.isCtrlPressed) return@onKeyEvent false
                            when(it.key) {
                                Key.Enter -> {
                                    scope.launch {
                                        runInterruptible(Dispatchers.IO) {
                                            outputStream.write(value.toByteArray())
                                            outputStream.flush()
                                        }
                                        value = ""
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                    )
                }
            }
        }
    }
}