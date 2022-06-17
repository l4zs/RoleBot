package de.l4zs.rolebot.core.io

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuildData (
    @SerialName("_id")
    val guildId: Snowflake,
    val logChannel: Snowflake?,
    val roleMessages: List<RoleMessage>?,
)

@Serializable
data class RoleMessage(
    @SerialName("_id")
    val messageId: Snowflake,
    val channelId: Snowflake,
    val title: String,
    val description: String?,
    val roles: List<Role>?,
)

@Serializable
data class Role(
    @SerialName("_id")
    val roleId: Snowflake,
    val label: String,
    val description: String?,
    val emoji: DiscordPartialEmoji?,
)
