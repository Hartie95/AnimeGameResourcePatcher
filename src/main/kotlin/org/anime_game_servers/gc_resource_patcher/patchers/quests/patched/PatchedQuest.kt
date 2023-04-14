package org.anime_game_servers.gc_resource_patcher.patchers.quests.patched

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.GainItem
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestCondition
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestExec

@Serializable
data class PatchedQuest(
    val mainId: Int,
    var collectionId:Int = -1,
    var series: Int = -1,
    var chapterId: Int = -1,
    var type: String? = null,

    var luaPath: String? = null,
    var repeatable: Boolean? = null,
    var recommendedLevel: Int = -1,
    var showType: String? = null,
    var suggestTrackOutOfOrder: Boolean? = null,
    var suggestTrackMainQuestList: List<Int>? = null,
    var rewardIdList: List<Int>? = null,
    var activityId: Int = -1,
    var taskId: Int = -1,
    var videoKey: Long = -1,

    var activeMode: String?= null,
    var mainQuestTag: String? = null,

    var subQuests: List<PatchSubQuest>? = null,
    var talks: List<TalkDataBinout>? = null,


    // Textmaps
    var titleTextMapHash: Long = -1,
    var descTextMapHash: Long = -1,

    ) {

    @Serializable
    data class PatchSubQuest(
        val subId: Int,
        val mainId: Int = -1,
        var order: Int = -1,
        var showType: String? = null,
        var finishParent: Boolean? = null,
        var isRewind: Boolean? = null,

        var luaPath: String? = null,
        var repeatable: Boolean? = null,
        var suggestTrackOutOfOrder: Boolean? = null,

        var versionBegin: String? = null,
        var versionEnd: String? = null,

        // quest conditions
        var acceptCondComb: String? = null,
        var acceptCond: List<QuestCondition>? = null,
        var finishCondComb: String? = null,
        var finishCond: List<QuestCondition>? = null,
        var failCondComb: String? = null,
        var failCond: List<QuestCondition>? = null,


        // quest execs
        var beginExec: List<QuestExec>? = null,
        var finishExec: List<QuestExec>? = null,
        var failExec: List<QuestExec>? = null,

        var gainItems: List<GainItem>? = null,


        // Textmaps
        var descTextMapHash: Long = -1,
        var stepDescTextMapHash: Long = -1,
    )

    @Serializable
    data class TalkDataBinout(
        val id: Int,
        val heroTalk: String? = null,
        val beginWay: String? = null,
        val beginCond: List<TalkBeginCond>? = null,
        val priority: Int = -1,
        val initDialog: Int = -1,
        val npcIds: List<Int>? = null,
        val performCfg: String? = null,
        val questId: Int = -1,
        val prePerformCfg: String? = null,
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class TalkBeginCond(
        @JsonNames("type", "_type")
        val type: String? = null,
        @JsonNames("params", "_params")
        val params: List<String>? = null
    )
}