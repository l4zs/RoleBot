package de.l4zs.rolebot.core.io

import de.l4zs.rolebot.module.role.Role
import de.l4zs.rolebot.module.setting.BotGuild
import de.l4zs.rolebot.module.setting.BotUser
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object RoleBotDatabase : KoinComponent {

    val users = database.getCollection<BotUser>("users")
    val guildSettings = database.getCollection<BotGuild>("guild_settings")
    val roles = database.getCollection<Role>("roles")
}
