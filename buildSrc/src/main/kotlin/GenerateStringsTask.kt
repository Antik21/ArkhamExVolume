import com.google.gson.Gson
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateStringsTask : DefaultTask() {
    @Input
    val languages: List<String> = listOf("en", "ru")

    @InputDirectory
    val stringsDir = project.layout.projectDirectory.dir("src/main/resources/strings")

    @OutputDirectory
    val outputDir = project.layout.buildDirectory.dir("generated/strings")

    @TaskAction
    fun generateStrings() {
        val gson = Gson()
        val keysByLocale = mutableMapOf<String, Set<String>>()

        for (lang in languages) {
            val file = stringsDir.file("strings_$lang.json").asFile
            require(file.exists()) { "File ${file.name} not found in ${stringsDir.asFile}." }

            val jsonMap = gson.fromJson(file.readText(), Map::class.java) as Map<String, String>
            keysByLocale[lang] = jsonMap.keys
        }

        val referenceKeys = keysByLocale.values.first()
        if (!keysByLocale.values.all { it == referenceKeys }) {
            throw IllegalStateException("String keys mismatch across locales. Please ensure all keys are identical.")
        }

        val outputFile = outputDir.get().file("StringKey.kt").asFile
        val content = buildString {
            appendLine("package generated")
            appendLine()
            appendLine("object StringKey {")
            referenceKeys.forEach { key ->
                appendLine("    const val ${key.uppercase()} = \"$key\"")
            }
            appendLine("}")
        }

        outputFile.writeText(content)
        println("Strings.kt generated successfully at ${outputFile.path}")
    }
}
