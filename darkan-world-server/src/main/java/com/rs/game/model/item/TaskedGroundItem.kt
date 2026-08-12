package com.rs.game.model.item

import com.rs.game.World
import com.rs.game.World.getPlayerByUsername
import com.rs.game.model.entity.async.AsyncTaskScheduler
import com.rs.game.model.entity.async.ScheduledTask
import com.rs.lib.game.GroundItem
import kotlinx.coroutines.CoroutineScope

class TaskedGroundItem(item: GroundItem) : GroundItem(item, item.tile, item.creatorUsername, item.type) {
    val asyncTasks = AsyncTaskScheduler()

    override fun tick() {
        super.tick()
        asyncTasks.tick()
    }
}

fun TaskedGroundItem.schedule(mapping: String, task: suspend ScheduledTask.(CoroutineScope) -> Unit) {
    this.asyncTasks.schedule(mapping, task)
}

fun TaskedGroundItem.schedule(task: suspend ScheduledTask.(CoroutineScope) -> Unit) {
    this.asyncTasks.schedule(null, task)
}

fun TaskedGroundItem.cancel(mapping: String) {
    this.asyncTasks.cancel(mapping)
}

fun GroundItem.getOwner() = getPlayerByUsername(creatorUsername)