package me.pride.spirits.config;

import me.pride.spirits.Spirits;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
	public static void setup() {
		FileConfiguration config = Spirits.instance.getConfig();
		
		config.addDefault("Spirecite.Enabled", true);
		config.addDefault("Spirecite.WardenDrops", true);
		config.addDefault("Spirecite.Chance", 0.05);
		
		/*
		 *   	- Dark -
		 */
		/* Commandeer */
		config.addDefault("Dark.Abilities.Commandeer.Enabled", true);
		config.addDefault("Dark.Abilities.Commandeer.Cooldown", 3000);
		config.addDefault("Dark.Abilities.Commandeer.SelectRange", 12);
		config.addDefault("Dark.Abilities.Commandeer.Health", 4);
		
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
		
		/*
		 *   	- Light -
		 */
		/* Protect */
		config.addDefault("Light.Abilities.Protect.Enabled", true);
		config.addDefault("Light.Abilities.Protect.Deflect.Cooldown", 1000);
		config.addDefault("Light.Abilities.Protect.Deflect.Speed", 1);
		config.addDefault("Light.Abilities.Protect.Deflect.Damage", 2);
		config.addDefault("Light.Abilities.Protect.Deflect.MinRange", 4);
		config.addDefault("Light.Abilities.Protect.Deflect.MaxRange", 8);
		config.addDefault("Light.Abilities.Protect.Deflect.MaxSize", 4.0);
		config.addDefault("Light.Abilities.Protect.Protect.Cooldown", 5000);
		config.addDefault("Light.Abilities.Protect.Protect.MinProtect", 10);

		/* Sanctuary */
		config.addDefault("Light.Combos.Sanctuary.Enabled", true);
		config.addDefault("Light.Combos.Sanctuary.Cooldown", 8000);
		config.addDefault("Light.Combos.Sanctuary.SizeIncrement", 0.3);
		config.addDefault("Light.Combos.Sanctuary.MaxSize", 12);
		config.addDefault("Light.Combos.Sanctuary.MaxPulses", 5);
		config.addDefault("Light.Combos.Sanctuary.Damage", 3);
		config.addDefault("Light.Combos.Sanctuary.Repel", 0.3);
		config.addDefault("Light.Combos.Sanctuary.Resistance.EffectDuration", 3);
		config.addDefault("Light.Combos.Sanctuary.Resistance.EffectAmplifier", 1);
		
		/*
		 *    	- Neutral -
		 */
		/* Rematerialize */
		config.addDefault("Spirit.Abilities.Rematerialize.Enabled", true);
		config.addDefault("Spirit.Abilities.Rematerialize.Cooldown", 6000);
		config.addDefault("Spirit.Abilities.Rematerialize.Increment", 0.1);
		config.addDefault("Spirit.Abilities.Rematerialize.MinRadius", 5);
		config.addDefault("Spirit.Abilities.Rematerialize.MaxRadius", 15);
		config.addDefault("Spirit.Abilities.Rematerialize.BlindDuration", 2);
		
		/* Skyrocket */
		config.addDefault("Spirit.Combos.Skyrocket.Enabled", true);
		config.addDefault("Spirit.Combos.Skyrocket.Cooldown", 6000);
		config.addDefault("Spirit.Combos.Skyrocket.Launch", 2.5);
		config.addDefault("Spirit.Combos.Skyrocket.Range", 25);
		config.addDefault("Spirit.Combos.Skyrocket.SlamRadius", 2.5);
		config.addDefault("Spirit.Combos.Skyrocket.RevertTime", 4500);
		
		Spirits.instance.getConfig().options().copyDefaults(true);
		Spirits.instance.saveConfig();
	}
}
