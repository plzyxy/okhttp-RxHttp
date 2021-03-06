package rxhttp

import kotlinx.coroutines.CoroutineScope
import okhttp3.Headers
import okhttp3.OkHttpClient
import rxhttp.wrapper.callback.ProgressCallbackImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface IRxHttp {

    //断点下载进度偏移量，进在带进度断点下载时生效
    val breakDownloadOffSize: Long

    /**
     * 失败重试/超时处理，需要重写此方法，扩展方法IRxHttp.awaitXxx，最终都会调用本方法
     */
    suspend fun <T> await(
        client: OkHttpClient = HttpSender.getOkHttpClient(),
        parser: Parser<T>
    ): T
}

suspend fun IRxHttp.awaitBoolean() = await<Boolean>()

suspend fun IRxHttp.awaitByte() = await<Byte>()

suspend fun IRxHttp.awaitShort() = await<Short>()

suspend fun IRxHttp.awaitInt() = await<Int>()

suspend fun IRxHttp.awaitLong() = await<Long>()

suspend fun IRxHttp.awaitFloat() = await<Float>()

suspend fun IRxHttp.awaitDouble() = await<Double>()

suspend fun IRxHttp.awaitString() = await<String>()

suspend inline fun <reified T : Any> IRxHttp.awaitList() = await<List<T>>()

suspend inline fun <reified K : Any, reified V : Any> IRxHttp.awaitMap() = await<Map<K, V>>()

suspend fun IRxHttp.awaitBitmap() = await(BitmapParser())

suspend fun IRxHttp.awaitHeaders(): Headers = awaitOkResponse().headers()

suspend fun IRxHttp.awaitOkResponse() = await(OkResponseParser())

suspend inline fun <reified T : Any> IRxHttp.await() = await(object : SimpleParser<T>() {})

suspend fun IRxHttp.awaitDownload(destPath: String) = await(DownloadParser(destPath))

//以上awaitXxx方法都会调用本方法
suspend fun <T> IRxHttp.await(parser: Parser<T>) = await(parser = parser)

/**
 * @param destPath 本地存储路径
 * @param coroutine 用于开启一个协程，来控制进度回调所在的线程
 * @param progress 进度回调
 */
suspend fun IRxHttp.awaitDownload(
    destPath: String,
    coroutine: CoroutineScope? = null,
    progress: (Progress) -> Unit
): String {
    val clone = HttpSender.clone(ProgressCallbackImpl(coroutine, breakDownloadOffSize, progress))
    return await(clone, DownloadParser(destPath))
}
