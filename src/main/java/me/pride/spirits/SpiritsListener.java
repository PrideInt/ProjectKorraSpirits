package me.pride.spirits;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import com.projectkorra.projectkorra.util.ActionBar;
import me.pride.spirits.abilities.dark.Commandeer;
import me.pride.spirits.abilities.dark.Obelisk;
import me.pride.spirits.abilities.light.Blessing;
import me.pride.spirits.abilities.light.Blessing.BlessType;
import me.pride.spirits.abilities.light.Protect;
import me.pride.spirits.abilities.light.Protect.ProtectType;
import me.pride.spirits.abilities.light.passives.Lightborn;
import me.pride.spirits.abilities.light.passives.other.LightBlood;
import me.pride.spirits.abilities.spirit.Disappear;
import me.pride.spirits.abilities.spirit.Rematerialize;
import me.pride.spirits.abilities.spirit.combos.Possess;
import me.pride.spirits.api.ReplaceableSpirit;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Atrium;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.Filter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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

		if (CoreAbility.hasAbility(player, Possess.class)) {
			Possess.getPossessedFrom(player).ifPresent(possessed -> possessed.swingMainHand());
		}
		
		if (bPlayer == null) return;
		
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		
		if (coreAbil == null) return;
		
		if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof DarkSpiritAbility && bPlayer.isElementToggled(SpiritElement.DARK_SPIRIT)) {
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
			} else if (coreAbil instanceof LightSpiritAbility && bPlayer.isElementToggled(SpiritElement.LIGHT_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Protect" -> {
						if (CoreAbility.hasAbility(player, Protect.class)) {
							if (Protect.isProtecting(player)) {
								Protect.removeWithoutCooldown(player);
								double stockpile = Protect.getStockpile(player);

								double minRange = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MinRange");
								double maxRange = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MaxRange");

								double damage = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.Damage") * stockpile;
								double knockback = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.Knockback") * stockpile;
								double range = ThreadLocalRandom.current().nextDouble(minRange * stockpile, maxRange * stockpile) * stockpile;
								double maxSize = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MaxSize") * stockpile;

								new Protect(player, GeneralMethods.getLeftSide(player.getLocation().clone().add(0, 1, 0), 0.7), damage, knockback, range, maxSize);
								new Protect(player, GeneralMethods.getRightSide(player.getLocation().clone().add(0, 1, 0), 0.7), damage, knockback, range, maxSize);
							}
						} else {
							new Protect(player, ProtectType.DEFLECT);
						}
						break;
					}
					case "Blessing" -> { new Blessing(player, BlessType.CLICK);
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
			if (coreAbil instanceof DarkSpiritAbility && bPlayer.isElementToggled(SpiritElement.DARK_SPIRIT)) {
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
			} else if (coreAbil instanceof LightSpiritAbility && bPlayer.isElementToggled(SpiritElement.LIGHT_SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Protect" -> { new Protect(player, ProtectType.PROTECT);
						break;
					}
					case "Blessing" -> { new Blessing(player, BlessType.SNEAK);
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
		/*
		If the transformed Spirit doesn't die before it reverts back, then we will apply the ratio of damage
		that it took while transformed back to the original entity.
		 */
		if (ReplaceableSpirit.containsKey(entity)) {
			Entity replaced = ReplaceableSpirit.fromEntity(entity).getReplacedDefinitions().getReplaced();
			if (replaced instanceof LivingEntity && entity instanceof LivingEntity) {
				LivingEntity oldEntity = (LivingEntity) replaced, newEntity = (LivingEntity) entity;
				
				double newMaxHealth = newEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				double oldMaxHealth = oldEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				
				double ratio = newEntity.getHealth() / newMaxHealth;
				
				oldEntity.setHealth(oldMaxHealth * ratio);
			}
		}
	}
	
	@EventHandler
	public void onSpiritDeath(final EntityDeathEvent event) {
		Entity entity = event.getEntity();
		
		if (Spirit.exists(entity)) {
			Spirit.of(entity).ifPresent(spirit -> {
				if (Filter.filterEntityLight(entity)) {
					entity.getWorld().spawnParticle(Particle.SPELL_INSTANT, entity.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				} else if (Filter.filterEntityDark(entity)) {
					entity.getWorld().spawnParticle(Particle.SPELL_WITCH, entity.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
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

			if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) && Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Enabled")) {
				if (Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Vulnerability")) {
					double multiplier = Spirits.instance.getConfig().getDouble("Light.Passives.Lightborn.VulnerabilityMultiplier");
					double offset = damage;

					if (multiplier < 1) {
						offset = damage * multiplier;
					}
					event.setDamage(offset);
				}
				if (Spirits.instance.getConfig().getBoolean("Light.Passives.Lightborn.Bleed.Enabled")) {
					Lightborn.addHit(player, 1);

					if (Lightborn.getHit(player) == Spirits.instance.getConfig().getInt("Light.Passives.Lightborn.Bleed.HitsToBleed")) {
						new LightBlood(player);

						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1, 2);
						Lightborn.setHit(player, 0);
					}
				}
			}
			if (Protect.isProtecting(player)) {
				double lights = Lightborn.LIGHTS.get(player.getUniqueId());
				double protection = Math.max(Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Protect.MinProtect") / 100.0, lights / 100.0);

				double offset = damage - (protection * damage);

				event.setDamage(offset);

				Lightborn.LIGHTS.put(player.getUniqueId(), lights / 2.0);
				Protect.stockpile(player, Protect.getStockpile(player) + lights / 100.0);
				ActionBar.sendActionBar(SpiritElement.LIGHT_SPIRIT.getColor() + "Light Energy: " + (int) (lights / 2.0) + " %", player);

				player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(ThreadLocalRandom.current().nextDouble(-1.5, 1.5), ThreadLocalRandom.current().nextDouble(0.8, 2), ThreadLocalRandom.current().nextDouble(-1.5, 1.5)), 1, 0.25, 0.25, 0.25);
			} else if (bPlayer.hasElement(SpiritElement.SPIRIT)) {
				if (Spirits.instance.getConfig().getBoolean("Spirit.Passives.Transient.Enabled")) {
					int chance = Spirits.instance.getConfig().getInt("Spirit.Passives.Transient.PhaseMeleeDamageChance");

					if (ThreadLocalRandom.current().nextInt(100) <= chance) {
						if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK || event.getCause() == DamageCause.ENTITY_EXPLOSION) {
							event.setDamage(0);
							player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 0.5F);
							ActionBar.sendActionBar(SpiritElement.SPIRIT.getSubColor() + "* Transience phased the damage away. *", player);
						}
					}
					if (event.getCause() == DamageCause.FALLING_BLOCK || event.getCause() == DamageCause.CRAMMING || event.getCause() == DamageCause.SUFFOCATION) {
						event.setDamage(0);
						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 0.5F);
						ActionBar.sendActionBar(SpiritElement.SPIRIT.getSubColor() + "* Transience phased the damage away. *", player);
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

		if (Possess.isPossessingImmovable(player) || Possess.isPossessed(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerSlotChange(final PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		int slot = event.getNewSlot() + 1;

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer != null) {
			String ability = bPlayer.getAbilities().get(slot);

			if (CoreAbility.hasAbility(player, Commandeer.class) && !ability.equalsIgnoreCase("Commandeer")) {
				CoreAbility.getAbility(player, Commandeer.class).remove();
			}
		}
	}
}

class MainListener implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void onCraft(final PrepareItemCraftEvent event) {
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
			
			double chance = Spirits.instance.getConfig().getDouble("Spirecite.Chance");
			chance = player.hasPotionEffect(PotionEffectType.LUCK) ? chance * 1.5 : chance;
			
			if (random.nextInt(100) <= chance) {
				Location center = block.getLocation().clone().add(0.5, 0.5, 0.5);
				
				double x = random.nextBoolean() ? random.nextGaussian() : -random.nextGaussian();
				double z = random.nextBoolean() ? random.nextGaussian() : -random.nextGaussian();
				
				block.getWorld().dropItem(center.clone().add(x / 2.0, 0, z / 2.0).add(new Vector(0, 0.25, 0).multiply(0.1)), Spirecite.SPIRECITE_FRAGMENTS);
			}
		} else if (block.getType() == Material.CRAFTING_TABLE && block.hasMetadata("station:ancient")) {
			StorageCache.removeLocationsFromCache(block.getWorld(), new int[]{ block.getX(), block.getY(), block.getZ() });
		}
	}
	
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		Block block = event.getBlockPlaced();
		Player player = event.getPlayer();
		
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
			block.setMetadata("spirecite:block", new FixedMetadataValue(Spirits.instance, 0));
		}
	}
	
	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		Entity entity = event.getEntity();
		Player killer = event.getEntity().getKiller();

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
				
				if (soulweaver.hasMetadata("healingstasis")) {
					double health = soulweaver.getHealth() + event.getDamage();
					health = health > soulweaver.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ? soulweaver.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : health;
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

						player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() / 3);
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
		if (event.getAbility().getPlayer().hasMetadata("soulweaver:restricted")) {
			event.getAbility().remove();
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
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
			if (Spirits.instance.getConfig().getBoolean("Light.CanStackTotems")) {
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
}