import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import org.anime_game_servers.gc_resource_patcher.patchers.quests.QuestsPatcher
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
    Patch()
        .subcommands(All(), Quests())
        .main(args)
}

data class BaseOptions(
    val resourcesBaseDir: Path,
    val patchBaseDir: Set<Path>,
    val serialisationOptions: SerializationOptions,
)

data class SerializationOptions(
    val usePrettyPrint: Boolean,
    val strictParsing: Boolean
)
class Patch : CliktCommand(help = "Generates the patched resources") {
    val resourcesBaseDir by option(help = "The Resources directory").path(mustExist = true, canBeFile = false)
        .default(Paths.get("").toAbsolutePath())
    val patchBaseDir by option(help = "The Patch directory").path(mustExist = true, canBeFile = false)
        .multiple(default = listOf(Paths.get("").toAbsolutePath())).unique()
    val prettyJson by option(help = "Pretty print json").flag(default = false)
    val strictParsing by option(help = "stricter json parsing, so for example unknown fields will throw an error").flag(
        default = false
    )


    val baseOptions by findOrSetObject {
        BaseOptions(
            resourcesBaseDir = resourcesBaseDir,
            patchBaseDir = patchBaseDir,
            SerializationOptions(prettyJson, strictParsing)
        )
    }

    override fun run() {
        baseOptions // forces initialization
    }
}


class Quests : CliktCommand(help = "Generates the patched quests resources") {
    val baseOptions by requireObject<BaseOptions>()
    val questExcelFiles by option(help = "adds a quest excel file").file(mustExist = true, canBeDir = false).multiple()
        .unique()
    val mainQuestExcelFiles by option(help = "adds a main quest accel file").file(mustExist = true, canBeDir = false)
        .multiple().unique()
    val binoutQuests by option(help = "Add a binout directory").path(mustExist = true, canBeFile = false).multiple()
        .unique()
    val questPatches by option(help = "Adds additional patched").path(mustExist = true, canBeFile = false).multiple()
        .unique()

    override fun run() {
        echo("generating patched quests")
        QuestsPatcher(baseOptions.serialisationOptions)
            .patch(
                resourcesBaseDir = baseOptions.resourcesBaseDir,
                patchesBaseDir = baseOptions.patchBaseDir,
                subExcelFiles = questExcelFiles,
                mainExcelFiles = mainQuestExcelFiles,
                addBinoutDir = binoutQuests,
                addPatchesDir = questPatches
            )
    }
}

class All : CliktCommand(help = "Generates all patched resources") {
    val baseOptions by requireObject<BaseOptions>()

    override fun run() {
        echo("generating all patches")
        echo("generating patched quests")
        QuestsPatcher(baseOptions.serialisationOptions)
            .patch(
                resourcesBaseDir = baseOptions.resourcesBaseDir,
                patchesBaseDir = baseOptions.patchBaseDir,
            )
    }
}