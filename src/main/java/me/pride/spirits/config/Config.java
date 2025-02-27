package me.pride.spirits.config;

import me.pride.spirits.Spirits;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
	public static void setup() {
		FileConfiguration config = Spirits.instance.getConfig();

		config.addDefault("CanAddSpiritElementAsBender", false);
		
		config.addDefault("Spirecite.Enabled", true);
		config.addDefault("Spirecite.WardenDrops", true);
		config.addDefault("Spirecite.Chance", 5);
		config.addDefault("Spirecite.FindSoullessAtriumChance", 5);

		config.addDefault("Light.CanStackTotems", true);
		config.addDefault("Spirit.Ghosts", true);
		
		/*
		 *   	- Dark -
		 */
		/* Commandeer */
		config.addDefault("Dark.Abilities.Commandeer.Enabled", true);
		config.addDefault("Dark.Abilities.Commandeer.Cooldown.Item", 3000);
		config.addDefault("Dark.Abilities.Commandeer.Cooldown.Health", 6000);
		config.addDefault("Dark.Abilities.Commandeer.Cooldown.Effects", 3000);
		config.addDefault("Dark.Abilities.Commandeer.SelectRange", 12);
		config.addDefault("Dark.Abilities.Commandeer.HealthSteal", 4);

		/* Obelisk */
		config.addDefault("Dark.Abilities.Obelisk.Enabled", true);
		config.addDefault("Dark.Abilities.Obelisk.Cooldown", 3000);
		config.addDefault("Dark.Abilities.Obelisk.SelectRange", 8);
		config.addDefault("Dark.Abilities.Obelisk.FindTargetRange", 20);
		config.addDefault("Dark.Abilities.Obelisk.ObeliskRange", 20);
		config.addDefault("Dark.Abilities.Obelisk.Speed", 1.2);
		config.addDefault("Dark.Abilities.Obelisk.Damage", 3);
		config.addDefault("Dark.Abilities.Obelisk.Knockback", 2);
		
		/* Corruption */
		config.addDefault("Dark.Combos.Corruption.Enabled", true);
		config.addDefault("Dark.Combos.Corruption.Cooldown", 6000);
		config.addDefault("Dark.Combos.Corruption.CorruptRadius", 7);
		config.addDefault("Dark.Combos.Corruption.CorruptSpeed", 0.8);
		config.addDefault("Dark.Combos.Corruption.MaxCorruptSpeed", 1);
		config.addDefault("Dark.Combos.Corruption.CorruptRate", 3);
		config.addDefault("Dark.Combos.Corruption.Duration", 10000);
		config.addDefault("Dark.Combos.Corruption.SpawnDarkSpirit.Enabled", true);
		config.addDefault("Dark.Combos.Corruption.SpawnDarkSpirit.Speed", 0.05);

		/* Rancor */
		config.addDefault("Dark.Passives.Rancor.Enabled", true);
		config.addDefault("Dark.Passives.Rancor.Radius", 1.25);
		config.addDefault("Dark.Passives.Rancor.LossRange", 3);

		/*
		 *   	- Light -
		 */
		/* Blessing */
		config.addDefault("Light.Abilities.Blessing.Enabled", true);
		config.addDefault("Light.Abilities.Blessing.Cooldown", 8000);
		config.addDefault("Light.Abilities.Blessing.Duration", 10000);
		config.addDefault("Light.Abilities.Blessing.Damage", 2);
		config.addDefault("Light.Abilities.Blessing.Radius", 8);
		config.addDefault("Light.Abilities.Blessing.SelectRange", 20);
		config.addDefault("Light.Abilities.Blessing.BlessRate", 10);
		config.addDefault("Light.Abilities.Blessing.BlessBlockPerRate", 3);
		// config.addDefault("Light.Abilities.Blessing.LetBlessingFinish", false);
		config.addDefault("Light.Abilities.Blessing.BlessRegularSpirits", true);
		config.addDefault("Light.Abilities.Blessing.GrowCrops", true);
		config.addDefault("Light.Abilities.Blessing.GrowGlowBerries", true);
		config.addDefault("Light.Abilities.Blessing.GrowTrees", true);
		config.addDefault("Light.Abilities.Blessing.ShriekParticles", false);

		/* Divination */
		config.addDefault("Light.Abilities.Divination.Enabled", true);
		config.addDefault("Light.Abilities.Divination.Cooldown", 12000);
		config.addDefault("Light.Abilities.Divination.Radius", 20);
		config.addDefault("Light.Abilities.Divination.ChimeraAmplifier", 1);
		config.addDefault("Light.Abilities.Divination.ChimeraDuration", 120);

		/* Protect */
		config.addDefault("Light.Abilities.Protect.Enabled", true);
		config.addDefault("Light.Abilities.Protect.Deflect.Cooldown", 1000);
		config.addDefault("Light.Abilities.Protect.Deflect.Speed", 1);
		config.addDefault("Light.Abilities.Protect.Deflect.Damage", 2);
		config.addDefault("Light.Abilities.Protect.Deflect.Knockback", 1.25);
		config.addDefault("Light.Abilities.Protect.Deflect.MinRange", 4);
		config.addDefault("Light.Abilities.Protect.Deflect.MaxRange", 8);
		config.addDefault("Light.Abilities.Protect.Deflect.MaxSize", 4.0);

		config.addDefault("Light.Abilities.Protect.Deflect.Stockpile.Cooldown", 6000);

		config.addDefault("Light.Abilities.Protect.Protect.Cooldown", 0);
		config.addDefault("Light.Abilities.Protect.Protect.MinProtect", 10);
		config.addDefault("Light.Abilities.Protect.Protect.SlowAmplifier", 2);

		/* Restore */
		config.addDefault("Light.Abilities.Restore.Enabled", true);
		config.addDefault("Light.Abilities.Restore.MaxCooldown", 20000);
		config.addDefault("Light.Abilities.Restore.SelectRange", 6);
		config.addDefault("Light.Abilities.Restore.Restore", 1);
		config.addDefault("Light.Abilities.Restore.RestoreRate", 30);
		config.addDefault("Light.Abilities.Restore.RestoreDurability", 3);
		config.addDefault("Light.Abilities.Restore.HealthFlashAnimation", true);
		config.addDefault("Light.Abilities.Restore.EnhanceItems", true);

		/* Sanctuary */
		config.addDefault("Light.Combos.Sanctuary.Enabled", true);
		config.addDefault("Light.Combos.Sanctuary.Cooldown", 8000);
		config.addDefault("Light.Combos.Sanctuary.SizeIncrement", 0.3);
		config.addDefault("Light.Combos.Sanctuary.MaxSize", 12);
		config.addDefault("Light.Combos.Sanctuary.MaxPulses", 5);
		config.addDefault("Light.Combos.Sanctuary.Height", 4);
		config.addDefault("Light.Combos.Sanctuary.Damage", 3);
		config.addDefault("Light.Combos.Sanctuary.Repel", 0.3);
		config.addDefault("Light.Combos.Sanctuary.Resistance.EffectDuration", 3);
		config.addDefault("Light.Combos.Sanctuary.Resistance.EffectAmplifier", 1);

		/* Sanctuary */
		config.addDefault("Light.Combos.Ivorytower.Enabled", true);
		config.addDefault("Light.Combos.Ivorytower.Cooldown", 8000);
		config.addDefault("Light.Combos.Ivorytower.Radius", 20);
		config.addDefault("Light.Combos.Ivorytower.TowerRadius", 1.5);
		config.addDefault("Light.Combos.Ivorytower.TowerMaxHeight", 15);
		config.addDefault("Light.Combos.Ivorytower.TowerCount", 5);

		/* Lightborn */
		config.addDefault("Light.Passives.Lightborn.Enabled", true);
		config.addDefault("Light.Passives.Lightborn.Shed", true);
		config.addDefault("Light.Passives.Lightborn.ShedChance", 50);
		config.addDefault("Light.Passives.Lightborn.ShedRate", 30);
		config.addDefault("Light.Passives.Lightborn.AmplifyDamage", true);
		config.addDefault("Light.Passives.Lightborn.Amplifier", 1.10);
		config.addDefault("Light.Passives.Lightborn.Vulnerability", true);
		config.addDefault("Light.Passives.Lightborn.VulnerabilityMultiplier", 1.50);
		config.addDefault("Light.Passives.Lightborn.Bleed.Enabled", true);
		config.addDefault("Light.Passives.Lightborn.Bleed.HitsToBleed", 4);
		config.addDefault("Light.Passives.Lightborn.Bleed.Duration", 20000);
		config.addDefault("Light.Passives.Lightborn.Bleed.Damage", 3);
		config.addDefault("Light.Passives.Lightborn.Bleed.Heal", 3);

		/* Orbs */
		config.addDefault("Light.Passives.Orbs.Enabled", true);
		config.addDefault("Light.Passives.Orbs.Cooldown", 1000);
		config.addDefault("Light.Passives.Orbs.Orbs", 3);
		config.addDefault("Light.Passives.Orbs.HitRadius", 1.3);
		config.addDefault("Light.Passives.Orbs.ShootRange", 15);
		config.addDefault("Light.Passives.Orbs.Damage", 1);
		config.addDefault("Light.Passives.Orbs.AltForm", false);
		
		/*
		 *    	- Neutral -
		 */
		/* Disappear */
		config.addDefault("Spirit.Abilities.Disappear.Enabled", true);
		config.addDefault("Spirit.Abilities.Disappear.Cooldown", 4000);
		config.addDefault("Spirit.Abilities.Disappear.SelectRange", 10);
		config.addDefault("Spirit.Abilities.Disappear.Duration", 3000);

		/* Rematerialize */
		config.addDefault("Spirit.Abilities.Rematerialize.Enabled", true);
		config.addDefault("Spirit.Abilities.Rematerialize.Cooldown", 6000);
		config.addDefault("Spirit.Abilities.Rematerialize.MinSearchRadius", 5);
		config.addDefault("Spirit.Abilities.Rematerialize.MaxSearchRadius", 20);
		config.addDefault("Spirit.Abilities.Rematerialize.Delay", 35);

		/* Summon */
		config.addDefault("Spirit.Abilities.Summon.Enabled", true);
		config.addDefault("Spirit.Abilities.Summon.Cooldown", 5000);
		config.addDefault("Spirit.Abilities.Summon.ChargeTime", 3000);

		/* Possess */
		config.addDefault("Spirit.Combos.Possess.Enabled", true);
		config.addDefault("Spirit.Combos.Possess.Cooldown", 5000);
		config.addDefault("Spirit.Combos.Possess.Duration", 6000);
		config.addDefault("Spirit.Combos.Possess.SelectRange", 8);
		config.addDefault("Spirit.Combos.Possess.ChangeSkins", true);
		
		/* Skyrocket */
		config.addDefault("Spirit.Combos.Skyrocket.Enabled", true);
		config.addDefault("Spirit.Combos.Skyrocket.Cooldown", 6000);
		config.addDefault("Spirit.Combos.Skyrocket.Launch", 2.5);
		config.addDefault("Spirit.Combos.Skyrocket.Range", 25);
		config.addDefault("Spirit.Combos.Skyrocket.SlamRadius", 2.5);
		config.addDefault("Spirit.Combos.Skyrocket.RevertTime", 4500);

		/* Transient */
		config.addDefault("Spirit.Passives.Transient.Enabled", true);
		config.addDefault("Spirit.Passives.Transient.PhaseBendingDamageChance", 10);
		config.addDefault("Spirit.Passives.Transient.PhaseMeleeDamageChance", 75);
		
		Spirits.instance.getConfig().options().copyDefaults(true);
		Spirits.instance.saveConfig();
	}
}
