package org.anime_game_servers.gc_resource_patcher.patchers.quests.excel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExcelMainQuest(
    val id: Int,
    val type: String? = null,
    val collectionId:Int = -1,
    val activeMode: String?= null,
    val luaPath: String?= null,
    val repeatable: Boolean? = null,
    @SerialName("recommendLevel")
    val recommendedLevel: Int = -1,
    val suggestTrackMainQuestList: List<Int>? = null,
    val rewardIdList: List<Int>? = null,
    val showType: String? = null,
    val activityId: Int = -1,
    val chapterId: Int = -1,
    val taskID: Int = -1,
    val mainQuestTag: String? = null,

    // Textmaps
    val descTextMapHash: Long = -1,
    val stepDescTextMapHash: Long = -1,

)
