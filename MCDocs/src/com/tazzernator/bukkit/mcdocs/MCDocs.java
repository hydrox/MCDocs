/*
 * MCDocs by Tazzernator 
 * (Andrew Tajsic ~ atajsicDesigns ~ http://atajsic.com)
 * 
 * THIS PLUGIN IS LICENSED UNDER THE WTFPL - (Do What The Fuck You Want To Public License)
 * 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 * 
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *   
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 *   
 * */

package com.tazzernator.bukkit.mcdocs;

//Java Import
import java.util.logging.Logger;

//Bukkit Import
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

//Permissions Import
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

/**
 * MCDocs Plugin for Bukkit
 * 
 * @author Tazzernator
 *(Andrew Tajsic - atajsicDesigns - http://atajsic.com)
 *
 */

public class MCDocs extends JavaPlugin {
	//Listener, Logger, Permissions, Config.
	private final MCDocsListener playerListener = new MCDocsListener(this);
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	Configuration config;
	
	
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " by Tazzernator (Andrew Tajsic) - version " + pdfFile.getVersion() + " is disabled!" );
	}
	
	public void onEnable() {
		
		config = getConfiguration();
		
		//Setup Permissions
		setupPermissions();
		
		this.playerListener.setupConfig(config);
		
		//Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Priority.Normal, this);
		
		//Check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[" + pdfFile.getName() + "] (Tazzernator/Andrew Tajsic) - v" + pdfFile.getVersion() + " loaded.");
	}
		
	//Setup Function for Permissions
	@SuppressWarnings("static-access")
	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if(this.Permissions == null) {
			PluginDescriptionFile pdfFile = this.getDescription();
			if(test != null) {
				this.getServer().getPluginManager().enablePlugin(test);
				Permissions = ((Permissions)test).getHandler();
				log.info("[" + pdfFile.getName() + "] (Tazzernator/Andrew Tajsic) - Hooked into Permissions.");
			}
			else {
				log.info("[" + pdfFile.getName() + "] (Tazzernator/Andrew Tajsic) - Permissions absent! (Don't Worry, it's not essential!)" );
			}
		}
	}
}
	
