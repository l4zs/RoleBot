package de.l4zs.rolebot.module.owner.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEmoji
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.Role
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.kord

private class AddRoleCommandArguments : Arguments() {
    val guildId by snowflake {
        name = "guild-id"
        description = "The id of the guild"
    }
    val roleId by snowflake {
        name = "role-id"
        description = "The id of the role to add"
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
    val label by optionalString {
        name = "label"
        description = "The label of the role (use global emojis here)"
    }
    val description by optionalString {
        name = "description"
        description = "The description of the role"
    }
    val emoji by optionalEmoji {
        name = "emoji"
        description = "The custom (!) emoji of the role"
    }
}

suspend fun OwnerModule.ownerAddRoleCommand() {
    ephemeralSlashCommand(::AddRoleCommandArguments) {
        name = "manual-add-role"
        description = "Adds a role to a role selection message"

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
            val label = arguments.label ?: role.name
            val description = arguments.description ?: ""
            val emoji = arguments.emoji

            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            if (roleMessage.roles?.any { it.roleId == roleId } == true) {
                respond {
                    content = "Role ${role.mention} is already in the role message '$messageTitle'"
                }
                return@action
            }

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = guildData.roleMessages.map {
                        if (it.title == messageTitle) {
                            it.copy(
                                roles = (it.roles ?: listOf()) + Role(
                                    roleId,
                                    label,
                                    description,
                                    if (emoji != null) DiscordPartialEmoji(
                                        id = emoji.id,
                                        name = emoji.name,
                                        animated = emoji.isAnimated.optional()
                                    ) else null,
                                )
                            )
                        } else it
                    }
                )
            )

            updateMessage(roleMessage.title, guild, kord)

            respond {
                content = "Added role ${role.mention} to role message '$messageTitle'"
            }
        }
    }
}
