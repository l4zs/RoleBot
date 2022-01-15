package de.l4zs.rolebot.module.role

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.RoleBotDatabase
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.module.setting.SettingsModule
import de.l4zs.rolebot.module.setting.guildAdminOnly
import de.l4zs.rolebot.module.setting.updateMessage
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

private class RoleArguments : Arguments() {
    val role by role(
        "Role",
        "The Role to give/remove on click",
    )
    val label by string(
        "Label",
        "The Label shown on the button (may include non-combined emojis)"
    )
}

suspend fun SettingsModule.addRoleCommand() {
    ephemeralSlashCommand(::RoleArguments) {
        name = "add-role"
        description = "Add a role button"
        requireBotPermissions(Permission.ManageGuild)

        guildAdminOnly()

        action {

            if (arguments.role.name == "@everyone") {
                respond {
                    content = translate("command.role_command.everyone_role")
                }
                return@action
            }

            val guildSettings = RoleBotDatabase.guildSettings.findGuild(safeGuild)

            val roles = RoleBotDatabase.roles.find().toList().filter { it.guildId == safeGuild.id }.map { it.roleId }

            if (roles.contains(arguments.role.id)) {
                respond {
                    content = translate(
                        "command.role_command.already_added",
                        arrayOf(arguments.role.mention)
                    )
                }
                return@action
            }

            RoleBotDatabase.roles.insertOne(
                Role(
                    arguments.role.id,
                    arguments.label,
                    safeGuild.id
                )
            )

            if (guildSettings.roleChannelData != null) {
                updateMessage(
                    guildSettings.guildId,
                    this@ephemeralSlashCommand.kord
                )
            }

            respond {
                content = translate(
                    "command.role_command.added",
                    arrayOf(arguments.role.mention)
                )
            }
        }
    }
}
