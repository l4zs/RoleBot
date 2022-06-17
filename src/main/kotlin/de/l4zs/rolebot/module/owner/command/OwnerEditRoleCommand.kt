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
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.kord

private class EditRoleCommandArguments : Arguments() {
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
        description = "The message from which the role should be edited"

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

suspend fun OwnerModule.ownerEditRoleCommand() {
    ephemeralSlashCommand(::EditRoleCommandArguments) {
        name = "manual-edit-role"
        description = "Edits a role from a role selection message"

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
            val messageRole = roleMessage.roles?.find { it.roleId == roleId }
            if (messageRole == null) {
                respond {
                    content = "Could not find role ${role.mention} in role message '$messageTitle'"
                }
                return@action
            }

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = guildData.roleMessages.map {
                        if (it.title == messageTitle) {
                            it.copy(
                                roles = it.roles?.map { r ->
                                    if (r.roleId == roleId) {
                                        r.copy(
                                            label = label,
                                            description = description,
                                            emoji = if (emoji != null) DiscordPartialEmoji(
                                                id = emoji.id,
                                                name = emoji.name,
                                                animated = emoji.isAnimated.optional()
                                            ) else null,
                                        )
                                    } else r
                                }
                            )
                        } else it
                    }
                )
            )

            updateMessage(roleMessage.title, guild, kord)

            respond {
                content = "Edited role ${role.mention} from role message '$messageTitle'"
            }
        }
    }
}
