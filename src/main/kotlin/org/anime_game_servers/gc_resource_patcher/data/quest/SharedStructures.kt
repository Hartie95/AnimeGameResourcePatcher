package org.anime_game_servers.gc_resource_patcher.data.quest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
import org.anime_game_servers.gc_resource_patcher.data.helpers.nullableEnumValueOfOrDefault
import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey
import org.anime_game_servers.gc_resource_patcher.data.interfaces.StringKey

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestContent(
    @JsonNames("type", "_type")
    val typeString: String? = null,
    @Transient
    val type: QuestContentType? = nullableEnumValueOfOrDefault(typeString, QuestContentType.QUEST_CONTENT_UNKNOWN),
    @JsonNames("param_str", "_param_str")
    val paramString: String? = null,
    @JsonNames("param", "_param")
    val param: List<Int>? = null,
    @JsonNames("count", "_count")
    val count: Int = -1
) : StringKey {
    override fun getStringKey() = (typeString ?: "")+(param?.getOrNull(0) ?: "")
}
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestCondition(
    @JsonNames("type", "_type")
    val typeString: String? = null,
    @Transient
    val type: QuestCondType? = nullableEnumValueOfOrDefault(typeString, QuestCondType.QUEST_COND_UNKNOWN),
    @JsonNames("param", "_param")
    val param: List<Int>? = null,
) : StringKey {
    override fun getStringKey() = (typeString ?: "")+(param?.getOrNull(0) ?: "")
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestExec(
    @JsonNames("type", "_type")
    val typeString: String? = null,
    @Transient
    val type: QuestExecType? = nullableEnumValueOfOrDefault(typeString, QuestExecType.QUEST_EXEC_UNKNOWN),
    @JsonNames("param", "_param")
    val param: List<String>? = null
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestGuide(
    @JsonNames("type", "_type")
    val typeString: String? = null,
    @Transient
    val type: QuestGuideType? = nullableEnumValueOfOrDefault(typeString, QuestGuideType.QUEST_GUIDE_UNKNOWN),
    @JsonNames("param", "_param")
    val param: List<String>? = null,
    val guideScene: Int = -1,
    val guideStyle: String? = null,
    val guideLayer: String? = null,
    val autoGuide: String? = null,
)

enum class QuestGuideType(val id: Int) : IntKey {
    QUEST_GUIDE_NONE(0),
    QUEST_GUIDE_LOCATION(1),
    QUEST_GUIDE_NPC(2),
    QUEST_GUIDE_GADGET(3),
    QUEST_GUIDE_SHOW_OR_HIDE_NPC(4),
    QUEST_GUIDE_DUNGEON_ENTRY(5),
    QUEST_GUIDE_UNKNOWN(9999);

    override fun getIntKey() = id
}

@Serializable
data class GainItem (
    val itemId: Int = -1,
    val count: Int = -1
) : IntKey{
    override fun getIntKey() = itemId
}
