package de.l4zs.rolebot.module.settings

import de.l4zs.rolebot.module.roleInteraction
import de.l4zs.rolebot.module.settings.command.addRoleCommand
import de.l4zs.rolebot.module.settings.command.createRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.editRoleCommand
import de.l4zs.rolebot.module.settings.command.editRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.reloadRoleMessageCommand
import de.l4zs.rolebot.module.settings.command.removeRoleCommand
import de.l4zs.rolebot.module.settings.command.removeRoleMessageCommand
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
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
    }
}

suspend fun setPresence(kord: Kord) {
    kord.editPresence {
        status = PresenceStatus.Online
        playing("Kotlin")
    }
}
