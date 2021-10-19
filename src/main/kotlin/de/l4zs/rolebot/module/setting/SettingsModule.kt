package de.l4zs.rolebot.module.setting

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import de.l4zs.rolebot.core.io.Database
import de.l4zs.rolebot.module.role.addRoleCommand
import de.l4zs.rolebot.module.role.reloadCommand
import de.l4zs.rolebot.module.role.removeRoleCommand
import dev.kord.common.entity.Permission
import org.koin.core.component.inject

typealias SuspendFunction = suspend () -> Unit

class SettingsModule : Extension() {
    override val name: String = "settings"
    override val bundle: String = "settings"
    val database: Database by inject()

    override suspend fun setup() {
        languageCommand()
        roleChannelCommand()
        addRoleCommand()
        removeRoleCommand()
        reloadCommand()
    }
}

fun SlashCommand<*, *>.guildAdminOnly() {
    check {
        anyGuild()
        hasPermission(Permission.ManageGuild)
    }
}