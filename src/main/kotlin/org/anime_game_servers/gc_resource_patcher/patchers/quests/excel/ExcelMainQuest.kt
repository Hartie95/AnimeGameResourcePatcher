package org.anime_game_servers.gc_resource_patcher.patchers.quests.excel

import kotlinx.serialization.Serializable

@Serializable
data class ExcelMainQuest(
    val id: Int,
    val type: String? = null,
    val activeMode: String?= null,
    val luaPath: String?= null,
    val repeatable: Boolean? = null,
    val suggestTrackMainQuestList: List<Int>? = null,
    val rewardIdList: List<Int>? = null,
    val showType: String? = null,

    // Textmaps
    val descTextMapHash: Long = -1,
    val stepDescTextMapHash: Long = -1,

)
