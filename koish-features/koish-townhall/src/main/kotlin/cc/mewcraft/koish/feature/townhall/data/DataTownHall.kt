package cc.mewcraft.koish.feature.townhall.data

import cc.mewcraft.koish.feature.townhall.component.EnhancementType
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import java.util.*

data class DataTownHall(
    @param:BsonId
    @param:BsonRepresentation(BsonType.OBJECT_ID)
    val townUUID: UUID,
    val enhancements: List<EnhancementType>,
)