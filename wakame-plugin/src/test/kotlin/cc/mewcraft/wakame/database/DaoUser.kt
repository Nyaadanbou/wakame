package cc.mewcraft.wakame.database

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import java.time.LocalDateTime

class DaoUser(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DaoUser>(Users)

    var name: String by Users.name
    var password: String by Users.password
    var isBanned: Boolean by Users.isBanned
    var createdAt: LocalDateTime by Users.createdAt
}