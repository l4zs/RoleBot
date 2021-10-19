package de.l4zs.rolebot.util

import dev.kord.core.behavior.MessageBehavior
import kotlinx.coroutines.delay
import kotlin.time.Duration

suspend fun MessageBehavior.deleteAfterwards(duration: Duration = Duration.seconds(3)) {
    delay(duration)
    delete()
}
