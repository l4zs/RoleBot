package de.l4zs.rolebot.util

import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed

suspend fun updateMessage(
    title: String,
    guild: GuildBehavior,
    kord: Kord,
) {
    val guildData = PluginDatabase.guilds.findGuild(guild)
    val roleMessage = guildData.roleMessages?.first { it.title == title } ?: return
    findMessageSafe(guild.id, kord, roleMessage.channelId, roleMessage.messageId)?.edit {

        content = " "
        embed {
            this.title = roleMessage.title
            description = roleMessage.description

            roleMessage.roles?.forEach {
                val role = guild.getRole(it.roleId)
                field {
                    inline = false
                    value = ""
                    if (it.emoji != null) {
                        value = "<:${it.emoji.name}:${it.emoji.id}> "
                    }
                    value += "**${it.label} - ${role.mention}**\n${it.description ?: ""}"
                }
            }
        }
        if (!roleMessage.roles.isNullOrEmpty()) {
            roleMessage.roles.chunked(5).forEach {
                actionRow {
                    it.forEach {
                        roleButton(it.roleId.value.toString(), it.label, it.emoji)
                    }
                }
            }
        } else {
            components = mutableListOf()
        }
    }
}

suspend fun findMessageSafe(guildId: Snowflake, kord: Kord, channelId: Snowflake, messageId: Snowflake): Message? {
    return kord.getGuild(guildId)?.getChannelOfOrNull<TextChannel>(channelId)?.getMessageOrNull(messageId)
}

private fun ActionRowBuilder.roleButton(
    name: String,
    label: String,
    emoji: DiscordPartialEmoji? = null,
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
        this.emoji = emoji
        this.label = label
        disabled = !(additionalCondition)
    }
}
