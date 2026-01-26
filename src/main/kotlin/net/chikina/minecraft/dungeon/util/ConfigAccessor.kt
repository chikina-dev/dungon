package net.chikina.minecraft.dungeon.util

import java.io.File
import java.io.IOException
import net.chikina.minecraft.dungeon.Dungeon
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration

class ConfigAccessor(private val plugin: Dungeon, fileName: String) {
    private val file: File = File(plugin.dataFolder, fileName)
    private var config: YamlConfiguration? = null

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        if (!file.exists()) {
            try {
                val resource = plugin.getResource("default_config.yml")
                if (resource != null) {
                    file.writeBytes(resource.readBytes())
                } else {
                    file.createNewFile()
                }
            } catch (e: IOException) {
                Log.error("Could not create $fileName!")
                e.printStackTrace()
            }
        }
        reloadConfig()
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun getConfig(): YamlConfiguration {
        if (config == null) {
            reloadConfig()
        }
        return config!!
    }

    fun <T> getStructList(path: String, mapper: (TypedSection) -> T): List<T> {
        val root = getConfig()
        val list = root.getMapList(path)
        val result = mutableListOf<T>()
        for (item in list) {
            if (item is Map<*, *>) {
                try {
                    @Suppress("UNCHECKED_CAST") val map = item as Map<String, Any?>
                    result.add(mapper(TypedSection(map)))
                } catch (e: Exception) {
                    // Skip invalid entries
                }
            }
        }
        return result
    }

    class TypedSection(private val map: Map<String, Any?>) {
        fun getString(key: String): String? = map[key] as? String

        fun getSection(key: String): TypedSection? {
            val subMap = map[key]
            if (subMap is Map<*, *>) {
                @Suppress("UNCHECKED_CAST") return TypedSection(subMap as Map<String, Any?>)
            }
            return null
        }

        fun getStringReq(key: String): String =
                getString(key) ?: error("Missing required string: $key")
        fun getIntReq(key: String): Int =
                (map[key] as? Number)?.toInt() ?: error("Missing required int: $key")
        fun getDoubleReq(key: String): Double =
                (map[key] as? Number)?.toDouble() ?: error("Missing required double: $key")

        fun getFloatReq(key: String): Float =
                (map[key] as? Number)?.toFloat() ?: error("Missing required float: $key")
        fun getSectionReq(key: String): TypedSection =
                getSection(key) ?: error("Missing required section: $key")
        fun getBoolean(key: String, def: Boolean = false): Boolean = (map[key] as? Boolean) ?: def
        fun getInt(key: String, def: Int = 0): Int = (map[key] as? Number)?.toInt() ?: def
    }

    abstract class ConfigModel(protected val section: TypedSection) {
        protected fun getString(key: String): String = section.getStringReq(key)
        protected fun getInt(key: String): Int = section.getIntReq(key)
        protected fun getDouble(key: String): Double = section.getDoubleReq(key)
        protected fun getFloat(key: String): Float = section.getFloatReq(key)
        protected fun getBoolean(key: String, def: Boolean = false): Boolean =
                section.getBoolean(key, def)
        protected fun getInt(key: String, def: Int): Int = section.getInt(key, def)

        protected fun getLocation(key: String): Location {
            val s = section.getSectionReq(key)
            val worldName = s.getStringReq("world")
            val world = Bukkit.getWorld(worldName) ?: error("World not found: $worldName")
            val x = s.getDoubleReq("x")
            val y = s.getDoubleReq("y")
            val z = s.getDoubleReq("z")
            val yaw = s.getFloatReq("yaw")
            val pitch = s.getFloatReq("pitch")
            return Location(world, x, y, z, yaw, pitch)
        }
    }
}
