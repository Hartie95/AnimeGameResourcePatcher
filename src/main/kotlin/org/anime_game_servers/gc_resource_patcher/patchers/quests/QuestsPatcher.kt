package org.anime_game_servers.gc_resource_patcher.patchers.quests

import SerializationOptions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey
import org.anime_game_servers.gc_resource_patcher.data.interfaces.StringKey
import org.anime_game_servers.gc_resource_patcher.data.quest.*
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

@OptIn(ExperimentalSerializationApi::class)
class QuestsPatcher(serializationOptions: SerializationOptions){
    companion object {
        const val excelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.json"
        const val oldExcelSubFileName : String = "ExcelBinOutput/QuestExcelConfigData.old.json"
        const val excelMainFileName : String = "ExcelBinOutput/MainQuestExcelConfigData.json"

        const val txtMainFileName = "txt/MainQuestData.txt"

        const val binoutFolder = "BinOutput/Quest/"

        const val patchFolder = "Patches/Quest/"
        const val outputFolder = "Generated/Quest/"
    }


    private val jsonSerializer = Json { ignoreUnknownKeys = !serializationOptions.strictParsing;
        prettyPrint = serializationOptions.usePrettyPrint
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
            ?: listOf(QuestContent("QUEST_CONTENT_UNKNOWN", param = emptyList()))
        val cleanFailCond = failCond?.filter { it.type != null }
            ?: listOf(QuestContent("QUEST_CONTENT_UNKNOWN", param = emptyList()))
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
    inline infix fun <reified T : Any> T.merge(other: T): T {
        var propertiesByName = T::class.declaredMemberProperties.associateBy { it.name }
        val primaryConstructor = T::class.primaryConstructor
            ?: run {
                if(this!=null && this::class.primaryConstructor!=null){
                    propertiesByName = this::class.declaredMemberProperties.associateBy { it.name } as Map<String, KProperty1<T, *>>
                    return@run this::class.primaryConstructor!!
                } else if(other!=null && other::class.primaryConstructor!=null){
                    propertiesByName = other::class.declaredMemberProperties.associateBy { it.name } as Map<String, KProperty1<T, *>>
                    return@run other::class.primaryConstructor!!
                }
                throw IllegalArgumentException("merge type must have a primary constructor ${T::class.simpleName}")
            }
        val args = primaryConstructor.parameters.associateWith { parameter ->
            val property = propertiesByName[parameter.name]
                ?: throw IllegalStateException("no declared member property found with name '${parameter.name}'")
            val isNulleable = parameter.type.isMarkedNullable
            val propertyClass = property.returnType.classifier as KClass<*>
            if(propertyClass == List::class){
                getListValue(property as KProperty1<T, List<*>?>, other, this)
            } else if(propertyClass == Int::class)
                getIntValue(property as KProperty1<T, Int>, other, this)
            else if(propertyClass == Long::class)
                getLongValue(property as KProperty1<T, Long>, other, this)
            else if(propertyClass == String::class)
                getStringValue(property as KProperty1<T, String?>, other, this)
            else if(isNulleable){
                (property.get(other) ?: property.get(this))
            } else {
                println("Unknown non null type ${propertyClass.simpleName}")
                (property.get(other) ?: property.get(this))
            }
        }
        return primaryConstructor.callBy(args)
    }


    inline infix fun <reified T : Any> T.cloneReplacing(map: Map<String, Any>): T {
        val propertiesByName = T::class.declaredMemberProperties.associateBy { it.name }
        val primaryConstructor = T::class.primaryConstructor
            ?: run {
                throw IllegalArgumentException("merge type must have a primary constructor ${T::class.simpleName}")
            }
        val args = primaryConstructor.parameters.associateWith { parameter ->
            val property = propertiesByName[parameter.name]
                ?: throw IllegalStateException("no declared member property found with name '${parameter.name}'")

            if(map.containsKey(parameter.name)){
                map[parameter.name]
            } else {
                property.get(this)
            }
        }
        return primaryConstructor.callBy(args)
    }


    fun <T : Any> getIntValue(property:KProperty1<T, Int>,other: T, main: T): Int{
        val otherVal = property.get(other)
        if(otherVal != -1)
            return otherVal
        return property.get(main)
    }
    fun <T : Any> getLongValue(property:KProperty1<T, Long>,other: T, main: T): Long{
        val otherVal = property.get(other)
        if(otherVal != -1L)
            return otherVal
        return property.get(main)
    }
    fun <T : Any> getStringValue(property:KProperty1<T, String?>,other: T, main: T): String?{
        val mainVal = property.get(main)?.let { if(it.isBlank()) null else it }
        val otherVal = property.get(other)?.let { if(it.isBlank()) null else it }
        if(otherVal!=null){
            return otherVal
        }
        return mainVal
    }
    fun <T : Any> getListValue(property:KProperty1<T, List<*>?>,other: T, main: T): List<*>?{
        val mainVal = property.get(main)
        val otherVal = property.get(other)
        if(mainVal==null){
            return otherVal
        } else if(otherVal==null){
            return mainVal
        }
        //TODO merge logic
        val listType = property.returnType.arguments.get(0).type
        if(listType!=null && listType.isSubtypeOf(IntKey::class.starProjectedType)){
            val mainMap = (mainVal as List<IntKey>).associateBy { it.getIntKey() }
            val otherMap = (otherVal as List<IntKey>).associateBy { it.getIntKey() }
            val mergedMap = mainMap.toMutableMap()
            val onlyOtherKeys = otherMap.keys - mainMap.keys
            val onlyMainKeys = mainMap.keys - otherMap.keys
            val bothKeys = otherMap.keys - onlyOtherKeys
            onlyMainKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!
            }
            onlyOtherKeys.forEach { key ->
                mergedMap[key] = otherMap[key]!!
            }
            bothKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!.merge(otherMap[key]!!)
            }
            return mergedMap.values.toList()
        } else if(listType!=null && listType.isSubtypeOf(StringKey::class.starProjectedType)) {
            val mainMap = (mainVal as List<StringKey>).associateBy { it.getStringKey() }
            val otherMap = (otherVal as List<StringKey>).associateBy { it.getStringKey() }
            val mergedMap = mainMap.toMutableMap()
            val onlyOtherKeys = otherMap.keys - mainMap.keys
            val onlyMainKeys = mainMap.keys - otherMap.keys
            val bothKeys = otherMap.keys - onlyOtherKeys
            onlyMainKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!
            }
            onlyOtherKeys.forEach { key ->
                mergedMap[key] = otherMap[key]!!
            }
            bothKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!.merge(otherMap[key]!!)
            }
            return mergedMap.values.toList()
        }


        return otherVal
        /*val newList = mutableListOf<Any>()
        newList.addAll(mainVal as Collection<Any>)
        newList.addAll(otherVal as Collection<Any>)
        return newList*/
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
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(SubQuestData.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.subId }
    }

    fun readExcelMainFile(file : File) : Map<Int, MainQuestData>? {
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(MainQuestData.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.id }
    }

    fun readBinoutFiles(binoutFileName: Path) : Map<Int, MainQuestData>? {
        val config = MainQuestData.serializer()
        return binoutFileName.toFile().listFiles()?.associate{
            val quest = it.inputStream().use { stream ->
                try {
                    jsonSerializer.decodeFromStream(config, stream)
                }catch (ex: Throwable){
                    println("error in file ${it.name}")
                    ex.printStackTrace()
                    MainQuestData(0)
                }
            }
            Pair(quest.id, quest)
        }
    }
    fun readPatchFiles(patchFolderName:Path) : Map<Int, MainQuestData>?{
        val config = MainQuestData.serializer()
        return patchFolderName.toFile().listFiles()?.associate<File, Int, MainQuestData> {
            val quest = it.inputStream().use { stream ->
                    try {
                        jsonSerializer.decodeFromStream(config, stream)
                    }catch (ex: Throwable){
                        println("error in file ${it.name}")
                        ex.printStackTrace()
                        MainQuestData(0)
                    }
                }
                Pair(quest.id, quest)
        }
    }
}
