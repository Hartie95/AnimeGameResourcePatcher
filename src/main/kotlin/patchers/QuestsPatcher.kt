package patchers

import SerializationOptions
import kotlinx.serialization.ExperimentalSerializationApi
import org.anime_game_servers.game_data_models.data.quest.*
import java.io.File
import java.nio.file.Path
@OptIn(ExperimentalSerializationApi::class)
class QuestsPatcher(serializationOptions: SerializationOptions) : BasePatcher(serializationOptions){
    companion object {
        const val excelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.json"
        const val oldExcelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.old.json"
        const val excelMainFileName : String = "ExcelBinOutput/MainQuestExcelConfigData.json"

        const val txtMainFileName = "txt/MainQuestData.txt"

        const val binoutFolder = "BinOutput/Quest/"

        const val patchFolder = "Patches/Quest/"
        const val outputFolder = "Generated/Quest/"
    }

    fun patch(resourcesBaseDir:Path, patchesBaseDir:Set<Path>? = null,
              subExcelFiles: Set<File>?=null, mainExcelFiles: Set<File>?=null,
              addBinoutDir: Set<Path>?=null, addPatchesDir: Set<Path>?=null
    ) {
        val excelSubQuestsFiles: MutableSet<File> = mutableSetOf()
        val excelMainQuestsFiles: MutableSet<File> = mutableSetOf()
        val binoutDirs: MutableSet<Path> = mutableSetOf()
        val patchDirs: MutableSet<Path> = mutableSetOf()

        resourcesBaseDir.let {
            excelSubQuestsFiles += it.resolve(excelSubFileName).toFile()
            excelSubQuestsFiles += it.resolve(oldExcelSubFileName).toFile()
            excelMainQuestsFiles += it.resolve(excelMainFileName).toFile()
            binoutDirs.add(it.resolve(binoutFolder))
            patchDirs.add(it.resolve(patchFolder))
        }
        subExcelFiles?.let {
            excelSubQuestsFiles += it
        }
        mainExcelFiles?.let {
            excelMainQuestsFiles += it
        }
        addBinoutDir?.let {
            binoutDirs += it
        }
        patchesBaseDir?.forEach {
            patchDirs.add(it.resolve(patchFolder))
        }
        addPatchesDir?.let {
            patchDirs += it
        }

        val excelSubQuestsMapList = mutableListOf<Map<Int, SubQuestData>>()
        val excelMainQuestsMapList = mutableListOf<Map<Int, MainQuestData>>()
        val binoutQuestsList = mutableListOf<Map<Int, MainQuestData>>()
        val questsPatchesList = mutableListOf<Map<Int, MainQuestData>>()


        excelSubQuestsFiles.forEach {
            readExcelSubFile(it)?.let {
                excelSubQuestsMapList.add(it)
            }
        }

        excelMainQuestsFiles.forEach {
            readExcelMainFile(it)?.let {
                excelMainQuestsMapList.add(it)
            }
        }

        binoutDirs.forEach {
            readBinoutFiles(it)?.let {
                binoutQuestsList.add(it)
            }
        }

        patchDirs.forEach {
            readPatchFiles(it)?.let {
                questsPatchesList.add(it)
            }
        }

        val mainIds = mutableSetOf<Int>()
        val subIds = mutableSetOf<Int>()
        val mainSubIds = mutableMapOf<Int, MutableSet<Int>>()

        excelMainQuestsMapList.forEach { map ->
            map.keys.forEach { key ->
                mainIds += key
            }
        }
        binoutQuestsList.forEach {
            it.forEach { (key, value) ->
                mainIds += key
                value.subQuests?.forEach { subQuest ->
                    subIds += subQuest.subId
                    mainSubIds.getOrPut(subQuest.mainId) { mutableSetOf() } += subQuest.subId
                }
            }
        }

        questsPatchesList.forEach { patchMap ->
            patchMap.forEach { (key, value) ->
                mainIds += key
                value.subQuests?.forEach { subQuest ->
                    subIds += subQuest.subId
                    mainSubIds.getOrPut(subQuest.mainId) { mutableSetOf() } += subQuest.subId
                }
            }
        }
        excelSubQuestsMapList.forEach { subQuestMap ->
            subQuestMap.forEach { (key, value) ->
                subIds += key
                mainIds += value.mainId
                mainSubIds.getOrPut(value.mainId) { mutableSetOf() } += key
            }
        }

        val generatedQuests = mutableMapOf<Int, MainQuestData>()
        mainIds.forEach {mainId ->

            val subQuestsList = mutableListOf<SubQuestData>()
            var mainQuest = MainQuestData(mainId, subQuests = subQuestsList)
            val subQuests = mutableMapOf<Int, SubQuestData>()
            mainSubIds[mainId]?.forEach { subId->
                var subQuest = SubQuestData(subId, mainId)
                excelSubQuestsMapList.forEach { subMap ->
                    subMap[subId]?.let {
                        subQuest = subQuest.merge(it)
                    }
                }
                subQuests[subId]=subQuest
            }
            excelMainQuestsMapList.forEach {mainMap->
                mainMap.get(mainId)?.let{
                    mainQuest = mainQuest merge it
                }
            }
            /*txtMainQuests?.get(mainId)?.let {
                mainQuest = mainQuest.merge(it)
            }*/


            binoutQuestsList.forEach { binoutMap ->
                binoutMap.get(mainId)?.let {
                    mainQuest = mainQuest.merge(it)
                    it.subQuests?.forEach { subQuest ->
                        subQuests[subQuest.subId] = subQuests[subQuest.subId]?.merge(subQuest) ?: subQuest
                    }
                }
            }
            questsPatchesList.forEach { patchMap ->
                patchMap.get(mainId)?.let {
                    mainQuest = mainQuest.merge(it)
                    it.subQuests?.forEach { subQuest ->
                        subQuests[subQuest.subId] = subQuests[subQuest.subId]?.merge(subQuest) ?: subQuest
                    }
                }
            }

            subQuestsList.addAll(subQuests.map { it.value.cleanup() })
            mainQuest = mainQuest.cloneReplacing(mapOf("subQuests" to subQuestsList))
            generatedQuests[mainId] = mainQuest
        }

        val targetDir = resourcesBaseDir.resolve(outputFolder).toFile()
        targetDir.mkdirs()

        generatedQuests.forEach { (key, value) ->
            //println(value.toString())
            File(targetDir, "$key.json").writeText(jsonSerializer.encodeToString(MainQuestData.serializer(), value))
        }
    }

    private fun SubQuestData.cleanup() : SubQuestData{
        val cleanFinishCond = finishCond?.filter { it.type != null }
            ?: listOf(QuestCondition("QUEST_CONTENT_UNKNOWN", param = emptyList()))
        val cleanFailCond = failCond?.filter { it.type != null }
            ?: listOf(QuestCondition("QUEST_CONTENT_UNKNOWN", param = emptyList()))
        val cleanAcceptCond = acceptCond?.filter { it.type != null }
            ?: listOf(QuestCondition("QUEST_COND_UNKNOWN", param = emptyList()))

        val cleanFinishExec: List<QuestExec> = finishExec?.filter { it.type != null }
            ?: listOf(QuestExec("QUEST_EXEC_UNKNOWN", param = emptyList()))
        val cleanFailExec: List<QuestExec> = failExec?.filter { it.type != null }
            ?: listOf(QuestExec("QUEST_EXEC_UNKNOWN", param = emptyList()))
        val cleanBeginExec: List<QuestExec> = beginExec?.filter { it.type != null }
            ?: listOf(QuestExec("QUEST_EXEC_UNKNOWN", param = emptyList()))
        val cleanGuide: QuestGuide = guide?.let {
            if(it.type!=null) it
            else QuestGuide("QUEST_GUIDE_NONE", param = emptyList())
        } ?: QuestGuide("QUEST_EXEC_UNKNOWN", param = emptyList())
        return this.cloneReplacing(mapOf(
            "finishCond" to cleanFinishCond,
            "failCond" to cleanFailCond,
            "acceptCond" to cleanAcceptCond,
            "finishExec" to cleanFinishExec,
            "failExec" to cleanFailExec,
            "beginExec" to cleanBeginExec,
            "guide" to cleanGuide
        ))
    }




    fun Int.ifIsSet(function:(Int)->Unit){
        if(this != -1)
            function.invoke(this)
    }
    fun Long.ifIsSet(function:(Long)->Unit){
        if(this != -1L)
            function.invoke(this)
    }


    fun readExcelSubFile(file : File) : Map<Int, SubQuestData>? {
        return readExcelFile(file, SubQuestData.serializer())
    }

    fun readExcelMainFile(file : File) : Map<Int, MainQuestData>? {
        return readExcelFile(file, MainQuestData.serializer())
    }

    fun readBinoutFiles(binoutFileName: Path) : Map<Int, MainQuestData>? {
        return readBinoutFolderFile(binoutFileName, MainQuestData.serializer())
    }
    fun readPatchFiles(patchFolderName:Path) : Map<Int, MainQuestData>?{
        return readBinoutFolderFile(patchFolderName, MainQuestData.serializer())
    }
}