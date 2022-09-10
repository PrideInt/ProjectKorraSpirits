package me.pride.spirits.abilities.light.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
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
	
	private List<Block> sources;
	private Set<Tower> towers;
	
	public Ivorytower(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		radius = Spirits.instance.getConfig().getDouble(path + "Radius");
		size = Spirits.instance.getConfig().getDouble(path + "TowerSize");
		height = Spirits.instance.getConfig().getDouble(path + "TowerMaxHeight");
		
		sources = GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius).stream().filter(b -> b.hasMetadata("spirits:blessed_source")).collect(Collectors.toList());
		this.towers = new HashSet<>();
		
		for (int i = 0; i < sources.size(); i++) {
			this.towers.add(new Tower(height, radius, this.sources.get(i).getLocation().clone().add(0.5, 0.5, 0.5), this));
		}
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		towers.removeIf(tower -> {
			if (!tower.update() || !player.isSneaking()) {
				TempBlock next = tower.tower().peek();
				
				if (next != null) next.revertBlock();
			}
			return tower.tower().size() <= 0;
		});
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
		return "Ivorytower";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Combos.Ivorytower.Enabled", true);
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
	public Object createNewComboInstance(Player player) {
		return new Ivorytower(player);
	}
	
	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		return null;
	}
	
	class Tower {
		private double height;
		private double size;
		private CoreAbility ability;
		
		private Location origin, location, destination;
		private Vector vector;
		private Queue<TempBlock> tower;
		
		public Tower(double height, double size, Location origin, CoreAbility ability) {
			this.height = height;
			this.size = size;
			this.origin = origin.clone();
			this.ability = ability;
			
			this.location = this.origin.clone();
			this.destination = this.origin.clone().add(0, height, 0);
			this.vector = new Vector(0, height, 0);
			this.tower = new LinkedList<>();
		}
		public boolean update() {
			if (this.location.distanceSquared(this.destination) > this.height * this.height) {
				return false;
			}
			this.location.add(this.vector.multiply(1));
			
			for (Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.size)) {
				this.tower.add(new TempBlock(block, Material.QUARTZ_BLOCK.createBlockData()));
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.size)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					if (Commands.invincible.contains(entity)) continue;
					
					entity.setVelocity(this.vector.clone().multiply(1.5));
					DamageHandler.damageEntity(entity, 2, this.ability);
				}
			}
			return true;
		}
		public Queue<TempBlock> tower() { return this.tower; }
	}
}
