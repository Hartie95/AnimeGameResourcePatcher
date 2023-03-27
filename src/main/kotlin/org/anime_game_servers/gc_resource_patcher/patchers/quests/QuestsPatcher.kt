package org.anime_game_servers.gc_resource_patcher.patchers.quests

import app.softwork.serialization.csv.CSVFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
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

    private val jsonSerializer = Json { ignoreUnknownKeys = true }
    private val txtSerializer = CSVFormat(separator = "\t")

    fun patch() {
        val excelSubQuests = readExcelSubFile(excelSubFileName)
        val excelSubQuestsOld = readExcelSubFile(oldExcelSubFileName)

        val excelMainQuests = readExcelMainFile(excelMainFileName)
        //val txtMainQuests = readTxtMainQuests(txtMainFileName)

        val binoutQuests = readBinoutFiles(binoutFolder)
        val questsPatches = readPatchFiles(patchFolder)
        questsPatches?.forEach {
            println(it.toString())
        }

    }

    fun readExcelSubFile(excelFileName : String) : List<ExcelSubQuest>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(ExcelSubQuest.serializer())
        return jsonSerializer.decodeFromString(config, excelText)
    }

    fun readExcelMainFile(excelFileName : String) : List<ExcelMainQuest>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(ExcelMainQuest.serializer())
        return jsonSerializer.decodeFromString(config, excelText)
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

    fun readTxtMainQuests(txtFileName:String): List<TxtMainQuest>?{
        val file = File(txtFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(TxtMainQuest.serializer())
        return txtSerializer.decodeFromString(config, excelText)
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