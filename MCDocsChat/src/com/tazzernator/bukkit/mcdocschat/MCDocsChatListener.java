/*
 * MCDocsChat by Tazzernator 
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

package com.tazzernator.bukkit.mcdocschat;

//java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


//bukkit iimports
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.util.config.Configuration;

//Listener Class
public class MCDocsChatListener extends PlayerListener {
	
	//Some Variables for the class.
	private MCDocsChat plugin;
	Configuration config;
	public static final Logger log = Logger.getLogger("Minecraft");
	String line;
	String fixedLine;
	private ArrayList<MCDocsChatCommands> records = new ArrayList<MCDocsChatCommands>();
	
	//Config Defaults.
	public String onlinePlayersFormat = "%prefix%name ";
	public List<String> commandsList = new ArrayList<String>();
	
	/*
	 * -- Constructor for MCDocsListener --
	 * All we do here is import the instance.
	 */
	public MCDocsChatListener(MCDocsChat instance) {
        this.plugin = instance;
    }

	/*
	 * -- Configuration Methods --
	 * We check for config.yml, if it doesn't exists we create a default (defaultCofig), then we load (loadConfig). 
	 */
	public void setupConfig(Configuration config){
		
		this.config = config;
		
		if (!(new File(plugin.getDataFolder(), "config.yml")).exists()){
			defaultConfig();
		}
        loadConfig();
	}
	
	private void defaultConfig(){
		//default commands and files
		commandsList.add("/example:&eExample Use. My name is %prefix%name &eand I'm on world %world");
		config.setProperty("commands-list", commandsList);
		config.save();
	}
	
	private void loadConfig(){
		config.load();
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		commandsList = config.getStringList("commands-list", commandsList);	
		

		//Update our Commands
        MCDocsChatCommands record = null;
        records.clear();
        for (String c : commandsList){
        	try{
        		String[] parts = c.split(":");
        		record = new MCDocsChatCommands(parts[0], parts[1]);
        		records.add(record);
        	}
        	catch (Exception e) {
        		log.info("[MCDocsChat] Error reading the commandsList. config.yml incorrect.");
        	}
        }
	}
		
	/*
	 * -- Main Methods --
	 * onPlayerCommandPreprocess:
	 * 
	 * Is checked whenever a user uses /$
	 * check to see if the user's command matches
	 * Performs a Permissions Node check - if failed, do nothing.
	 * if pass: read command's file to list lines, then forward the player, command, and page number to linesProcess.
	 * 
	 * linesProcess:				
	 * 
	 * For each line in a txt file, various %variables are replaced with their corresponding match.
	 * How many lines in a document are determined, and thus how many pages to split it into.
	 * The header is loaded from header-format and the variables are replaced
	 * Finally the lines are sent to the player.
	 */
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		
		//Find the current Player, Message, And Folder
		String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
        
        
		for (MCDocsChatCommands r : records){
        	line = null;
        	fixedLine = null;
        	String command = r.getCommand();
        	String permission = "allow";
        	
        	if (split[0].equalsIgnoreCase(command)){
        		
        		//Permissions check - Hopefully should default to allow if it isn't installed.
        		if (MCDocsChat.Permissions != null){
        			try{
	        			String permissionCommand = "mcdocschat." + command;
	        			if(!MCDocsChat.Permissions.has(player, permissionCommand)){
	        				permission = "deny";
	        			}
        			}
        			catch(Exception ex){
            			log.info("[MCDocsChat] ERROR! Group not found for " + player.getName());
            		}
        		}
        		if (permission == "allow"){
		    	           line = r.getString() + " ";
		    	           //Finally - Process our lines!
		                   linesProcess(player, command);
        		}
                    
                    
        		}
                event.setCancelled(true);
        	}        	
		
        if(split[0].equalsIgnoreCase("/mcdc")){
    		try{
    			if(split[1].equalsIgnoreCase("-reload")){
        			loadConfig();
        			player.sendMessage("MCDocsChat has been reloaded.");
        			log.info("[MCDocsChat] Reloaded by " + player.getName());
        			event.setCancelled(true);
        		}
    			
    			if(split[1].equalsIgnoreCase("-add")){
    				String permission = "allow";
    				if (MCDocsChat.Permissions != null){
            			try{
    	        			String permissionCommand = "mcdocschat.modify";
    	        			if(!MCDocsChat.Permissions.has(player, permissionCommand)){
    	        				permission = "deny";
    	        			}
            			}
            			catch(Exception ex){
                			log.info("[MCDocsChat] ERROR! Group not found for " + player.getName());
                		}
            		}
    				if (permission == "allow"){
	        			String full = event.getMessage();
	        			String modified = full.replaceFirst("/mcdc ", "");
	        			modified = modified.replaceFirst("-add ", "");
	        			if((modified.contains("/")) && (modified.contains(":"))){
	        				commandsList.add(modified);
	            			config.setProperty("commands-list", commandsList);
	            			config.save();
	            			loadConfig();
	            			log.info("[MCDocsChat] Command added by " + player.getName());
	            			player.sendMessage("Command added successfully.");
	        			}
	        			else{
	        				player.sendMessage("Incorrect structure. Command has not been added.");
	        			}
    				}
    				else{
    					player.sendMessage("Permission Denied.");
    					log.info("[MCDocsChat] " + player.getName() + " was denied acces to /mcdc -add");
    				}
        			
        			event.setCancelled(true);
        		}
    			if(split[1].equalsIgnoreCase("-del")){
    				String permission = "allow";
    				if (MCDocsChat.Permissions != null){
            			try{
    	        			String permissionCommand = "mcdocschat.modify";
    	        			if(!MCDocsChat.Permissions.has(player, permissionCommand)){
    	        				permission = "deny";
    	        			}
            			}
            			catch(Exception ex){
                			log.info("[MCDocsChat] ERROR! Group not found for " + player.getName());
                		}
            		}
    				if (permission == "allow"){
    					int num = 0;
    					for (MCDocsChatCommands r : records){
    						if(r.getCommand().equalsIgnoreCase(split[2])){
    							commandsList.remove(num);
    						}
    						num++;
    					}
    					config.setProperty("commands-list", commandsList);
            			config.save();
            			loadConfig();
            			log.info("[MCDocsChat] Command deleted by " + player.getName());
            			player.sendMessage("Command deleted successfully.");
    				}
    				else{
    					player.sendMessage("Permission Denied.");
    					log.info("[MCDocsChat] " + player.getName() + " was denied acces to /mcdc -del");
    				}
        			
        			event.setCancelled(true);
        		}
    		}
    		catch(Exception ex){
    			player.sendMessage("MCDocsChat");
    			player.sendMessage("/mcdc -reload");
    			player.sendMessage("/mcdc -add </command:message>");
        		event.setCancelled(true);
    		}
    	}
	}

	private void linesProcess(Player player, String command){
		//Change all ampersands to Minecraft's weird thingo. And now in 4.0, change some variables.
		
        	fixedLine = line.replace("%name", player.getName());
        	fixedLine = fixedLine.replace("%size", onlineCount());
        	fixedLine = fixedLine.replace("%world", player.getWorld().getName());
        	fixedLine = fixedLine.replace("%ip", player.getAddress().getAddress().getHostAddress());
        	if (MCDocsChat.Permissions != null){
        		String group = MCDocsChat.Permissions.getGroup(player.getWorld().getName(), player.getName());
        		if(fixedLine.contains("%online_")){
        			String tempString = fixedLine.trim();
        			String[] firstSplit = tempString.split(" ");
        			for(String s : firstSplit){
        				if(s.contains("%online_")){
        					String[] secondSplit = s.split("_");
        					String groupName = secondSplit[1].toLowerCase();
        					fixedLine = fixedLine.replace("%online_" + secondSplit[1], onlineGroup(groupName));
        				}
        			}
        		}
        		fixedLine = fixedLine.replace("%group", group);
        		try{
	        		fixedLine = fixedLine.replace("%prefix", MCDocsChat.Permissions.getGroupPrefix(player.getWorld().getName(), group));
	        		fixedLine = fixedLine.replace("%suffix", MCDocsChat.Permissions.getGroupSuffix(player.getWorld().getName(), group));
        		}
        		catch (Exception e){
        			fixedLine = fixedLine.replace("%prefix", "");
            		fixedLine = fixedLine.replace("%suffix", "");
        		}
        	}
        	fixedLine = fixedLine.replace("%online", onlineNames());
        	fixedLine = fixedLine.replace('&', '§');
        
        	player.chat(fixedLine);
	}
	
	/*
	 * -- Variable Methods --
	 * The following methods are used for various %variables in the txt files.
	 * 
	 * onlineNames: FInds the current online players, and using online-players-format, applies some permissions variables.
	 * onlineGroup: Finds the current online players, check if they're in the group specified, and using online-players-format, applies some permissions variables.
	 * onlineCount: Returns the current amount of users online.
	 */
	
	private String onlineNames(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineNames = null;
        String nameFinal = null;
        for (Player o : online){
        	nameFinal = onlinePlayersFormat.replace("%name", o.getName());
        	if (MCDocsChat.Permissions != null){
        		try{
	        		String group = MCDocsChat.Permissions.getGroup(o.getWorld().getName(), o.getName());
	        		nameFinal = nameFinal.replace("%group", group);
	        		nameFinal = nameFinal.replace("%prefix", MCDocsChat.Permissions.getGroupPrefix(o.getWorld().getName(), group));
	        		nameFinal = nameFinal.replace("%suffix", MCDocsChat.Permissions.getGroupSuffix(o.getWorld().getName(), group));
        		}
        		catch(Exception ex){
        			log.info("[MCDocsChat] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
        		}
        	}
        	nameFinal = nameFinal.replace('&', '§');
        	if (onlineNames == null){
        		onlineNames = nameFinal;
        	}
        	else{
        		onlineNames = onlineNames + nameFinal;
        	}
        }
        return onlineNames;
	}

	private String onlineGroup(String group){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineGroup = null;
        String nameFinal = null;
        for (Player o : online){
        	String group1 = MCDocsChat.Permissions.getGroup(o.getWorld().getName(), o.getName());
        	group1 = group1.toLowerCase();
        	if (group1.equals(group)){
        		try{
	        		nameFinal = onlinePlayersFormat.replace("%name", o.getName());
	        		nameFinal = nameFinal.replace("%group", group1);
	        		nameFinal = nameFinal.replace("%prefix", MCDocsChat.Permissions.getGroupPrefix(o.getWorld().getName(), group1));
	        		nameFinal = nameFinal.replace("%suffix", MCDocsChat.Permissions.getGroupSuffix(o.getWorld().getName(), group1));
	            	nameFinal = nameFinal.replace('&', '§');     
        		}
            	catch(Exception ex){
        			log.info("[MCDocsChat] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
        		}
        	}
        	if (onlineGroup == null){
        		onlineGroup = nameFinal;
        	}
        	else{
        		onlineGroup = onlineGroup + "&f, " + nameFinal;
        	}
        }
        if (onlineGroup == null){
        	onlineGroup = " ";
        }
        return onlineGroup;
	}
	
	private String onlineCount(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        int onlineCount = 0;
        for (@SuppressWarnings("unused") Player o : online){
        	onlineCount++;
        }
        return Integer.toString(onlineCount);
	}
	
}
	
