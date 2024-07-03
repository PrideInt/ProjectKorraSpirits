package me.pride.spirits.abilities.dark.passives;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class Darkness extends DarkSpiritAbility implements AddonAbility, PassiveAbility {
	public Darkness(Player player) {
		super(player);

		if (CoreAbility.hasAbility(player, Darkness.class)) {
			return;
		}
		start();
	}
	
	@Override
	public void progress() {
		/**
		 * Apply withering deterioration to nearby non-dark entities.
		 */
		Tools.trackEntitySpirit(player.getLocation(), 1.25, e -> Filter.filterGeneralEntity(e, player, this), (entity, light, dark, neutral) -> {
			if (!dark) {
				PotionEffectType.WITHER.createEffect(30, 0).apply(entity);
			}
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
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public String getName() {
		return "Darkness";
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
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}
	
	@Override
	public boolean isInstantiable() {
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}
}
