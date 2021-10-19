package de.l4zs.rolebot.module.setting

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class BotGuild(
    @SerialName("_id")
    val guildId: Snowflake,
    val roleChannelData: RoleChannelData? = null,
)

@JvmRecord
@Serializable
data class RoleChannelData(
    val roleChannel: Snowflake,
    val roleChannelMessage: Snowflake,
)
