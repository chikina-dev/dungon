package net.chikina.minecraft.dungeon.map

import net.chikina.minecraft.dungeon.math.noise.Perlin

class MapGenerator(
  val config: MapConfig,
) {
  private val elevationGen = Perlin(config.seed)
  private val temperatureGen = Perlin(config.seed + 1)
  private val humidityGen = Perlin(config.seed + 2)
  private val resourceGen = Perlin(config.seed + 3)
  private val magicGen = Perlin(config.seed + 4)
  private val caveGen = Perlin(config.seed + 5)
  private val variantGen = Perlin(config.seed + 6)

  private val scaleElev = 0.02 * config.scaleMultiplier
  private val scaleTemp = 0.005 * config.scaleMultiplier
  private val scaleHum = 0.005 * config.scaleMultiplier
  private val scaleRes = 0.02 * config.scaleMultiplier
  private val scaleMagic = 0.02 * config.scaleMultiplier
  private val scaleCave = 0.04 * config.scaleMultiplier
  private val scaleVariant = 0.05 * config.scaleMultiplier

  fun generate(chunkX: Int, chunkZ: Int): Chunk {
    val chunk = Chunk(chunkX, chunkZ, config.top, config.minY)
    val startX = chunkX * MapConstants.CHUNK_WIDTH
    val startZ = chunkZ * MapConstants.CHUNK_HEIGHT

    for (z in 0 until MapConstants.CHUNK_HEIGHT) {
      val absZ = startZ + z
      for (x in 0 until MapConstants.CHUNK_WIDTH) {
        val absX = startX + x

        val env = getEnvironment(absX, absZ)
        val biome = Biome.resolve(env.temperature, env.humidity, env.elevation)

        for (y in config.minY until config.maxTerrainY) {
          val variant = getVariant(absX.toDouble(), y.toDouble(), absZ.toDouble())
          val material = BlockResolver.resolve(y, env, biome, config, variant)
          chunk.setBlock(x, z, y, material.ordinal)
        }
      }
    }
    return chunk
  }

  fun getEnvironment(x: Int, z: Int): Environment {
    val elev = fbm(x.toDouble(), z.toDouble(), 4, 0.5, 2.0, scaleElev, elevationGen)

    val temp = temperatureGen.noise(x * scaleTemp, 0.0, z * scaleTemp)
    val hum = humidityGen.noise(x * scaleHum, 0.0, z * scaleHum)
    val res = resourceGen.noise(x * scaleRes, 0.0, z * scaleRes)
    val magic = magicGen.noise(x * scaleMagic, 0.0, z * scaleMagic)

    return Environment(x.toDouble(), z.toDouble(), elev, temp, hum, res, magic)
  }

  private fun fbm(
    x: Double,
    z: Double,
    octaves: Int,
    persistence: Double,
    lacunarity: Double,
    scale: Double,
    generator: Perlin,
  ): Double {
    var total = 0.0
    var frequency = scale
    var amplitude = 1.0
    var maxValue = 0.0

    for (i in 0 until octaves) {
      total += generator.noise(x * frequency, 0.0, z * frequency) * amplitude
      maxValue += amplitude

      amplitude *= persistence
      frequency *= lacunarity
    }

    return total / maxValue
  }

  fun getDensity(x: Double, y: Double, z: Double): Double = caveGen.noise(x * scaleCave, y * scaleCave, z * scaleCave)

  fun getVariant(x: Double, y: Double, z: Double): Double = variantGen.noise(x * scaleVariant, y * scaleVariant, z * scaleVariant)

  fun getTerrainHeight(x: Int, z: Int): Int {
    val env = getEnvironment(x, z)
    val biome = Biome.resolve(env.temperature, env.humidity, env.elevation)

    val baseAmplitude =
      if (biome == Biome.EXTREME_HILLS || biome == Biome.MOUNTAIN_PEAKS) 120.0 else 60.0

    val maxY = config.maxTerrainY - 1
    val maxAvailableHeight = (maxY - config.baseHeight).toDouble()
    val safeAmplitude = baseAmplitude.coerceAtMost(maxAvailableHeight * 0.9)

    val naturalSurfaceHeight = (config.baseHeight + env.elevation * safeAmplitude).toInt()
    return naturalSurfaceHeight.coerceAtMost(maxY)
  }
}
