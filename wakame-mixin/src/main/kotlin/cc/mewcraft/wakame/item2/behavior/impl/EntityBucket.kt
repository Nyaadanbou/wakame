package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.item2.*
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.*
import cc.mewcraft.wakame.util.adventure.plain
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * 用桶捕捉生物的逻辑.
 */
object EntityBucket : ItemBehavior {

    // 当玩家手持一个生物桶右键方块顶部时
    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val entityData = itemstack.getData(ItemDataTypes.ENTITY_BUCKET_DATA) ?: return

        val loc = wrappedEvent.event.interactionPoint
        if (loc == null) {
            return // 没有交互点
        }

        if (!ProtectionManager.canUseItem(player, itemstack, loc)) {
            return
        }

        if (!wrappedEvent.event.action.isRightClick /*|| !hasEntityBucketBehavior(itemstack)*/) {
            return // 不是右键点击
        }

        if (wrappedEvent.event.blockFace != BlockFace.UP) {
            return // 不是点击的方块顶部
        }

        if (wrappedEvent.event.clickedBlock == null) {
            return // 没有点击方块 (什么情况下 BlockFace 不为 null 但 clickedBlock 为 null ?)
        }

        wrappedEvent.event.isCancelled = true
        wrappedEvent.actionPerformed = true

        val deserializedEntity = Bukkit.getUnsafe().deserializeEntity(entityData, player.world)
        deserializedEntity.spawnAt(loc, CreatureSpawnEvent.SpawnReason.BUCKET)

        // 还原物品状态
        if (player.gameMode != GameMode.CREATIVE) {
            itemstack.resetData(DataComponentTypes.CUSTOM_MODEL_DATA)
            itemstack.resetData(DataComponentTypes.MAX_STACK_SIZE)
            itemstack.removeData(ItemDataTypes.ENTITY_BUCKET_DATA)
        }

        // TODO 播放交互音效
    }

    // 当玩家手持一个生物桶右键生物时
    override fun handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
        val entityBucket = itemstack.getProp(ItemPropertyTypes.ENTITY_BUCKET) ?: return
        val entityBucketData = itemstack.getData(ItemDataTypes.ENTITY_BUCKET_DATA)

        if (!ProtectionManager.canInteractWithEntity(player, clicked, itemstack)) {
            return
        }

        // 已经是一个装有生物的生物桶了
        if (entityBucketData != null) {
            event.isCancelled = true
            return
        }

        // 检查是否可以捕捉该生物
        val entityTypeKey = clicked.type.key
        if (entityTypeKey !in entityBucket.allowedEntities ||
            !player.hasPermission("koish.item.behavior.entity_bucket.capture.${entityTypeKey.asString()}")
        ) {
            return
        }

        // 处理创造模式和多桶叠加的情况
        if (itemstack.amount > 1 || player.gameMode == GameMode.CREATIVE) {
            val newStack = itemstack.clone().asOne()
            asEntityBucket(newStack, clicked, player)
            if (player.gameMode != GameMode.CREATIVE) {
                // 非创造模式下, 扣除一个空桶
                itemstack.subtract(1)
            }
            player.inventory.addItem(newStack)
        } else {
            asEntityBucket(itemstack, clicked, player)
        }

        clicked.remove()
        event.isCancelled = true
    }

    private fun hasEntityBucketBehavior(itemstack: ItemStack): Boolean {
        return itemstack.hasProp(ItemPropertyTypes.ENTITY_BUCKET)
    }

    private fun hasEntityBucketData(itemstack: ItemStack): Boolean {
        return itemstack.hasData(ItemDataTypes.ENTITY_BUCKET_DATA)
    }

    private fun asEntityBucket(itemstack: ItemStack, clicked: Entity, player: Player) {
        // 先让 clicked 处于静止状态
        clicked.velocity = Vector(0, 0, 0)
        clicked.fallDistance = 0.0f

        // 向 itemstack 写入实体数据
        val serializedEntity = Bukkit.getUnsafe().serializeEntity(clicked)
        itemstack.setData(ItemDataTypes.ENTITY_BUCKET_DATA, serializedEntity)

        // 确保 itemstack 不会叠加
        if (itemstack.getData(DataComponentTypes.MAX_STACK_SIZE) != 1) {
            itemstack.setData(DataComponentTypes.MAX_STACK_SIZE, 1)
        }

        when (clicked) {
            is Armadillo -> asArmadilloBucket(itemstack, clicked, player)

            is Bee -> asBeeBucket(itemstack, clicked, player)

            is Camel -> asCamelBucket(itemstack, clicked, player)

            is Cat -> asCatBucket(itemstack, clicked, player)

            is Chicken -> asChickenBucket(itemstack, clicked, player)

            is Cow -> asCowBucket(itemstack, clicked, player)

            is Dolphin -> asDolphinBucket(itemstack, clicked, player)

            is Donkey -> asDonkeyBucket(itemstack, clicked, player)

            is Fox -> asFoxBucket(itemstack, clicked, player)

            is Frog -> asFrogBucket(itemstack, clicked, player)

            is GlowSquid -> asGlowSquidBucket(itemstack, clicked, player)

            is Goat -> asGoatBucket(itemstack, clicked, player)

            is HappyGhast -> asHappyGhast(itemstack, clicked, player)

            is Hoglin -> asHoglinBucket(itemstack, clicked, player)

            is Horse -> asHorseBucket(itemstack, clicked, player)

            is Llama -> asLlamaBucket(itemstack, clicked, player)

            is MushroomCow -> asMooshroom(itemstack, clicked, player)

            is Mule -> asMuleBucket(itemstack, clicked, player)

            is Ocelot -> asOcelotBucket(itemstack, clicked, player)

            is Panda -> asPandaBucket(itemstack, clicked, player)

            is Parrot -> asParrotBucket(itemstack, clicked, player)

            is Pig -> asPigBucket(itemstack, clicked, player)

            is PolarBear -> asPolarBearBucket(itemstack, clicked, player)

            is Rabbit -> asRabbitBucket(itemstack, clicked, player)

            is Sheep -> asSheepBucket(itemstack, clicked, player)

            is SkeletonHorse -> asSkeletonHorseBucket(itemstack, clicked, player)

            is Sniffer -> asSnifferBucket(itemstack, clicked, player)

            is Squid -> asSquidBucket(itemstack, clicked, player)

            is Strider -> asStriderBucket(itemstack, clicked, player)

            is TraderLlama -> asTraderLlamaBucket(itemstack, clicked, player)

            is Turtle -> asTurtleBucket(itemstack, clicked, player)

            is Wolf -> asWolfBucket(itemstack, clicked, player)

            is Allay -> asAllayBucket(itemstack, clicked, player)

            is IronGolem -> asIronGolemBucket(itemstack, clicked, player)

            is Snowman -> asSnowGolemBucket(itemstack, clicked, player)

            is Villager -> asVillagerBucket(itemstack, clicked, player)

            is WanderingTrader -> asWanderingTraderBucket(itemstack, clicked, player)

            is ZombieVillager -> asZombieVillagerBucket(itemstack, clicked, player)

            else -> TODO("unsupported entity type: ${clicked.type}")
        }
    }

    private fun ItemStack.setCustomModelData(value: String) {
        setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString(value))
    }

    private fun <T : EntityBucketInfo> ItemStack.setEntityBucketInfo(value: T) {
        setData(ItemDataTypes.ENTITY_BUCKET_INFO, value)
    }

    //<editor-fold desc="Animals">
    private fun asArmadilloBucket(itemstack: ItemStack, clicked: Armadillo, player: Player) {
        itemstack.setCustomModelData("armadillo")
        itemstack.setEntityBucketInfo(
            ArmadilloEntityBucketInfo(
                state = clicked.state.name,
                isAdult = clicked.isAdult,
            )
        )
        // TODO 播放交互音效
    }

    private fun asBeeBucket(itemstack: ItemStack, clicked: Bee, player: Player) {
        itemstack.setCustomModelData("bee")
        itemstack.setEntityBucketInfo(
            BeeEntityBucketInfo(
                isAdult = clicked.isAdult,
            )
        )
        // TODO 播放交互音效
    }

    private fun asCamelBucket(itemstack: ItemStack, clicked: Camel, player: Player) {
        itemstack.setCustomModelData("camel")
        itemstack.setEntityBucketInfo(
            CamelEntityBucketInfo(
                isAdult = clicked.isAdult,
                owner = clicked.owner?.name ?: "none"
            )
        )
        // TODO 播放交互音效
    }

    private fun asCatBucket(itemstack: ItemStack, clicked: Cat, player: Player) {
        itemstack.setCustomModelData("cat/white") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(
            CatEntityBucketInfo(
                collarColor = clicked.collarColor.name,
                variant = clicked.catType.key.value()
            )
        )
        // TODO 播放交互音效
    }

    private fun asChickenBucket(itemstack: ItemStack, clicked: Chicken, player: Player) {
        itemstack.setCustomModelData("chicken/temperate") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(ChickenEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asCowBucket(itemstack: ItemStack, clicked: Cow, player: Player) {
        itemstack.setCustomModelData("cow/temperate") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(CowEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asDolphinBucket(itemstack: ItemStack, clicked: Dolphin, player: Player) {
        itemstack.setCustomModelData("dolphin")
        itemstack.setEntityBucketInfo(DolphinEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asDonkeyBucket(itemstack: ItemStack, clicked: Donkey, player: Player) {
        itemstack.setCustomModelData("donkey")
        itemstack.setEntityBucketInfo(DonkeyEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asFoxBucket(itemstack: ItemStack, clicked: Fox, player: Player) {
        itemstack.setCustomModelData("fox/red") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(FoxEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asFrogBucket(itemstack: ItemStack, clicked: Frog, player: Player) {
        itemstack.setCustomModelData("frog/temperate") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(FrogEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asGlowSquidBucket(itemstack: ItemStack, clicked: GlowSquid, player: Player) {
        itemstack.setCustomModelData("glow_squid")
        itemstack.setEntityBucketInfo(GlowSquidEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asGoatBucket(itemstack: ItemStack, clicked: Goat, player: Player) {
        itemstack.setCustomModelData("goat")
        itemstack.setEntityBucketInfo(GoatEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asHappyGhast(itemstack: ItemStack, clicked: HappyGhast, player: Player) {
        itemstack.setCustomModelData("happy_ghast")
        itemstack.setEntityBucketInfo(HappyGhastEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asHoglinBucket(itemstack: ItemStack, clicked: Hoglin, player: Player) {
        itemstack.setCustomModelData("hoglin")
        itemstack.setEntityBucketInfo(HoglinEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asHorseBucket(itemstack: ItemStack, clicked: Horse, player: Player) {
        itemstack.setCustomModelData("horse") // 由于可能性太多, 贴图不考虑品种
        itemstack.setEntityBucketInfo(HorseEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asLlamaBucket(itemstack: ItemStack, clicked: Llama, player: Player) {
        itemstack.setCustomModelData("llama/creamy") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(LlamaEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asMooshroom(itemstack: ItemStack, clicked: MushroomCow, player: Player) {
        itemstack.setCustomModelData("mooshroom/red") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(MooshroomEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asMuleBucket(itemstack: ItemStack, clicked: Mule, player: Player) {
        itemstack.setCustomModelData("mule")
        itemstack.setEntityBucketInfo(MuleEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asOcelotBucket(itemstack: ItemStack, clicked: Ocelot, player: Player) {
        itemstack.setCustomModelData("ocelot")
        itemstack.setEntityBucketInfo(OcelotEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asPandaBucket(itemstack: ItemStack, clicked: Panda, player: Player) {
        itemstack.setCustomModelData("panda")
        itemstack.setEntityBucketInfo(PandaEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asParrotBucket(itemstack: ItemStack, clicked: Parrot, player: Player) {
        itemstack.setCustomModelData("parrot/red") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(ParrotEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asPigBucket(itemstack: ItemStack, clicked: Pig, player: Player) {
        itemstack.setCustomModelData("pig/temperate") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(PigEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asPolarBearBucket(itemstack: ItemStack, clicked: PolarBear, player: Player) {
        itemstack.setCustomModelData("polar_bear")
        itemstack.setEntityBucketInfo(PolarBearEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asRabbitBucket(itemstack: ItemStack, clicked: Rabbit, player: Player) {
        itemstack.setCustomModelData("rabbit/white") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(RabbitEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asSheepBucket(itemstack: ItemStack, clicked: Sheep, player: Player) {
        itemstack.setCustomModelData("sheep/white") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(SheepEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asSkeletonHorseBucket(itemstack: ItemStack, clicked: SkeletonHorse, player: Player) {
        itemstack.setCustomModelData("skeleton_horse")
        itemstack.setEntityBucketInfo(SkeletonHorseEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asSnifferBucket(itemstack: ItemStack, clicked: Sniffer, player: Player) {
        itemstack.setCustomModelData("sniffer")
        itemstack.setEntityBucketInfo(SnifferEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asSquidBucket(itemstack: ItemStack, clicked: Squid, player: Player) {
        itemstack.setCustomModelData("squid")
        itemstack.setEntityBucketInfo(SquidEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asStriderBucket(itemstack: ItemStack, clicked: Strider, player: Player) {
        itemstack.setCustomModelData("strider")
        itemstack.setEntityBucketInfo(StriderEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asTraderLlamaBucket(itemstack: ItemStack, clicked: TraderLlama, player: Player) {
        itemstack.setCustomModelData("trader_llama/creamy") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(TraderLlamaEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asTurtleBucket(itemstack: ItemStack, clicked: Turtle, player: Player) {
        itemstack.setCustomModelData("turtle")
        itemstack.setEntityBucketInfo(TurtleEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asWolfBucket(itemstack: ItemStack, clicked: Wolf, player: Player) {
        itemstack.setCustomModelData("wolf/pale") // TODO 根据变种设置
        itemstack.setEntityBucketInfo(WolfEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }
    //</editor-fold>

    //<editor-fold desc="Animals Like">
    private fun asAllayBucket(itemstack: ItemStack, clicked: Allay, player: Player) {
        itemstack.setCustomModelData("allay")
        itemstack.setEntityBucketInfo(AllayEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asSnowGolemBucket(itemstack: ItemStack, clicked: Snowman, player: Player) {
        itemstack.setCustomModelData("snow_golem")
        itemstack.setEntityBucketInfo(SnowGolemEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }

    private fun asIronGolemBucket(itemstack: ItemStack, clicked: IronGolem, player: Player) {
        itemstack.setCustomModelData("iron_golem")
        itemstack.setEntityBucketInfo(IronGolemEntityBucketInfo()) // TODO 填充生物信息
        // TODO 播放交互音效
    }
    //</editor-fold>

    //<editor-fold desc="NPCs">
    private fun asVillagerBucket(itemstack: ItemStack, clicked: Villager, player: Player) {
        itemstack.setCustomModelData("villager/${clicked.villagerType.key.value()}")
        itemstack.setEntityBucketInfo(
            VillagerEntityBucketInfo(
                level = clicked.villagerLevel,
                region = clicked.villagerType.key().value(),
                profession = clicked.profession.key().value()
            )
        )
        // TODO 播放交互音效
    }

    private fun asWanderingTraderBucket(itemstack: ItemStack, clicked: WanderingTrader, player: Player) {
        itemstack.setCustomModelData("wandering_trader")
        itemstack.setEntityBucketInfo(
            WanderingTraderEntityBucketInfo(
                clicked.recipes.map { recipe ->
                    val ingreds = recipe.ingredients.map {
                        val name = it.getData(DataComponentTypes.ITEM_NAME)!!.plain
                        val amount = it.amount
                        if (amount > 1) "$name x$amount" else name
                    }
                    val result = run {
                        val name = recipe.result.getData(DataComponentTypes.ITEM_NAME)!!.plain
                        val amount = recipe.result.amount
                        if (amount > 1) "$name x$amount" else name
                    }
                    "${ingreds.joinToString(" + ")} -> $result"
                }
            )
        )
        // TODO 播放交互音效
    }

    private fun asZombieVillagerBucket(itemstack: ItemStack, clicked: ZombieVillager, player: Player) {
        itemstack.setCustomModelData("zombie_villager")
        itemstack.setEntityBucketInfo(
            ZombieVillagerEntityBucketInfo(
                region = clicked.villagerType.key().value(),
                profession = clicked.villagerProfession.key().value()
            )
        )
        // TODO 播放交互音效
    }
    //</editor-fold>
}
