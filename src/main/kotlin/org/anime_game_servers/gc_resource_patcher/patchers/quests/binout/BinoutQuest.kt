package org.anime_game_servers.gc_resource_patcher.patchers.quests.binout

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestCondition
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestExec
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestGuide

@Serializable
data class BinoutQuest(
    val id: Int,
    // e.g. ICLLDPJFIMA
    val collectionId: Int = -1,
    val series: Int = -1,
    val chapterId: Int = -1,
    val type: String? = null,


    val luaPath: String? = null,
    @SerialName("recommendLevel")
    val recommendedLevel: Int = -1,
    val showType: String? = null,
    val repeatable: Boolean? = null,
    val suggestTrackOutOfOrder: Boolean? = null,
    val suggestTrackMainQuestList: List<Int>? = null,
    val rewardIdList: List<Int>? = null,
    val activeMode: String?= null,
    val activityId: Int = -1,
    val mainQuestTag: String? = null,
    val showRedPoint: Boolean? = null,
    val taskId: Int = -1,

    val subQuests: List<BinoutSubQuest>? = null,
    val talks: List<TalkDataBinout>? = null,


    // Textmaps
    val titleTextMapHash: Long = -1,
    val descTextMapHash: Long = -1,
) {


    @Serializable
    data class BinoutSubQuest(
        val subId: Int,
        val mainId: Int,
        val order: Int = -1,
        val isMpBlock: Boolean? = null,
        val showType: String? = null,
        val finishParent: Boolean? = null,
        val isRewind: Boolean? = null,

        val luaPath: String? = null,
        val repeatable: Boolean? = null,
        val suggestTrackOutOfOrder: Boolean? = null,
        val trialAvatarList: List<Int>? = null,

        val versionBegin: String? = null,
        val versionEnd: String? = null,

        // quest conditions
        val acceptCondComb: String? = null,
        val acceptCond: List<QuestCondition>? = null,
        val finishCondComb: String? = null,
        val finishCond: List<QuestCondition>? = null,
        val failCondComb: String? = null,
        val failCond: List<QuestCondition>? = null,


        // quest execs
        val beginExec: List<QuestExec>? = null,
        val finishExec: List<QuestExec>? = null,
        val failExec: List<QuestExec>? = null,

        val guide: QuestGuide? = null,
        val showGuide: String? = null,
        val banType: String? = null,
        val exclusiveNpcList: List<Int>? = null,
        val exclusiveNpcPriority: Int = -1,
        val sharedNpcList: List<Int>? = null,
        val exclusivePlaceList: List<Int>? = null,

        // Textmaps
        val descTextMapHash: Long = -1,
        val stepDescTextMapHash: Long = -1,
    )

    @Serializable
    data class TalkDataBinout(
        val id: Int,
        val heroTalk: String? = null,
        val beginWay: String? = null,
        val beginCond: List<BeginCond>? = null,
        val priority: Int = -1,
        val initDialog: Int = -1,
        val npcIds: List<Int>? = null,
        val performCfg: String? = null,
        val questId: Int = -1,
        val prePerformCfg: String? = null,
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class BeginCond(
        @JsonNames("type", "_type")
        val type: String? = null,
        @JsonNames("params", "_params")
        val params: List<String>? = null
    )
}
