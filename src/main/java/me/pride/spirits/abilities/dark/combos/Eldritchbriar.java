package me.pride.spirits.abilities.dark.combos;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import me.pride.spirits.Spirits;
import me.pride.spirits.game.DarkSpiritAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Eldritchbriar extends DarkSpiritAbility implements AddonAbility, ComboAbility {
	public Eldritchbriar(Player player) {
		super(player);
	}
	
	@Override
	public void progress() {
	
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
		return 0;
	}
	
	@Override
	public String getName() {
		return "Eldritchbriar";
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
		return Spirits.getVersion(this.getElement());
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}
	
	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		return null;
	}
}
