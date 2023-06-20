package patchers

import SerializationOptions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.anime_game_servers.game_data_models.data.activity.ActivityCondData
import org.anime_game_servers.game_data_models.data.interfaces.IntKey
import org.anime_game_servers.game_data_models.data.interfaces.StringKey
import org.anime_game_servers.game_data_models.loader.JsonDataLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

open class BasePatcher(serializationOptions: SerializationOptions) {
    val jsonSerializer = Json {
        ignoreUnknownKeys = !serializationOptions.strictParsing;
        prettyPrint = serializationOptions.usePrettyPrint
    }
    val dataLoader = JsonDataLoader(jsonSerializer, okio.FileSystem.Companion.SYSTEM)


    fun <T: IntKey> readExcelFile(file : File, type : KSerializer<T>) : Map<Int, T>? {
        if(!file.exists()){
            return null
        }
        return try {
            dataLoader.loadFileList(file.path, type).associateBy { it.getIntKey() }
        } catch (ex: Throwable){
            println("error in file ${file.name}")
            ex.printStackTrace()
            null
        }
    }

    fun <T: IntKey> readBinoutFolderFile(folder: Path, type : KSerializer<T>) : Map<Int, T>? {
        if(!folder.exists()){
            return null
        }
        return try {
            dataLoader.loadFolderObjectList(folder.toString(), type).associateBy { it.getIntKey() }
        } catch (ex: Throwable){
            println("error in file ${folder.fileName}")
            ex.printStackTrace()
            null
        }
    }

    inline fun <reified T:IntKey> patchExcelFile(resourcesBaseDir: Path, patchesBaseDir:Set<Path>? = null, addPatchesDir: Set<Path>?=null,
                                  kSerializer: KSerializer<T> , excelFile:String, patchFile:String, outputFolder: String, outputFile: String, baseObjectCreator:(Int)->T){
        val excelFiles: MutableSet<File> = mutableSetOf()
        val patchFiles: MutableSet<File> = mutableSetOf()

        resourcesBaseDir.let {
            excelFiles += it.resolve(excelFile).toFile()
            patchFiles += it.resolve(patchFile).toFile()
        }
        patchesBaseDir?.forEach {
            patchFiles += it.resolve(patchFile).toFile()
        }
        addPatchesDir?.forEach {
            patchFiles += it.resolve(patchFile).toFile()
        }

        val excels = mutableMapOf<File, Map<Int, T>>()
        val patches = mutableMapOf<File, Map<Int, T>>()
        val condIds = mutableSetOf<Int>()
        excelFiles.forEach { file->
            readExcelFile(file, kSerializer)?.let {
                condIds.addAll(it.keys)
                excels[file] = it
            }
        }
        patchFiles.forEach { file->
            readExcelFile(file, kSerializer)?.let {
                condIds.addAll(it.keys)
                patches[file] = it
            }
        }


        val generatedModels = mutableListOf<T>()
        condIds.forEach {modelId ->
            var model : T = baseObjectCreator(modelId)
            excels.values.forEach {modelMap->
                modelMap.get(modelId)?.let{
                    model = model merge it
                }
            }
            patches.values.forEach {mainMap->
                mainMap.get(modelId)?.let{
                    model = model merge it
                }
            }

            generatedModels += model
        }

        val targetDir = resourcesBaseDir.resolve(outputFolder).toFile()
        targetDir.mkdirs()

        File(targetDir, outputFile).writeText(jsonSerializer.encodeToString(ListSerializer(kSerializer), generatedModels))
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

}