package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.serialization.json.GSON
import com.google.gson.JsonElement
import xyz.xenondevs.commons.gson.fromJson
import xyz.xenondevs.commons.gson.parseJson
import xyz.xenondevs.commons.gson.writeToFile
import xyz.xenondevs.commons.provider.MutableProvider
import xyz.xenondevs.commons.provider.mutableProvider
import java.io.File
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

internal object PermanentStorage {

    private val dir = File("plugins/Wakame/.internal_data/storage/")
    private val storage = HashMap<String, JsonElement>()

    init {
        // load storage map
        dir.walk()
            .filter(File::isFile)
            .forEach {
                val key = it.relativeTo(dir).invariantSeparatorsPath.removeSuffix(".json")
                storage[key] = it.parseJson()
            }
    }

    // raw create
    fun storeRaw(key: String, data: JsonElement?) {
        val f = getFile(key)
        if (data != null) {
            storage[key] = data
            f.parentFile.mkdirs()
            data.writeToFile(f)
        } else {
            f.delete()
        }
    }

    // raw read
    fun retrieveRaw(key: String): JsonElement? =
        storage[key]

    fun store(key: String, data: Any?) =
        storeRaw(key, data?.let { GSON.toJsonTree(it) })

    fun has(key: String): Boolean =
        key in storage

    fun remove(key: String) =
        store(key, null)

    inline fun <reified T> retrieve(key: String, alternativeProvider: () -> T): T =
        retrieveOrNull(key) ?: alternativeProvider()

    fun <T> retrieve(type: Type, key: String, alternativeProvider: () -> T): T =
        retrieveOrNull<T>(type, key) ?: alternativeProvider()

    fun <T> retrieve(type: KType, key: String, alternativeProvider: () -> T): T =
        retrieve(type.javaType, key, alternativeProvider)

    inline fun <reified T> retrieveOrNull(key: String): T? =
        GSON.fromJson<T>(retrieveRaw(key))

    fun <T> retrieveOrNull(type: Type, key: String): T? =
        GSON.fromJson(retrieveRaw(key), type) as? T

    fun <T> retrieveOrNull(type: KType, key: String): T? =
        retrieveOrNull(type.javaType, key)

    inline fun <reified T> retrieveOrStore(key: String, noinline alternativeProvider: () -> T): T =
        retrieveOrStore(typeOf<T>(), key, alternativeProvider)

    fun <T> retrieveOrStore(type: KType, key: String, alternativeProvider: () -> T): T =
        retrieveOrStore(type.javaType, key, alternativeProvider)

    fun <T> retrieveOrStore(type: Type, key: String, alternativeProvider: () -> T): T =
        retrieveOrNull(type, key) ?: alternativeProvider().also { store(key, it) }

    inline fun <reified T> storedValue(key: String, noinline alternativeProvider: () -> T): MutableProvider<T> =
        storedValue(typeOf<T>(), key, alternativeProvider)

    fun <T> storedValue(type: KType, key: String, alternativeProvider: () -> T): MutableProvider<T> =
        storedValue(type.javaType, key, alternativeProvider)

    fun <T> storedValue(type: Type, key: String, alternativeProvider: () -> T): MutableProvider<T> =
        mutableProvider(
            { retrieveOrStore(type, key, alternativeProvider) },
            { store(key, it) }
        )

    private fun getFile(key: String) =
        File(dir, "$key.json")

}