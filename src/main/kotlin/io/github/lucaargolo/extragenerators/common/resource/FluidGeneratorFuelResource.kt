package io.github.lucaargolo.extragenerators.common.resource

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import java.io.InputStreamReader

class FluidGeneratorFuelResource: SimpleSynchronousResourceReloadListener {

    private val fluidKeysMap = linkedMapOf<String, LinkedHashMap<FluidKey, FluidGeneratorFuel>>()
    val clientFluidKeysMap = linkedMapOf<String, LinkedHashMap<FluidKey, FluidGeneratorFuel>>()

    fun test(id: String, fluidKey: FluidKey): FluidGeneratorFuel? {
        val map = if(clientFluidKeysMap.isEmpty()) fluidKeysMap else clientFluidKeysMap
        map[id]?.forEach { (key, fuel) ->
            if(key == fluidKey) return fuel
        }
        return null
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(fluidKeysMap.size)
        fluidKeysMap.forEach { (id, fluidKeyMap) ->
            buf.writeString(id)
            buf.writeInt(fluidKeyMap.size)
            fluidKeyMap.forEach { (fluidKey, fuel) ->
                fluidKey.toMcBuffer(buf)
                fuel.toBuf(buf)
            }
        }
    }

    fun fromBuf(buf: PacketByteBuf) {
        clientFluidKeysMap.clear()
        val fluidKeysMapSize = buf.readInt()
        repeat(fluidKeysMapSize) {
            val fluidKeysMapId = buf.readString()
            val fluidKeyMapSize = buf.readInt()
            repeat(fluidKeyMapSize) {
                val fluidKey = FluidKey.fromMcBuffer(buf)
                val fuel = FluidGeneratorFuel.fromBuf(buf) ?: FluidGeneratorFuel(0, FluidKeys.EMPTY.withAmount(FluidAmount.ZERO), 0.0)
                clientFluidKeysMap.getOrPut(fluidKeysMapId) { linkedMapOf() } [fluidKey] = fuel
            }
        }
    }

    override fun getFabricId() = ModIdentifier("fluid_generators")

    override fun reload(manager: ResourceManager) {
        fluidKeysMap.clear()
        ExtraGenerators.LOGGER.info("Loading fluid generators resource.")
        manager.findResources("fluid_generators") { r -> r.endsWith(".json") }.forEach { fluidsResource ->
            val id = fluidsResource.path.split("/").lastOrNull()?.replace(".json", "") ?: return@forEach
            val resource = manager.getResource(fluidsResource)
            ExtraGenerators.LOGGER.info("Loading $id fluid generators resource at $fluidsResource.")
            try {
                val json = ExtraGenerators.PARSER.parse(InputStreamReader(resource.inputStream, "UTF-8"))
                val jsonArray = json.asJsonArray
                jsonArray.forEach { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
                    val generatorFuel = FluidGeneratorFuel.fromJson(jsonObject.get("fuel").asJsonObject)
                    generatorFuel?.let {
                        fluidKeysMap.getOrPut(id) { linkedMapOf() }[it.fluidInput.fluidKey] = it
                    }
                }
            }catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Unknown error while trying to read $id fluid generators resource at $fluidsResource", e)
            }
        }
        ExtraGenerators.LOGGER.info("Finished loading fluid generators resource (${fluidKeysMap.map { it.value.size }.sum()} entries loaded).")
    }

}