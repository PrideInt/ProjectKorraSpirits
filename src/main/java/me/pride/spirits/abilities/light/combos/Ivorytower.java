package me.pride.spirits.abilities.light.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Ivorytower extends LightSpiritAbility implements AddonAbility, ComboAbility {
	private final String path = Tools.path(this, Path.COMBOS);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.RADIUS)
	private double size;
	@Attribute(Attribute.HEIGHT)
	private double height;
	private int towerCount;

	private long delay;
	
	private List<Block> sources;
	private Set<Tower> towers;
	
	public Ivorytower(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(this, player.getLocation())) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.radius = Spirits.instance.getConfig().getDouble(path + "Radius");
		this.size = Spirits.instance.getConfig().getDouble(path + "TowerRadius");
		this.height = Spirits.instance.getConfig().getDouble(path + "TowerMaxHeight");
		this.towerCount = Spirits.instance.getConfig().getInt(path + "TowerCount");
		
		this.sources = GeneralMethods.getBlocksAroundPoint(player.getLocation(), this.radius).stream().filter(b -> b.hasMetadata("spirits:blessed_source")).collect(Collectors.toList());
		this.towers = new HashSet<>();

		this.delay = System.currentTimeMillis() + 1000;
		
		for (int i = 0; i < this.towerCount; i++) {
			this.towers.add(new Tower(this.height, this.size, this.sources.get(ThreadLocalRandom.current().nextInt(this.sources.size())).getLocation().clone().add(0.5, 0.5, 0.5), this));
		}
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		towers.removeIf(tower -> {
			if (!tower.update() || !player.isSneaking()) {
				Set<TempBlock> next = tower.tower().peek();

				if (next != null) {
					next.forEach(tempBlock -> tempBlock.revertBlock());
					tower.tower().pop();
				}
			}
			return System.currentTimeMillis() > delay && tower.tower().size() <= 0;
		});
		if (towers.size() == 0) {
			remove();
			return;
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Combos.Ivorytower.Enabled", true);
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
		return "Ivorytower";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public List<Location> getLocations() {
		return towers.stream().map(Tower::getLocation).collect(Collectors.toList());
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
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new Ivorytower(player);
	}
	
	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		ArrayList<ComboManager.AbilityInformation> info = new ArrayList<>();
		info.add(new ComboManager.AbilityInformation("Protect", ClickType.LEFT_CLICK));
		info.add(new ComboManager.AbilityInformation("Protect", ClickType.SHIFT_DOWN));
		return info;
	}
	
	class Tower {
		private double height;
		private double size;
		private CoreAbility ability;
		
		private Location origin, location, destination;
		private Vector vector;
		private Stack<Set<TempBlock>> tower;
		
		public Tower(double height, double size, Location origin, CoreAbility ability) {
			this.height = height;
			this.size = size;
			this.origin = origin.clone();
			this.ability = ability;
			
			this.location = this.origin.clone();
			this.destination = this.origin.clone().add(0, height, 0);
			this.vector = GeneralMethods.getDirection(this.location, this.destination).normalize();
			this.tower = new Stack<>();
		}
		public boolean update() {
			if (this.location.distanceSquared(this.origin) > this.height * this.height) {
				return false;
			}
			this.location.add(this.vector.multiply(1));

			Set<TempBlock> blocks = new HashSet<>();
			for (Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.size)) {
				blocks.add(new TempBlock(block, Material.QUARTZ_BLOCK.createBlockData()));
			}
			this.tower.push(blocks);

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.size)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					if (Commands.invincible.contains(entity)) continue;
					
					entity.setVelocity(this.vector.clone().multiply(1.5));
					DamageHandler.damageEntity(entity, 2, this.ability);
				}
			}
			return true;
		}
		public Stack<Set<TempBlock>> tower() {
			return this.tower;
		}
		public Location getLocation() {
			return location;
		}
	}
}
