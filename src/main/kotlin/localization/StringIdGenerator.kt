package localization

import java.io.File

fun main() {
    val languages = listOf("en", "ru")
    val outputFileName = "Strings.kt"
    val outputFile = File(outputFileName)

    val builder = StringBuilder()
    builder.appendLine("object Strings {")

    val keys = mutableSetOf<String>()

    for (lang in languages) {
        val file = File("strings_${lang}.json")
        if (!file.exists()) continue

        val jsonMap = file.readText(Charsets.UTF_8)
            .let { com.google.gson.Gson().fromJson(it, Map::class.java) as Map<String, String> }

        for (key in jsonMap.keys) {
            keys.add(key)
        }
    }

    keys.forEach { key ->
        builder.appendLine("    const val ${key.uppercase()} = \"$key\"")
    }

    builder.appendLine("}")
    outputFile.writeText(builder.toString())
    println("Strings.kt generated successfully!")
}
