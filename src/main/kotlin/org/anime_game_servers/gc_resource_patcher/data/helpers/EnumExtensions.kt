package org.anime_game_servers.gc_resource_patcher.data.helpers

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? =
    runCatching { enumValueOf<T>(name) }.getOrNull()

inline fun <reified T : Enum<T>> enumValueOfOrDefault(name: String, default: T): T =
    runCatching { enumValueOf<T>(name) }.getOrNull()?:default

/**
 * return null if not set, so name == null, return fallback if it's an unknown value
 */
inline fun <reified T : Enum<T>> nullableEnumValueOfOrDefault(name: String?, default: T): T? =
    name?.let { runCatching { enumValueOf<T>(name) }.getOrNull()?:default}
