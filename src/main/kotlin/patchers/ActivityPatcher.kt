package patchers

import SerializationOptions
import org.anime_game_servers.game_data_models.data.activity.ActivityCondData
import org.anime_game_servers.game_data_models.data.activity.ActivityData
import java.nio.file.Path

class ActivityPatcher(serializationOptions: SerializationOptions) : BasePatcher(serializationOptions) {
    companion object {
        const val excelConditionFile: String = "ExcelBinOutput/NewActivityCondExcelConfigData.json"
        const val excelDataFile: String = "ExcelBinOutput/NewActivityExcelConfigData.json"

        const val txtConditionFile: String = "txt/NewActivityCondData.txt"
        const val txtDataFile: String = "txt/NewActivityData.txt"

        const val patchBaseFolder = "Patches/Activity"
        const val patchConditionFile = "$patchBaseFolder/NewActivityCondData.json"
        const val patchDataFile = "$patchBaseFolder/NewActivityData.json"

        const val outputFolder = "Generated/Activity/"
        const val outputConditionFile = "NewActivityCondData.json"
        const val outputDataFile = "NewActivityData.json"
    }

    fun patch(
        resourcesBaseDir: Path, patchesBaseDir: Set<Path>? = null, addPatchesDir: Set<Path>? = null
    ) {
        patchCondFile(resourcesBaseDir, patchesBaseDir, addPatchesDir)
        patchDataFile(resourcesBaseDir, patchesBaseDir, addPatchesDir)
    }


    fun patchCondFile(resourcesBaseDir: Path, patchesBaseDir: Set<Path>? = null, addPatchesDir: Set<Path>? = null) {
        patchExcelFile(
            resourcesBaseDir,
            patchesBaseDir,
            addPatchesDir,
            ActivityCondData.serializer(),
            excelConditionFile,
            patchConditionFile,
            outputFolder,
            outputConditionFile
        ) {
            ActivityCondData(it)
        }
    }

    fun patchDataFile(resourcesBaseDir: Path, patchesBaseDir: Set<Path>? = null, addPatchesDir: Set<Path>? = null) {
        patchExcelFile(
            resourcesBaseDir,
            patchesBaseDir,
            addPatchesDir,
            ActivityData.serializer(),
            excelDataFile,
            patchDataFile,
            outputFolder,
            outputDataFile
        ) {
            ActivityData(it)
        }
    }

}