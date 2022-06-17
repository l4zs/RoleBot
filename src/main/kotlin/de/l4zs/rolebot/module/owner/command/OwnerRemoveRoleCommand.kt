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

private class RemoveRoleCommandArguments : Arguments() {
    val guildId by snowflake {
        name = "guild-id"
        description = "The guild id"
    }
    val roleId by snowflake {
        name = "role-id"
        description = "The role id"
    }
    val roleMessage by string {
        name = "role-message"
        description = "The message from which the role should be removed"

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

suspend fun OwnerModule.ownerRemoveRoleCommand() {
    ephemeralSlashCommand(::RemoveRoleCommandArguments) {
        name = "manual-remove-role"
        description = "Removes a role from a role selection message"

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
            val roleId = arguments.roleId
            val role = guild.getRole(roleId)
            val messageTitle = arguments.roleMessage

            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            if (roleMessage.roles?.any { it.roleId == roleId } == false) {
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
                                roles = it.roles?.filter { r -> r.roleId != roleId }
                            )
                        } else it
                    }
                )
            )

            updateMessage(roleMessage.title, guild, kord)

            respond {
                content = "Removed role ${role.mention} from role message '$messageTitle'"
            }
        }
    }
}
