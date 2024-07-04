package cc.mewcraft.wakame.command

import org.incendo.cloud.permission.Permission

object CommandPermissions {
    val CRAFT = Permission.of("wakame.command.craft")
    val DEBUG = Permission.of("wakame.command.debug")
    val HEPHAESTUS = Permission.of("wakame.command.hephaestus")
    val ITEM = Permission.of("wakame.command.item")
    val LOOT = Permission.of("wakame.command.loot")
    val PLUGIN = Permission.of("wakame.command.plugin")
    val REFORGE = Permission.of("wakame.command.reforge")
    val RESOURCEPACK = Permission.of("wakame.command.resourcepack")
    val SKILL = Permission.of("wakame.command.skill")
}