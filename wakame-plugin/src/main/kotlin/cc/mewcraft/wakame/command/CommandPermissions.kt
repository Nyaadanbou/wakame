package cc.mewcraft.wakame.command

import org.incendo.cloud.permission.Permission

object CommandPermissions {

    // Attribute
    val ATTRIBUTE = Permission.of("wakame.command.attribute")

    // Catalog
    val CATALOG_ITEM = Permission.of("wakame.command.catalog.item")
    val CATALOG_KIZAMI = Permission.of("wakame.command.catalog.kizami")

    // Crafting
    val CRAFT = Permission.of("wakame.command.craft")

    // Debug
    val DEBUG = Permission.of("wakame.command.debug")

    // Item
    val ITEM = Permission.of("wakame.command.item")

    // Loot
    val LOOT = Permission.of("wakame.command.loot")

    // Plugin
    val PLUGIN = Permission.of("wakame.command.plugin")

    // Reforge
    val REFORGE = Permission.of("wakame.command.reforge")
    val REFORGE_BLACKSMITH = Permission.of("wakame.command.reforge.blacksmith")
    val REFORGE_MERGING = Permission.of("wakame.command.reforge.merging")
    val REFORGE_MODDING = Permission.of("wakame.command.reforge.modding")
    val REFORGE_REROLLING = Permission.of("wakame.command.reforge.rerolling")

    // Resource pack
    val RESOURCEPACK = Permission.of("wakame.command.resourcepack")

    // Extra: Towny Network
    val TOWNY_NETWORK = Permission.of("koish.command.extra.towny.network")
}