package de.l4zs.rolebot.core.io

import de.l4zs.rolebot.module.setting.BotGuild
import de.l4zs.rolebot.module.setting.BotUser
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import org.litote.kmongo.coroutine.CoroutineCollection

suspend fun CoroutineCollection<BotUser>.findUser(user: UserBehavior) =
    findOneById(user.id.value) ?: BotUser(user.id).also { save(it) }

suspend fun CoroutineCollection<BotGuild>.findGuild(guild: GuildBehavior) =
    findOneById(guild.id.value) ?: BotGuild(guild.id).also { save(it) }
