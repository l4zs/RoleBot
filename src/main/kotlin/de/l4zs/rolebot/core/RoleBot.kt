package de.l4zs.rolebot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import kotlinx.coroutines.CoroutineScope

@PluginMain
class RoleBot(wrapper: PluginWrapper) : Plugin(wrapper) {

    /**
     * Add additional [ExtensibleBot] settings.
     *
     * **Do not add [Extensions][Extension] in here, use [addExtensions] instead.**
     */
    override suspend fun ExtensibleBotBuilder.apply() {
        kord {
            defaultStrategy = EntitySupplyStrategy.rest
        }
    }

    /**
     * Add new extensions.
     */
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
    }

    /**
     * This is being executed directly after the bot got started.
     */
    override fun CoroutineScope.atLaunch(bot: ExtensibleBot) {
    }
}
