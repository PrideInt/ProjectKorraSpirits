package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.AirSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.EarthSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.FireSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.WaterSpirit;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Summon extends SpiritAbility implements AddonAbility, MultiAbility {
	public Summon(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		} else if (hasAbility(player, Summon.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		// We want to make the Summon multiability different for each player
		MultiAbilityManager.multiAbilityList.removeIf(multiAbilityInfo -> multiAbilityInfo.getName().equals("Summon"));

		MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo("Summon", getMultiAbilities()));
		MultiAbilityManager.bindMultiAbility(player, "Summon");
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
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
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "Summon";
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

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> multis = new ArrayList<>();

		if (bPlayer == null) {
			return multis;
		}
		Element element = SpiritElement.SPIRIT;

		List<Element> elements = bPlayer.getElements();
		if (elements.contains(SpiritElement.LIGHT_SPIRIT) && !elements.contains(SpiritElement.DARK_SPIRIT)) {
			element = SpiritElement.LIGHT_SPIRIT;
		} else if (elements.contains(SpiritElement.DARK_SPIRIT) && !elements.contains(SpiritElement.LIGHT_SPIRIT)) {
			element = SpiritElement.DARK_SPIRIT;
		}
		multis.add(new MultiAbilityInfoSub(AirSpirit.getName(element), SpiritElement.SPIRIT));
		multis.add(new MultiAbilityInfoSub(EarthSpirit.getName(element), SpiritElement.SPIRIT));
		multis.add(new MultiAbilityInfoSub(FireSpirit.getName(element), SpiritElement.SPIRIT));
		multis.add(new MultiAbilityInfoSub(WaterSpirit.getName(element), SpiritElement.SPIRIT));

		if (element.equals(SpiritElement.LIGHT_SPIRIT)) {
			multis.add(new MultiAbilityInfoSub("Lightbringer", SpiritElement.LIGHT_SPIRIT));
			multis.add(new MultiAbilityInfoSub("Taiguang", SpiritElement.LIGHT_SPIRIT));
		} else if (element.equals(SpiritElement.DARK_SPIRIT)) {
			multis.add(new MultiAbilityInfoSub("Darkness", SpiritElement.DARK_SPIRIT));
			multis.add(new MultiAbilityInfoSub("Bongtoi", SpiritElement.LIGHT_SPIRIT));
		}
		return multis;
	}
}
