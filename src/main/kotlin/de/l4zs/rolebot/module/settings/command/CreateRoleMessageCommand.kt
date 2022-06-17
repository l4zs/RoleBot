package de.l4zs.rolebot.module.settings.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.RoleMessage
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class CreateRoleMessageCommandArguments : Arguments() {
    val channel by channel {
        name = "channel"
        description = "The channel to send the message in"
        requireSameGuild = true
        requiredChannelTypes = mutableSetOf(ChannelType.GuildText)
    }
    val title by string {
        name = "title"
        description = "The title for the role selection message"
    }
    val description by optionalString {
        name = "description"
        description = "The description for the role selection message"
    }
}

suspend fun SettingsModule.createRoleMessageCommand() {
    ephemeralSlashCommand(::CreateRoleMessageCommandArguments) {
        name = "create-role-message"
        description = "Creates a role selection message"

        guildAdminOnly()

        action {
            val title = arguments.title
            val description = arguments.description ?: "Click on the role you want to assign to yourself"
            val channel = safeGuild.getChannel(arguments.channel.id) as TextChannel

            val guildData = PluginDatabase.guilds.findGuild(safeGuild)

            if (guildData.roleMessages?.any { it.title == title } == true) {
                respond {
                    content = "A role selection message with this title already exists"
                }
                return@action
            }

            val message = channel.createMessage {
                content = "Loading message..."
            }

            message.pin("Role selection '$title'")

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = (guildData.roleMessages ?: listOf()).plus(
                        RoleMessage(
                            message.id,
                            channel.id,
                            title,
                            description,
                            null
                        )
                    )
                )
            )

            updateMessage(title, safeGuild, kord)

            respond {
                content = "Role selection message created"
            }
        }
    }
}
