package de.l4zs.rolebot.module.settings.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEmoji
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
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
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class AddRoleCommandArguments : Arguments() {
    val role by role {
        name = "role"
        description = "The role to add"
        validate {
            failIf {
                value == value.guild.asGuild().everyoneRole
            }
        }
    }
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

suspend fun SettingsModule.addRoleCommand() {
    ephemeralSlashCommand(::AddRoleCommandArguments) {
        name = "add-role"
        description = "Adds a role to a role selection message"

        guildAdminOnly()

        action {
            val role = arguments.role
            val messageTitle = arguments.roleMessage
            val label = arguments.label ?: role.name
            val description = arguments.description ?: ""
            val emoji = arguments.emoji

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }

            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }

            if (roleMessage.roles?.any { it.roleId == role.id } == true) {
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
                                    role.id,
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

            updateMessage(roleMessage.title, safeGuild, kord)

            respond {
                content = "Added role ${role.mention} to role message '$messageTitle'"
            }
        }
    }
}
