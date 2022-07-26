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
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class EditRoleCommandArguments : Arguments() {
    val role by role {
        name = "role"
        description = "The role to edit"
        validate {
            failIf {
                value == value.guild.asGuild().everyoneRole
            }
        }
    }
    val roleMessage by string {
        name = "role-message"
        description = "The message from which the role should be edited"

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

suspend fun SettingsModule.editRoleCommand() {
    ephemeralSlashCommand(::EditRoleCommandArguments) {
        name = "edit-role"
        description = "Edits a role from a role selection message"

        guildAdminOnly()

        action {
            val role = arguments.role
            val messageTitle = arguments.roleMessage
            val description = arguments.description ?: ""
            val emoji = arguments.emoji
            val label = arguments.label ?: if (emoji == null) role.name else ""

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }
            if (roleMessage == null) {
                respond {
                    content = "Could not find role message '$messageTitle'"
                }
                return@action
            }
            val messageRole = roleMessage.roles?.find { it.roleId == role.id }
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
                                    if (r.roleId == role.id) {
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

            updateMessage(roleMessage.title, safeGuild, kord)

            respond {
                content = "Edited role ${role.mention} from role message '$messageTitle'"
            }
        }
    }
}
