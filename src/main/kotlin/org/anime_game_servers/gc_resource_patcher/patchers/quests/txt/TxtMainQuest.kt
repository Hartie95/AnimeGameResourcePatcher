package org.anime_game_servers.gc_resource_patcher.patchers.quests.txt

import kotlinx.serialization.Serializable

@Serializable
data class TxtMainQuest(
    val mainId:Int,
    val series:Int = -1,
    val taskGroup:Int = -1,
    val taskType: Int = -1,
    val enableMode: Int = -1,
    val luaPath: String?= null,
    val recommendedLevel: Int = -1,
    val repeatable: Int = -1,
    val rewardIdList: String? = null,
    val chapterId: Int = -1,
    val eventId: Int = -1,
    val activityId: String? = null,
    val activityType: Int = -1,
    val videoKey: String? = null
){
    fun getRewardIdList(): List<Int>? {
        return rewardIdList?.split(",")?.map { it.toInt() }
    }
}
