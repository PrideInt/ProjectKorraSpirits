package me.pride.spirits.util;

import me.pride.spirits.api.SpiritType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Some people that helped me with the project, and for that, I am grateful.
 * In honor of all of you, here are some special AESTHETIC variants.
 *
 * People who have helped me with the project:
 *
 * - Nysseus
 *
 * And of course, for the developers and contributors of ProjectKorra, thank you for making this possible.
 */
public class SpecialThanks {

	public static final String ME = "5e7db6d3-add9-4aab-b1fc-3dda8f5713f4";

	public static final String MIST = "8621211e-283b-46f5-87bc-95a66d68880e";
	public static final String OMNICYPHER = "a197291a-cd78-43bb-aa38-52b7c82bc68c";
	public static final String STRANGE = "d7757be8-86de-4898-ab4f-2b1b2fbc3dfa";
	public static final String ALEX = "a47a4d04-9f51-44ba-9d35-8de6053e9289";
	public static final String SIMPLICITEE = "5031c4e3-8103-49ea-b531-0d6ae71bad69";
	public static final String VAHAGN = "592fb564-701a-4a5e-9d65-13f7ed0acf59";
	public static final String JUSTAHUMAN = "476ca51b-ec04-431b-87da-dd22b20aa8bf";
	public static final String DREIG = "71d42b35-dd94-408e-941d-88d4a61031c7";
	public static final String DOODER = "b6bd2ceb-4922-4707-9173-8a02044e9069";

	public static final String AZTL = "e98a2f7d-d571-4900-a625-483cbe6774fe";
	public static final String SOBKI = "dd578a4f-d35e-4fed-94db-9d5a627ff962";
	public static final String COOLADE = "96f40c81-dd5d-46b6-9afe-365114d4a082";
	public static final String JACKLIN = "833a7132-a9ec-4f0a-ad9c-c3d6b8a1c7eb";
	public static final String FINN = "7bb267eb-cf0b-4fb9-a697-27c2a913ed92";
	public static final String RUNEFIST = "9636d66a-bff8-48e4-993e-68f0e7891c3b";
	public static final String PLASMAROB = "4f7cf9cd-ee04-4480-8ca0-7bca9b1db302";
	public static final String CARBOGEN = "c364ffe2-de9e-4117-9735-6d14bde038f6";
	public static final String LOONY = "623df34e-9cd4-438d-b07c-1905e1fc46b6";
	public static final String SORIN = "1c30007f-f8ef-4b4e-aff0-787aa1bc09a3";
	public static final String KINGBIRDY = "3b5bdfab-8ae1-4794-b160-4f33f31fde99";
	public static final String SAMMY = "7159aaec-c7f2-4fc2-86cc-09e3fa303c40";
	public static final String VIDCOM = "929b14fc-aaf1-4f0f-84c2-f20c55493f53";
	public static final String BIT = "dbd59467-7307-4981-8e3e-7dcf36dd569c";
	public static final String DOMI = "5e30a511-c9eb-4326-be40-ba4dfc5cd7c1";
	public static final String JED = "4eb6315e-9dd1-49f7-b582-c1170e497ab0";
	public static final String KWILSON = "524f1fdb-28dd-456b-a005-7f61d71fe836";

	public static final String[] DEVELOPERS = {
		MIST,
		OMNICYPHER,
		STRANGE,
		ALEX,
		SIMPLICITEE,
		VAHAGN,
		JUSTAHUMAN,
		DREIG,
		DOODER
	};

	public static final String[] CONTRIBUTORS = {
		AZTL,
		SOBKI,
		COOLADE,
		JACKLIN,
		FINN,
		RUNEFIST,
		PLASMAROB,
		CARBOGEN,
		LOONY,
		SORIN,
		KINGBIRDY,
		SAMMY,
		VIDCOM,
		BIT,
		DOMI,
		DOODER,
		JED,
		KWILSON
	};

	public static final String[] THANKS = {
		ME,
	};

	public static Material getOrbType(Player player) {
		UUID uuid = player.getUniqueId();

		if (uuid.toString().equals(ME)) {
			return Material.BLUE_ICE;
		}
		for (String developer : DEVELOPERS) {
			if (uuid.toString().equals(developer)) {
				return Material.AMETHYST_BLOCK;
			}
		}
		for (String contributor : CONTRIBUTORS) {
			if (uuid.toString().equals(contributor)) {
				return Material.PRISMARINE;
			}
		}
		return Material.SEA_LANTERN;
	}

	public static Particle getParticle(Player player) {
		UUID uuid = player.getUniqueId();

		if (uuid.toString().equals(ME)) {
			return Particle.GLOW;
		}
		return SpiritType.SPIRIT.particles();
	}
}
