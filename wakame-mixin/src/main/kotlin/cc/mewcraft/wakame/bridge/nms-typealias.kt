package cc.mewcraft.wakame.bridge

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.Unit
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable

typealias MojangUnit = Unit
typealias MojangResourceKey<T> = ResourceKey<T>
typealias MojangIdentifier = Identifier
typealias MojangRegistry<T> = Registry<T>
typealias MojangEnchantment = Enchantment
typealias MojangStack = ItemStack
typealias MojangEntity = Entity
typealias MojangEntityType<T> = EntityType<T>
typealias MojangDamageSource = DamageSource
typealias MojangIngredient = Ingredient
typealias MojangLootTable = LootTable
typealias MojangLootParams = LootParams
typealias MojangLootParamsBuilder = LootParams.Builder
typealias MojangComponent = Component
typealias MojangBuiltInRegistries = BuiltInRegistries