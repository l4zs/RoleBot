package de.l4zs.rolebot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import de.l4zs.rolebot.module.role.RoleInteractionModule
import de.l4zs.rolebot.module.setting.SettingsModule
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class RoleBotPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::SettingsModule)
        add(::RoleInteractionModule)
        add(::RoleBotExtension)
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        kord {
            defaultStrategy = EntitySupplyStrategy.cacheWithCachingRestFallback
        }
    }
}

class RoleBotExtension : Extension() {
    override val name: String = "role_bot"

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                kord.editPresence {
                    status = PresenceStatus.Online
                    playing("Kotlin")
                }
            }
        }
    }
}
