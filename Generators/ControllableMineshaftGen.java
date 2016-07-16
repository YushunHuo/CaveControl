/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CaveControl.Generators;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import Reika.CaveControl.CaveControl;
import Reika.CaveControl.Registry.CaveOptions;
import Reika.CaveControl.Registry.ControlOptions;
import Reika.DragonAPI.Auxiliary.BiomeTypeList;

public class ControllableMineshaftGen extends MapGenMineshaft {

	private static final Random rand = new Random();
	private static final boolean global = CaveOptions.GLOBAL.getState();

	private static final double BASE_FACTOR = 0.004;

	public ControllableMineshaftGen() {
		super(getMap());
	}

	private static Map getMap() {
		Map map = new HashMap();
		map.put("chance", "1");
		return map;
	}

	@Override
	protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
	{
		return rand.nextDouble() < this.getConfig(worldObj, chunkX, chunkZ, ControlOptions.MINESHAFTS)*BASE_FACTOR ? super.canSpawnStructureAtCoords(chunkX, chunkZ) : false;
	}

	private float getConfig(World world, int chunkX, int chunkZ, ControlOptions c) {
		if (global) {
			return CaveControl.config.getGlobalFloat(c);
		}
		int x = chunkX*16;
		int z = chunkZ*16;
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		return c.getValue(BiomeTypeList.getEntry(biome));
	}
}
