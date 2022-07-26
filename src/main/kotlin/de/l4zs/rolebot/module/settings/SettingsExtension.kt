package de.l4zs.rolebot.module.settings

import de.l4zs.rolebot.module.settings.event.roleInteraction
import de.l4zs.rolebot.module.settings.command.addRoleCommand
import de.l4zs.rolebot.module.settings.command.createRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.editRoleCommand
import de.l4zs.rolebot.module.settings.command.editRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.reloadRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.removeRoleCommand
import de.l4zs.rolebot.module.settings.command.removeRoleMessageCommand
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.on
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import kotlinx.coroutines.delay
import org.pf4j.Extension

@Extension
class SettingsExtension : SettingsExtensionPoint {

    override suspend fun SettingsModule.apply() {
        createRoleMessageCommand()
        editRoleMessageCommand()
        reloadRoleMessageCommand()
        removeRoleMessageCommand()

        addRoleCommand()
        editRoleCommand()
        removeRoleCommand()

        roleInteraction()
        kord.on<AllShardsReadyEvent> {
            delay(1000)
            kord.editPresence {
                status = PresenceStatus.Online
                playing("Kotlin")
            }
        }
    }
}
