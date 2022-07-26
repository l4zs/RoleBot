package de.l4zs.rolebot.util

import com.kotlindiscord.kord.extensions.commands.converters.impl.StringConverterBuilder
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild

fun StringConverterBuilder.autoCompleteRoleMessages() {
    autoComplete { event ->
        val guildId = event.interaction.getChannel().data.guildId.value ?: return@autoComplete
        val guild = kord.getGuild(guildId) ?: return@autoComplete
        val guildData = PluginDatabase.guilds.findGuild(guild)
        val roleMessages = guildData.roleMessages ?: return@autoComplete

        suggestStringMap(
            roleMessages.associate { it.title to it.title }
        )
    }
}
