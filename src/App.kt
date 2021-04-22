fun main(args: Array<String>) {
    val sibnet =
        handleSibnetRedirect("https://dv98.sibnet.ru/38/30/47/3830478.mp4?st=uwS-JHb0GRNJ-kcfAk65Zg&e=1619024000&stor=8&noip=1")
    val myvitv = handleMyviEmbeddedUrl("https://www.myvi.tv/embed/rnaoif1wd5jw3poog11t7cgj8w")
    val myvitop = handleMyviEmbeddedUrl("https://www.myvi.top/embed/z769p4zsfabrxmky7kbm398sne")
    readLine()
}