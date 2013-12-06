/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CaveControl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import Reika.CaveControl.Registry.CaveOptions;
import Reika.CaveControl.Registry.ControlOptions;
import Reika.CaveControl.Registry.ControllableBiomes;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Interfaces.ConfigList;
import Reika.DragonAPI.Interfaces.IDRegistry;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CaveConfig extends ControlledConfig {

	public CaveConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] blocks, IDRegistry[] items, IDRegistry[] id, int cfg) {
		super(mod, option, blocks, items, id, cfg);
	}

	private HashMap<ControllableBiomes, HashMap<ControlOptions, Object>> options = new HashMap();
	private Object[] global = new Object[ControlOptions.optionList.length];

	//Initialization of the config
	@Override
	public void initProps(FMLPreInitializationEvent event) {

		super.initProps(event);

		config.load();

		for (int i = 0; i < ControllableBiomes.biomeList.length; i++) {
			ControllableBiomes biome = ControllableBiomes.biomeList[i];
			String biomename = biome.displayName;
			for (int j = 0; j < ControlOptions.optionList.length; j++) {
				ControlOptions o = ControlOptions.optionList[j];
				String optionname = o.displayName;
				if (o.isBoolean)
					this.addDataEntry(biome, o, config.get(biomename, optionname, o.defaultState).getBoolean(o.defaultState));
				else
					this.addDataEntry(biome, o, config.get(biomename, optionname, o.defaultValue).getDouble(o.defaultValue));
			}
		}

		for (int j = 0; j < ControlOptions.optionList.length; j++) {
			ControlOptions o = ControlOptions.optionList[j];
			String optionname = o.displayName;
			if (o.isBoolean)
				global[o.ordinal()] = (config.get("$Global Control", optionname, o.defaultState).getBoolean(o.defaultState));
			else
				global[o.ordinal()] = (config.get("$Global Control", optionname, o.defaultValue).getDouble(o.defaultValue));
		}

		/*******************************/
		//save the data
		config.save();
	}

	private void addDataEntry(ControllableBiomes biome, ControlOptions option, Object value) {
		HashMap<ControlOptions, Object> map = options.get(biome);
		if (map == null) {
			map = new HashMap();
		}
		map.put(option, value);
		options.put(biome, map);
	}

	@Override
	protected void resetConfigFile() {
		String path = this.getConfigPath()+"_Old_Config_Backup.txt";
		File backup = new File(path);
		if (backup.exists())
			backup.delete();
		try {
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Writing Backup File to "+path);
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Use this to restore custom IDs if necessary.");
			backup.createNewFile();
			if (!backup.exists())
				ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Could not create backup file at "+path+"!");
			else {
				PrintWriter p = new PrintWriter(backup);
				p.println("#####----------THESE ARE ALL THE OLD CONFIG SETTINGS YOU WERE USING----------#####");
				p.println("#####---IF THEY DIFFER FROM THE DEFAULTS, YOU MUST RE-EDIT THE CONFIG FILE---#####");
				for (int i = 0; i < CaveOptions.optionList.length; i++) {
					String label = CaveOptions.optionList[i].getLabel();
					if (CaveOptions.optionList[i].isBoolean())
						controls[i] = CaveOptions.optionList[i].setState(config);
					if (CaveOptions.optionList[i].isNumeric())
						controls[i] = CaveOptions.optionList[i].setValue(config);
					if (CaveOptions.optionList[i].isDecimal())
						controls[i] = CaveOptions.optionList[i].setDecimal(config);
					p.println(label+": "+String.valueOf(controls[i]));
				}

				for (int i = 0; i < ControllableBiomes.biomeList.length; i++) {
					ControllableBiomes biome = ControllableBiomes.biomeList[i];
					String biomename = biome.displayName;
					for (int j = 0; j < ControlOptions.optionList.length; j++) {
						ControlOptions o = ControlOptions.optionList[j];
						String optionname = o.displayName;
						if (o.isBoolean)
							this.addDataEntry(biome, o, config.get(biomename, optionname, o.defaultState).getBoolean(o.defaultState));
						else
							this.addDataEntry(biome, o, config.get(biomename, optionname, o.defaultValue).getDouble(o.defaultValue));
					}
				}

				for (int j = 0; j < ControlOptions.optionList.length; j++) {
					ControlOptions o = ControlOptions.optionList[j];
					String optionname = o.displayName;
					if (o.isBoolean)
						global[o.ordinal()] = (config.get("$Global Control", optionname, o.defaultState).getBoolean(o.defaultState));
					else
						global[o.ordinal()] = (config.get("$Global Control", optionname, o.defaultValue).getDouble(o.defaultValue));
				}

				p.close();
			}
		}
		catch (IOException e) {
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Could not create backup file due to IOException!");
			e.printStackTrace();
		}
		configFile.delete();
	}

	public float getFloat(ControllableBiomes biome, ControlOptions type) {
		try {
			return ((Double)options.get(biome).get(type)).floatValue();
		}
		catch (NullPointerException e) {
			throw new RegistrationException(CaveControl.instance, "No data loaded for biome type "+biome.displayName+"!");
		}
		catch (ClassCastException e) {
			throw new RegistrationException(CaveControl.instance, "Invalid data for biome type "+biome.displayName+" and option "+type.displayName+"!");
		}
	}

	public boolean getBoolean(ControllableBiomes biome, ControlOptions type) {
		try {
			return (Boolean)options.get(biome).get(type);
		}
		catch (NullPointerException e) {
			throw new RegistrationException(CaveControl.instance, "No data loaded for biome type "+biome.displayName+"!");
		}
		catch (ClassCastException e) {
			throw new RegistrationException(CaveControl.instance, "Invalid data for biome type "+biome.displayName+" and option "+type.displayName+"!");
		}
	}

	public float getGlobalFloat(ControlOptions type) {
		try {
			return ((Double)global[type.ordinal()]).floatValue();
		}
		catch (ClassCastException e) {
			throw new RegistrationException(CaveControl.instance, "Invalid data for global option "+type.displayName+"!");
		}
	}

	public boolean getGlobalBoolean(ControlOptions type) {
		try {
			return (Boolean)global[type.ordinal()];
		}
		catch (ClassCastException e) {
			throw new RegistrationException(CaveControl.instance, "Invalid data for global option "+type.displayName+"!");
		}
	}

	public boolean shouldGenerateCaves() {
		return !CaveOptions.GLOBAL.getState() || (Double)global[ControlOptions.CAVES.ordinal()] > 0;
	}

	public boolean shouldGenerateRavines() {
		return !CaveOptions.GLOBAL.getState() || (Double)global[ControlOptions.RAVINES.ordinal()] > 0;
	}
}
