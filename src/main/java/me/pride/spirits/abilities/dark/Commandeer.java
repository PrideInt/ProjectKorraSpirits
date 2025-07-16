package me.pride.spirits.abilities.dark;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Commandeer extends DarkSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);
	
	public enum CommandeerMode {
		ITEM("OBJECT"), HEALTH("ABUNDANCE"), EFFECTS("MAGIC");
		private String name;
		
		CommandeerMode(String name) {
			this.name = name;
		}
		public String modeName() { return this.name; }
	}
	private CommandeerMode mode;
	
	@Attribute(Attribute.COOLDOWN)
	private long itemCooldown, healthCooldown, effectsCooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute("HealthSteal")
	private double healthSteal;
	
	private double bold;
	
	private LivingEntity target;
	
	public Commandeer(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.itemCooldown = Spirits.instance.getConfig().getLong(path + "Cooldown.Item");
		this.healthCooldown = Spirits.instance.getConfig().getLong(path + "Cooldown.Health");
		this.effectsCooldown = Spirits.instance.getConfig().getLong(path + "Cooldown.Effects");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.healthSteal = Spirits.instance.getConfig().getDouble(path + "HealthSteal");
		
		this.mode = CommandeerMode.ITEM;
		
		start();
	}
	
	@Override
	public void progress() {
		if (target != null) {
			switch (mode) {
				case ITEM: stealItem(); break;
				case HEALTH: stealHealth(); break;
				case EFFECTS: stealEffects(); break;
			}
			remove();
			return;
		}
		sendActionBar();
	}
	
	private void sendActionBar() {
		bold += 0.05;
		ChatColor colour = this.getElement().getColor(), subColour = this.getElement().getSubColor();
		String type = mode.modeName().toUpperCase();
		
		if (bold > 2) bold = 0;
		
		if (bold > 1) {
			type = ChatColor.BOLD + type;
		} else {
			type = mode.modeName();
		}
		ActionBar.sendActionBar(subColour + "* " + colour + type + subColour + " *", player);
	}
	
	private double health(boolean add, double health, LivingEntity entity) {
		double change = add ? entity.getHealth() + health : entity.getHealth() - health;
		double value = entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
		
		return value > entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue() ? value : change;
	}
	
	private void stealHealth() {
		Location location = player.getLocation().add(0, 1, 0), dest = target.getLocation().add(0, 1, 0);
		double posHealth = health(true, healthSteal, player), negHealth = health(false, healthSteal, target);
		
		player.setHealth(posHealth); target.setHealth(negHealth);
	}
	
	private void stealEffects() {
		for (PotionEffect active : target.getActivePotionEffects()) {
			for (PotionEffectType type : Tools.getPositiveEffects()) {
				if (active.getType().equals(type)) {
					int duration = active.getDuration(), amp = active.getAmplifier();
					
					new TempPotionEffect(player, new PotionEffect(type, duration, amp));
					target.removePotionEffect(type);
				}
			}
		}
	}
	
	private void stealItem() {
		EntityEquipment item = target.getEquipment();
		ItemStack heldItem;
		
		heldItem = item.getItemInMainHand();
		item.setItemInMainHand(new ItemStack(Material.AIR));
		
		if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
			player.getInventory().addItem(heldItem);
		} else if (player.getInventory().firstEmpty() == -1) {
			player.getWorld().dropItem(player.getEyeLocation(), heldItem);
		} else {
			player.getInventory().setItemInMainHand(heldItem);
		}
		player.getWorld().spawnParticle(Particle.WITCH, GeneralMethods.getRightSide(player.getLocation().clone().add(0, 1.2, 0), 0.55), 6, 0.125, 0.125, 0.125, 0.05);
		player.getWorld().spawnParticle(Particle.WITCH, GeneralMethods.getRightSide(target.getLocation().clone().add(0, 1.2, 0), 0.55), 6, 0.125, 0.125, 0.125, 0.05);
	}
	
	public void take() {
		LivingEntity entity = (LivingEntity) Tools.rayTraceEntity(player, selectRange);

		if (entity != null) {
			this.target = entity;
		}
	}
	public static void take(Player player) { getAbility(player, Commandeer.class).take(); }
	
	public void switchMode() {
		if (bPlayer.isOnCooldown("CommandeerMode")) return;
		switch (mode) {
			case HEALTH:
				mode = CommandeerMode.EFFECTS;
				break;
			case EFFECTS:
				mode = CommandeerMode.ITEM;
				break;
			case ITEM:
				mode = CommandeerMode.HEALTH;
				break;
			default:
				break;
		}
		player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.9F, 1F);
		bPlayer.addCooldown("CommandeerMode", 500);
	}
	public static void switchMode(Player player) { getAbility(player, Commandeer.class).switchMode(); }
	
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
		switch (mode) {
			case ITEM:
				return itemCooldown;
			case HEALTH:
				return healthCooldown;
			case EFFECTS:
				return effectsCooldown;
		}
		return 0;
	}
	
	@Override
	public String getName() {
		return "Commandeer";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void remove() {
		bPlayer.addCooldown(this);
		super.remove();
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Abilities.Commandeer.Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}
}
