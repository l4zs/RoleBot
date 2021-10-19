package de.l4zs.rolebot.module.role

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class Role(
    @SerialName("_id")
    val roleId: Snowflake,
    val label: String,
    @SerialName("guild_id")
    val guildId: Snowflake
)
