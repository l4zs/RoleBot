package de.l4zs.rolebot.module.settings.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.findMessageSafe
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class RemoveRoleMessageCommandArguments : Arguments() {
    val title by string {
        name = "title"
        description = "The title of the role selection message"

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
}

suspend fun SettingsModule.removeRoleMessageCommand() {
    ephemeralSlashCommand(::RemoveRoleMessageCommandArguments) {
        name = "remove-role-message"
        description = "Removes a role selection message"

        guildAdminOnly()

        action {
            val title = arguments.title

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)
            val roleMessage = guildData.roleMessages?.find { it.title == title }
            if (roleMessage == null) {
                respond {
                    content = "No role message with title `$title` found"
                }
                return@action
            }

            val (confirmed) = confirmation {
                content = "Are you sure you want to remove the role message `$title`? \n" +
                        "This will delete the message and all associated roles. \n" +
                        "This action cannot be undone!"
            }

            if (!confirmed) {
                respond {
                    content = "Cancelled removal of role message `$title`"
                }
                return@action
            }

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = guildData.roleMessages.filter { it.title != title }
                )
            )

            findMessageSafe(
                safeGuild.id,
                kord,
                roleMessage.channelId,
                roleMessage.messageId
            )?.delete("Removed role selection '$title' by ${event.interaction.user.mention}")

            respond {
                content = "Removed role message `$title`"
            }
        }
    }
}
