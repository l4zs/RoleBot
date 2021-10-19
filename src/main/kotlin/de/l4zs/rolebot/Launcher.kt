package de.l4zs.rolebot

import ch.qos.logback.classic.Logger
import de.l4zs.rolebot.config.Config
import de.l4zs.rolebot.core.RoleBot
import io.sentry.Sentry
import io.sentry.SentryOptions
import org.slf4j.LoggerFactory
import java.security.Security

suspend fun main() {
    initializeLogging()
    initializeSentry()

    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")
    Security.setProperty("securerandom.drbg.config", "HMAC_DRBG,SHA-512,256,pr_and_reseed")

    RoleBot().start()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Config.LOG_LEVEL
}

private fun initializeSentry() {
    val configure: (SentryOptions) -> Unit =
        if (Config.ENVIRONMENT.useSentry) {
            { it.dsn = Config.SENTRY_TOKEN }
        } else {
            { it.dsn = "" }
        }

    Sentry.init(configure)
}
