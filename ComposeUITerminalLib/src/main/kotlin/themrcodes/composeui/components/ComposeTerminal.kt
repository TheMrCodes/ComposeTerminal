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


data class ComposeTerminalStyle(
    val backgroundColor: Color = Color.White,
    val fontColor: Color = Color.Black,
)

fun updateStyle(tag: String, lastStyle: SpanStyle, terminalStyle: ComposeTerminalStyle): SpanStyle {
    /*
    // \u001b[0m
    // \u001b[1m
    // \u001b[2m
    // \u001b[3m
    // \u001b[4m
    // \u001b[5m
    // \u001b[6m
    // \u001b[7m
    // \u001b[8m
    // \u001b[0;30m
    // \u001b[0;30;40m
    // \u001b[1;34;42m

    // Reset = '\u001b[0m'

    // Colors:
    //   Foreground       = 38 to 256 Colors
    //   Background Color = 40-256

    // Font Style
    //   Bold       = \u001b[1m
    //   Underline  = \u001b[4m
    //   Reversed   = \u001b[7m

    // Cursor navigation:
    //   Up     = \u001b[{n}A
    //   Down   = \u001b[{n}B
    //   Right  = \u001b[{n}C
    //   Left   = \u001b[{n}D

    // Next Line    = \u001b[{n}E moves cursor to beginning of line n lines down
    // Prev Line    = \u001b[{n}F moves cursor to beginning of line n lines down

    // Set Column   = \u001b[{n}G moves cursor to column n
    // Set Position = \u001b[{n};{m}H moves cursor to row n column m

    // Clear Screen = \u001b[{n}J clears the screen
    //   n=0 clears from cursor until end of screen,
    //   n=1 clears from cursor to beginning of screen
    //   n=2 clears entire screen

    // Clear Line: \u001b[{n}K clears the current line
    //   n=0 clears from cursor to end of line
    //   n=1 clears from cursor to start of line
    //   n=2 clears entire line

    // Save Position: \u001b[{s} saves the current cursor position
    // Save Position: \u001b[{u} restores the cursor to the last saved positio
    */
    println("'$tag'")
    return lastStyle
}


@Composable
fun ComposeTerminal(
    channel: ChannelShell,
    modifier: Modifier = Modifier,
    theme: ComposeTerminalStyle = ComposeTerminalStyle(),
) {
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
            .onSizeChanged { }
            .then(modifier)
    ) {
        val scrollStateX = rememberScrollState()

        SelectionContainer {
            var lastStyle = remember { SpanStyle(color = theme.fontColor, background = theme.backgroundColor) }
            val regex = remember { Regex("\\u001b\\[[0-8]{0,8};?(3[0-9])?;?(4[0-9])?[mA-K]") }
            LazyColumn(Modifier.horizontalScroll(scrollStateX)) {
                items(lines) { line ->
                    Text(buildAnnotatedString {
                        pushStyle(lastStyle)
                        val parts = regex.split(line).toList()
                        val found = regex.findAll(line).toList()
                        for (i in parts.indices) {
                            append(parts[i])
                            found.getOrNull(i)?.let {
                                //TODO clear screen
                                //TODO colors not found if \r
                                lastStyle = updateStyle(it.value, lastStyle, theme)
                                pushStyle(lastStyle)
                            }
                        }
                    }, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
            }
        }
    }
}