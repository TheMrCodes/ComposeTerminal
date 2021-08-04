package themrcodes.composeui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.jcraft.jsch.ChannelShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ComposeTerminal(channel: ChannelShell, modifier: Modifier = Modifier) {
    val lines = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val cin = channel.inputStream.buffered()
        val cout = channel.outputStream.buffered()
        channel.connect()

        launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            while (true) {
                val n = cin.read(buffer)
                val newText = String(buffer, 0, n, Charsets.UTF_8)
                val lastLine = if (lines.isNotEmpty()) lines.last() else ""
                val newLines = (lastLine + newText)
                    .split("\r\n")
                    .map { it.split("\r") }

                if (newLines.isNotEmpty()) {
                    if (lines.isNotEmpty())
                        lines[lines.lastIndex] = newLines.first().last()
                    else
                        lines.add(newLines.first().last())
                    for (line in newLines.drop(1))
                        lines.add(line.last())
                }


                if (n == 0) delay(10)
            }
        }
        launch(Dispatchers.IO) {
            while (true) {
                val command = readLine() ?: continue
                cout.write((command + '\n').toByteArray())
                cout.flush()
            }
        }
    }


    BoxWithConstraints(
        modifier = Modifier
            .onSizeChanged {  }
            .then(modifier)
    ) {
        val scrollStateX = rememberScrollState()

        SelectionContainer {
            val defaultColor = remember { Color.Black }
            var lastColor = remember { defaultColor }
            val colors = remember { mapOf(
                0 to defaultColor,
                30 to Color.Black,
                31 to Color.Red,
                32 to Color.Green,
                33 to Color.Yellow,
                34 to Color.Blue,
                35 to Color.Magenta,
                36 to Color.Cyan,
                37 to Color.White,
                //38 to 256 Colors

                //TODO 40-47 Colors Background
                //TODO 48 to 256 Colors Background
                // Bold: \u001b[1m
                // Underline: \u001b[4m
                // Reversed: \u001b[7m

                /*TODO Cursor navigation
                    Up: \u001b[{n}A
                    Down: \u001b[{n}B
                    Right: \u001b[{n}C
                    Left: \u001b[{n}D

                    Next Line: \u001b[{n}E moves cursor to beginning of line n lines down
                    Prev Line: \u001b[{n}F moves cursor to beginning of line n lines down

                    Set Column: \u001b[{n}G moves cursor to column n
                    Set Position: \u001b[{n};{m}H moves cursor to row n column m

                    Clear Screen: \u001b[{n}J clears the screen
                        n=0 clears from cursor until end of screen,
                        n=1 clears from cursor to beginning of screen
                        n=2 clears entire screen
                    Clear Line: \u001b[{n}K clears the current line
                        n=0 clears from cursor to end of line
                        n=1 clears from cursor to start of line
                        n=2 clears entire line

                    Save Position: \u001b[{s} saves the current cursor position
                    Save Position: \u001b[{u} restores the cursor to the last saved position
                */
            ) }
            val regex = remember { "(\u001b\\[0m)|(\u001b\\[[014]{0,3};?[0-9][0-9]m)".toRegex() }
            LazyColumn(Modifier.horizontalScroll(scrollStateX)) {
                items(lines) { line ->
                    Text(buildAnnotatedString {
                        pushStyle(SpanStyle(color = lastColor))
                        val parts = regex.split(line).toList()
                        val found = regex.findAll(line).toList()
                        for (i in parts.indices) {
                            append(parts[i])
                            found.getOrNull(i)?.let {
                                val num = it.value.split("\\[([0-9]{2};)?".toRegex())
                                    .last().removeSuffix("m").toInt()
                                lastColor = colors[num] ?: error("Color $num not found!")
                                //TODO Background / foreground colors
                                //TODO clear screen
                                //TODO colors not found if \r
                                pushStyle(SpanStyle(color = lastColor))
                            }
                        }
                    }, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
            }
        }
    }
}