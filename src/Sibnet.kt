import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Connect to passed [stringUrl] and return valid url if redirect found.
 *
 * @return
 * entry url if [stringUrl] host not sibnet.ru or if status code [HttpURLConnection.HTTP_OK],
 * null if location header not found or status code not [HttpURLConnection.HTTP_MOVED_TEMP],
 * valid url otherwise
 */
@Suppress("UsePropertyAccessSyntax", "UnnecessaryVariable")
fun handleSibnetRedirect(stringUrl: String): String? {
    val url = URL(stringUrl)
    if (!url.host.contains("sibnet.ru")) return stringUrl

    val connection: HttpsURLConnection = (url.openConnection() as HttpsURLConnection).apply {
        setConnectTimeout(15_000)
        setReadTimeout(15_000)
        setInstanceFollowRedirects(false)
        addRequestProperty("Referer", "https://us-central1-shikimori-fbf37.cloudfunctions.net/")
        disconnect()
    }

    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) return stringUrl
    if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) return null

    val videoUrl = connection.getHeaderField("Location")

    return videoUrl
}