package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ActionBar;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Divination extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute("EffectAmplifier")
	private int effectAmplifier;
	@Attribute("EffectDuration")
	private int effectDuration;

	private long chargeTime;
	private boolean charged;

	private List<Block> divinationBlocks;

	public Divination(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.radius = Spirits.instance.getConfig().getDouble(path + "Radius");
		this.effectAmplifier = Spirits.instance.getConfig().getInt(path + "ChimeraAmplifier");
		this.effectDuration = Spirits.instance.getConfig().getInt(path + "ChimeraDuration");

		this.chargeTime = 0; // TODO: make calculation for charge time

		this.divinationBlocks = new ArrayList<>();

		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
			if (StorageCache.divinedBlockAlreadyMined(block)) {
				continue;
			}
			if (isAir(block.getType()) || block.getLocation().getY() > 0) {
				continue;
			} else if (block.getType() != Material.DEEPSLATE || block.getType() != Material.STONE) {
				continue;
			} else if (Filter.filterIndestructible(block)) {
				continue;
			}
			this.divinationBlocks.add(block);
		}
		if (this.divinationBlocks.isEmpty()) {
			ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getColor() + "* No blocks to divinate. *", player);
			return;
		}
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (player.isSneaking()) {
			if (System.currentTimeMillis() > getStartTime() + chargeTime) {
				charged = true;
			}
		} else {
			if (!charged) {
				ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getColor() + "* You had no chimeras of this region. *", player);
				remove();
				return;
			} else {
				ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getColor() + "* You just had a chimera. Follow it. *", player);

				PotionEffectType.CONFUSION.createEffect(effectDuration, effectAmplifier).apply(player);

				Block divined = divinate();
				// TODO: Implement the divination trail effect

				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
	}

	private Block divinate() {
		return divinationBlocks.get(ThreadLocalRandom.current().nextInt(divinationBlocks.size()));
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Abilities.Divination.Enabled");
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "Divination";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}

	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}

	@Override
	public void load() { }

	@Override
	public void stop() { }
}
