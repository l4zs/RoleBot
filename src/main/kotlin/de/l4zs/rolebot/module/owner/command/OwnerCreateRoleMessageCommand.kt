package de.l4zs.rolebot.module.owner.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.RoleMessage
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.util.updateMessage
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.kord

private class CreateRoleMessageCommandArguments : Arguments() {
    val guildId by snowflake {
        name = "guild-id"
        description = "The id of the guild"
    }
    val channelId by snowflake {
        name = "channel-id"
        description = "The id of the channel"
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

suspend fun OwnerModule.ownerCreateRoleMessageCommand() {
    ephemeralSlashCommand(::CreateRoleMessageCommandArguments) {
        name = "manual-create-role-message"
        description = "Creates a role selection message"

        ownerOnly()

        action {
            val title = arguments.title
            val description = arguments.description ?: "Click on the role you want to assign to yourself"
            val guildId = arguments.guildId
            val guild = kord.getGuild(guildId)
            if (guild == null) {
                respond {
                    content = "Guild not found"
                }
                return@action
            }
            val channelId = arguments.channelId
            val channel = guild.getChannel(channelId) as TextChannel

            val guildData = PluginDatabase.guilds.findGuild(guild)

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

            updateMessage(title, guild, kord)

            respond {
                content = "Role selection message created"
            }
        }
    }
}
