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
	private ArrayList<String> lines = new ArrayList<String>();
	private ArrayList<String> fixedLines = new ArrayList<String>();
	private ArrayList<MCDocsCommands> records = new ArrayList<MCDocsCommands>();
	
	//Config Defaults.
	public String headerFormat = "&c%commandname - Page %current of %count &f| &7%command <page>";
	public String onlinePlayersFormat = "%prefix%name%suffix";
	public List<String> commandsList = new ArrayList<String>();
	
	//Constructor.
	public MCDocsListener(MCDocs instance) {
        this.plugin = instance;
    }

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
		config.save();
	}
	
	private void loadConfig(){
		config.load();
		headerFormat = config.getString("header-format", headerFormat);
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		commandsList = config.getStringList("commands-list", commandsList);
	}
		
	//Method to determine the online names.
	private String onlineNames(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineNames = null;
        String nameFinal = null;
        for (Player o : online){
        	nameFinal = onlinePlayersFormat.replace("%name", o.getName());
        	if (MCDocs.Permissions != null){
        		String group = MCDocs.Permissions.getGroup(o.getWorld().getName(), o.getName());
        		nameFinal = nameFinal.replace("%group", group);
        		nameFinal = nameFinal.replace("%prefix", MCDocs.Permissions.getGroupPrefix(o.getWorld().getName(), group));
        		nameFinal = nameFinal.replace("%suffix", MCDocs.Permissions.getGroupSuffix(o.getWorld().getName(), group));
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
        		nameFinal = onlinePlayersFormat.replace("%name", o.getName());
        		nameFinal = nameFinal.replace("%group", group1);
        		nameFinal = nameFinal.replace("%prefix", MCDocs.Permissions.getGroupPrefix(o.getWorld().getName(), group1));
        		nameFinal = nameFinal.replace("%suffix", MCDocs.Permissions.getGroupSuffix(o.getWorld().getName(), group1));
            	nameFinal = nameFinal.replace('&', '§');        		
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
	
	
	//Method to determine how many people are online.
	private String onlineCount(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        int onlineCount = 0;
        for (@SuppressWarnings("unused") Player o : online){
        	onlineCount++;
        }
        return Integer.toString(onlineCount);
	}
	
	private void linesProcess(Player player, String command, int page){
		//Change all ampersands to Minecraft's weird thingo. And now in 4.0, change some variables.
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
        	fixedLine = fixedLine.replace('&', '§');
        	fixedLines.add(fixedLine);
        }
        
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
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		
		//Find our player and message
		String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
        File folder = plugin.getDataFolder();
        String folderName = folder.getParent();
                        
		//Update our Commands
        MCDocsCommands record = null;
        records.clear();
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
        	}
        	catch (Exception e) {
        		System.out.println("MCDocs: Error reading the commandsList. config.yml incorrect.");
        	}
        	
        }
		
        
        for (MCDocsCommands r : records){
        	lines.clear();
        	fixedLines.clear();
        	String command = r.getCommand();
        	int page = 0;
        	
        	String permission = "allow";
        	
        	if (split[0].equalsIgnoreCase(command)){
        		
        		//Permissions check - Hopefully should default to allow if it isn't installed.
        		if (MCDocs.Permissions != null){
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
		                    
		                    //If split[1] does not exist, or has a letter, page = 1
		                    try{
		                        page = Integer.parseInt(split[1]);
		                    }
		                    catch(Exception ex){
		                    	page = 1;
		                    }
		                    
		                  //Finally - Process our lines!
		                  linesProcess(player, command, page);
	                    
	                    }
	        		
	                 catch (Exception ex) {
	                	player.sendMessage("File not found!");
	                	}
        		}
                 event.setCancelled(true);
        	}        	
        }   
        if(split[0].equalsIgnoreCase("/mcdocs")){
    		try{
    			if(split[1].equalsIgnoreCase("-reload")){
        			loadConfig();
        			player.sendMessage("MCDocs has been reloaded.");
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

	public void onPlayerJoin(PlayerJoinEvent event){
    	//MOTD On Login -- We try to find a group motd file, and if that fails, we try and find a normal motd file, and if that fails we give up.
		if (MCDocs.Permissions != null){
			groupMotd(event);
		}
		else{
			standardMotd(event);
		}
	}

	public void groupMotd(PlayerJoinEvent event){
		File folder = plugin.getDataFolder();
        String folderName = folder.getParent();
		Player player = event.getPlayer();
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
          
            linesProcess(player, "/motd", 1);
            }
    	catch (Exception ex) {
    		standardMotd(event);
     	}
	}
	
	public void standardMotd(PlayerJoinEvent event){
		File folder = plugin.getDataFolder();
        String folderName = folder.getParent();
		Player player = event.getPlayer();
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
          
            linesProcess(player, "/motd", 1);
    	}
    	catch (Exception ex) {
        	
     	}
	}
}
