package org.anime_game_servers.gc_resource_patcher.data.quest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey
import org.anime_game_servers.gc_resource_patcher.data.talks.TalkData

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MainQuestData(
    @JsonNames("id", "mainId")
    val id: Int,

    val collectionId: Int = -1,
    val series: Int = -1,
    val chapterId: Int = -1,
    @JsonNames("taskType", "type")
    val taskType: String? = null,


    val luaPath: String? = null,
    @SerialName("recommendLevel")
    val recommendedLevel: Int = -1,
    val showType: String? = null,
    val repeatable: Boolean? = null,
    val suggestTrackOutOfOrder: Boolean? = null,
    val suggestTrackMainQuestList: List<Int>? = null,
    val rewardIdList: List<Int>? = null,
    val activeMode: String? = null,
    val activityId: Int = -1,
    val mainQuestTag: String? = null,
    val showRedPoint: Boolean? = null,
    @JsonNames("taskId", "taskID")
    val taskId: Int = -1,

    // Textmaps
    val titleTextMapHash: Long = -1,
    val descTextMapHash: Long = -1,

    // only txt
    val activityType: Int = -1,
    val videoKey: Long = -1,

    // only binout and patched
    val subQuests: List<SubQuestData>? = null,
    val talks: List<TalkData>? = null,
): IntKey{
    override fun getIntKey() = id
}
