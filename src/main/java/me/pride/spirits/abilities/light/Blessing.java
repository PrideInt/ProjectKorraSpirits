package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Blessing extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	public enum BlessType {
		CLICK, SNEAK
	}
	private BlessType type;

	public static final Set<Bless> BLESSINGS = new HashSet<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RADIUS)
	private int radius;
	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	private int blessRate;
	private int blessPerRate;
	// private boolean letBlessingFinish;
	private boolean blessRegularSpirits;

	private boolean validated;
	private boolean progressing;
	private int rate;

	private Location target;
	private BlockData quartz, lotv;
	private List<Block> blessedArea;
	private Set<Block> blessedBlocks;
	private Set<LivingEntity> blessedEntities;

	public Blessing(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Blessing.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.duration = Spirits.instance.getConfig().getLong(path + "Duration");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.radius = Spirits.instance.getConfig().getInt(path + "Radius");
		this.selectRange = Spirits.instance.getConfig().getInt(path + "SelectRange");
		this.blessRate = Spirits.instance.getConfig().getInt(path + "BlessRate");
		this.blessPerRate = Spirits.instance.getConfig().getInt(path + "BlessBlockPerRate");
		// this.letBlessingFinish = Spirits.instance.getConfig().getBoolean(path + "LetBlessingFinish");
		this.blessRegularSpirits = Spirits.instance.getConfig().getBoolean(path + "BlessRegularSpirits");

		this.quartz = Material.QUARTZ_BLOCK.createBlockData();
		this.lotv = Material.LILY_OF_THE_VALLEY.createBlockData();
		this.blessedBlocks = new HashSet<>();
		this.blessedEntities = new HashSet<>();

		this.validated = true;
	}

	public Blessing(Player player, BlessType type) {
		this(player);

		if (!this.validated) {
			return;
		}
		this.type = type;

		if (type == BlessType.CLICK) {
			Block targetBlock = Tools.rayTraceBlock(player, selectRange);

			if (targetBlock == null) {
				return;
			} else if (RegionProtection.isRegionProtected(this, targetBlock.getLocation())) {
				return;
			}
			this.target = targetBlock.getLocation().clone().add(0.5, 0.5, 0.5);
			this.blessedArea = blessedArea(targetBlock.getLocation(), radius);

			this.progressing = true;

			/*
			 * Spawning particles. I don't know if this looks good
			 *
			double distance = player.getLocation().clone().distanceSquared(this.target);
			Location particleLocation = player.getLocation().clone().add(0, 1, 0);

			for (int i = 0; i < (int) Math.sqrt(distance); i += 3) {
				particleLocation.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLocation, 1, 0.25, 0.25, 0.25, 0);
				particleLocation.add(particleLocation.getDirection().multiply(i));
			}
			 */
			bPlayer.addCooldown(this);
			start();
		} else if (type == BlessType.SNEAK) {
			this.target = player.getLocation().clone();
			this.blessedArea = new ArrayList<>();

			this.progressing = true;

			List<Block> area = blessedArea(player.getLocation(), radius);

			if (area.isEmpty()) {
				return;
			}
			for (int i = 0; i < 8; i++) {
				Block block = area.get(ThreadLocalRandom.current().nextInt(area.size()));

				for (Block blessed : blessedArea(block.getLocation(), 1.95)) {
					this.blessedArea.add(blessed);
				}
			}
			start();
		}
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		if (progressing) {
			if (type == BlessType.SNEAK) {
				if (!player.isSneaking()) {
					bPlayer.addCooldown(this);
					progressing = false;
				}
			}
			target.getWorld().spawnParticle(Particle.SPELL_INSTANT, target, 3, radius / 1.5, radius / 1.5, radius / 1.5, 0);

			rate = rate > blessRate ? 0 : rate + 1;
			if (rate == 0) {
				for (int i = 0; i < blessPerRate; i++) {
					Block block = blessedArea.get(ThreadLocalRandom.current().nextInt(blessedArea.size()));

					if (!blessedBlocks.contains(block)) {
						block.setMetadata("spirits:blessed_source", new FixedMetadataValue(Spirits.instance, 0));

						// block.getWorld().spawnParticle(Particle.SPELL_INSTANT, block.getLocation().clone().add(0.5, 0.85, 0.5), 3, 0.25, 0.25, 0.25, 0.05);
						// block.getWorld().spawnParticle(Particle.GLOW, block.getLocation().clone().add(0.5, 0.85, 0.5), 3, 0.25, 0.25, 0.25, 0.05);
						block.getWorld().spawnParticle(Particle.SHRIEK, block.getLocation().clone().add(0.5, 0.85, 0.5), 1, 0.25, 0.25, 0.25, 0, 1);

						blessedBlocks.add(block);

						BlockData data = GeneralMethods.isSolid(block) ? quartz : lotv;
						new TempBlock(block, data, duration, this);
					}
				}
			}
			Tools.trackEntitySpirit(target, radius, e -> e instanceof LivingEntity && e.getType() != EntityType.ARMOR_STAND && e.getUniqueId() != player.getUniqueId(), (entity, light, dark, neutral) -> {
				if (light || (neutral && blessRegularSpirits)) {
					PotionEffectType.REGENERATION.createEffect(20, 1).apply(entity);

					if (!entity.hasMetadata("spirits:blessed")) {
						entity.getWorld().spawnParticle(Particle.SONIC_BOOM, entity.getLocation().clone().add(0, 1, 0), 1, 0.25, 0.25, 0.25, 0);
						BLESSINGS.add(new Bless(this, entity, duration));
						entity.setMetadata("spirits:blessed", new FixedMetadataValue(Spirits.instance, 0));
					}
				} else if (dark) {
					DamageHandler.damageEntity(entity, damage, this);
				}
			});
		}
	}

	private List<Block> blessedArea(Location location, double radius) {
		return GeneralMethods.getBlocksAroundPoint(location, radius)
				.stream()
				.filter(b -> {
					boolean isLiquidAbove = b.getRelative(BlockFace.UP).isLiquid();
					boolean isLiquidSide = b.getRelative(BlockFace.NORTH).isLiquid() || b.getRelative(BlockFace.SOUTH).isLiquid() || b.getRelative(BlockFace.EAST).isLiquid() || b.getRelative(BlockFace.WEST).isLiquid();

					boolean isAirAbove = isAir(b.getRelative(BlockFace.UP).getType());
					boolean isAirSide = isAir(b.getRelative(BlockFace.NORTH).getType()) || isAir(b.getRelative(BlockFace.SOUTH).getType()) || isAir(b.getRelative(BlockFace.EAST).getType()) || isAir(b.getRelative(BlockFace.WEST).getType());

					// If the block is a valid surface block (meaning that the block itself isn't air or liquid and the block above it is air or a plant,
					// or if the blocks to the side are also valid surface blocks, meaning those blocks are air or liquid)
					boolean isValidSurfaceBlock = !isAir(b.getType()) && !b.isLiquid() && !GeneralMethods.isSolid(b.getRelative(BlockFace.UP)) && (isAirAbove || isAirSide || isLiquidAbove || isPlant(b.getRelative(BlockFace.UP)));

					// If the block is a valid side surface block (meaning that the block itself isn't air or liquid and the
					// blocks to the side are air or liquid, also we don't want the block to be waterlogged either)
					boolean isValidSideSurface = (!b.isLiquid() && isLiquidSide) || (!isAir(b.getType()) && isAirSide) && !(b instanceof Waterlogged);

					boolean isValidSurfacePlant = !GeneralMethods.isSolid(b) && (isPlant(b) || b.getType() == Material.DEAD_BUSH) && isAir(b.getRelative(BlockFace.UP).getType());
					return !RegionProtection.isRegionProtected(player, b.getLocation(), this) && (isValidSurfaceBlock || isValidSideSurface || isValidSurfacePlant);
				})
				.collect(Collectors.toList());
	}

	public static void handleBlessings() {
		if (!BLESSINGS.isEmpty()) {
			BLESSINGS.removeIf(b -> !b.handle());
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Abilities.Blessing.Enabled", true);
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
	public boolean isIgniteAbility() {
		return false;
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "Blessing";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void remove() {
		super.remove();
		blessedBlocks.iterator().forEachRemaining(b -> b.removeMetadata("spirits:blessed_source", Spirits.instance));
		blessedEntities.iterator().forEachRemaining(e -> e.removeMetadata("spirits:blessed", Spirits.instance));
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}

	class Bless {
		private Blessing blessing;
		private LivingEntity entity;
		private long duration;

		private int interval;
		private double size;

		public Bless(Blessing blessing, LivingEntity entity, long duration) {
			this.blessing = blessing;
			this.entity = entity;
			this.duration = System.currentTimeMillis() + duration;

			this.size = 3.6;
		}
		public boolean handle() {
			if (System.currentTimeMillis() > duration) {
				return false;
			}
			PotionEffectType.REGENERATION.createEffect(20, 1).apply(entity);
			entity.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, entity.getLocation().clone().add(0, 1, 0), 1, 0.25, 0.25, 0.25, 0);

			Location location = entity.getLocation().clone();

			if (size >= 3) {
				interval = interval > 35 ? 0 : interval + 1;
			}
			if (interval == 0) {
				if (size >= 3) {
					size = 0;
				}
				size += 0.5;
				for (double i = 0; i <= Math.PI; i += Math.PI / 15) {
					double y = size * Math.cos(i);

					for (double j = 0; j <= 2 * Math.PI; j += Math.PI / 30) {
						double x = size * Math.cos(j) * Math.sin(i);
						double z = size * Math.sin(j) * Math.sin(i);

						location.add(x, y, z);

						for (Entity e : GeneralMethods.getEntitiesAroundPoint(entity.getLocation(), 1.25)) {
							if (e.getUniqueId() == entity.getUniqueId() || !(e instanceof LivingEntity)) continue;

							e.setVelocity(location.getDirection().setY(0.5).multiply(0.5));
							DamageHandler.damageEntity(e, damage, blessing);
						}
						if (ThreadLocalRandom.current().nextInt(10) == 0) {
							location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 1, 0, 0, 0, 0);
						}

						location.subtract(x, y, z);
					}
				}
			}
			return true;
		}
	}
}
