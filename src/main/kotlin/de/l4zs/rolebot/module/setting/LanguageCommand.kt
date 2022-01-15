package de.l4zs.rolebot.module.setting

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.RoleBotDatabase
import de.l4zs.rolebot.core.io.findUser
import java.util.*

private class LanguageArguments : Arguments() {
    val language by stringChoice(
        "language", "The language you want to use",
        mapOf(
            "German" to SupportedLocales.GERMAN.toLanguageTag(),
            "English" to SupportedLocales.ENGLISH.toLanguageTag()
        )
    )
}

suspend fun SettingsModule.languageCommand() {
    ephemeralSlashCommand(::LanguageArguments) {
        name = "language"
        description = "Changed the language of the bot"

        action {
            val locale = Locale.forLanguageTag(arguments.language)

            val botUser = RoleBotDatabase.users.findUser(user)
            val newUser = botUser.copy(language = locale)
            RoleBotDatabase.users.save(newUser)

            respond { content = translate("commands.language.changed", arrayOf(arguments.language)) }
        }
    }
}
