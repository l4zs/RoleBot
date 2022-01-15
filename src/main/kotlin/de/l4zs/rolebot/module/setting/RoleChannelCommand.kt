package de.l4zs.rolebot.module.setting

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.RoleBotDatabase
import de.l4zs.rolebot.core.io.findGuild
import dev.kord.common.entity.*
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

private class RoleChannelArguments : Arguments() {
    val channel by channel("channel", "Text Channel to use for Role Channel", validator = { _, value ->
        if (value.type != ChannelType.GuildText) {
            throw DiscordRelayedException(translate("commands.role_channel.no_text_channel", arrayOf(value.data.name)))
        }

        val botPermissions = (safeGuild.getChannel(value.id) as TextChannel).getEffectivePermissions(value.kord.selfId)
        if (Permission.ManageMessages !in botPermissions) {
            throw DiscordRelayedException(translate("command.role_channel.channel_missing_perms", arrayOf(value.data.name)))
        }
    })
}

suspend fun SettingsModule.roleChannelCommand() {
    ephemeralSlashCommand(::RoleChannelArguments) {
        name = "channel"
        description = "Changed the role channel"
        requireBotPermissions(Permission.ManageGuild)

        guildAdminOnly()

        action {

            val guildSettings = RoleBotDatabase.guildSettings.findGuild(safeGuild)

            if (guildSettings.roleChannelData != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.role_channel.new.confirm")
                }

                if (!confirmed) {
                    edit { content = translate("settings.role_channel.new.aborted") }
                    return@action
                }
            }

            val textChannel = (safeGuild.getChannel(arguments.channel.id) as TextChannel)
                // disable the cache for this one, because message caching has issues
                .withStrategy(EntitySupplyStrategy.rest)

            if (textChannel.getLastMessage() != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.role_channel.try_delete_messages")
                }

                if (confirmed) {
                    val messages = textChannel
                        .messages
                        .map { it.id }
                        .toList()
                    textChannel.bulkDelete(messages)
                }
            }

            val message = textChannel.createMessage {
                content = translate("settings.loading")
            }

            message.pin("Main Role Channel message")

            RoleBotDatabase.guildSettings.save(
                guildSettings.copy(
                    roleChannelData = RoleChannelData(
                        roleChannel = arguments.channel.id,
                        message.id
                    )
                )
            )

            updateMessage(
                safeGuild.id,
                this@ephemeralSlashCommand.kord,
                true
            )

            respond {
                content = translate(
                    "commands.role_channel.changed",
                    arrayOf(arguments.channel.mention)
                )
            }
        }
    }
}

suspend fun updateMessage(
    guildId: Snowflake,
    kord: Kord,
    initialUpdate: Boolean = false
) {
    findMessageSafe(guildId, kord)?.edit {
        if (initialUpdate) {
            // Clear initial loading text
            // This requires the content to be explicitly an empty string
            // afterwards we will just send null (or effectively no value) to shrink down the request size
            content = ""
        }
        embed {
            title = "Roles"
            val desc = mutableListOf(
                "Click the buttons to get the role you want to have.",
                "",
            )
            desc.addAll(
                RoleBotDatabase.roles.find().toList().filter { it.guildId == guildId }.map { it.label + " - " + kord.getGuild(guildId)?.getRole(it.roleId)?.mention }
            )
            description = desc.joinToString("\n")
        }
        if (RoleBotDatabase.roles.find().toList().any { it.guildId == guildId }) {
            actionRow {
                RoleBotDatabase.roles.find().toList().filter { it.guildId == guildId }.forEach {
                    roleButton(it.roleId.value.toString(), it.label)
                }
            }
        }
    }
}

suspend fun findMessageSafe(guildId: Snowflake, kord: Kord): Message? {
    val guildSettings = RoleBotDatabase.guildSettings.findOneById(guildId)
    val (channelId, messageId) = guildSettings?.roleChannelData ?: return null

    val message = kord.getGuild(guildId)?.getChannelOfOrNull<TextChannel>(channelId)?.getMessageOrNull(messageId)

    // if the message is not found, disable the feature
    if (message == null) {
        RoleBotDatabase.guildSettings.save(guildSettings.copy(roleChannelData = null))
    }

    return message
}

private fun ActionRowBuilder.roleButton(
    name: String,
    label: String,
    buttonStyle: ButtonStyle = ButtonStyle.Primary,
    additionalCondition: Boolean = true,
    enabled: Boolean = false,
    enabledStyle: ButtonStyle = ButtonStyle.Success,
) {
    val style = if (enabled) {
        enabledStyle
    } else {
        buttonStyle
    }

    interactionButton(style, name) {
        this.label = label
        disabled = !(additionalCondition)
    }
}
