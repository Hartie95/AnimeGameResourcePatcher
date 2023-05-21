package org.anime_game_servers.gc_resource_patcher.patchers.quests

import app.softwork.serialization.csv.CSVFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.anime_game_servers.gc_resource_patcher.data.interfaces.IntKey
import org.anime_game_servers.gc_resource_patcher.data.interfaces.StringKey
import org.anime_game_servers.gc_resource_patcher.data.quest.MainQuestData
import org.anime_game_servers.gc_resource_patcher.data.quest.SubQuestData
import org.anime_game_servers.gc_resource_patcher.patchers.quests.txt.TxtMainQuest
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

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

        val generatedQuests = mutableMapOf<Int, MainQuestData>()
        mainIds.forEach {mainId ->

            val subQuestsList = mutableListOf<SubQuestData>()
            var mainQuest = MainQuestData(mainId, subQuests = subQuestsList)
            val subQuests = mutableMapOf<Int, SubQuestData>()
            mainSubIds[mainId]?.forEach { subId->
                val subQuest = SubQuestData(subId, mainId)

                excelSubQuestsOld?.get(subId)?.let {
                    subQuest.merge(it)
                }
                excelSubQuests?.get(subId)?.let {
                    subQuest.merge(it)
                }

                subQuests[subId]=subQuest
            }
            excelMainQuests?.get(mainId)?.let {
                mainQuest = mainQuest merge it
            }
            /*txtMainQuests?.get(mainId)?.let {
                mainQuest = mainQuest.merge(it)
            }*/


            binoutQuests?.get(mainId)?.let {
                mainQuest = mainQuest.merge(it)
                it.subQuests?.forEach { subQuest ->
                    subQuests[subQuest.subId]?.merge(subQuest)
                }
            }

            questsPatches?.get(mainId)?.let {
                mainQuest = mainQuest.merge(it)
                it.subQuests?.forEach { subQuest ->
                    subQuests[subQuest.subId]?.merge(subQuest)
                }
            }

            subQuestsList.addAll(subQuests.map { it.value })
            generatedQuests[mainId] = mainQuest
        }

        generatedQuests.forEach { (key, value) ->
            //println(value.toString())
            File("$outputFolder$key.json").writeText(jsonSerializer.encodeToString(MainQuestData.serializer(), value))
        }
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
                getListValue(property as KProperty1<T, List<*>>, other, this)
            } else if(propertyClass == Int::class)
                getIntValue(property as KProperty1<T, Int>, other, this)
            else if(propertyClass == Long::class)
                getLongValue(property as KProperty1<T, Long>, other, this)
            else if(isNulleable){
                (property.get(other) ?: property.get(this))
            } else {
                println("Unknown non null type ${propertyClass.simpleName}")
                (property.get(other) ?: property.get(this))
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
    fun <T : Any> getListValue(property:KProperty1<T, List<*>>,other: T, main: T): List<*>?{
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
            //TODO merge each element
            val mainMap = (mainVal as List<IntKey>).associateBy { it.getIntKey() }
            val otherMap = (otherVal as List<IntKey>).associateBy { it.getIntKey() }
            val mergedMap = mainMap.toMutableMap()
            val onlyOtherKeys = otherMap.keys - mainMap.keys
            val onlyMainKeys = mainMap.keys - otherMap.keys
            val bothKEys = otherMap.keys - onlyOtherKeys
            onlyMainKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!
            }
            onlyOtherKeys.forEach { key ->
                mergedMap[key] = otherMap[key]!!
            }
            bothKEys.forEach { key ->
                mergedMap[key] = mainMap[key]!!.merge(otherMap[key]!!)
            }
            return mergedMap.values.toList()
        } else if(listType!=null && listType.isSubtypeOf(StringKey::class.starProjectedType)) {
            //TODO merge each element
            val mainMap = (mainVal as List<StringKey>).associateBy { it.getStringKey() }
            val otherMap = (otherVal as List<StringKey>).associateBy { it.getStringKey() }
            val mergedMap = mainMap.toMutableMap()
            val onlyOtherKeys = otherMap.keys - mainMap.keys
            val onlyMainKeys = mainMap.keys - otherMap.keys
            val bothKEys = otherMap.keys - onlyOtherKeys
            onlyMainKeys.forEach { key ->
                mergedMap[key] = mainMap[key]!!
            }
            onlyOtherKeys.forEach { key ->
                mergedMap[key] = otherMap[key]!!
            }
            bothKEys.forEach { key ->
                mergedMap[key] = mainMap[key]!!.merge(otherMap[key]!!)
            }
            return mergedMap.values.toList()
        }


        val newList = mutableListOf<Any>()
        newList.addAll(mainVal as Collection<Any>)
        newList.addAll(otherVal as Collection<Any>)
        return newList
    }

    fun Int.ifIsSet(function:(Int)->Unit){
        if(this != -1)
            function.invoke(this)
    }
    fun Long.ifIsSet(function:(Long)->Unit){
        if(this != -1L)
            function.invoke(this)
    }


    fun readExcelSubFile(excelFileName : String) : Map<Int, SubQuestData>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(SubQuestData.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.subId }
    }

    fun readExcelMainFile(excelFileName : String) : Map<Int, MainQuestData>? {
        val file = File(excelFileName)
        if(!file.exists()){
            return null
        }

        val excelText= file.readText()
        val config = ListSerializer(MainQuestData.serializer())
        return jsonSerializer.decodeFromString(config, excelText).associateBy { it.id }
    }

    fun readBinoutFiles(binoutFileName: String) : Map<Int, MainQuestData>? {
        val config = MainQuestData.serializer()
        return Path(binoutFileName).toFile().listFiles()?.associate{
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

    fun readPatchFiles(patchFolderName:String) : Map<Int, MainQuestData>?{
        val config = MainQuestData.serializer()
        return Path(patchFolderName).toFile().listFiles()?.associate<File, Int, MainQuestData> {
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