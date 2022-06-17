package de.l4zs.rolebot.module.settings.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
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

private class RemoveRoleCommandArguments : Arguments() {
    val role by role {
        name = "role"
        description = "The role to remove"
    }
    val roleMessage by string {
        name = "role-message"
        description = "The message from which the role should be removed"

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

suspend fun SettingsModule.removeRoleCommand() {
    ephemeralSlashCommand(::RemoveRoleCommandArguments) {
        name = "remove-role"
        description = "Removes a role from a role selection message"

        guildAdminOnly()

        action {
            val role = arguments.role
            val messageTitle = arguments.roleMessage

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            if (roleMessage.roles?.any { it.roleId == role.id } == false) {
                respond {
                    content = "Role ${role.mention} is not in the role message '$messageTitle'"
                }
                return@action
            }

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = guildData.roleMessages.map {
                        if (it.title == messageTitle) {
                            it.copy(
                                roles = it.roles?.filter { r -> r.roleId != role.id }
                            )
                        } else it
                    }
                )
            )

            updateMessage(roleMessage.title, safeGuild, kord)

            respond {
                content = "Removed role ${role.mention} from role message '$messageTitle'"
            }
        }
    }
}
