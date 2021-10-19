package de.l4zs.rolebot.module.role

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.module.setting.SettingsModule
import de.l4zs.rolebot.module.setting.guildAdminOnly
import de.l4zs.rolebot.module.setting.updateMessage
import de.l4zs.rolebot.util.safeGuild
import dev.kord.common.entity.Permission

private class RemoveRoleArguments : Arguments() {
    val role by role(
        "Role",
        "The Role to remove"
    )
}

suspend fun SettingsModule.removeRoleCommand() {
    ephemeralSlashCommand(::RemoveRoleArguments) {
        name = "remove-role"
        description = "Remove a role button"
        requireBotPermissions(Permission.ManageGuild)

        guildAdminOnly()

        action {

            val guildSettings = database.guildSettings.findGuild(safeGuild)

            val roles = database.roles.find().toList().filter { it.guildId == safeGuild.id }.map { it.roleId }

            if (!roles.contains(arguments.role.id)) {
                respond {
                    content = translate(
                        "command.role_command.not_added",
                        arrayOf(arguments.role.mention)
                    )
                }
                return@action
            }

            database.roles.deleteOneById(arguments.role.id)

            if (guildSettings.roleChannelData != null) {
                updateMessage(
                    guildSettings.guildId,
                    database,
                    this@ephemeralSlashCommand.kord
                )
            }

            respond {
                content = translate(
                    "command.role_command.removed",
                    arrayOf(arguments.role.mention)
                )
            }
        }
    }
}
