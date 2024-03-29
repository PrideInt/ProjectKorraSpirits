package me.pride.spirits;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import com.projectkorra.projectkorra.util.ActionBar;
import me.pride.spirits.abilities.dark.Commandeer;
import me.pride.spirits.abilities.dark.Obelisk;
import me.pride.spirits.abilities.light.Protect;
import me.pride.spirits.abilities.spirit.Disappear;
import me.pride.spirits.api.ReplaceableSpirit;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.Filter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
					case "Protect" -> { new Protect(player);
						break;
					}
				}
			} else if (coreAbil instanceof SpiritAbility && bPlayer.isElementToggled(SpiritElement.SPIRIT)) {
				switch (bPlayer.getBoundAbilityName()) {
					case "Disappear" -> { new Disappear(player);
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
		if (ReplaceableSpirit.containsKey(entity)) {
			Entity replaced = ReplaceableSpirit.fromEntity(entity).getReplacedCache().cache().getLeft();
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
		
		if (ReplaceableSpirit.containsKey(entity)) {
			ReplaceableSpirit.fromEntity(entity).ifCachePresent(cache -> {
				Entity e = cache.cache().getLeft();

				if (Filter.filterEntityLight(e)) {
					e.getWorld().spawnParticle(Particle.SPELL_INSTANT, e.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				} else if (Filter.filterEntityDark(e)) {
					e.getWorld().spawnParticle(Particle.SPELL_WITCH, e.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				} else {
					e.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, e.getLocation().clone().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25);
				}
				e.remove();
			});
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
			result = new ItemStack(Material.GOLD_INGOT, 1);
			
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
		
		if (meta.getPersistentDataContainer().has(new NamespacedKey(Spirits.instance, "ancient_station"), PersistentDataType.STRING)) {
			Block block = event.getBlockPlaced();
			Player player = event.getPlayer();
			
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
		}
	}
	
	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			Entity entity = event.getEntity();
			Player killer = event.getEntity().getKiller();
			
			if (entity.getType() == EntityType.WARDEN && entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
				ItemStack spirecite = Spirecite.SPIRECITE.clone();
				spirecite.setAmount(ThreadLocalRandom.current().nextInt(2, 5));
				
				event.getDrops().add(spirecite);
				AncientSoulweaver.of((Warden) entity).ifPresent(soulweaver -> soulweaver.remove());
				
				BendingBossBar.from(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY).ifPresent(bar -> bar.remove());
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			Entity entity = event.getEntity();
			
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
	}
	
	@EventHandler
	public void onEntityRightClick(final PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		
		if (Spirits.instance.getConfig().getBoolean("Spirecite.Enabled")) {
			Entity entity = event.getRightClicked();
			Player player = event.getPlayer();
			
			if (entity.getType() == EntityType.WARDEN) {
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
					((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 0));
					new AncientSoulweaver((Warden) entity);
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
}