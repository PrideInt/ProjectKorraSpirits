package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.dark.PlotterSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.dark.TerrorSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.light.AttackSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.light.GuardSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.AirSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.EarthSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.FireSpirit;
import me.pride.spirits.abilities.spirit.summoner.spirits.neutral.WaterSpirit;
import me.pride.spirits.abilities.spirit.summoner.util.Pathfollower;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.List;

public class Summon extends SpiritAbility implements AddonAbility, MultiAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	private enum SummonerType {
		AIR, EARTH, FIRE, WATER, LIGHT, LIGHT2, DARK, DARK2
	}
	private SummonerType summonerType;

	private final List<Pair<SummonerType, SummonedSpirit>> SUMMONINGS = List.of(
			Pair.of(SummonerType.AIR, new AirSpirit()),
			Pair.of(SummonerType.EARTH, new EarthSpirit()),
			Pair.of(SummonerType.FIRE, new FireSpirit()),
			Pair.of(SummonerType.WATER, new WaterSpirit()),
			Pair.of(SummonerType.LIGHT, new AttackSpirit()),
			Pair.of(SummonerType.LIGHT2, new GuardSpirit()),
			Pair.of(SummonerType.DARK, new TerrorSpirit()),
			Pair.of(SummonerType.DARK2, new PlotterSpirit())
	);

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;

	private int charge;
	private boolean charged;
	private SpiritType playerSpiritType;

	public Summon(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		} else if (hasAbility(player, Summon.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.chargeTime = Spirits.instance.getConfig().getLong(path + "ChargeTime");

		if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && !bPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
			this.playerSpiritType = SpiritType.LIGHT;
		} else if (bPlayer.hasElement(SpiritElement.DARK_SPIRIT) && !bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT)) {
			this.playerSpiritType = SpiritType.DARK;
		} else {
			this.playerSpiritType = SpiritType.SPIRIT;
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
		if (player.isSneaking()) {
			charge++;

			if (charge >= (chargeTime / 1000) * 20) {
				charged = true;
			}
			if (charged) {
				player.getWorld().spawnParticle(SpiritType.SPIRIT.particles(), player.getEyeLocation(), 3, 0.5, 0.5, 0.5, 0.1);
			}
			switch (player.getInventory().getHeldItemSlot()) {
				case 0:
					summonerType = SummonerType.AIR;
					break;
				case 1:
					summonerType = SummonerType.EARTH;
					break;
				case 2:
					summonerType = SummonerType.FIRE;
					break;
				case 3:
					summonerType = SummonerType.WATER;
					break;
				case 4:
					if (playerSpiritType == SpiritType.LIGHT) {
						summonerType = SummonerType.LIGHT;
					} else if (playerSpiritType == SpiritType.DARK) {
						summonerType = SummonerType.DARK;
					}
					break;
				case 5:
					if (playerSpiritType == SpiritType.LIGHT) {
						summonerType = SummonerType.LIGHT2;
					} else if (playerSpiritType == SpiritType.DARK) {
						summonerType = SummonerType.DARK2;
					}
					break;
				default:
					break;
			}
		} else {
			if (charged) {
				player.getWorld().strikeLightningEffect(player.getLocation());

				SUMMONINGS.stream()
						.filter(pair -> pair.getLeft().equals(summonerType))
						.findFirst()
						.ifPresent(pair -> {
							EntityType type = pair.getRight().defaultEntityType();

							if (playerSpiritType == SpiritType.LIGHT) {
								type = pair.getRight().defaultLightEntityType();
							} else if (playerSpiritType == SpiritType.DARK) {
								type = pair.getRight().defaultDarkEntityType();
							}

							pair.getRight().spawnEntity(player.getWorld(), player.getLocation(), type, pair.getRight().defaultSpiritType(), 10000, e -> {
								new Pathfollower(player, e);

								e.setCustomName(pair.getRight().getSpiritName(playerSpiritType));
								e.setCustomNameVisible(true);

								if (e instanceof Tameable) {
									((Tameable) e).setOwner(player);
								}
								// e.getWorld().spawnParticle(playerSpiritType.particles(), e.getLocation().clone().add(0, 0.8, 0), 3, 0.35, 0.35, 0.35, 0.05);
							});
						});

				MultiAbilityManager.unbindMultiAbility(player);
				bPlayer.addCooldown(this);
				remove();
				return;
			} else if (!charged && charge > 0) {
				charge = 0;
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Spirit.Abilities.Summon.Enabled");
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
			multis.add(new MultiAbilityInfoSub("Bongtoi", SpiritElement.DARK_SPIRIT));
		}
		return multis;
	}
}
