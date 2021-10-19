package de.l4zs.rolebot.util

import com.kotlindiscord.kord.extensions.extensions.Extension

/**
 * Allows to lazily access other extensions in an [Extension].
 *
 * Example:
 * ```kotlin
 * val roleModule: RoleModule by extension()
 * ```
 */
inline fun <reified T> Extension.extension(): Lazy<T> = lazy { bot.findExtension<T>()!! }
