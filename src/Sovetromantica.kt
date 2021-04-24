import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

@Suppress("UsePropertyAccessSyntax", "UnnecessaryVariable")
fun handleSovetromanticaEmbeddedUrl(stringUrl: String): String {
    val url = URL(stringUrl)
    if (!url.host.contains("sovetromantica")) return stringUrl

    val sslContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(null, null, SecureRandom())

    val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
        setConnectTimeout(15_000)
        setReadTimeout(15_000)
        setSSLSocketFactory(sslContext.socketFactory)
        connect()
    }

    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_OK) return stringUrl

    val rawHtml = connection.inputStream.use { readRawHtml(it) }
    val extractedVideoUrl = extractVideoUrl(rawHtml)
    val videoUrl = validateQuality(extractedVideoUrl)

    return videoUrl
}

@Suppress("UnnecessaryVariable")
private fun extractVideoUrl(rawHtml: String): String {
    val document = Jsoup.parseBodyFragment(rawHtml)
    val scripts = document.getElementsByTag("script")
    val stringUrl = scripts.map(Element::data)
        .find { it.contains(".m3u8") }
        ?.replace("^.*?(http)|\\.m3u8.*$".toRegex(), "$1") ?: throw Exception("Can't extract url")
    return stringUrl
}

@Suppress("UsePropertyAccessSyntax")
private fun validateQuality(stringUrl: String): String {
    val availableQualities = listOf("_1080p", "_720p", "_480p", "_360p")
    val format = ".m3u8"

    var bestQuality = ""
    for (quality in availableQualities) {
        val url = URL(stringUrl + quality + format)
        val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
            setConnectTimeout(15_000)
            setReadTimeout(15_000)
            disconnect()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) continue
        if (responseCode != HttpURLConnection.HTTP_OK) break

        bestQuality = quality
    }

    if (bestQuality.isBlank()) throw Exception("Can't validate quality")

    return stringUrl + bestQuality + format
}