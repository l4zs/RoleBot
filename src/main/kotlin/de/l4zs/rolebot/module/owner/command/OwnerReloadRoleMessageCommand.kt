package de.l4zs.rolebot.module.owner.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.kord

private class ReloadRoleMessageCommandArguments : Arguments() {
    val guildId by snowflake {
        name = "guild-id"
        description = "The id of the guild"
    }
    val roleMessage by string {
        name = "role-message"
        description = "The message to which the role should be added"

        autoComplete { _ ->
            val guild = kord.getGuild(guildId) ?: return@autoComplete
            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessages = guildData.roleMessages ?: return@autoComplete

            suggestStringMap(
                roleMessages.map { it.title to it.title }.toMap()
            )
        }
    }
}

suspend fun OwnerModule.ownerReloadRoleMessageCommand() {
    ephemeralSlashCommand(::ReloadRoleMessageCommandArguments) {
        name = "manual-reload-role-message"
        description = "Reloads the role message"

        ownerOnly()

        action {
            val guildId = arguments.guildId
            val guild = kord.getGuild(guildId)
            if (guild == null) {
                respond {
                    content = "Guild not found"
                }
                return@action
            }
            val messageTitle = arguments.roleMessage

            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            updateMessage(roleMessage.title, guild, kord)

            respond {
                content = "Successfully reloaded role message '$messageTitle'"
            }
        }
    }
}
