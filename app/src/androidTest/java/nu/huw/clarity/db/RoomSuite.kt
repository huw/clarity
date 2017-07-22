package nu.huw.clarity.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(Suite::class)
@Suite.SuiteClasses(AttachmentTest::class)
class RoomSuite

fun <T> LiveData<T>.blockingObserve(seconds: Long = 2): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val innerObserver = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(innerObserver)
    latch.await(seconds, TimeUnit.SECONDS)
    return value
}