package de.l4zs.rolebot.module.owner

import de.l4zs.rolebot.module.owner.command.ownerAddRoleCommand
import de.l4zs.rolebot.module.owner.command.ownerCreateRoleMessageCommand
import de.l4zs.rolebot.module.owner.command.ownerEditRoleCommand
import de.l4zs.rolebot.module.owner.command.ownerEditRoleMessageCommand
import de.l4zs.rolebot.module.owner.command.ownerReloadRoleMessageCommand
import de.l4zs.rolebot.module.owner.command.ownerRemoveRoleCommand
import de.l4zs.rolebot.module.owner.command.ownerRemoveRoleMessageCommand
import dev.schlaubi.mikbot.plugin.api.owner.OwnerExtensionPoint
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import org.pf4j.Extension

@Extension
class OwnerExtension : OwnerExtensionPoint {

    override suspend fun OwnerModule.apply() {
        ownerCreateRoleMessageCommand()
        ownerEditRoleMessageCommand()
        ownerReloadRoleMessageCommand()
        ownerRemoveRoleMessageCommand()

        ownerAddRoleCommand()
        ownerEditRoleCommand()
        ownerRemoveRoleCommand()
    }
}
