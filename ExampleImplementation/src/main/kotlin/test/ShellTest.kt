import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import java.util.concurrent.TimeUnit

suspend fun main() = coroutineScope {
    val process = ProcessBuilder(listOf("wsl")).redirectErrorStream(true).start()

    launch {
        val inStream = process.inputStream.buffered()
        val buffer = ByteArray(8192)
        while (true) {
            val read = runInterruptible { inStream.read(buffer) }
            if (read == -1) break
            val text = String(buffer, 0, read)
            println(text)
        }
    }
    launch {
        val writer = process.outputStream
        while (true) {
            val read = readLine() ?: break
            when(read) {
                "SIGINT" -> {
                    writer.write(0x03)
                    writer.flush()
                }
                else -> {
                    runInterruptible {
                        writer.write((read + "\n").toByteArray())
                        writer.flush()
                    }
                }
            }
            if (read == "exit") break
        }
        runInterruptible { process.waitFor(2, TimeUnit.SECONDS) }
        println(process.exitValue())
        // 0x11 TAB-Char
    }
    //runInterruptible { process.waitFor(2, TimeUnit.SECONDS) }
    //println(process.exitValue())
    Unit
}