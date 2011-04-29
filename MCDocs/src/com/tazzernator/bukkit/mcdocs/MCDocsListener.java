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

//java imports
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;


//bukkit iimports
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.util.config.Configuration;

//Listener Class
public class MCDocsListener extends PlayerListener {
	
	//Some Variables for the class.
	private MCDocs plugin;
	Configuration config;
	public static final Logger log = Logger.getLogger("Minecraft");
	private ArrayList<String> fixedLines = new ArrayList<String>();
	private ArrayList<MCDocsCommands> records = new ArrayList<MCDocsCommands>();
	
	//Config Defaults.
	public String headerFormat = "&c%commandname - Page %current of %count &f| &7%command <page>";
	public String onlinePlayersFormat = "%prefix%name ";
	public boolean motdEnabled = true;
	public List<String> commandsList = new ArrayList<String>();
	
	/*
	 * -- Constructor for MCDocsListener --
	 * All we do here is import the instance.
	 */
	public MCDocsListener(MCDocs instance) {
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
		commandsList.add("/motd:motd.txt");
		commandsList.add("/rules:rules.txt");
		commandsList.add("/news:news.txt");
		commandsList.add("/register:register.txt");
		
		config.setProperty("header-format", headerFormat);
		config.setProperty("online-players-format", onlinePlayersFormat);
		config.setProperty("commands-list", commandsList);
		config.setProperty("motd-enabled", true);
		config.save();
	}
	
	private void loadConfig(){
		config.load();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
		headerFormat = config.getString("header-format", headerFormat);
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		commandsList = config.getStringList("commands-list", commandsList);	
		motdEnabled = config.getBoolean("motd-enabled", motdEnabled);
		
		//7 - 7.1 update check - add motd-enabled if it doesn't exist.
		Object val = config.getProperty("motd-enabled");
		if (val == null){
			config.setProperty("motd-enabled", true);
			log.info("[MCDocs] motd-enabled added to config.yml with default true.");
		}

		//Update our Commands
        MCDocsCommands record = null;
        records.clear();
        boolean passed = false;
        for (String c : commandsList){
        	try{
        		String[] parts = c.split(":");
        		if(parts.length == 3){
        			record = new MCDocsCommands(parts[0], folderName + "/MCDocs/" + parts[1], parts[2]);
        		}
        		else if(parts.length == 2){
        			record = new MCDocsCommands(parts[0], folderName + "/MCDocs/" + parts[1], "null");
           		}
        		records.add(record);
        		passed = true;
        	}
        	catch (Exception e) {
        		log.info("[MCDocs]: Error reading the commandsList. config.yml incorrect.");
        	}
        }
        if (val == null && passed == true){
        	config.save();
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
		
		ArrayList<String> lines = new ArrayList<String>();
		
		//Find the current Player, Message, And Folder
		String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
        
		for (MCDocsCommands r : records){
        	lines.clear();
        	fixedLines.clear();
        	String command = r.getCommand();
        	int page = 0;
        	String permission = "allow";
        	
        	if (split[0].equalsIgnoreCase(command)){
        		
        		//Permissions check - Hopefully should default to allow if it isn't installed.
        		if (MCDocs.Permissions != null){
        			try{
	        			String permissionCommand = "mcdocs." + command;
	        			String group = MCDocs.Permissions.getGroup(player.getWorld().getName(), player.getName());
	        			if((r.getGroup().equalsIgnoreCase(group)) || (r.getGroup().equals("null"))){
	        				permission = "allow";
	        			}
	        			else{
	        				permission = "deny";
	        			}
	        			if(!MCDocs.Permissions.has(player, permissionCommand)){
	        				permission = "deny";
	        				
	        			}
        			}
        			catch(Exception ex){
            			log.info("[MCDocs] ERROR! Group not found for " + player.getName());
            		}
        		}
        		if (permission == "allow"){
	        		try {
		        			//Add out lines to the list "lines"
		                    FileInputStream fis = new FileInputStream(r.getFile());
		                    Scanner scanner = new Scanner(fis, "UTF-8");
		    	                while (scanner.hasNextLine()) {
		    	                	try{
		    	                		lines.add(scanner.nextLine() + " ");
		    	                	}
		    	                	catch(Exception ex){
		    	                		lines.add(" ");
		    	                	}
		    	                }
		                    scanner.close();
		                    fis.close();                                       
	                 }	        		
	                 catch (Exception ex) {
	                	 	player.sendMessage("File not found!");
	                 }
	                
	                 //If split[1] does not exist, or has a letter, page = 1
                    try{
                        page = Integer.parseInt(split[1]);
                    }
                    catch(Exception ex){
                    	page = 1;
                    }
                    
                    //Finally - Process our lines!
                    variableSwap(player, lines);
                    linesProcess(player, command, page);
        		}
                event.setCancelled(true);
        	}        	
        }   
        if(split[0].equalsIgnoreCase("/mcdocs")){
    		try{
    			if(split[1].equalsIgnoreCase("-reload")){
        			loadConfig();
        			player.sendMessage("MCDocs has been reloaded.");
        			log.info("[MCDocs] Reloaded by " + player.getName());
        			event.setCancelled(true);
        		}
    		}
    		catch(Exception ex){
    			player.sendMessage("MCDocs");
    			player.sendMessage("/mcdocs -reload  |  Reloads MCDocs.");
        		event.setCancelled(true);
    		}
    	}
	}
	private void variableSwap(Player player, ArrayList<String> lines){
		ArrayList<String> tempLines = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		boolean include = false;
		String fileName = null;
		
		for(String l : lines){
			String fixedLine = l.replace("%name", player.getName());
        	fixedLine = fixedLine.replace("%size", onlineCount());
        	fixedLine = fixedLine.replace("%world", player.getWorld().getName());
        	fixedLine = fixedLine.replace("%ip", player.getAddress().getAddress().getHostAddress());
        	if (MCDocs.Permissions != null){
        		String group = MCDocs.Permissions.getGroup(player.getWorld().getName(), player.getName());
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
	        		fixedLine = fixedLine.replace("%prefix", MCDocs.Permissions.getGroupPrefix(player.getWorld().getName(), group));
	        		fixedLine = fixedLine.replace("%suffix", MCDocs.Permissions.getGroupSuffix(player.getWorld().getName(), group));
        		}
        		catch (Exception e){
        			fixedLine = fixedLine.replace("%prefix", "");
            		fixedLine = fixedLine.replace("%suffix", "");
        		}
        	}
        	fixedLine = fixedLine.replace("%online", onlineNames());
        	fixedLine = fixedLine.replace("%include_*", "");
        	fixedLine = fixedLine.replace('&', '§');
			if (l.contains("%include")){
				String tempString = l.trim();
    			String[] firstSplit = tempString.split(" ");
    			for(String s : firstSplit){
    				if(s.contains("%include")){
    					String[] secondSplit = s.split("_");
    					fileName = secondSplit[1];
    					include = true;
    					fixedLine = fixedLine.replace(s, "");
    				}
    			}
        	}
        	fixedLines.add(fixedLine);
        	if(include == true){
	        	try {
	    			//Add out lines to the list "lines"
	                FileInputStream fis = new FileInputStream(folderName + "/MCDocs/" + fileName);
	                Scanner scanner = new Scanner(fis, "UTF-8");
		                while (scanner.hasNextLine()) {
		                	try{
		                		tempLines.add(scanner.nextLine() + " ");
		                	}
		                	catch(Exception ex){
		                		tempLines.add(" ");
		                	}
		                }
		            variableSwap(player, tempLines);
	                scanner.close();
	                fis.close();                                       
	             }	        		
	             catch (Exception ex) {
	            	 log.info("[MCDocs] Included file " + fileName + " not found!");
	             }
        	}
		}
	}

	private void linesProcess(Player player, String command, int page){        
        //Define our page numbers
        int size = fixedLines.size();
        int pages;
        
        if(size % 9 == 0){
        	pages = size / 9;
        }
        else{
        	pages = size / 9 + 1;
        }
        
        //This here grabs the specified 9 lines, or if it's the last page, the left over amount of lines.
        String commandName = command.replace("/", "");
        commandName = commandName.toUpperCase();
        String header = null;
        
        if(pages != 1){
        	//Custom Header
			header = headerFormat;
            
            //Replace variables.
            header = header.replace('&', '§');
            header = header.replace("%commandname", commandName);
            header = header.replace("%current", Integer.toString(page));
            header = header.replace("%count", Integer.toString(pages));
            header = header.replace("%command", command);
            header = header + " ";
            
            player.sendMessage(header);
        }
        //Some Maths.
        int highNum = (page * 9);
        int lowNum = (page - 1) * 9;
        for (int number = lowNum; ((number < highNum) && (number < size)); number++){
        	player.sendMessage(fixedLines.get(number));
        }
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
        	if (MCDocs.Permissions != null){
        		try{
	        		String group = MCDocs.Permissions.getGroup(o.getWorld().getName(), o.getName());
	        		nameFinal = nameFinal.replace("%group", group);
	        		nameFinal = nameFinal.replace("%prefix", MCDocs.Permissions.getGroupPrefix(o.getWorld().getName(), group));
	        		nameFinal = nameFinal.replace("%suffix", MCDocs.Permissions.getGroupSuffix(o.getWorld().getName(), group));
        		}
        		catch(Exception ex){
        			log.info("[MCDocs] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
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
        	String group1 = MCDocs.Permissions.getGroup(o.getWorld().getName(), o.getName());
        	group1 = group1.toLowerCase();
        	if (group1.equals(group)){
        		try{
	        		nameFinal = onlinePlayersFormat.replace("%name", o.getName());
	        		nameFinal = nameFinal.replace("%group", group1);
	        		nameFinal = nameFinal.replace("%prefix", MCDocs.Permissions.getGroupPrefix(o.getWorld().getName(), group1));
	        		nameFinal = nameFinal.replace("%suffix", MCDocs.Permissions.getGroupSuffix(o.getWorld().getName(), group1));
	            	nameFinal = nameFinal.replace('&', '§');     
        		}
            	catch(Exception ex){
        			log.info("[MCDocs] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
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
	
	
	/*
	 * -- MOTD On Login -- 
	 * We try to find a group motd file, and if that fails, we try and find a normal motd file, and if that fails we give up.
	 */
	public void onPlayerJoin(PlayerJoinEvent event){
		if (motdEnabled == true){
	    	if (MCDocs.Permissions != null){
				groupMotd(event);
			}
			else{
				standardMotd(event);
			}
		}
	}

	public void groupMotd(PlayerJoinEvent event){
		ArrayList<String> lines = new ArrayList<String>();
		Player player = event.getPlayer();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
		String group = MCDocs.Permissions.getGroup(player.getWorld().getName(), player.getName());
		group = group.toLowerCase();
		lines.clear();
    	fixedLines.clear();
    	try {
            FileInputStream fis = new FileInputStream(folderName + "/MCDocs/motd-" + group + ".txt");
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (scanner.hasNextLine()) {
                	try{
                		lines.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex){
                		lines.add(" ");
                	}
                }
            scanner.close();
            fis.close();
            
            variableSwap(player, lines);
            linesProcess(player, "/motd", 1);
            }
    	catch (Exception ex) {
    		standardMotd(event);
     	}
	}
	
	public void standardMotd(PlayerJoinEvent event){
		ArrayList<String> lines = new ArrayList<String>();
		Player player = event.getPlayer();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
		lines.clear();
    	fixedLines.clear();	
    	
    	try{
    		FileInputStream fis = new FileInputStream(folderName + "/MCDocs/motd.txt");
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (scanner.hasNextLine()) {
                	try{
                		lines.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex1){
                		lines.add(" ");
                	}
                }
            scanner.close();
            fis.close();
          
            variableSwap(player, lines);
            linesProcess(player, "/motd", 1);
    	}
    	catch (Exception ex) {
     	}
	}
}
