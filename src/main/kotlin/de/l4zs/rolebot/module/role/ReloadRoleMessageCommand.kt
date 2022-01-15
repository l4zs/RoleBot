package de.l4zs.rolebot.module.role

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.RoleBotDatabase
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.module.setting.*
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.schlaubi.mikbot.plugin.api.util.safeGuild

suspend fun SettingsModule.reloadCommand() {
    ephemeralSlashCommand() {
        name = "reload-role-message"
        description = "Reload the Role Message"
        requireBotPermissions(Permission.ManageGuild)

        guildAdminOnly()

        action {
            val guildSettings = RoleBotDatabase.guildSettings.findGuild(safeGuild)

            if (guildSettings.roleChannelData != null) {
                if (RoleBotDatabase.roles.find().toList().none { it.guildId == safeGuild.id }) {
                    val channelId = RoleBotDatabase.guildSettings.findOneById(safeGuild.id)!!.roleChannelData!!.roleChannel
                    val channel = safeGuild.getChannel(channelId)

                    findMessageSafe(safeGuild.id, this@ephemeralSlashCommand.kord)?.delete()

                    val message = (channel as TextChannel).createMessage {
                        content = translate("settings.loading")
                    }

                    message.pin("Main Role Channel message")

                    RoleBotDatabase.guildSettings.save(
                        guildSettings.copy(
                            roleChannelData = RoleChannelData(
                                roleChannel = channelId,
                                message.id
                            )
                        )
                    )
                }

                updateMessage(
                    safeGuild.id,
                    this@ephemeralSlashCommand.kord,
                    true
                )

                respond {
                    content = translate("command.reload_command.done")
                }
                return@action
            }
            respond {
                content = translate("command.reload_command.error")
            }
        }
    }
}
