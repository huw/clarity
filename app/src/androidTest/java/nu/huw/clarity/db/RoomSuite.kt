package nu.huw.clarity.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(Suite::class)
@Suite.SuiteClasses(AttachmentDaoTest::class, ContextDaoTest::class, FolderDaoTest::class, PerspectiveDaoTest::class, TaskDaoTest::class)
class RoomSuite