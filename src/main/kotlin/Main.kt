import org.anime_game_servers.gc_resource_patcher.patchers.quests.QuestsPatcher

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
    QuestsPatcher.patch()
}