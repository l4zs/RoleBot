package de.l4zs.rolebot.module.role

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.hasRole
import de.l4zs.rolebot.core.io.RoleBotDatabase
import de.l4zs.rolebot.core.io.findGuild
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.mikbot.plugin.api.util.respondIfFailed

class RoleInteractionModule : Extension() {

    override val name = "role interaction handler"
    override val bundle: String = "button"

    override suspend fun setup() {
        event<ComponentInteractionCreateEvent> {
            check {
                failIf("Error: message ids don't match, please inform an Admin about this issue!") {
                    val interaction = this.event.interaction
                    val message = interaction.message
                    val guild = message?.getGuild()
                    val guildSettings = guild?.let { RoleBotDatabase.guildSettings.findGuild(it) }

                    /* return */
                    interaction.user.isBot ||
                        interaction.message?.id != guildSettings?.roleChannelData?.roleChannelMessage
                }

                respondIfFailed()
            }

            action {
                val interaction = event.interaction
                val ack = interaction.acknowledgeEphemeral()
                val guild = interaction.message?.getGuild()
                if (guild == null) {
                    ack.edit {
                        content = translate("button.role.error")
                    }
                    return@action
                }
                val member = guild.getMember(event.interaction.user.id)
                val role = try {
                    RoleBotDatabase.roles.find().toList().first { it.roleId == Snowflake(interaction.componentId.toLong()) }
                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                    ack.edit {
                        content = translate("button.role.error")
                    }
                    return@action
                }

                val guildRole = guild.getRole(role.roleId)

                var isAdd = false

                if (member.hasRole(guildRole)) {
                    member.removeRole(role.roleId)
                } else {
                    member.addRole(role.roleId)
                    isAdd = true
                }

                ack.edit {
                    content = if (isAdd) {
                        translate("button.role.add", arrayOf(guildRole.mention))
                    } else {
                        translate("button.role.remove", arrayOf(guildRole.mention))
                    }
                }
                return@action
            }
        }
    }
}
