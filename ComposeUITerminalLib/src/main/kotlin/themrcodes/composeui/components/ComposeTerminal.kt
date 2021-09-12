package themrcodes.composeui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
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
    //println("'$tag'")
    return lastStyle
}

enum class MetaKeys { CTRL, ALT, SHIFT }

@ExperimentalComposeUiApi
val mapper = mapOf(
    Key.DirectionUp to "\u001b[A",
    Key.DirectionDown to "\u001b[B",
    Key.DirectionRight to "\u001b[C",
    Key.DirectionLeft to "\u001b[D",

    Key.Window to "\u001b[H",
    Key.MoveEnd to "\u001b[F",

    Key.Tab to "\u0009",
    Key.Backspace to "\u007f",
    Key.MediaPause to "\u001a",
    Key.Escape to "\u001b",
    Key.Insert to "\u001b[2~",
    Key.Delete to "\u001b[3~",

    Key.NavigatePrevious to "\u001b[5~",
    Key.NavigateNext to "\u001b[6~",

    Key.F1 to "\u001bOP",
    Key.F2 to "\u001bOQ",
    Key.F3 to "\u001bOR",
    Key.F4 to "\u001bOS",
    Key.F5 to "\u001b[15~",
    Key.F6 to "\u001b[17~",
    Key.F7 to "\u001b[18~",
    Key.F8 to "\u001b[19~",
    Key.F9 to "\u001b[20~",
    Key.F10 to "\u001b[21~",
    Key.F11 to "\u001b[23~",
    Key.F12 to "\u001b[24~",
)
@ExperimentalComposeUiApi
val seqMapping = mapOf(
    listOf(Key.Backspace, "CTRL") to "\u0008",
    listOf(Key.Backspace, "ALT") to "\u001b\u007f",
    listOf(Key.Backspace, "CTRL", "ALT") to "\u001b\u0008",
    listOf(Key.Tab, "CTRL") to "\t",
    listOf(Key.Tab, "SHIFT") to "\u001b[Z",
    listOf(Key.NumPadDivide, "CTRL") to "\u001f",
)


@ExperimentalComposeUiApi
@Composable
fun ComposeTerminal(
    channel: ChannelShell,
    modifier: Modifier = Modifier,
    theme: ComposeTerminalStyle = ComposeTerminalStyle(),
) {
    val lines = remember { mutableStateListOf<String>() }
    val cin = remember { channel.inputStream.buffered() }
    val cout = remember { channel.outputStream.buffered() }
    val cein = remember { channel.extInputStream }

    LaunchedEffect(Unit) {
        FontFamily.Monospace
        channel.connect()

        launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            while (true) {
                val n = cin.read(buffer)
                if (n == -1) break

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

                if (n == 0)
                    delay(10)
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


    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    val isFocused: Boolean by interactionSource.collectIsFocusedAsState()
    BoxWithConstraints(
        modifier = Modifier
            .onSizeChanged { /*//TODO change TTY size*/ }
            .border(2.dp, if (isFocused) Color.Blue else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                focusRequester.requestFocus()
            }
            .onPreviewKeyEvent { e ->
                if (e.type == KeyEventType.KeyUp) {
                    if (e.key in listOf(
                            Key.AltLeft, Key.AltRight,
                            Key.ShiftLeft, Key.ShiftRight,
                            Key.CtrlLeft, Key.CtrlRight
                    )) {
                        return@onPreviewKeyEvent false
                    }

                    // TODO check sequence mapper

                    // If in mapper transmit code
                    if (e.key in mapper) {
                        cout.write(mapper[e.key]!!.toByteArray())
                        cout.flush()
                        return@onPreviewKeyEvent true
                    }

                    // Else take native char
                    cout.write(ByteArray(1) { e.nativeKeyEvent.keyChar.code.toByte() })
                    cout.flush()
                    return@onPreviewKeyEvent true
                } else
                    false
            }.then(modifier)
    ) {
        val scrollStateX = rememberScrollState()

        var lastStyle = remember { SpanStyle(color = theme.fontColor, background = theme.backgroundColor) }
        val regex = remember { Regex("\\u001b\\[[0-8]{0,8};?(3[0-9])?;?(4[0-9])?[mA-K]") }
        LazyColumn(Modifier.horizontalScroll(scrollStateX)) {
            itemsIndexed(lines) { lineIndex, line ->
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