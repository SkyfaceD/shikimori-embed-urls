import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun readRawHtml(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val sb = StringBuilder()
    reader.useLines { sequence ->
        for (line in sequence.iterator()) {
            if (line.isBlank()) continue
            sb.append(line)
        }
    }
    if (sb.isBlank()) throw Exception("Can't read html")
    return sb.toString()
}