package de.l4zs.rolebot.module.settings.event

import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.hasRole
import de.l4zs.rolebot.core.io.PluginDatabase
import de.l4zs.rolebot.core.io.findGuild
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule

suspend fun SettingsModule.roleInteraction() {
    event<GuildButtonInteractionCreateEvent> {
        action {
            val ack = event.interaction.deferEphemeralResponse()
            val interaction = event.interaction
            val message = interaction.message
            val guild = interaction.guild

            val guildData = PluginDatabase.guilds.findGuild(guild)
            val roleMessage = guildData.roleMessages?.find { it.messageId == message.id }
            if (roleMessage == null) {
                ack.respond {
                    content = "No role message found for this message"
                }
                return@action
            }
            val role = roleMessage.roles?.find { it.roleId.toString() == interaction.componentId }
            if (role == null) {
                ack.respond {
                    content = "No role found for this button"
                }
                return@action
            }
            val guildRole = guild.getRoleOrNull(role.roleId)
            if (guildRole == null) {
                ack.respond {
                    content = "Role not found in guild. Please inform an admin about this"
                }
                return@action
            }
            val member = guild.getMember(event.interaction.user.id)

            if (member.hasRole(guildRole)) {
                member.removeRole(guildRole.id)
                ack.respond {
                    content = "Removed role ${guildRole.mention}"
                }
                return@action
            } else {
                member.addRole(guildRole.id)
                ack.respond {
                    content = "Added role ${guildRole.mention}"
                }
                return@action
            }
        }
    }
}
