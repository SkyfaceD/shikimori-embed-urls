import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun main(args: Array<String>) {
    //@formatter:off
    val sibnet = handleSibnetRedirect("https://dv98.sibnet.ru/38/30/47/3830478.mp4?st=uwS-JHb0GRNJ-kcfAk65Zg&e=1619024000&stor=8&noip=1")
    val myvitv = handleMyviEmbeddedUrl("https://www.myvi.tv/embed/rnaoif1wd5jw3poog11t7cgj8w")
    val myvitop = handleMyviEmbeddedUrl("https://www.myvi.top/embed/z769p4zsfabrxmky7kbm398sne")
    //@formatter:on
    readLine()
}

/**
 * Why are we still here? Just to time measure? Yes
 * For what? I don't know
 *
 * Average results in millis for 100 iterations
 * sibnet=258.5498336633663
 * myvitv=295.11073564356434
 * myvitop=306.9740247524753
 */
@OptIn(ExperimentalTime::class)
fun timeMeasuring() {
    val sibnetKey = "sibnet"
    val myvitvKey = "myvitv"
    val myvitopKey = "myvitop"

    val map = linkedMapOf<String, MutableList<Double>>(
        sibnetKey to mutableListOf(),
        myvitvKey to mutableListOf(),
        myvitopKey to mutableListOf()
    )

    for (i in 0..1) {
        //@formatter:off
        val sibnet = measureTimedValue { handleSibnetRedirect("https://dv98.sibnet.ru/38/30/47/3830478.mp4?st=uwS-JHb0GRNJ-kcfAk65Zg&e=1619024000&stor=8&noip=1") }
        val myvitv = measureTimedValue { handleMyviEmbeddedUrl("https://www.myvi.tv/embed/rnaoif1wd5jw3poog11t7cgj8w") }
        val myvitop = measureTimedValue { handleMyviEmbeddedUrl("https://www.myvi.top/embed/z769p4zsfabrxmky7kbm398sne") }
        //@formatter:on

        map[sibnetKey]?.add(sibnet.duration.inMilliseconds)
        map[myvitvKey]?.add(myvitv.duration.inMilliseconds)
        map[myvitopKey]?.add(myvitop.duration.inMilliseconds)
    }

    println(map.mapValues { it.value.average() }.map { it }.joinToString("\n"))
}