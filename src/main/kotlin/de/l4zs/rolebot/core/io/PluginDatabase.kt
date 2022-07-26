package de.l4zs.rolebot.core.io

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.litote.kmongo.coroutine.CoroutineCollection

object PluginDatabase : KordExKoinComponent {
    val guilds = database.getCollection<GuildData>("guilds")
}

suspend fun CoroutineCollection<GuildData>.findGuild(guild: GuildBehavior): GuildData {
    return findOneById(guild.id) ?: GuildData(guild.id, null, null).also { save(it) }
}
