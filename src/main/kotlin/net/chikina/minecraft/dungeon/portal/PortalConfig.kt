package net.chikina.minecraft.dungeon.portal

import net.chikina.minecraft.dungeon.util.ConfigAccessor
import org.bukkit.Location

class PortalData(section: ConfigAccessor.TypedSection) : ConfigAccessor.ConfigModel(section) {
    val id: String = getString("id")
    val entrance: Location = getLocation("entrance")
    val exit: Location = getLocation("exit")
    val bidirectional: Boolean = getBoolean("bidirectional", false)
    val requiredLevel: Int = getInt("requiredLevel", 0)
}

class PortalConfig(private val configAccessor: ConfigAccessor) {
    private val portals = mutableListOf<PortalData>()

    init {
        load()
    }

    fun load() {
        configAccessor.reloadConfig()
        portals.clear()

        val loadedPortals = configAccessor.getStructList("portals", ::PortalData)
        portals.addAll(loadedPortals)
    }

    fun getPortalByEntry(location: Location): PortalData? {
        return portals.find {
            it.entrance.world == location.world && it.entrance.distanceSquared(location) <= 16.0
        }
    }

    fun getPortalByExit(location: Location): PortalData? {
        return portals.find {
            it.bidirectional &&
                    it.exit.world == location.world &&
                    it.exit.distanceSquared(location) <= 16.0
        }
    }
}
