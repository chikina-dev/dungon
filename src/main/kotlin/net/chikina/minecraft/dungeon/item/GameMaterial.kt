package net.chikina.minecraft.dungeon.item

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class GameMaterial(
        val displayName: String,
        val material: Material,
        val description: List<String> = emptyList(),
        val rarity: Rarity = Rarity.COMMON
) {
        ORB_HEALING(
                "Healing Orb",
                Material.SLIME_BALL,
                listOf("Essence of life."),
                Rarity.UNCOMMON
        ),
        ORB_FIRE(
                "Fire Orb",
                Material.MAGMA_CREAM,
                listOf("Burning with intensity."),
                Rarity.UNCOMMON
        ),
        ORB_WIND("Wind Orb", Material.FEATHER, listOf("Light as a feather."), Rarity.UNCOMMON),
        ORB_THUNDER(
                "Thunder Orb",
                Material.AMETHYST_SHARD,
                listOf("Crackling with energy."),
                Rarity.UNCOMMON
        ),
        ORB_VOID("Void Orb", Material.ENDER_PEARL, listOf("Dark and mysterious."), Rarity.RARE),
        ORB_ICE("Ice Orb", Material.SNOWBALL, listOf("Cold to the touch."), Rarity.UNCOMMON),
        ORB_HOOK("Grappling Orb", Material.STRING, listOf("Useful for mobility."), Rarity.COMMON),
        TOKEN_A("Goblin Token", Material.GOLD_NUGGET, listOf("Dropped by Goblin."), Rarity.COMMON),
        TOKEN_B("Orc Token", Material.IRON_NUGGET, listOf("Dropped by Orc."), Rarity.COMMON),
        TOKEN_C(
                "Slime Condensate",
                Material.SLIME_BALL,
                listOf("Dropped by Slime."),
                Rarity.COMMON
        ),
        TOKEN_D(
                "Spider Eye",
                Material.SPIDER_EYE,
                listOf("Dropped by Cave Spider."),
                Rarity.COMMON
        ),
        TOKEN_E("Bone Shard", Material.BONE, listOf("Dropped by Skeleton."), Rarity.COMMON),
        STAR_FRAGMENT(
                "Star Fragment",
                Material.PRISMARINE_CRYSTALS,
                listOf("A piece of a fallen star."),
                Rarity.UNCOMMON
        ),
        STAR_DUST(
                "Star Dust",
                Material.GLOWSTONE_DUST,
                listOf("Shimmering dust from the cosmos."),
                Rarity.UNCOMMON
        ),
        COSMIC_SHARD(
                "Cosmic Shard",
                Material.AMETHYST_CLUSTER,
                listOf("Vibrating with cosmic energy."),
                Rarity.RARE
        ),
        STELLAR_CORE("Stellar Core", Material.BEACON, listOf("The heart of a star."), Rarity.EPIC),
        GALACTIC_ESSENCE(
                "Galactic Essence",
                Material.NETHER_STAR,
                listOf("Pure energy of the galaxy."),
                Rarity.LEGENDARY
        );

        enum class Rarity(val color: NamedTextColor) {
                COMMON(NamedTextColor.WHITE),
                UNCOMMON(NamedTextColor.GREEN),
                RARE(NamedTextColor.BLUE),
                EPIC(NamedTextColor.LIGHT_PURPLE),
                LEGENDARY(NamedTextColor.GOLD)
        }
}
