package me.pride.spirits;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ChatUtil;
import me.pride.spirits.abilities.dark.Commandeer;
import me.pride.spirits.abilities.dark.Obelisk;
import me.pride.spirits.abilities.light.Blessing;
import me.pride.spirits.abilities.light.Blessing.BlessType;
import me.pride.spirits.abilities.light.Divination;
import me.pride.spirits.abilities.light.Protect;
import me.pride.spirits.abilities.light.Protect.ProtectType;
import me.pride.spirits.abilities.light.Restore;
import me.pride.spirits.abilities.light.passives.Lightborn;
import me.pride.spirits.abilities.light.passives.Orbs;
import me.pride.spirits.abilities.light.passives.other.LightBlood;
import me.pride.spirits.abilities.spirit.Disappear;
import me.pride.spirits.abilities.spirit.Rematerialize;
import me.pride.spirits.abilities.spirit.Summon;
import me.pride.spirits.abilities.spirit.combos.Possess;
import me.pride.spirits.abilities.spirit.passives.Transient;
import me.pride.spirits.abilities.spirit.summoner.util.Pathfollower;
import me.pride.spirits.api.DarkSpirit;
import me.pride.spirits.api.ReplaceableSpirit;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.api.event.EntitySpiritDestroyEvent;
import me.pride.spirits.commands.VersionCommand;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Atrium;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.GhostFactory;
import me.pride.spirits.util.Tools;
import me.pride.spirits.world.SpiritWorld;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class SpiritsListener implements Listener {
	public Listener mainListener() { return new MainListener(); }
	
	@EventHandler
	public void onSwing(final PlayerSwingEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (Possess.isPossessed(player)) {
			event.setCancelled(true);
			return;
		}
		if (CoreAbility.hasAbility(player, Possess.class)) {
			Possess.getPossessedFrom(player).ifPresent(possessed -> possessed.swingMainHand());
		}
		
		if (bPlayer == null) return;
		
		CoreAbility coreAbil = bPlayer.getBoundAbility();

		if (coreAbil == null) {
			if (bPlayer.isElementToggled(SpiritElement.LIGHT_SPIRIT) && !bPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
				if (Orbs.isAbsorbed(player) || Orbs.isAbsorbing(player)) {
					return;
				}
				Orbs.shoot(player);
			}
			return;
		}
		
		if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof LightSpiritAbility && bPlayer.isElementToggled(SpiritElement.LIGHT_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Protect" -> {
						if (CoreAbility.hasAbility(player, Protect.class)) {
							if (Protect.isProtecting(player)) {
								Protect.removeWithoutCooldown(player);

								Protect.getStockpiles(player, (range, damage, knockback, maxSize) -> {
									new Protect(player, GeneralMethods.getLeftSide(player.getLocation().clone().add(0, 1, 0), 0.7), range, damage, knockback, maxSize);
									new Protect(player, GeneralMethods.getRightSide(player.getLocation().clone().add(0, 1, 0), 0.7), range, damage, knockback, maxSize);
								});
								Protect.setStockpile(player, 1.0);
							}
						} else {
							new Protect(player, ProtectType.DEFLECT);
						}
						break;
					}
					case "Blessing" -> { new Blessing(player, BlessType.CLICK);
						break;
					}
					case "Restore" -> {
						if (Orbs.isAbsorbing(player) || Orbs.isAbsorbed(player)) {
							break;
						}
						/* TODO: Gotta set up some kind of OrbEffect (builder?) so developers can send certain effects with orbs */
						Orbs.shoot(player, false);
					}
				}
			} else if (coreAbil instanceof DarkSpiritAbility && bPlayer.isElementToggled(SpiritElement.DARK_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Commandeer" -> {
						if (CoreAbility.hasAbility(player, Commandeer.class)) {
							Commandeer.switchMode(player);
						} else {
							new Commandeer(player);
						}
						break;
					}
					case "Obelisk" -> { new Obelisk(player);
						break;
					}
				}
			} else if (coreAbil instanceof SpiritAbility && bPlayer.isElementToggled(SpiritElement.SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Summon" -> { new Summon(player);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSneak(final PlayerToggleSneakEvent event) {
		if (event.isCancelled()) return;
		if (!event.isSneaking()) return;
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) return;
		
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		
		if (coreAbil == null) return;
		
		if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof LightSpiritAbility && bPlayer.isElementToggled(SpiritElement.LIGHT_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Protect" -> { new Protect(player, ProtectType.PROTECT);
						break;
					}
					case "Blessing" -> { new Blessing(player, BlessType.SNEAK);
						break;
					}
					case "Restore" -> { new Restore(player);
						break;
					}
					case "Divination" -> { new Divination(player);
						break;
					}
				}
			} else if (coreAbil instanceof DarkSpiritAbility && bPlayer.isElementToggled(SpiritElement.DARK_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Commandeer" -> {
						if (CoreAbility.hasAbility(player, Commandeer.class)) {
							Commandeer.take(player);
						}
						break;
					}
					case "Obelisk" -> {
						if (CoreAbility.hasAbility(player, Obelisk.class)) {
							if (Obelisk.foundSource(player)) {
								Obelisk.startSearching(player);
							}
						}
						break;
					}
				}
			} else if (coreAbil instanceof SpiritAbility && bPlayer.isElementToggled(SpiritElement.SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Disappear" -> { new Disappear(player);
						break;
					}
					case "Rematerialize" -> { new Rematerialize(player);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSpiritDamage(final EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (event.getEntity() instanceof LivingEntity) {
			if (((LivingEntity) event.getEntity()).getHealth() <= 0) return;
		}
		/**
		 * If the transformed Spirit doesn't die before it reverts back, then we will apply the ratio of damage
		 * that it took while transformed back to the original entity.
		 */
		if (ReplaceableSpirit.containsKey(entity)) {
			Entity replaced = ReplaceableSpirit.fromEntity(entity).getReplacedDefinitions().getReplaced();
			if (replaced instanceof LivingEntity && entity instanceof LivingEntity) {
				LivingEntity oldEntity = (LivingEntity) replaced, newEntity = (LivingEntity) entity;
				
				double newMaxHealth = newEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
				double oldMaxHealth = oldEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
				
				double ratio = newEntity.getHealth() / newMaxHealth;
				
				oldEntity.setHealth(oldMaxHealth * ratio);
			}
		}
	}
	
	@EventHandler
	public void onSpiritDeath(final EntityDeathEvent event) {
		Entity entity = event.getEntity();

		/**
		 * Upon the death of a Spirit, we will have particles display around the entity.
		 */
		if (Spirit.exists(entity)) {
			Spirit.of(entity).ifPresent(spirit -> {
				if (Filter.filterEntityLight(entity)) {
					entity.getWorld().spawnParticle(Particle.INSTANT_EFFECT, entity.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				} else if (Filter.filterEntityDark(entity)) {
					entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				} else {
					entity.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, entity.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				}
				spirit.removeFromCache();
			});
		}
	}

	@EventHandler
	public void onPlayerDamage(final EntityDamageEvent event) {
		Entity entity = event.getEntity();
		double damage = event.getDamage();

		if (event.getFinalDamage() <= 0 || event.isCancelled()) {
			return;
		}
		if (entity.getType() == EntityType.PLAYER) {
			Player player = (Player) entity;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && !bPlayer.hasElement(SpiritElement.DARK_SPIRIT) && Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Enabled")) {
				/**
				 * We will check if the player is a light spirit and has the Lightborn passive, and if so, we will apply vulnerability.
				 */
				if (Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Vulnerability")) {
					double multiplier = Spirits.instance.getConfig().getDouble("Light.Passives.Lightborn.VulnerabilityMultiplier");
					double offset = damage;

					if (multiplier < 1) {
						offset = damage * multiplier;
					}
					event.setDamage(offset);
				}
				/**
				 * When the player takes a number of hits (damage), they will bleed.
				 */
				if (Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Bleed.Enabled")) {
					Lightborn.addHit(player, 1);

					if (Lightborn.getHit(player) == Spirits.instance.getConfig().getInt("Light.Passives.Lightborn.Bleed.HitsToBleed")) {
						new LightBlood(player);

						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1, 2);
						Lightborn.setHit(player, 0);
					}
				}
			} else if (bPlayer.hasElement(SpiritElement.DARK_SPIRIT) && !bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && Spirits.instance.getConfig().getBoolean("Dark.Passives.Darkness.Enabled")) {
				/**
				 * If the player is a dark spirit and has the Darkness passive, we will apply damage resistance at night.
				 */
				if (Spirits.instance.getConfig().getBoolean("Dark.Passives.Darkness.NightResistance")) {
					// is night check
					if (player.getWorld().getTime() < 23500 || player.getWorld().getTime() > 12500) {
						double resistance = Spirits.instance.getConfig().getDouble("Dark.Passives.Darkness.NightResistanceMultiplier");
						double offset = damage;

						if (resistance > 1) {
							offset = damage * resistance;
						}
						event.setDamage(offset - damage);
					}
				}
			}
			/**
			 * If the player is protecting with Protect, we apply damage reduction by Lightborn lights %.
			 *
			 * We will also stockpile the lights % that the player has used to protect for any Protect
			 * stockpiled deflection later on.
			 */
			if (Protect.isProtecting(player)) {
				double lights = Lightborn.getLights(player);
				double protection = Math.max(Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Protect.MinProtect") / 100.0, lights / 100.0);

				double offset = damage - (protection * damage);

				event.setDamage(offset);

				Lightborn.setLights(player, lights / 2.0);
				Protect.stockpile(player, Protect.getStockpile(player) + (lights / 200.0));

				ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getColor() + "Light Energy: " + (int) (lights / 2.0) + " %", player);

				player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(ThreadLocalRandom.current().nextDouble(-1.5, 1.5), ThreadLocalRandom.current().nextDouble(0.8, 2), ThreadLocalRandom.current().nextDouble(-1.5, 1.5)), 1, 0.25, 0.25, 0.25);
			} else if (bPlayer.hasElement(SpiritElement.SPIRIT)) {
				/**
				 * Transience passive. Negate all damage when the player is hit.
				 */
				if (Spirits.instance.getConfig().getBoolean("Spirit.Passives.Transient.Enabled")) {
					int chance = Spirits.instance.getConfig().getInt("Spirit.Passives.Transient.PhaseMeleeDamageChance");

					if (ThreadLocalRandom.current().nextInt(100) <= chance) {
						if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK || event.getCause() == DamageCause.ENTITY_EXPLOSION) {
							event.setDamage(0);
							Transient.sendTransience(player, 32);
						}
					}
					if (event.getCause() == DamageCause.FALLING_BLOCK || event.getCause() == DamageCause.CRAMMING || event.getCause() == DamageCause.SUFFOCATION) {
						event.setDamage(0);
						Transient.sendTransience(player, 32);
					} else if (event.getCause() == DamageCause.DROWNING) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityBendingDamage(final AbilityDamageEntityEvent event) {
		Entity entity = event.getEntity();

		if (entity.getType() == EntityType.PLAYER) {
			Player player = (Player) entity;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer.hasElement(SpiritElement.SPIRIT)) {
				if (Spirits.instance.getConfig().getBoolean("Spirit.Passives.Transient.Enabled")) {
					/**
					 * Transience passive. Negate all damage when the player is hit by bending abilities.
					 */
					int chance = Spirits.instance.getConfig().getInt("Spirit.Passives.Transient.PhaseBendingDamageChance");

					if (ThreadLocalRandom.current().nextInt(100) <= chance) {
						event.setDamage(0);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMovement(final PlayerMoveEvent event) {
		Player player = event.getPlayer();

		/**
		 * If the player is possessing an entity, we will cancel the event to prevent movement.
		 */
		if (Possess.isPossessingImmovable(player) || Possess.isPossessed(player)) {
			event.setCancelled(true);
		}
		/**
		 * If the player is moving (not moving cursor, but actually moving; x, y and z changes),
		 * we will store the locations of the player's movement in all existing Pathfollowers.
		 */
		Pathfollower.of(player).ifPresent(paths -> paths.iterator().forEachRemaining(path -> {
			double fromX, fromY, fromZ;
			double toX, toY, toZ;

			fromX = event.getFrom().getX();
			fromY = event.getFrom().getY();
			fromZ = event.getFrom().getZ();

			toX = event.getTo().getX();
			toY = event.getTo().getY();
			toZ = event.getTo().getZ();

			if (fromX != toX || fromY != toY || fromZ != toZ) {
				// player.sendMessage("adding locations to pathfollower");
				path.storeMakerLocations();
			}
		}));
	}

	@EventHandler
	public void onSpiritDestroy(final EntitySpiritDestroyEvent event) {
		/**
		 * When a Spirit is destroyed (dead, reverted, or anything that causes the Spirit
		 * to be removed from cache), we will remove the Pathfollower assigned to the Spirit entity.
		 */
		Pathfollower.of(event.getEntity()).ifPresent(path -> path.remove());
	}

	@EventHandler
	public void onPlayerSlotChange(final PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		int slot = event.getNewSlot() + 1;

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		/**
		 * Remove the Commandeer ability if the player switches to another slot or bind.
		 */
		if (bPlayer != null) {
			String ability = bPlayer.getAbilities().get(slot);

			if (CoreAbility.hasAbility(player, Commandeer.class) && !ability.equalsIgnoreCase("Commandeer")) {
				CoreAbility.getAbility(player, Commandeer.class).remove();
			}
		}
	}

	@EventHandler
	public void onEntityHealEvent(final EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		double amount = event.getAmount();

		if (entity.getType() == EntityType.PLAYER) {
			Player player = (Player) entity;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			/**
			 * If the player is a dark spirit and has the Darkness passive, we will invert healing from Regeneration or Instant Healing
			 * to damage the player instead.
			 */
			if (bPlayer.hasElement(SpiritElement.DARK_SPIRIT) && !bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && Spirits.instance.getConfig().getBoolean("Dark.Passives.Darkness.Enabled")) {
				if (event.getRegainReason() == RegainReason.MAGIC_REGEN || event.getRegainReason() == RegainReason.MAGIC) {
					event.setCancelled(true);
					player.damage(amount);
				} else if (event.getRegainReason() == RegainReason.WITHER) {
					event.setCancelled(true);

					double health = player.getHealth() + amount;
					if (health > player.getAttribute(Attribute.MAX_HEALTH).getValue()) {
						health = player.getAttribute(Attribute.MAX_HEALTH).getValue();
					}
					player.setHealth(health);
				}
			}
		}

	}
}

class MainListener implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void onCraft(final PrepareItemCraftEvent event) {
		/**
		 * Crafting recipes for Spirecite items.
		 */
		Recipe recipe = event.getRecipe();
		CraftingInventory table = event.getInventory();
		ItemStack[] matrix = table.getMatrix();
		
		List<ItemStack> positions = new ArrayList<>();
		NamespacedKey key = Spirecite.SPIRECITE_KEY;
		ItemStack result = new ItemStack(Material.AIR, 1);
		
		if (recipe.getResult().equals(Station.STATION) || recipe.getResult().equals(Spirecite.SPIRECITE_MEDALLION)) {
			positions = Arrays.asList(matrix[1], matrix[3], matrix[5], matrix[7]);
			
		} else if (recipe.getResult().equals(Spirecite.SPIRECITE)) {
			for (ItemStack slot : matrix) {
				positions.add(slot);
			}
			key = Spirecite.FRAGMENTS_KEY;
			result = new ItemStack(Material.RAW_GOLD, 1);
			result.getItemMeta().getPersistentDataContainer().set(Spirecite.SPIRECITE_KEY, PersistentDataType.STRING, "spirecite");
		} else if (recipe.getResult().equals(Spirecite.SPIRECITE_BLOCK)) {
			for (ItemStack slot : matrix) {
				positions.add(slot);
			}
			key = Spirecite.SPIRECITE_KEY;
			result = new ItemStack(Material.RAW_GOLD_BLOCK, 1);
			result.getItemMeta().getPersistentDataContainer().set(Spirecite.SPIRECITE_BLOCK_KEY, PersistentDataType.STRING, "spirecite_block");
		} else if (recipe.getResult().equals(Spirecite.SPIRECITE_CROWN)) {
			positions = Arrays.asList(matrix[3], matrix[4], matrix[5]);
			
		} else if (recipe.getResult().equals(Spirecite.SPIRECITE_MEDALLION)) {
			positions = Arrays.asList(matrix[0], matrix[2], matrix[3], matrix[4], matrix[5], matrix[7]);
			
		} else if (recipe.getResult().equals(Spirecite.SPIRECITE_CLUB)) {
			positions = Arrays.asList(matrix[1], matrix[3], matrix[4], matrix[5]);
		}
		for (ItemStack slot : positions) {
			if (!slot.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				table.setResult(result);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		if (block.getType() == Material.GOLD_ORE || block.getType() == Material.DEEPSLATE_GOLD_ORE) {
			if (player.getGameMode() != GameMode.SURVIVAL) return;
			if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

			/**
			 * Drop Spirecite fragments when breaking gold ore every chance.
			 */
			double chance = Spirits.instance.getConfig().getDouble("Spirecite.Chance");
			chance = player.hasPotionEffect(PotionEffectType.LUCK) ? chance * 1.5 : chance; // If player has Luck effect, increase chance by 50%.
			
			if (random.nextInt(100) <= chance) {
				Location center = block.getLocation().clone().add(0.5, 0.5, 0.5);
				
				double x = random.nextBoolean() ? random.nextGaussian() : -random.nextGaussian();
				double z = random.nextBoolean() ? random.nextGaussian() : -random.nextGaussian();
				
				block.getWorld().dropItem(center.clone().add(x / 2.0, 0, z / 2.0).add(new Vector(0, 0.25, 0).multiply(0.1)), Spirecite.SPIRECITE_FRAGMENTS);
			}
		} else if (block.getType() == Material.CRAFTING_TABLE && block.hasMetadata("station:ancient")) {
			/**
			 * When an Ancient Station is broken, we will remove its location from the cache; this will update
			 * in the JSON as well when server stops. This effectively removes and destroys the station from the world.
			 */
			StorageCache.removeLocationsFromCache(block.getWorld(), new int[]{ block.getX(), block.getY(), block.getZ() });
		}
	}
	
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		Block block = event.getBlockPlaced();
		Player player = event.getPlayer();

		/**
		 * Check to see if the block being placed is an Ancient Station, and if so, we will see if it's in an ancient city as well as
		 * being in the deep dark biome. Additionally, if there are at least 3 Spirecite blocks nearby, then we will allow the
		 * player to "construct" (place) the Ancient Station.
		 */
		if (meta.getPersistentDataContainer().has(new NamespacedKey(Spirits.instance, "ancient_station"), PersistentDataType.STRING)) {
			Predicate<Player> condition = p -> {
				if (p.getWorld().getBiome(p.getLocation()) == Biome.DEEP_DARK) {
					StructureSearchResult result = p.getWorld().locateNearestStructure(p.getLocation(), Structure.ANCIENT_CITY, 2, false);
					
					if (result != null) {
						int count = 0;
						for (Block spirecite : GeneralMethods.getBlocksAroundPoint(p.getLocation(), 5)) {
							if (spirecite.hasMetadata("spirecite:block")) {
								count++;
							}
						}
						return count >= 3;
					}
				}
				return false;
			};
			if (condition.test(player)) {
				StorageCache.addLocationsToCache(block.getWorld(), new int[]{ block.getX(), block.getY(), block.getZ() });
				block.setMetadata("station:ancient", new FixedMetadataValue(Spirits.instance, 0));
			} else {
				ActionBar.sendActionBar(ChatColor.of("#e8204c") + "Not enough spiritual energy to construct a station.", player);
				event.setBuild(false);
				event.setCancelled(true);
				return;
			}
		} else if (meta.getPersistentDataContainer().has(Spirecite.SPIRECITE_BLOCK_KEY, PersistentDataType.STRING)) {
			/**
			 * Set block metadata on the Spirecite block when placed.
			 *
			 * TODO: Should make this persistent. Will need to do more storage stuff.
			 */
			block.setMetadata("spirecite:block", new FixedMetadataValue(Spirits.instance, 0));
		}
	}
	
	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		Entity entity = event.getEntity();
		Player killer = event.getEntity().getKiller();

		/**
		 * Upon the death of an Ancient Soulweaver, we will drop random amounts of Spirecite (NOT fragments)
		 * ranging from 2 to 5 pieces.
		 *
		 * We'll also remove the boss bar and the entity from the cache.
		 */
		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			if (entity.getType() == EntityType.WARDEN && entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
				ItemStack spirecite = Spirecite.SPIRECITE.clone();
				spirecite.setAmount(ThreadLocalRandom.current().nextInt(2, 5));
				
				event.getDrops().add(spirecite);
				AncientSoulweaver.of((Warden) entity).ifPresent(soulweaver -> soulweaver.remove());
				
				BendingBossBar.from(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY).ifPresent(bar -> bar.remove());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(final EntityDamageEvent event) {
		Entity entity = event.getEntity();

		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			if (entity.getType() == EntityType.WARDEN && entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
				Warden soulweaver = (Warden) entity;

				/**
				 * When the Ancient Soulweaver is in its Healing Stasis mode, we will heal the entity by the amount of damage
				 * that it has taken. Negates the damage taken AND heals the Ancient Soulweaver.
				 */
				if (soulweaver.hasMetadata("healingstasis")) {
					double health = soulweaver.getHealth() + event.getDamage();
					health = health > soulweaver.getAttribute(Attribute.MAX_HEALTH).getValue() ? soulweaver.getAttribute(Attribute.MAX_HEALTH).getValue() : health;
					soulweaver.setHealth(health);
					
					event.setCancelled(true);
				}
				// TODO: remove this later, testing purposes only
				if (event.getDamage() < 20) {
					event.setDamage(Math.min(event.getDamage(), 5.0));
				}
				// event.setDamage(Math.min(event.getDamage(), 5.0));
				
				BendingBossBar.from(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY).ifPresent(bar -> {
					bar.update(event.getDamage(), false);
				});
			}
		}
		/**
		 * If the player has totem stacks in them (all light spirit players can stack totems), then we will
		 * stop the player from dying as soon as they take a death blow. This will give them 1/4 of their health
		 * back and apply regeneration, fire resistance, and absorption effects, plus resetting their drowning air,
		 * fall distance and exhaustion.
		 */
		if (Spirits.instance.getConfig().getBoolean("Light.CanStackTotems")) {
			if (entity.getType() == EntityType.PLAYER) {
				Player player = (Player) entity;

				if (!event.isCancelled() && player.getHealth() - event.getFinalDamage() <= 0) {
					if (StorageCache.totemStackCache().containsKey(player.getUniqueId())) {
						event.setCancelled(true);

						PotionEffectType.BLINDNESS.createEffect(20, 1).apply(player);
						PotionEffectType.REGENERATION.createEffect(200, 1).apply(player);
						PotionEffectType.FIRE_RESISTANCE.createEffect(200, 0).apply(player);
						PotionEffectType.ABSORPTION.createEffect(100, 1).apply(player);

						player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue() / 4);
						player.setFireTicks(0);
						player.setFallDistance(0);
						player.setExhaustion(0);
						player.setRemainingAir(player.getMaximumAir());
						player.setVelocity(new Vector(0, 0, 0));

						int stack = StorageCache.totemStackCache().get(player.getUniqueId());

						StorageCache.updateTotems(player.getUniqueId(), stack - 1);

						if (stack - 1 == 0) {
							StorageCache.removeUUIDFromTotems(player.getUniqueId());
						}
						player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityRightClick(final PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		
		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			Entity entity = event.getRightClicked();
			Player player = event.getPlayer();
			
			if (entity.getType() == EntityType.WARDEN) {
				Warden warden = (Warden) entity;

				// TODO: testing purposes only
				if (player.isSneaking()) {
					if (entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
						AncientSoulweaver.of((Warden) entity).ifPresent(soulweaver -> soulweaver.remove());
						BendingBossBar.from(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY).ifPresent(bar -> bar.remove());
					}
					return;
				}
				// TODO: Soulless Atrium
				if (!BendingBossBar.exists(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY)) {
					new BendingBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY, AncientSoulweaver.NAME, BarColor.BLUE, 1000, true, 4000, player);
					warden.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 0));
					new AncientSoulweaver(warden);
				}
			}
		}
	}
	
	@EventHandler
	public void onAbilityStart(final AbilityStartEvent event) {
		/**
		 * If the player has been restricted by the Ancient Soulweaver, they will not
		 * be able to use any abilities.
		 */
		if (event.getAbility().getPlayer().hasMetadata("soulweaver:restricted")) {
			event.getAbility().remove();
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		/**
		 * When a player joins, we want the boss bar to be displayed for them if an Ancient Soulweaver exists.
		 */
		BendingBossBar.fromPlayer(event.getPlayer()).ifPresent(bar -> {
			bar.bossBar().addPlayer(event.getPlayer());
		});
		AncientSoulweaver.ANCIENT_SOULWEAVER.ifPresent(soulweaver -> {
			if (soulweaver.world().getPlayers().isEmpty()) {
				for (Entity entity : soulweaver.world().getEntities()) {
					if (entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
						soulweaver.setEntity((Warden) entity);
					}
				}
			}
		});
		if (SpiritWorld.isSpiritWorld(event.getPlayer().getWorld())) {
			SpiritWorld.of(event.getPlayer().getWorld()).getBossBar().addPlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerLeave(final PlayerQuitEvent event) {
		if (GhostFactory.isGhostEnabled()) {
			Spirits.instance.getGhostFactory().unghost(event.getPlayer());
		}
	}

	@EventHandler
	public void onRightClick(final PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();

		if (action != Action.RIGHT_CLICK_AIR) {
			return;
		}
		if (action == Action.RIGHT_CLICK_BLOCK) {
			/*
			if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE && event.getClickedBlock().hasMetadata("station:ancient")) {
				StorageCache.removeLocationsFromCache(event.getClickedBlock().getWorld(), new int[]{ event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ() });
			}
			 */
			Block block = event.getClickedBlock();

			if (block.getType() == Material.CHEST) {
				/**
				 * If the player is in an ancient city, then they will have a chance to find a Soulless Atrium in any chest.
				 */
				if (ThreadLocalRandom.current().nextInt(100) <= Spirits.instance.getConfig().getInt("Spirecite.FindSoullessAtriumChance")) {
					if (player.getWorld().getBiome(player.getLocation()) == Biome.DEEP_DARK) {
						StructureSearchResult result = player.getWorld().locateNearestStructure(player.getLocation(), Structure.ANCIENT_CITY, 2, false);

						if (result != null) {
							Chest chest = (Chest) block.getState();
							chest.update();

							chest.getInventory().setItem(chest.getInventory().getSize() / 2, Atrium.SOULLESS_ATRIUM);
							return;
						}
					}
				}
			}
		}
		if (event.getHand() == EquipmentSlot.HAND || event.getHand() == EquipmentSlot.OFF_HAND) {
			ItemStack item = event.getItem();

			if (item == null) {
				return;
			}
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			/**
			 * If the player is a light spirit and holding a Totem of Undying, we will stack
			 * the totems through shift right-click.
			 */
			if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && Spirits.instance.getConfig().getBoolean("Light.CanStackTotems")) {
				if (item.getType() == Material.TOTEM_OF_UNDYING) {
					int stack = StorageCache.totemStackCache().containsKey(player.getUniqueId()) ? StorageCache.totemStackCache().get(player.getUniqueId()) + 1 : 1;

					if (player.isSneaking()) {
						StorageCache.updateTotems(player.getUniqueId(), stack);

						int amount = item.getAmount();
						ItemStack totem = amount > 1 ? new ItemStack(item.getType(), amount - 1) : new ItemStack(Material.AIR);

						if (event.getHand() == EquipmentSlot.HAND) {
							player.getInventory().setItemInMainHand(totem);
						} else {
							player.getInventory().setItemInOffHand(totem);
						}
						player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 0.75F);
						ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getSubColor() + "* Totem Stack: " + stack + " *", player);
					} else {
						ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getSubColor() + "* Totem Stack: " + (stack - 1) + " *", player);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerChangedWorlds(final PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		/**
		 * When a player changes worlds to a Spirit World, we want to add a new boss bar for the player.
		 */
		World from = event.getFrom(), to = player.getWorld();

		if (SpiritWorld.isSpiritWorld(to)) {
			SpiritWorld.of(to).getBossBar().addPlayer(player);
		} else if (SpiritWorld.isSpiritWorld(from)) {
			SpiritWorld.of(from).getBossBar().removePlayer(player);
		}
	}

	@EventHandler
	public void onCommand(final PlayerCommandPreprocessEvent event) {
		String command = event.getMessage().toLowerCase();
		String[] args = command.split("\\s+");

		String[] bAliases = {"/bending", "/bend", "/b", "/pk", "/projectkorra", "/korra", "/mtla", "/tla"};
		String[] versionAliases = {"v", "version"};

		if (Arrays.asList(bAliases).contains(args[0]) && args.length >= 2) {
			if (Arrays.asList(versionAliases).contains(args[1].toLowerCase()) && event.getPlayer().hasPermission("bending.command.version")) {
				new BukkitRunnable() {
					@Override
					public void run() {
						VersionCommand.info(event.getPlayer());
					}
				}.runTaskLater(Spirits.instance, 2);
			}
		}
	}

	@EventHandler
	public void onPlayerElementChange(final PlayerChangeElementEvent event) {
		if (Spirits.instance.getConfig().getBoolean("CanAddSpiritElementAsBender")) {
			return;
		}
		OfflinePlayer offlinePlayer = event.getTarget();
		Player player = (Player) offlinePlayer;
		Element element = event.getElement();

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.getResult() == Result.ADD) {
			if (SpiritElement.isSpiritElement(element)) {
				for (Element e : bPlayer.getElements()) {
					if (!SpiritElement.isSpiritElement(e)) {
						// event.setCancelled(true);    To add when this event becomes cancellable in the future.

						/**
						 * Hacky way to "deny" the player from adding a Spirit element to their elements.
						 */
						new BukkitRunnable() {
							@Override
							public void run() {
								ChatUtil.sendBrandingMessage(player, "Ignore previous message. You cannot add a Spirit element to your elements.");
								bPlayer.getElements().remove(element);
							}
						}.runTaskLater(Spirits.instance, 2);
						break;
					}
				}
			}
		}
	}
}