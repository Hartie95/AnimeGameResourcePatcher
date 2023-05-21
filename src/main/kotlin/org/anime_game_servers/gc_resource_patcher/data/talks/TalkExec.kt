package org.anime_game_servers.gc_resource_patcher.data.talks

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class TalkExec(
    @JsonNames("type", "_type")
    val type: String? = null,
    @JsonNames("param", "_param")
    val param: List<String>? = null
)
