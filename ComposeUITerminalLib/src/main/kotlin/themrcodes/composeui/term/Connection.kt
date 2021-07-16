package themrcodes.composeui.term

import java.io.InputStream
import java.io.OutputStream

interface Connection: AutoCloseable {
    val inputStream: InputStream
    val outputStream: OutputStream
    fun requestResize(term: Term) {}
}