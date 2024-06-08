package me.pride.spirits.abilities.dark.combos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.region.RegionProtection;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ReplaceableSpirit;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.builder.SpiritBuilder;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempBlock.RevertTask;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

public class Corruption extends DarkSpiritAbility implements AddonAbility, ComboAbility {
	// TODO: make corrupted temp blocks not dupe
	private final String path = Tools.path(this, Path.COMBOS);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("RevertTime")
	private long revertTime;
	private double speed, maxSpeed;
	private double spawnSpeed;
	private int corruptRate;
	private boolean spawnSpirit;
	
	private double corruptSpeed;
	private double spawnSpiritSpeed;
	
	private Location origin;
	
	private List<Block> blocks;
	private Set<Block> corrupted;
	private Set<Spirit> spirits;
	
	public Corruption(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		radius = Spirits.instance.getConfig().getDouble(path + "CorruptRadius");
		speed = Spirits.instance.getConfig().getDouble(path + "CorruptSpeed");
		maxSpeed = Spirits.instance.getConfig().getDouble(path + "MaxCorruptSpeed");
		corruptRate = Spirits.instance.getConfig().getInt(path + "CorruptRate");
		duration = Spirits.instance.getConfig().getLong(path + "Duration");
		spawnSpirit = Spirits.instance.getConfig().getBoolean(path + "SpawnDarkSpirit.Enabled");
		spawnSpeed = Math.min(1.0, Spirits.instance.getConfig().getDouble(path + "SpawnDarkSpirit.Speed"));
		
		revertTime = duration;
		origin = player.getLocation().clone();
		
		blocks = GeneralMethods.getBlocksAroundPoint(origin, radius)
					.stream()
					.filter(b -> !isAir(b.getType()) && isAir(b.getRelative(BlockFace.UP).getType()) && !RegionProtection.isRegionProtected(this, b.getLocation()))
					.collect(Collectors.toList());

		corrupted = new HashSet<>();
		spirits = new HashSet<>();
		
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		startCorruption();
	}
	
	private void startCorruption() {
		if (corruptRate > 1) {
			for (int i = 0; i < corruptRate; i++) {
				changeBlocks();
			}
		} else {
			changeBlocks();
		}
		corruption();
	}
	
	private void changeBlocks() {
		corruptSpeed = corruptSpeed > maxSpeed ? 0 : corruptSpeed + speed;

		if (corruptSpeed == 0) {
			Block block = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));

			if (corrupted.contains(block) && corrupted.size() < blocks.size()) {
				while (corrupted.contains(block)) {
					block = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
				}
			}
			corrupted.add(block);

			BlockData blockData = isPlant(block) ? Material.DEAD_BUSH.createBlockData() : Material.MYCELIUM.createBlockData();

			Block block_ = block;
			new TempBlock(block, blockData, revertTime).setRevertTask(() -> block_.removeMetadata("spirits:corrupted_blocks", Spirits.instance));

			spawnDarkSpirit(block.getLocation().clone().add(0.5, 1.5, 0.5));
			player.getWorld().spawnParticle(Particle.SPELL_WITCH, block.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);

			if (block.hasMetadata("spirits:blessed_source")) {
				block.removeMetadata("spirits:blessed_source", Spirits.instance);
			}
			block.setMetadata("spirits:corrupted_blocks", new FixedMetadataValue(Spirits.instance, 0));
		}
	}
	
	private void corruption() {
		for (Block corruptedBlock : corrupted) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(corruptedBlock.getLocation(), 1.25)) {
				if (Filter.filterEntityDark(entity)) {
					continue;
				}
				if (entity instanceof LivingEntity) {
					PotionEffectType.POISON.createEffect(20, 1).apply((LivingEntity) entity);

					if (Filter.filterGeneralEntity(entity, player, this) && Filter.filterEntityLight(entity)) {
						if (ReplaceableSpirit.isReplacedEntity(entity)) {
							continue;
						}
						spirits.add(SpiritBuilder.dark().spawn(player.getWorld(), entity.getLocation()).replace(entity).build());
					}
				}
			}
		}
	}
	
	private void spawnDarkSpirit(Location location) {
		if (spawnSpirit) {
			spawnSpiritSpeed = spawnSpiritSpeed > 1 ? 0 : spawnSpiritSpeed + spawnSpeed;
			
			if (spawnSpiritSpeed == 0) {
				spirits.add(SpiritBuilder.dark().spawn(player.getWorld(), location).build());
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Combos.Corruption.Enabled", true);
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
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
		return "Corruption";
	}
	
	@Override
	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<>();
		for (int i = 0; i < blocks.size(); i++) {
			if (!corrupted.contains(i)) continue;
			
			locations.add(blocks.get(i).getLocation());
		}
		return locations;
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void remove() {
		super.remove();
		for (Spirit spirit : spirits) {
			if (spirit == null) continue;
			
			Spirit.destroy(spirit);
		}
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
	public String getDescription() {
		return "";
	}
	
	@Override
	public String getInstructions() {
		return "";
	}

	@Override
	public void load() { }

	@Override
	public void stop() { }
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new Corruption(player);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> info = new ArrayList<>();
		info.add(new AbilityInformation("Obelisk", ClickType.SHIFT_UP));
		info.add(new AbilityInformation("Obelisk", ClickType.SHIFT_DOWN));
		info.add(new AbilityInformation("Obelisk", ClickType.SHIFT_UP));
		info.add(new AbilityInformation("Obelisk", ClickType.SHIFT_DOWN));
		return info;
	}
	
}