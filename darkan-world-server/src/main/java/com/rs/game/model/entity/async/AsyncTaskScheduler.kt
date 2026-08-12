package com.rs.game.model.entity.async

import com.rs.engine.thread.AsyncTaskExecutor
import com.rs.game.model.entity.Entity
import com.rs.lib.util.Logger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class AsyncTaskScheduler {
    private val tasks = LinkedList<ScheduledTask>()
    private val namedTasks: MutableMap<String, ScheduledTask> = Object2ObjectOpenHashMap()

    fun tick() {
        val currentTasks = tasks.toList()
        for (task in currentTasks) {
            if (!tasks.contains(task)) continue

            if (!task.started) {
                task.started = true
                if (!runIsolated(task, "start") { task.coroutine.resume(Unit) }) {
                    removeTask(task)
                    continue
                }
            }

            if (!runIsolated(task, "tick") { task.tick() }) {
                removeTask(task)
                continue
            }

            if (!task.isWaiting())
                tasks.remove(task)
        }
    }

    private fun removeTask(task: ScheduledTask) {
        tasks.remove(task)
        task.mapping?.let { namedTasks.remove(it) }
    }

    fun schedule(name: String? = null, block: suspend ScheduledTask.(CoroutineScope) -> Unit) {
        val task = ScheduledTask(name)
        if (namedTasks[name] != null)
            cancel(name)
        if (name != null)
            namedTasks[name] = task
        task.coroutine = suspend {
            block(task, CoroutineScope(AsyncTaskExecutor.getWorldThreadExecutor().asCoroutineDispatcher()))
        }.createCoroutine(completion = task)
        tasks.add(task)
    }

    fun cancel(name: String?) {
        val task = namedTasks[name];
        if (task != null) {
            task.stop()
            tasks.remove(task)
            namedTasks.remove(name)
        }
    }

    fun stopAll() {
        tasks.forEach { it.stop() }
        tasks.clear()
        namedTasks.clear()
    }

    companion object {
        const val TASK_TIMEOUT_MS = 5000L

        private val ISOLATION_EXECUTOR: ExecutorService = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("ScheduledTask-Isolation-", 0).factory()
        )

        private fun runIsolated(task: ScheduledTask, phase: String, block: () -> Unit): Boolean {
            val future = ISOLATION_EXECUTOR.submit(block)
            return try {
                future.get(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                true
            } catch (e: TimeoutException) {
                Logger.error(AsyncTaskScheduler::class.java, "runIsolated",
                    "Coroutine task '${task.mapping ?: "unnamed"}' exceeded ${TASK_TIMEOUT_MS}ms during $phase phase. " +
                    "Task abandoned to prevent the world thread from freezing. " +
                    "Likely cause: an infinite loop or blocking I/O in the coroutine body without yielding via wait().")
                future.cancel(true)
                runCatching { task.stop() }
                false
            } catch (e: ExecutionException) {
                Logger.handle(AsyncTaskScheduler::class.java, "runIsolated:$phase", e.cause ?: e)
                runCatching { task.stop() }
                false
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                future.cancel(true)
                runCatching { task.stop() }
                false
            } catch (e: Throwable) {
                Logger.handle(AsyncTaskScheduler::class.java, "runIsolated:$phase", e)
                runCatching { task.stop() }
                false
            }
        }
    }
}

fun Entity.schedule(mapping: String, task: suspend ScheduledTask.(CoroutineScope) -> Unit) {
    this.asyncTasks.schedule(mapping, task)
}

fun Entity.schedule(task: suspend ScheduledTask.(CoroutineScope) -> Unit) {
    this.asyncTasks.schedule(null, task)
}

fun Entity.cancel(mapping: String) {
    this.asyncTasks.cancel(mapping)
}
