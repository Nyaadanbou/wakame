package cc.mewcraft.wakame.database

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object Users : IntIdTable("dsl_users") {
    val name = varchar("name", 64).uniqueIndex()
    val password = varchar("password", 64)
    val isBanned = bool("is_banned").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}