package me.pride.spirits.abilities.dark.combos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritBuilder;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.game.DarkSpiritAbility;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempBlock.RevertTask;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class Corruption extends DarkSpiritAbility implements AddonAbility, ComboAbility {
	// TODO: make corrupted temp blocks not dupe, harm players near corrupted blocks
	private final String path = Tools.path(this, Path.COMBOS);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.SPEED)
	private double max_speed;
	@Attribute(Attribute.SPEED)
	private int corrupt_rate;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("RevertTime")
	private long revert_time;
	private boolean spawn_spirit;
	@Attribute(Attribute.SPEED)
	private double spawn_speed;
	
	private double corruptSpeed;
	private double spawnSpiritSpeed;
	
	private Location origin;
	
	private List<Block> blocks;
	private List<Integer> corrupted;
	private Set<Spirit> spirits;
	private static final Pair<String, MetadataValue> METADATA_VALUE = Pair.of("spirits:corrupted_blocks", new FixedMetadataValue(Spirits.instance, 0));
	
	public Corruption(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		radius = Spirits.instance.getConfig().getDouble(path + "CorruptRadius");
		speed = Spirits.instance.getConfig().getDouble(path + "CorruptSpeed");
		max_speed = Spirits.instance.getConfig().getDouble(path + "MaxCorruptSpeed");
		corrupt_rate = Spirits.instance.getConfig().getInt(path + "CorruptRate");
		duration = Spirits.instance.getConfig().getLong(path + "Duration");
		spawn_spirit = Spirits.instance.getConfig().getBoolean(path + "SpawnDarkSpirit.Enabled");
		spawn_speed = Math.min(1.0, Spirits.instance.getConfig().getDouble(path + "SpawnDarkSpirit.Speed"));
		
		revert_time = duration;
		origin = player.getLocation().clone();
		
		blocks = GeneralMethods.getBlocksAroundPoint(origin, radius)
				.stream().filter(b -> !isAir(b.getType()) && isAir(b.getRelative(BlockFace.UP).getType()) && !GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation()))
				.collect(Collectors.toList());
		corrupted = new ArrayList<>();
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
		if (corrupt_rate > 1) {
			for (int i = 0; i < corrupt_rate; i++) {
				changeBlocks();
			}
		} else {
			changeBlocks();
		}
		corruption();
	}
	
	private void changeBlocks() {
		corruptSpeed += speed;
		if (corruptSpeed > max_speed) {
			int index = ThreadLocalRandom.current().nextInt(blocks.size());
			
			if (!corrupted.contains(index)) {
				Block block = blocks.get(index);
				BlockData corruptBlockData = isPlant(block) ? Material.DEAD_BUSH.createBlockData() : Material.MYCELIUM.createBlockData();
				
				new TempBlock(block, corruptBlockData, revert_time).setRevertTask(new RevertTask() {
					@Override
					public void run() {
						block.removeMetadata(METADATA_VALUE.getLeft(), Spirits.instance);
					}
				});
				spawnDarkSpirit(block.getLocation().clone().add(0.5, 1.5, 0.5));
				player.getWorld().spawnParticle(Particle.SPELL_WITCH, block.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				
				block.setMetadata(METADATA_VALUE.getLeft(), METADATA_VALUE.getRight());
				corrupted.add(index);
			}
			corruptSpeed = 0;
		}
	}
	
	private void corruption() {
		for (int i = 0; i < blocks.size(); i++) {
			if (corrupted.contains(i)) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(blocks.get(i).getLocation(), 1.25)) {
					if (entity instanceof LivingEntity) {
						if (Filter.filterEntityFromAbility(entity, player, this) && Filter.filterEntityLight(entity)) {
							if (!Tools.isReplacedEntity(entity)) {
								spirits.add(SpiritBuilder.dark().spawn(player.getWorld(), entity.getLocation()).replace(entity).build());
							}
						}
					}
				}
			}
		}
	}
	
	private void spawnDarkSpirit(Location location) {
		if (spawn_spirit) {
			spawnSpiritSpeed += spawn_speed;
			
			if (spawnSpiritSpeed > 1) {
				spirits.add(SpiritBuilder.dark().spawn(player.getWorld(), location).build());
				spawnSpiritSpeed = 0;
			}
		}
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
	public boolean isIgniteAbility() { return false; }
	
	@Override
	public boolean isExplosiveAbility() { return false; }
	
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
		for (Spirit spirit : spirits) {
			if (spirit == null) continue;
			
			Spirit.destroy(spirit);
		}
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Combos.Corruption.Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion(this.getElement());
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