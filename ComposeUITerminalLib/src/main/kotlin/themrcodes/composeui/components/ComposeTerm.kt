package themrcodes.composeui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import themrcodes.composeui.term.Connection
import themrcodes.composeui.term.EmulatorVT100
import themrcodes.composeui.term.Term


class ComposeTerm: Term {

    var cursor by mutableStateOf(0 to 0)

    var reversed by mutableStateOf(false)
    var fcolor by mutableStateOf(Color.White)
    var bcolor by mutableStateOf(Color.Black)
    var updateState by mutableStateOf(1)
    var lines = mutableStateListOf<String>()


    lateinit var connection: Connection
    override fun start(connection: Connection) {
        val emulator = EmulatorVT100(this, connection.inputStream)
        emulator.reset()
        emulator.start()
    }

    @Composable
    fun render(connection: Connection) {
        val scrollState = rememberScrollState()
        LazyColumn(modifier = Modifier.height(400.dp).fillMaxWidth().scrollable(scrollState, Orientation.Vertical)) {
            items(lines) {
                Text(it, modifier = Modifier.fillParentMaxWidth().background(Color.Red))
            }
        }
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
        if (lines.size < y) {
            while (lines.size <= y) {
                lines.add("")
            }
        }
        lines[y] = String(buf, s, len)
        updateState += 1
    }
    override fun drawString(str: String, x: Int, y: Int) {
        println("drawString")
        lines.addAll(str.split("\n"))
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