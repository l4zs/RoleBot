package de.l4zs.rolebot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import de.l4zs.rolebot.config.Config
import de.l4zs.rolebot.core.io.Database
import de.l4zs.rolebot.core.io.findUser
import de.l4zs.rolebot.module.role.RoleInteractionModule
import de.l4zs.rolebot.module.setting.SettingsModule
import dev.kord.common.entity.PresenceStatus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class RoleBot : KoinComponent {

    private lateinit var bot: ExtensibleBot
    private val database = Database()

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            extensions {
                add(::SettingsModule)
                add(::RoleInteractionModule)
            }
            presence {
                status = PresenceStatus.Online
                playing("Kotlin")
            }

            chatCommands {
                enabled = false
            }

            applicationCommands {
                enabled = true

                register = true
//                Config.TEST_GUILD?.let {
//                    defaultGuild(it)
//                }
            }

            i18n {
                defaultLocale = SupportedLocales.ENGLISH
                localeResolver { _, _, user ->
                    user?.let {
                        database.users.findUser(it).language
                    }
                }
            }

            hooks {
                afterKoinSetup {
                    registerKoinModules()
                }
            }
        }

        coroutineScope {
            launch {
                bot.start()
            }
        }
    }

    private fun registerKoinModules() {
        getKoin().loadModules(
            listOf(
                module { single { database } }
            )
        )
    }
}
