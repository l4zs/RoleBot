package de.l4zs.rolebot.core.io

import de.l4zs.rolebot.config.Config
import de.l4zs.rolebot.module.role.Role
import de.l4zs.rolebot.module.setting.BotGuild
import de.l4zs.rolebot.module.setting.BotUser
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {

    private val client = KMongo.createClient(Config.MONGO_URL).coroutine
    private val database = client.getDatabase(Config.MONGO_DATABASE)

    val users = database.getCollection<BotUser>("users")
    val guildSettings = database.getCollection<BotGuild>("guild_settings")
    val roles = database.getCollection<Role>("roles")
}
