package de.l4zs.rolebot.module.setting

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@JvmRecord
@Serializable
data class BotUser(
    @SerialName("_id")
    val id: Snowflake,
    @Contextual
    val language: Locale = SupportedLocales.ENGLISH,
)
