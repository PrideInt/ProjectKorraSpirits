package me.pride.spirits.abilities.spirit.summoner.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class Pathfollower {
	private final static Set<Pathfollower> PATHS = new HashSet<>();

	private Player maker;
	private Entity follower;

	private double distance;
	private boolean following;

	private Queue<Location> path;

	public Pathfollower(Player maker, Entity follower) {
		this.maker = maker;
		this.follower = follower;

		this.path = new LinkedList<>();
		PATHS.add(this);
	}

	public void storeMakerLocations() {
		path.add(maker.getLocation());
	}

	public void follow() {
		if (path.size() >= 50) {
			following = true;
		}
		if (following) {
			if (path.peek() != null) {
				Location location = path.poll();
				location.setPitch(-10);

				follower.teleport(location);
			} else {
				following = false;
			}
		}
	}

	public static void handle() {
		PATHS.iterator().forEachRemaining(path -> path.follow());
	}

	public static Optional<Set<Pathfollower>> of(Player player) {
		return Optional.of(PATHS).filter(paths -> paths.stream().anyMatch(path -> path.getMaker().getUniqueId() == player.getUniqueId()));
	}
	public static Optional<Pathfollower> of(Entity entity) {
		return PATHS.stream().filter(path -> path.getFollower().getUniqueId() == entity.getUniqueId()).findFirst();
	}

	public void remove() {
		PATHS.remove(this);
	}

	public Player getMaker() {
		return maker;
	}
	public Entity getFollower() {
		return follower;
	}
	public double getDistance() {
		return distance;
	}
	public Queue<Location> getPath() {
		return path;
	}
	public static Set<Pathfollower> getPaths() {
		return PATHS;
	}
}
