package de.l4zs.rolebot.module.role

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.l4zs.rolebot.core.io.findGuild
import de.l4zs.rolebot.module.setting.*
import de.l4zs.rolebot.util.safeGuild
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel

suspend fun SettingsModule.reloadCommand() {
    ephemeralSlashCommand() {
        name = "reload-role-message"
        description = "Reload the Role Message"
        requireBotPermissions(Permission.ManageGuild)

        guildAdminOnly()

        action {
            val guildSettings = database.guildSettings.findGuild(safeGuild)

            if (guildSettings.roleChannelData != null) {
                if (database.roles.find().toList().none { it.guildId == safeGuild.id }) {
                    val channelId = database.guildSettings.findOneById(safeGuild.id)!!.roleChannelData!!.roleChannel
                    val channel = safeGuild.getChannel(channelId)

                    findMessageSafe(database, safeGuild.id, this@ephemeralSlashCommand.kord)?.delete()

                    val message = (channel as TextChannel).createMessage {
                        content = translate("settings.loading")
                    }

                    message.pin("Main Role Channel message")

                    database.guildSettings.save(
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
                    database,
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
