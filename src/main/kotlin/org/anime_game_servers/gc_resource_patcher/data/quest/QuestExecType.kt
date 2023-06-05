package org.anime_game_servers.gc_resource_patcher.data.quest

import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey

enum class QuestExecType(val id: Int) : IntKey {
    /**
     * No action
     */
    QUEST_EXEC_NONE (0),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEL_PACK_ITEM (1),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UNLOCK_POINT (2),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UNLOCK_AREA (3),

    /**
     * TODO document specifics
     * (currently unused on official)
     */
    QUEST_EXEC_UNLOCK_FORCE (4),

    /**
     * TODO document specifics
     * (currently unused on official)
     */
    QUEST_EXEC_LOCK_FORCE (5),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_CHANGE_AVATAR_ELEMET (6),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REFRESH_GROUP_MONSTER (7),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_IS_FLYABLE (8),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_IS_WEATHER_LOCKED (9),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_IS_GAME_TIME_LOCKED (10),

    /**
     * TODO document specifics
     * (currently unused on official)
     */
    QUEST_EXEC_SET_IS_TRANSFERABLE (11),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_GRANT_TRIAL_AVATAR (12),

    /**
     * TODO document specifics
     * (currently unused)
     */
    QUEST_EXEC_OPEN_BORED (13),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ROLLBACK_QUEST (14),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_NOTIFY_GROUP_LUA (15),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_OPEN_STATE (16),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_LOCK_POINT (17),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEL_PACK_ITEM_BATCH (18),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REFRESH_GROUP_SUITE (19),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REMOVE_TRIAL_AVATAR (20),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_GAME_TIME (21),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_WEATHER_GADGET (22),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ADD_QUEST_PROGRESS (23),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_NOTIFY_DAILY_TASK (24),

    /**
     * TODO document specifics
     * (currently only used for random quests on official)
     */
    QUEST_EXEC_CREATE_PATTERN_GROUP (25),
    /**
     * TODO document specifics
     * (currently only used for random quests on official)
     */
    QUEST_EXEC_REMOVE_PATTERN_GROUP (26),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REFRESH_GROUP_SUITE_RANDOM (27),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ACTIVE_ITEM_GIVING (28),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEL_ALL_SPECIFIC_PACK_ITEM (29),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ROLLBACK_PARENT_QUEST (30),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_LOCK_AVATAR_TEAM (31),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UNLOCK_AVATAR_TEAM (32),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UPDATE_PARENT_QUEST_REWARD_INDEX (33),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_DAILY_TASK_VAR (34),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_INC_DAILY_TASK_VAR (35),

    /**
     * TODO document specifics
     * (currently unused on official)
     */
    QUEST_EXEC_DEC_DAILY_TASK_VAR (36),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ACTIVE_ACTIVITY_COND_STATE (37),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_INACTIVE_ACTIVITY_COND_STATE (38),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ADD_CUR_AVATAR_ENERGY (39),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_START_BARGAIN (41),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_STOP_BARGAIN (42),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_QUEST_GLOBAL_VAR (43),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_INC_QUEST_GLOBAL_VAR (44),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEC_QUEST_GLOBAL_VAR (45),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REGISTER_DYNAMIC_GROUP (46),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UNREGISTER_DYNAMIC_GROUP (47),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_SET_QUEST_VAR (48),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_INC_QUEST_VAR (49),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEC_QUEST_VAR (50),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_RANDOM_QUEST_VAR (51),

    /**
     * TODO document specifics
     * (currently unused on official)
     */
    QUEST_EXEC_ACTIVATE_SCANNING_PIC (52),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_RELOAD_SCENE_TAG (53),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_REGISTER_DYNAMIC_GROUP_ONLY (54),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_CHANGE_SKILL_DEPOT (55),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_ADD_SCENE_TAG (56),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEL_SCENE_TAG (57),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_INIT_TIME_VAR (58),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_CLEAR_TIME_VAR (59),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_MODIFY_CLIMATE_AREA (60),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_GRANT_TRIAL_AVATAR_AND_LOCK_TEAM (61),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_CHANGE_MAP_AREA_STATE (62),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_DEACTIVE_ITEM_GIVING (63),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_CHANGE_SCENE_LEVEL_TAG (64),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_UNLOCK_PLAYER_WORLD_SCENE (65),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_LOCK_PLAYER_WORLD_SCENE (66),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_FAIL_MAINCOOP (67),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_MODIFY_WEATHER_AREA (68),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_MODIFY_ARANARA_COLLECTION_STATE (69),

    /**
     * TODO document specifics
     */
    QUEST_EXEC_GRANT_TRIAL_AVATAR_BATCH_AND_LOCK_TEAM (70),

    /**
     * An unknown condition type, check the representation string to get the missing enum values name
     */
    QUEST_EXEC_UNKNOWN (9999);

    override fun getIntKey() = id
}