package de.l4zs.rolebot.module.owner.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
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

private class EditRoleMessageCommandArgument : Arguments() {
    val guildId by snowflake {
        name = "guild-id"
        description = "The id of the guild"
    }
    val oldTitle by string {
        name = "title"
        description = "The old title of the role selection message"

        autoComplete { _ ->
            val guild = kord.getGuild(guildId) ?: return@autoComplete
            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessages = guildData.roleMessages ?: return@autoComplete

            suggestStringMap(
                roleMessages.associate { it.title to it.title }
            )
        }
    }
    val title by optionalString {
        name = "new-title"
        description = "The title for the role selection message"
    }
    val description by optionalString {
        name = "description"
        description = "The description for the role selection message"
    }
}

suspend fun OwnerModule.ownerEditRoleMessageCommand() {
    ephemeralSlashCommand(::EditRoleMessageCommandArgument) {
        name = "manual-edit-role-message"
        description = "Edit a role selection message"

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
            val messageTitle = arguments.oldTitle
            val newTitle = arguments.title ?: messageTitle
            val newDescription = arguments.description

            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessage = guildData.roleMessages?.find { it.title == messageTitle }
            if (roleMessage == null) {
                respond {
                    content = "Could not find a role selection message with the title `$messageTitle`"
                }
                return@action
            }

            PluginDatabase.guilds.save(
                guildData.copy(
                    roleMessages = guildData.roleMessages.map {
                        if (it.title == messageTitle) {
                            it.copy(
                                title = newTitle,
                                description = newDescription
                            )
                        } else {
                            it
                        }
                    }
                )
            )

            updateMessage(newTitle, guild, kord)

            respond {
                content = "Successfully edited the role selection message `$messageTitle`"
            }
        }
    }
}
