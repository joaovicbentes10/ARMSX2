package com.armsx2.nevoa.home

data class CategoryPool<T>(val activeCategory: String?, val games: List<T>)

/** Categories narrow the pool before search/sort. A removed category restores all games. */
fun <T> categoryPool(
    games: List<T>,
    selected: String?,
    names: List<String>,
    members: (String) -> Set<String>,
    key: (T) -> String?,
): CategoryPool<T> {
    val active = selected?.takeIf { it in names } ?: return CategoryPool(null, games)
    val keys = members(active)
    return CategoryPool(active, games.filter { game -> key(game)?.let { it in keys } == true })
}
