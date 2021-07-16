package themrcodes.composeui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import themrcodes.composeui.term.Connection
import themrcodes.composeui.term.EmulatorVT100
import themrcodes.composeui.term.Term
import java.time.LocalDateTime


class ComposeTerm: Term {

    var cursor by mutableStateOf(0 to 0)

    var reversed by mutableStateOf(false)
    var fcolor by mutableStateOf(Color.White)
    var bcolor by mutableStateOf(Color.Black)
    var updateState by mutableStateOf(1)
    var text by mutableStateOf("")


    lateinit var connection: Connection
    override fun start(connection: Connection) {
        val emulator = EmulatorVT100(this, connection.inputStream)
        emulator.reset()
        emulator.start()
    }

    @Composable
    fun render(connection: Connection) {
        Text(text, modifier = Modifier.fillMaxWidth(), softWrap = true)
    }

    override fun getColumnCount(): Int = 80
    override fun getRowCount(): Int = 24

    override fun getCharWidth(): Int = 10
    override fun getCharHeight(): Int = 10

    override fun setCursor(x: Int, y: Int) {
        cursor = x to y
    }



    override fun clear() {
        println("clear")
    }

    override fun draw_cursor() {
        println("draw_cursor")
        updateState += 1
    }

    override fun redraw(x: Int, y: Int, width: Int, height: Int) {
        updateState += 1
    }

    override fun clear_area(x1: Int, y1: Int, x2: Int, y2: Int) {
        println("clear_area")
        updateState += 1
    }

    override fun scroll_area(x: Int, y: Int, w: Int, h: Int, dx: Int, dy: Int) {
        println("scroll_area")
        updateState += 1
    }

    override fun drawBytes(buf: ByteArray, s: Int, len: Int, x: Int, y: Int) {
        println("drawBytes")
        text += String(buf, s, len)
        updateState += 1
    }
    override fun drawString(str: String, x: Int, y: Int) {
        println("drawString")
        text += str
        updateState += 1
    }
    override fun beep() {
        println("Beep!!")
    }


    override fun setDefaultForeGround(foreground: Any?) {}
    override fun setDefaultBackGround(background: Any?) {}

    override fun setForeGround(foreground: Any?) {}
    override fun setBackGround(background: Any?) {}


    override fun setBold() {
        // TODO
    }
    override fun setUnderline() {
        // TODO
    }

    override fun setReverse() {
        reversed = true
    }
    override fun resetAllAttributes() {
        println("//TODO reset All")
    }


    override fun getTermWidth(): Int = charWidth * columnCount
    override fun getTermHeight(): Int = charHeight * rowCount


    private val colors = arrayOf<Any>(
        Color.Black, Color.Red, Color.Green,
        Color.Yellow, Color.Blue, Color.Magenta,
        Color.Cyan, Color.White
    )
    override fun getColor(index: Int): Any? {
        return colors.getOrNull(index)
    }

}