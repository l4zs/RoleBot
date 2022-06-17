package de.l4zs.rolebot.module.settings.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class ReloadRoleMessageCommandArguments: Arguments() {
    val roleMessage by string {
        name = "role-message"
        description = "The message to which the role should be added"

        autoComplete { event ->
            val guildId = event.interaction.getChannel().data.guildId.value ?: return@autoComplete
            val guild = kord.getGuild(guildId) ?: return@autoComplete
            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessages = guildData.roleMessages ?: return@autoComplete

            suggestStringMap(
                roleMessages.map { it.title to it.title }.toMap()
            )
        }
    }
}

suspend fun SettingsModule.reloadRoleMessageCommand() {
    ephemeralSlashCommand(::ReloadRoleMessageCommandArguments) {
        name = "reload-role-message"
        description = "Reloads the role message"

        guildAdminOnly()

        action {
            val messageTitle = arguments.roleMessage

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            updateMessage(roleMessage.title, safeGuild, kord)

            respond {
                content = "Successfully reloaded role message '$messageTitle'"
            }
        }
    }
}
