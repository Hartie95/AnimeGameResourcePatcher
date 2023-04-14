package org.anime_game_servers.gc_resource_patcher.patchers.quests.txt

import kotlinx.serialization.Serializable

@Serializable
data class TxtMainQuest(
    val mainId:Int,
    val collectionId:Int = -1,
    val series:Int = -1,
    val taskType: Int = -1,
    val activeMode: Int = -1,
    val luaPath: String?= null,
    val recommendedLevel: Int = -1,
    val repeatable: Int = -1,
    val rewardIdList: String? = null,
    val chapterId: Int = -1,
    val taskId: Int = -1,
    val activityId: Int = -1,
    val activityType: Int = -1,
    val videoKey: Long = -1
){
    fun getTaskTypeString():String?{
        return when(taskType){
            0 -> "AQ"
            1 -> "FQ"
            2 -> "LQ"
            3 -> "EQ"
            4 -> "DQ"
            5 -> "IQ"
            6 -> "VQ"
            7 -> "WQ"
            else -> null
        }
    }
    fun getRewardIdList(): List<Int>? {
        return rewardIdList?.split(",")?.map { it.toInt() }
    }
    fun getActiveModeString():String? {
        return when(activeMode){
            0 -> "PLAY_MODE_ALL"
            1 -> "PLAY_MODE_SINGLE"
            2 -> "PLAY_MODE_MULTIPLE"
            else -> null
        }
    }
    fun getRepeatableBool():Boolean? {
        return when(repeatable){
            0 -> false
            1 -> true
            else -> null
        }
    }
}
