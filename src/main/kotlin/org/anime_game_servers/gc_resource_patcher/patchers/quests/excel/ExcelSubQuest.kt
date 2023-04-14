package org.anime_game_servers.gc_resource_patcher.patchers.quests.excel

import kotlinx.serialization.Serializable
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestCondition
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestExec
import org.anime_game_servers.gc_resource_patcher.patchers.quests.shared.QuestGuide

@Serializable
data class ExcelSubQuest(
    val subId: Int,
    val mainId: Int = -1,
    val order: Int = -1,
    val isMpBlock: Boolean? = null,
    val isRewind: Boolean? = null,
    val finishParent: Boolean? = null,
    val showType: String? = null,

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
    val trialAvatarList: List<Int>? = null,
    val exclusivePlaceList: List<Int>? = null,

    // Textmaps
    val descTextMapHash: Long = -1,
    val stepDescTextMapHash: Long = -1,
    val guideTipsTextMapHash: Long = -1,
)