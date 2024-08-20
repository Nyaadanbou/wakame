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
    val REFORGE_MERGING = Permission.of("wakame.command.reforge.merging")
    val REFORGE_MODDING = Permission.of("wakame.command.reforge.modding")
    val REFORGE_REROLLING = Permission.of("wakame.command.reforge.rerolling")
    val RESOURCEPACK = Permission.of("wakame.command.resourcepack")
    val SKILL = Permission.of("wakame.command.skill")
    val STATION = Permission.of("wakame.command.station")
}