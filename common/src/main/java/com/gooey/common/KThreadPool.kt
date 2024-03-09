package com.gooey.common

import android.os.SystemClock
import android.util.Log
import com.blankj.utilcode.util.AppUtils
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
class KThreadPool : ThreadPoolExecutor {
    val actives by lazy { ArrayList<JobInfo>() }
    val history by lazy { ArrayList<JobInfo>() }
    val logger by lazy { ThreadPoolLogger(this) }

    constructor(corePoolSize: Int,
                maximumPoolSize: Int,
                keepAliveTime: Long,
                unit: TimeUnit?,
                workQueue: BlockingQueue<Runnable?>?)
            : this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
        Executors.defaultThreadFactory(), AbortPolicy()
    )

    constructor(corePoolSize: Int,
                maximumPoolSize: Int,
                keepAliveTime: Long,
                unit: TimeUnit?,
                workQueue: BlockingQueue<Runnable?>?,
                threadFactory: ThreadFactory?)
            : this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
        threadFactory, AbortPolicy()
    )

    constructor(corePoolSize: Int,
                maximumPoolSize: Int,
                keepAliveTime: Long,
                unit: TimeUnit?,
                workQueue: BlockingQueue<Runnable?>?,
                handler: RejectedExecutionHandler?)
            : this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
        Executors.defaultThreadFactory(), handler)

    constructor(corePoolSize: Int,
                maximumPoolSize: Int,
                keepAliveTime: Long,
                unit: TimeUnit?,
                workQueue: BlockingQueue<Runnable?>?,
                threadFactory: ThreadFactory?,
                handler: RejectedExecutionHandler?)
            : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
        StatisticPolicy(handler)) {
        pools.add(this)
    }

    override fun execute(command: Runnable?) {
        if (watcher && command != null) {
            val job = JobInfo(command.hashCode())
            if (stack) {
                job.callstack = Throwable()
            }
            job.enqueueTime = SystemClock.elapsedRealtimeNanos()
            synchronized(actives) {
                actives.add(job)
            }
        }
        super.execute(command)
    }

    override fun beforeExecute(t: Thread?, r: Runnable?) {
        if (!watcher) {
            return
        }
        if (r != null) {
            var found: JobInfo? = null
            synchronized(actives) {
                found = actives.find {
                    it.hash == r.hashCode()
                }
            }
            found?.apply {
                startTime = SystemClock.elapsedRealtimeNanos()
                running = true
                logger.logWait(this)
            }
        }
    }

    override fun afterExecute(r: Runnable?, t: Throwable?) {
        if (!watcher) {
            return
        }
        if (r != null) {
            var found: JobInfo? = null
            synchronized(actives) {
                found = actives.find {
                    it.hash == r.hashCode()
                }
                val local = found
                if (local != null) {
                    actives.remove(local)
                    if (history.size >= historyLen) {
                        history.removeAt(0)
                    }
                    history.add(local)
                }
            }
            found?.apply {
                endTime = SystemClock.elapsedRealtimeNanos()
                result = t
                logger.logEnd(this)
            }
        }
    }

    override fun shutdown() {
        super.shutdown()
        pools.remove(this)
    }

    override fun shutdownNow(): MutableList<Runnable> {
        pools.remove(this)
        return super.shutdownNow()
    }

    companion object {
        val pools = ArrayList<KThreadPool>()
        var watcher = AppUtils.isAppDebug()
        var stack = true
        var historyLen = 10

        @JvmStatic
        fun newFixedThreadPool(nThreads: Int, threadFactory: ThreadFactory?): ExecutorService? {
            return KThreadPool(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue<Runnable>(),
                threadFactory)
        }

        @JvmStatic
        fun okHttpExecutor(threadFactory: ThreadFactory): KThreadPool {
            return KThreadPool(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
                SynchronousQueue(), threadFactory)
        }
    }
}

class StatisticPolicy(private val input: RejectedExecutionHandler?)
    : RejectedExecutionHandler {
    override fun rejectedExecution(r: Runnable?, executor: ThreadPoolExecutor?) {
        input?.rejectedExecution(r, executor)
    }
}

class ThreadPoolLogger(private val pool: KThreadPool) {
    fun logWait(job: JobInfo) {
        val w = job.startTime - job.enqueueTime
        if (w > TimeUnit.SECONDS.toNanos(30)) {
            Log.d("System.err", "Wait too long, w=$w", job.callstack)
            synchronized(pool.actives) {
                pool.history.forEach {
                    val ww = it.startTime - it.enqueueTime
                    val rr = it.endTime - it.startTime
                    Log.d("KThreadPool", "w=$ww, r=$rr", it.callstack)
                }
                Log.d("KThreadPool", "------------------------------------------------------")
            }
        }
    }

    fun logEnd(job: JobInfo) {
        val r = job.endTime - job.startTime
        if (r > TimeUnit.SECONDS.toNanos(30)) {
            val w = job.startTime - job.enqueueTime
            Log.d("System.err", "Running too long, w=$w, r=$r", job.callstack)
        }
    }
}

class JobInfo(val hash: Int) {
    var callstack: Throwable? = null
    var result: Throwable? = null
    var enqueueTime = 0L
    var startTime = 0L
    var endTime = 0L
    var running = false
}