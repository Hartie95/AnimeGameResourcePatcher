package org.anime_game_servers.gc_resource_patcher.data.quest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey
import org.anime_game_servers.gc_resource_patcher.data.interfaces.StringKey

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestCondition(
    @JsonNames("type", "_type")
    val type: String? = null,
    @JsonNames("param_str", "_param_str")
    val paramString: String? = null,
    @JsonNames("param", "_param")
    val param: List<Int>? = null
) : StringKey {
    override fun getStringKey() = (type ?: "")+(param?.getOrNull(0) ?: "")
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestExec(
    @JsonNames("type", "_type")
    val type: String? = null,
    @JsonNames("param", "_param")
    val param: List<String>? = null
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuestGuide(
    @JsonNames("type", "_type")
    val type: String? = null,
    @JsonNames("param", "_param")
    val param: List<String>? = null,
    val guideScene: Int = -1,
    val guideStyle: String? = null,
    val guideLayer: String? = null
)

@Serializable
data class GainItem (
    val itemId: Int = -1,
    val count: Int = -1
) : IntKey{
    override fun getIntKey() = itemId
}
