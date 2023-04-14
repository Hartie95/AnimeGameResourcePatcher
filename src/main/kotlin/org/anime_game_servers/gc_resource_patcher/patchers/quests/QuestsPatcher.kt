package org.anime_game_servers.gc_resource_patcher.patchers.quests

import app.softwork.serialization.csv.CSVFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.anime_game_servers.gc_resource_patcher.patchers.quests.QuestsPatcher.ifIsSet
import org.anime_game_servers.gc_resource_patcher.patchers.quests.binout.BinoutQuest
import org.anime_game_servers.gc_resource_patcher.patchers.quests.excel.ExcelMainQuest
import org.anime_game_servers.gc_resource_patcher.patchers.quests.excel.ExcelSubQuest
import org.anime_game_servers.gc_resource_patcher.patchers.quests.patched.PatchedQuest
import org.anime_game_servers.gc_resource_patcher.patchers.quests.txt.TxtMainQuest
import java.io.File
import kotlin.io.path.Path

@OptIn(ExperimentalSerializationApi::class)
object QuestsPatcher {
    const val excelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.json"
    const val oldExcelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.old.json"
    const val excelMainFileName : String = "ExcelBinOutput/MainQuestExcelConfigData.json"

    const val txtMainFileName = "txt/MainQuestData.txt"

    const val binoutFolder = "BinOutput/Quest/"

    const val patchFolder = "Patches/Quest/"
    const val outputFolder = "Generated/Quest/"

    private val jsonSerializer = Json { ignoreUnknownKeys = true }
    private val txtSerializer = CSVFormat(separator = "\t")

    fun patch() {
        val excelSubQuests = readExcelSubFile(excelSubFileName)
        val excelSubQuestsOld = readExcelSubFile(oldExcelSubFileName)

        val excelMainQuests = readExcelMainFile(excelMainFileName)
        val txtMainQuests = readTxtMainQuests(txtMainFileName)

        val binoutQuests = readBinoutFiles(binoutFolder)
        val questsPatches = readPatchFiles(patchFolder)

        val mainIds = mutableSetOf<Int>()
        val subIds = mutableSetOf<Int>()
        val mainSubIds = mutableMapOf<Int, MutableSet<Int>>()

        excelMainQuests?.forEach { (key, value) ->
            mainIds+= key
        }
        txtMainQuests?.forEach { (key, value) ->
            mainIds+= key
        }
        binoutQuests?.forEach { (key, value) ->
            mainIds+= key
            value.subQuests?.forEach { subQuest ->
                subIds+=subQuest.subId
                mainSubIds.getOrPut(subQuest.mainId) { mutableSetOf() }+=subQuest.subId
            }
        }
        questsPatches?.forEach { (key, value) ->
            mainIds+= key
            value.subQuests?.forEach { subQuest ->
                subIds+=subQuest.subId
                mainSubIds.getOrPut(subQuest.mainId) { mutableSetOf() }+=subQuest.subId
            }
        }
        excelSubQuests?.forEach { (key, value) ->
            subIds += key
            mainIds += value.mainId
            mainSubIds.getOrPut(value.mainId) { mutableSetOf() }+=key
        }
        excelSubQuestsOld?.forEach { (key, value) ->
            subIds += key
            mainIds += value.mainId
            mainSubIds.getOrPut(value.mainId) { mutableSetOf() }+=key
        }

        val generatedQuests = mutableMapOf<Int, PatchedQuest>()
        mainIds.forEach {mainId ->
            val mainQuest = PatchedQuest(mainId)
            val subQuests = mutableMapOf<Int, PatchedQuest.PatchSubQuest>()
            mainSubIds[mainId]?.forEach { subId->
                val subQuest = PatchedQuest.PatchSubQuest(subId, mainId)

                excelSubQuestsOld?.get(subId)?.let {
                    subQuest.merge(it)
                }
                excelSubQuests?.get(subId)?.let {
                    subQuest.merge(it)
                }

                subQuests[subId]=subQuest
            }
            excelMainQuests?.get(mainId)?.let {
                mainQuest.merge(it)
            }
            txtMainQuests?.get(mainId)?.let {
                mainQuest.merge(it)
            }


            binoutQuests?.get(mainId)?.let {
                mainQuest.merge(it)
                it.subQuests?.forEach { subQuest ->
                    subQuests[subQuest.subId]?.merge(subQuest)
                }
            }

            questsPatches?.get(mainId)?.let {
                mainQuest.merge(it)
                it.subQuests?.forEach { subQuest ->
                    subQuests[subQuest.subId]?.merge(subQuest)
                }
            }

            mainQuest.subQuests=subQuests.map { it.value }
            generatedQuests[mainId] = mainQuest
        }

        generatedQuests.forEach {
            println(it.toString())
        }

        generatedQuests.forEach { (key, value) ->
            File("$outputFolder$key.json").writeText(jsonSerializer.encodeToString(PatchedQuest.serializer(), value))
        }
    }

    private fun PatchedQuest.merge(excelMainQuest: ExcelMainQuest){
        excelMainQuest.type?.let {
            this.type = it
        }
        excelMainQuest.collectionId.ifIsSet {
            this.collectionId = it
        }
        excelMainQuest.luaPath?.let {
            this.luaPath = it
        }
        excelMainQuest.repeatable?.let {
            this.repeatable = it
        }
        excelMainQuest.recommendedLevel.ifIsSet {
            this.recommendedLevel = it
        }
        excelMainQuest.suggestTrackMainQuestList?.let {
            this.suggestTrackMainQuestList = it
        }
        excelMainQuest.rewardIdList?.let {
            this.rewardIdList = it
        }
        excelMainQuest.showType?.let {
            this.showType = it
        }
        excelMainQuest.activeMode?.let {
            this.activeMode = it
        }
        excelMainQuest.mainQuestTag?.let {
            this.mainQuestTag = it
        }
        excelMainQuest.activityId.ifIsSet {
            this.activityId = it
        }
        excelMainQuest.chapterId.ifIsSet {
            this.chapterId = it
        }
        excelMainQuest.taskID.ifIsSet {
            this.taskId = it
        }
        excelMainQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
        excelMainQuest.stepDescTextMapHash.ifIsSet {
            // todo
        }
    }
    private fun PatchedQuest.merge(txtMainQuest: TxtMainQuest){
        txtMainQuest.series.ifIsSet {
            this.series = it
        }
        txtMainQuest.collectionId.ifIsSet {
            this.collectionId = it
        }
        txtMainQuest.getTaskTypeString()?.let {
            this.type = it
        }
        txtMainQuest.getActiveModeString()?.let {
            this.activeMode = it
        }
        txtMainQuest.luaPath?.let {
            this.luaPath = it
        }
        txtMainQuest.recommendedLevel.ifIsSet {
            this.recommendedLevel = it
        }
        txtMainQuest.getRepeatableBool()?.let {
            this.repeatable = it
        }
        txtMainQuest.getRewardIdList()?.let {
            this.rewardIdList = it
        }
        txtMainQuest.chapterId.ifIsSet {
            this.chapterId = it
        }
        txtMainQuest.taskId.ifIsSet {
            this.taskId = it
        }
        txtMainQuest.activityId.ifIsSet {
            this.activityId = it
        }
        txtMainQuest.activityType.ifIsSet {
            // todo
        }
        txtMainQuest.videoKey.ifIsSet {
            this.videoKey = it
        }
    }

    private fun PatchedQuest.merge(binoutMainQuest: BinoutQuest){
        binoutMainQuest.collectionId.ifIsSet {
            this.collectionId = it
        }
        binoutMainQuest.series.ifIsSet {
            this.series = it
        }
        binoutMainQuest.chapterId.ifIsSet {
            this.chapterId = it
        }
        binoutMainQuest.type?.let {
            this.type = it
        }
        binoutMainQuest.luaPath?.let {
            this.luaPath = it
        }
        binoutMainQuest.recommendedLevel.ifIsSet {
            this.recommendedLevel = it
        }
        binoutMainQuest.showType?.let {
            this.showType = it
        }
        binoutMainQuest.activeMode?.let {
            this.activeMode = it
        }
        binoutMainQuest.activityId.ifIsSet {
            this.activityId = it
        }
        binoutMainQuest.mainQuestTag?.let {
            this.mainQuestTag = it
        }
        binoutMainQuest.taskId.ifIsSet {
            this.taskId = it
        }
        binoutMainQuest.repeatable?.let {
            this.repeatable = it
        }
        binoutMainQuest.suggestTrackOutOfOrder?.let {
            this.suggestTrackOutOfOrder = it
        }
        binoutMainQuest.suggestTrackMainQuestList?.let {
            this.suggestTrackMainQuestList = it
        }
        binoutMainQuest.rewardIdList?.let {
            this.rewardIdList = it
        }
        binoutMainQuest.talks?.let {
            //this.talks = it //todo
        }
        binoutMainQuest.titleTextMapHash.ifIsSet {
            this.titleTextMapHash = it
        }
        binoutMainQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
    }
    private fun PatchedQuest.merge(patchedQuest: PatchedQuest){
        patchedQuest.collectionId.ifIsSet {
            this.collectionId = it
        }
        patchedQuest.series.ifIsSet {
            this.series = it
        }
        patchedQuest.chapterId.ifIsSet {
            this.chapterId = it
        }
        patchedQuest.type?.let {
            this.type = it
        }
        patchedQuest.luaPath?.let {
            this.luaPath = it
        }
        patchedQuest.repeatable?.let {
            this.repeatable = it
        }
        patchedQuest.recommendedLevel.ifIsSet {
            this.recommendedLevel = it
        }
        patchedQuest.showType?.let {
            this.showType = it
        }
        patchedQuest.activeMode?.let {
            this.activeMode = it
        }
        patchedQuest.mainQuestTag?.let {
            this.mainQuestTag = it
        }
        patchedQuest.suggestTrackOutOfOrder?.let {
            this.suggestTrackOutOfOrder = it
        }
        patchedQuest.suggestTrackMainQuestList?.let {
            this.suggestTrackMainQuestList = it
        }
        patchedQuest.rewardIdList?.let {
            this.rewardIdList = it
        }
        patchedQuest.activityId.ifIsSet {
            this.activityId = it
        }
        patchedQuest.taskId.ifIsSet {
            this.taskId = it
        }
        patchedQuest.videoKey.ifIsSet {
            this.videoKey = it
        }
        patchedQuest.activeMode?.let {
            this.activeMode = it
        }
        patchedQuest.mainQuestTag?.let {
            this.mainQuestTag = it
        }
        patchedQuest.talks?.let {
            this.talks = it
        }
        patchedQuest.titleTextMapHash.ifIsSet {
            this.titleTextMapHash = it
        }
        patchedQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
    }

    private fun PatchedQuest.PatchSubQuest.merge(excelQuest:ExcelSubQuest){
        excelQuest.order.ifIsSet {
            this.order = it
        }
        excelQuest.isRewind?.let {
            this.isRewind = it
        }
        excelQuest.finishParent?.let {
            this.finishParent = it
        }

        excelQuest.acceptCondComb?.let {
            this.acceptCondComb = it
        }
        excelQuest.acceptCond?.let {
            this.acceptCond = it
        }
        excelQuest.finishCondComb?.let {
            this.finishCondComb = it
        }
        excelQuest.finishCond?.let {
            this.finishCond = it
        }
        excelQuest.failCondComb?.let {
            this.failCondComb = it
        }
        excelQuest.failCond?.let {
            this.failCond = it
        }

        excelQuest.beginExec?.let {
            this.beginExec = it
        }
        excelQuest.finishExec?.let {
            this.finishExec = it
        }
        excelQuest.failExec?.let {
            this.failExec = it
        }
        excelQuest.guide?.let {
            //todo
        }
        excelQuest.banType?.let {
            //todo
        }
        excelQuest.exclusiveNpcList?.let {
            //todo
        }
        excelQuest.exclusiveNpcPriority.ifIsSet {
            //todo
        }
        excelQuest.sharedNpcList?.let {
            //todo
        }
        excelQuest.trialAvatarList?.let {
            //todo
        }
        excelQuest.exclusivePlaceList?.let {
            //todo
        }
        excelQuest.guide?.let {
            //todo
        }
        excelQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
        excelQuest.stepDescTextMapHash.ifIsSet {
            this.stepDescTextMapHash = it
        }
        excelQuest.guideTipsTextMapHash.ifIsSet {
            //todo
        }

    }
    private fun PatchedQuest.PatchSubQuest.merge(binoutQuest:BinoutQuest.BinoutSubQuest){
        binoutQuest.order.ifIsSet {
            this.order = it
        }
        binoutQuest.showType?.let {
            this.showType = it
        }
        binoutQuest.finishParent?.let {
            this.finishParent = it
        }
        binoutQuest.isRewind?.let {
            this.isRewind = it
        }
        binoutQuest.luaPath?.let {
            this.luaPath = it
        }
        binoutQuest.repeatable?.let {
            this.repeatable = it
        }
        binoutQuest.suggestTrackOutOfOrder?.let {
            this.suggestTrackOutOfOrder = it
        }
        binoutQuest.versionBegin?.let {
            this.versionBegin = it
        }
        binoutQuest.versionEnd?.let {
            this.versionEnd = it
        }
        binoutQuest.acceptCondComb?.let {
            this.acceptCondComb = it
        }
        binoutQuest.acceptCond?.let {
            this.acceptCond = it
        }
        binoutQuest.finishCondComb?.let {
            this.finishCondComb = it
        }
        binoutQuest.finishCond?.let {
            this.finishCond = it
        }
        binoutQuest.failCondComb?.let {
            this.failCondComb = it
        }
        binoutQuest.failCond?.let {
            this.failCond = it
        }
        binoutQuest.beginExec?.let {
            this.beginExec = it
        }
        binoutQuest.finishExec?.let {
            this.finishExec = it
        }
        binoutQuest.failExec?.let {
            this.failExec = it
        }
        binoutQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
        binoutQuest.stepDescTextMapHash.ifIsSet {
            this.stepDescTextMapHash = it
        }
    }
    private fun PatchedQuest.PatchSubQuest.merge(patchSubQuest:PatchedQuest.PatchSubQuest){
        patchSubQuest.order.ifIsSet {
            this.order = it
        }
        patchSubQuest.showType?.let {
            this.showType = it
        }
        patchSubQuest.finishParent?.let {
            this.finishParent = it
        }
        patchSubQuest.isRewind?.let {
            this.isRewind = it
        }
        patchSubQuest.luaPath?.let {
            this.luaPath = it
        }
        patchSubQuest.repeatable?.let {
            this.repeatable = it
        }
        patchSubQuest.suggestTrackOutOfOrder?.let {
            this.suggestTrackOutOfOrder = it
        }
        patchSubQuest.versionBegin?.let {
            this.versionBegin = it
        }
        patchSubQuest.versionEnd?.let {
            this.versionEnd = it
        }
        patchSubQuest.acceptCondComb?.let {
            this.acceptCondComb = it
        }
        patchSubQuest.acceptCond?.let {
            this.acceptCond = it
        }
        patchSubQuest.finishCondComb?.let {
            this.finishCondComb = it
        }
        patchSubQuest.finishCond?.let {
            this.finishCond = it
        }
        patchSubQuest.failCondComb?.let {
            this.failCondComb = it
        }
        patchSubQuest.failCond?.let {
            this.failCond = it
        }
        patchSubQuest.beginExec?.let {
            this.beginExec = it
        }
        patchSubQuest.finishExec?.let {
            this.finishExec = it
        }
        patchSubQuest.failExec?.let {
            this.failExec = it
        }
        patchSubQuest.gainItems?.let {
            this.gainItems = it
        }
        patchSubQuest.descTextMapHash.ifIsSet {
            this.descTextMapHash = it
        }
        patchSubQuest.stepDescTextMapHash.ifIsSet {
            this.stepDescTextMapHash = it
        }
    }

    private fun Int.ifIsSet(function:(Int)->Unit){
        if(this != -1)
            function.invoke(this)
    }
    private fun Long.ifIsSet(function:(Long)->Unit){
        if(this != -1L)
            function.invoke(this)
    }


    fun readExcelSubFile(excelFileName : String) : Map<Int, ExcelSubQuest>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(ExcelSubQuest.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.subId }
    }

    fun readExcelMainFile(excelFileName : String) : Map<Int, ExcelMainQuest>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(ExcelMainQuest.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.id }
    }

    fun readBinoutFiles(binoutFileName: String) : Map<Int, BinoutQuest>? {
        val config = BinoutQuest.serializer()
        return Path(binoutFileName).toFile().listFiles()?.associate{
            val quest = it.inputStream().use { stream ->
                try {
                    jsonSerializer.decodeFromStream(config, stream)
                }catch (ex: Throwable){
                    println("error in file ${it.name}")
                    ex.printStackTrace()
                    BinoutQuest(0)
                }
            }
            Pair(quest.id, quest)
        }
    }

    fun readTxtMainQuests(txtFileName:String): Map<Int, TxtMainQuest>?{
        val file = File(txtFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(TxtMainQuest.serializer())
        //return txtSerializer.decodeFromString(config, excelText).associateBy { it.mainId }
        return null
    }

    fun readPatchFiles(patchFolderName:String) : Map<Int, PatchedQuest>?{
        val config = PatchedQuest.serializer()
        return Path(patchFolderName).toFile().listFiles()?.associate<File, Int, PatchedQuest> {
            val quest = it.inputStream().use { stream ->
                    try {
                        jsonSerializer.decodeFromStream(config, stream)
                    }catch (ex: Throwable){
                        println("error in file ${it.name}")
                        ex.printStackTrace()
                        PatchedQuest(0)
                    }
                }
                Pair(quest.mainId, quest)
        }
    }
}