package core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jcraft.jsch.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import themrcodes.composeui.components.ComposeTerm
import themrcodes.composeui.components.Terminal
import themrcodes.composeui.term.Connection
import themrcodes.composeui.term.Term
import java.io.File
import java.io.InputStream
import java.io.OutputStream


@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
fun main() = application {
    val builder = ProcessBuilder(listOf("cmd"))
    builder.redirectErrorStream(true)
    builder.directory(File("."))
    val p = builder.start()



    val session = JSch().run {
        val s = getSession("", "", 22)
        s.setPassword("".toByteArray())
        s.userInfo = object: UserInfo {
            override fun getPassphrase(): String { return "" }
            override fun getPassword(): String { return "" }
            override fun promptPassword(message: String?): Boolean { return true }
            override fun promptPassphrase(message: String?): Boolean { return true }
            override fun promptYesNo(message: String?): Boolean { return true }
            override fun showMessage(message: String?) {}
        }
        s.connect()
        s
    }

    println("Recompose")
    val channel = session.openChannel("shell") as ChannelShell
    val cin = channel.inputStream
    val cout = channel.outputStream
    channel.connect()
    // channel.setPtySize() sets grid size and display size
    val con = object: Connection {
        override val inputStream = cin
        override val outputStream = cout
        override fun close() {
            inputStream.close()
            outputStream.close()
        }
        override fun requestResize(term: Term) {
            channel.setPtySize(term.columnCount, term.rowCount, term.termWidth, term.termHeight)
        }
    }

    Window(onCloseRequest = {
        session.disconnect()
        channel.disconnect()
    }) {
        Column {
            val term = remember { ComposeTerm() }
            LaunchedEffect(Unit) {
                runInterruptible(Dispatchers.IO) {
                    term.start(con)
                }
            }
            term.render(con)
            Terminal(cin, cout)
/*
//            Button(
//                modifier = Modifier.background(Color.White),
//                onClick = {
//                    println("Show date...")
//                    channel.outputStream.write("date".toByteArray())
//                    channel.outputStream.flush()
//                }
//            ) {
//                Text("Button")
//            }
//            var textState by remember { mutableStateOf("") }
//            TextField(
//                value = textState,
//                onValueChange = { textState = it },
//                Modifier.onKeyEvent {
//                    if (it.type != KeyEventType.KeyUp) return@onKeyEvent false
//                    when(it.key) {
//                        Key.Enter -> {
//                            if (it.isCtrlPressed) {
//                                println("Launch command...")
//                                val command = textState
////                                channel.outputStream.write(command.toByteArray())
////                                channel.outputStream.flush()
////                                channel.connect()
//                                textState = ""
//                                false
//                            } else {
//                                false
//                            }
//                        }
//                        else -> false
//                    }
//                }
//            )
*/
        }
    }

}