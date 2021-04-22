import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import javax.net.ssl.HttpsURLConnection

/**
 * Simple workaround.
 * Read html from passed [stringUrl] and extract stream url.
 *
 * @return
 * old url if [stringUrl] host not myvi.tv or if status code not [HttpURLConnection.HTTP_OK],
 * stream url otherwise
 */
@Suppress("UsePropertyAccessSyntax", "UnnecessaryVariable")
fun handleMyviEmbedUrl(stringUrl: String): String {
    val url = URL(stringUrl)
    if (!url.host.contains("myvi.tv")) return stringUrl

    val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
        setConnectTimeout(15_000)
        setReadTimeout(15_000)
        disconnect()
    }

    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_OK) return stringUrl

    val rawHtml = connection.inputStream.use { readRawHtml(it) }

    val videoUrl = extractVideoUrlFromRawHtml(rawHtml)

    return videoUrl
}

private fun readRawHtml(inputStream: InputStream): String {
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

private fun extractVideoUrlFromRawHtml(rawHtml: String): String {
    val document = Jsoup.parseBodyFragment(rawHtml)
    val scripts = document.getElementsByTag("script")
    val stringUrl = scripts.map(Element::data)
        .find { it.contains("PlayerLoader.CreatePlayer") }
        ?.substringAfter("PlayerLoader.CreatePlayer(\"v=")
        ?.replaceAfterLast("\");", "")
        ?.split("(?=https)".toRegex())
        ?.find { it.contains("stream") } ?: throw Exception("Can't find script")
    val url = URLDecoder.decode(stringUrl, "UTF-8")
    return url.substringBefore("\\u0026tp=", "")
}