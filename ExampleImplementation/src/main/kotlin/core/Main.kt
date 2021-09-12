package core

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.UserInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import themrcodes.composeui.components.ComposeTerminal



@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
fun main() = application {
    val session = JSch().run {
        val s = getSession("pi", "192.168.0.14", 22) //TODO remove data
        s.setPassword("hCmqnr7RoaQbv13o9qOz".toByteArray())
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

    Window(onCloseRequest = {
        session.disconnect()
        channel.disconnect()
    }) {
        var scaling by remember { mutableStateOf(1f) }
        //CompositionLocalProvider(LocalDensity provides Density(scaling, 1.25f)) {
            remember {
                window.rootPane.addMouseWheelListener {
                    if (it.isControlDown) {
                        val delta = (0.1f * it.preciseWheelRotation.toFloat() * it.scrollAmount.toFloat() * -1f)
                        if (!delta.isNaN() && ((0.1f < scaling && scaling < 5f) || (0.1f < scaling && delta < 0) || (scaling < 5f && delta > 0)))
                            scaling += delta
                    }
                }
            }

            Column(Modifier.fillMaxSize()) {
                ComposeTerminal(channel, Modifier.fillMaxSize())
            }
        //}
    }

}