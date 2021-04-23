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
 * Read html from passed [stringUrl], extract stream url and return valid url if redirect found.
 * Works with myvi.tv and myvi.top
 *
 * @return
 * entry url if [stringUrl] host not myvi or if status code not [HttpURLConnection.HTTP_OK],
 * valid url otherwise
 */
@Suppress("UsePropertyAccessSyntax", "UnnecessaryVariable")
fun handleMyviEmbeddedUrl(stringUrl: String): String {
    val url = URL(stringUrl)
    if (!url.host.contains("myvi")) return stringUrl

    val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
        setConnectTimeout(15_000)
        setReadTimeout(15_000)
        addRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36"
        )
        disconnect()
    }

    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_OK) return stringUrl

    val rawHtml = connection.inputStream.use { readRawHtml(it) }
    val extractedVideoUrl = extractVideoUrl(rawHtml)
    val videoUrl = handleMyviRedirect(extractedVideoUrl) ?: throw Exception("Can't redirect")

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

private fun extractVideoUrl(rawHtml: String): String {
    val document = Jsoup.parseBodyFragment(rawHtml)
    val scripts = document.getElementsByTag("script")
    val stringUrl = scripts.map(Element::data)
        .find { it.contains("PlayerLoader.CreatePlayer") }
        ?.substringAfter("PlayerLoader.CreatePlayer(\"v=")
        ?.replaceAfterLast("\");", "")
        ?.split("(?=https)".toRegex())
        ?.find { it.contains("stream") } ?: throw Exception("Can't find script or url")
    val url = URLDecoder.decode(stringUrl, "UTF-8")
        .substringBefore("\\u0026tp=", "")
    return if (url.startsWith("//")) "https:".plus(url) else url
}

/**
 * Connect to passed [stringUrl] and return valid url if redirect found.
 *
 * @return
 * entry url if [stringUrl] host not myvi or if status code [HttpURLConnection.HTTP_OK],
 * null if location header not found or status code not [HttpURLConnection.HTTP_MOVED_TEMP],
 * valid url otherwise
 */
@Suppress("UsePropertyAccessSyntax", "UnnecessaryVariable")
fun handleMyviRedirect(stringUrl: String): String? {
    val url = URL(stringUrl)

    val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
        setConnectTimeout(15_000)
        setReadTimeout(15_000)
        setInstanceFollowRedirects(false)
        addRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36"
        )
        disconnect()
    }

    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) return stringUrl
    if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) return null

    val videoUrl = connection.getHeaderField("location")

    return videoUrl
}
