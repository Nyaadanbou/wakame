package cc.mewcraft.wakame.serialization.json.serializer

import cc.mewcraft.wakame.util.data.Version
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal object VersionSerialization : TypeAdapter<Version>() {

    override fun write(writer: JsonWriter, value: Version) {
        writer.value(value.toString())
    }

    override fun read(reader: JsonReader): Version {
        return Version(reader.nextString())
    }

}