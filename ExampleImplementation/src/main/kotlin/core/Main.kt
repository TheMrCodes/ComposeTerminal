package core

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import themrcodes.composeui.components.Terminal
import java.io.File


@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
fun main() = application {
    val builder = ProcessBuilder(listOf("cmd"))
    builder.redirectErrorStream(true)
    builder.directory(File("."))
    val p = builder.start()

    Window {
        Terminal(p.inputStream, p.outputStream)
    }

}