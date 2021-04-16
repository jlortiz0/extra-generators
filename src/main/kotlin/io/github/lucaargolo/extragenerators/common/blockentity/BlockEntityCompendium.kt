package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

@Suppress("UNCHECKED_CAST")
object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val ITEM_GENERATOR_TYPE = register("item_generator", BlockEntityType.Builder.create( { ItemGeneratorBlockEntity() }, *BlockCompendium.itemGeneratorArray() ).build(null)) as BlockEntityType<ItemGeneratorBlockEntity>
    val FLUID_GENERATOR_TYPE = register("fluid_generator", BlockEntityType.Builder.create( { FluidGeneratorBlockEntity() }, *BlockCompendium.fluidGeneratorArray() ).build(null)) as BlockEntityType<FluidGeneratorBlockEntity>
    val FLUID_ITEM_GENERATOR_TYPE = register("fluid_item_generator", BlockEntityType.Builder.create( { FluidItemGeneratorBlockEntity() }, *BlockCompendium.fluidItemGeneratorArray() ).build(null)) as BlockEntityType<FluidItemGeneratorBlockEntity>


}