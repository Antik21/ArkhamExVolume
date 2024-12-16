package localization

import com.google.gson.Gson
import java.io.File
import java.security.InvalidKeyException

class LocalizationManager private constructor(language: Language) {

    companion object {
        private const val FILE_NAME_TEMPLATE = "strings_%s.json"

        private val instances: MutableMap<Language, LocalizationManager> = mutableMapOf()

        fun getInstance(language: Language): LocalizationManager {
            return instances.getOrPut(language) { LocalizationManager(language) }
        }
    }

    private val strings: MutableMap<String, String> = mutableMapOf()

    init {
        loadStrings(language)
    }

    private fun loadStrings(language: Language) {
        val fileLocale = FILE_NAME_TEMPLATE.format(language.locale)
        val resourceStream = this::class.java.classLoader.getResourceAsStream("strings/$fileLocale")
            ?: throw IllegalStateException("Localization file not found: $fileLocale")

        val jsonContent = resourceStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val localizedStrings = Gson().fromJson(jsonContent, Map::class.java) as Map<String, String>
        strings.putAll(localizedStrings)
    }

    @Throws(InvalidKeyException::class, IllegalStateException::class)
    fun getString(key: String, vararg args: Any?): String {
        if (strings.isEmpty()) throw IllegalStateException("LocalizationManager wasn't initialized")
        val template = strings[key] ?: throw InvalidKeyException("String not found for key: $key")

        return template.format(*args)
    }
}
