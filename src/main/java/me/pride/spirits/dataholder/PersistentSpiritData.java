package me.pride.spirits.dataholder;

import me.pride.spirits.api.Spirit;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

@Deprecated
public class PersistentSpiritData implements PersistentDataType<int[], Spirit> {
	@Override
	public Class<int[]> getPrimitiveType() {
		return int[].class;
	}
	
	@Override
	public Class<Spirit> getComplexType() {
		return Spirit.class;
	}
	
	@Override
	public int[] toPrimitive(Spirit spirit, PersistentDataAdapterContext persistentDataAdapterContext) {
		return new int[0];
	}
	
	@Override
	public Spirit fromPrimitive(int[] ints, PersistentDataAdapterContext persistentDataAdapterContext) {
		return null;
	}
}
