package net.chikina.minecraft.dungeon.map

import net.chikina.minecraft.dungeon.foraging.TreeStructure
import net.chikina.minecraft.dungeon.foraging.TreeType
import net.chikina.minecraft.dungeon.map.structure.StructureGenerator
import java.util.concurrent.ConcurrentHashMap

class WorldMap(
  val config: MapConfig,
) {
  private val regions = ConcurrentHashMap<Long, Region>()
  private val generator = MapGenerator(config)
  private val structureGenerator = StructureGenerator(config.seed)

  init {
    structureGenerator.register(TreeStructure.createBlueprint(TreeType.OAK))
  }

  fun getChunk(chunkX: Int, chunkZ: Int): Chunk {
    val regionX = Math.floorDiv(chunkX, MapConstants.REGION_WIDTH)
    val regionZ = Math.floorDiv(chunkZ, MapConstants.REGION_HEIGHT)

    val region = getRegion(regionX, regionZ)

    var localX = chunkX % MapConstants.REGION_WIDTH
    var localZ = chunkZ % MapConstants.REGION_HEIGHT

    if (localX < 0) localX += MapConstants.REGION_WIDTH
    if (localZ < 0) localZ += MapConstants.REGION_HEIGHT

    var chunk = region.getChunk(localX, localZ)
    if (chunk == null) {
      chunk = generator.generate(chunkX, chunkZ)
      structureGenerator.populate(chunk, generator)
      region.setChunk(localX, localZ, chunk)
    }
    return chunk
  }

  private fun getRegion(regionX: Int, regionZ: Int): Region {
    val key = (regionX.toLong() shl 32) or (regionZ.toLong() and 0xFFFFFFFFL)
    return regions.getOrPut(key) { Region(regionX, regionZ) }
  }

  fun getEnvironment(x: Int, z: Int): Environment = generator.getEnvironment(x, z)

  fun getDensity(x: Double, y: Double, z: Double): Double = generator.getDensity(x, y, z)

  fun getVariant(x: Double, y: Double, z: Double): Double = generator.getVariant(x, y, z)

  fun getStructureGenerator(): StructureGenerator = structureGenerator
}
