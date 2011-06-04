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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

//bukkit iimports
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;

//iConomy import.
import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

//Listener Class
public class MCDocsListener extends PlayerListener {
	
	//Some Variables for the class.
	private MCDocs plugin;
	Configuration config;
	static iConomy iConomy = null;
	public static final Logger log = Logger.getLogger("Minecraft");
	private ArrayList<String> fixedLines = new ArrayList<String>();
	private ArrayList<MCDocsCommands> records = new ArrayList<MCDocsCommands>();
	
	//Config Defaults.
	public String headerFormat = "&c%commandname - Page %current of %count &f| &7%command <page>";
	public String onlinePlayersFormat = "%prefix%name";
	public String newsFile = "news.txt";
	public int newsLines = 1;
	public boolean motdEnabled = true;
	public boolean commandLogEnabled = true;
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
		config.setProperty("command-log-enabled", true);
		config.setProperty("news-file", newsFile);
		config.setProperty("news-lines", 1);
		saveConfig();
	}
	
	private void loadConfig(){
		config.load();
		
		//7 - 7.1 update check - add motd-enabled if it doesn't exist.
		Object val = config.getProperty("motd-enabled");
		if (val == null){
			config.setProperty("motd-enabled", true);
			log.info("[MCDocs] motd-enabled added to config.yml with default true.");
		}
		
		//8 - 9 update check - add news-file if it doesn't exist.
		Object val2 = config.getProperty("news-file");
		if (val2 == null){
			config.setProperty("news-file", newsFile);
			config.setProperty("news-lines", 1);
			log.info("[MCDocs] news-file added to config.yml with default news.txt.");
			log.info("[MCDocs] news-lines added to config.yml with default 1.");
		}
		
		headerFormat = config.getString("header-format", headerFormat);
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		commandsList = config.getStringList("commands-list", commandsList);	
		motdEnabled = config.getBoolean("motd-enabled", motdEnabled);
		commandLogEnabled = config.getBoolean("command-log-enabled", commandLogEnabled);
		newsFile = config.getString("news-file", newsFile);
		newsLines = config.getInt("news-lines", newsLines);

		//Update our Commands
        MCDocsCommands record = null;
        records.clear();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
        
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
        		log.info("[MCDocs]: Error reading the commandsList. config.yml incorrect.");
        	}
        }
        //Finally re-write what we've loaded just incase of an update.
        saveConfig();
	}
	
	private void saveConfig(){
	    //Since version 9.3, we're going to manually save the config, as to support commenting.
		PrintWriter stream = null;
		File folder = plugin.getDataFolder();
		if (folder != null) {
			folder.mkdirs();
        }
		String folderName = folder.getParent();
		PluginDescriptionFile pdfFile = this.plugin.getDescription();
		
		try {
			stream = new PrintWriter(folderName + "/MCDocs/config.yml");
			//Let's write our goods ;)
				stream.println("#MCDocs " + pdfFile.getVersion() + " by Tazzernator / Andrew Tajsic version ");
				stream.println("#Configuration File.");
				stream.println("#For detailed assistance please visit: http://atajsic.com/wiki/index.php/MCDocs");
				stream.println();
				stream.println("#Here we determine which command will show which file. ");
				stream.println("commands-list:");
				for (String c : commandsList){
					stream.println("- " + c);
				}
				stream.println();
				stream.println("#This changes the pagination header that is added to MCDocs automatically when there is > 10 lines of text.");
				stream.println("header-format: '" + config.getString("header-format", headerFormat) + "'");
				stream.println();
				stream.println("#Format to use when using %online or %online_group.");
				stream.println("online-players-format: '" + config.getString("online-players-format", onlinePlayersFormat) + "'");
				stream.println();
				stream.println("#The file to displayed when using %news.");
				stream.println("news-file: " + config.getString("news-file", newsFile));
				stream.println();
				stream.println("#How many lines to show when using %news.");
				stream.println("news-lines: " + config.getInt("news-lines", newsLines));
				stream.println();
				stream.println("#Show a MOTD at login? Yes: true | No: false");
				stream.println("motd-enabled: " + config.getBoolean("motd-enabled", motdEnabled));
				stream.println();
				stream.println("#Inform the console when a player uses a command from the commands-list.");
				stream.println("command-log-enabled: " + config.getBoolean("command-log-enabled", commandLogEnabled));
				stream.close();
		} catch (FileNotFoundException e) {
			log.info("[MCDocs]: Error saving the config.yml.");
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
	 * 
	 * variableSwap
	 * 
	 * For each line in a txt file, various %variables are replaced with their corresponding match.
	 *  
	 *  
	 * linesProcess:				
	 *
	 * How many lines in a document are determined, and thus how many pages to split it into.
	 * The header is loaded from header-format and the variables are replaced
	 * Finally the lines are sent to the player.
	 */
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		
		//List of lines we read our first file into.
		ArrayList<String> lines = new ArrayList<String>();
		
		//Find the current Player, Message
		String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
        
        //Here we are going to support spaces in commands.
		int count = split.length;
		int lastInput = count - 1;
		String playerCommand = "";
		if(checkIfNumber(split[lastInput])){
			for (int i=0; i<lastInput; i++){
				playerCommand = playerCommand + split[i] + " ";
			}
		}
		else {
			for (int i=0; i<count; i++){
				playerCommand = playerCommand + split[i] + " ";
			}
		}
		
		//Cut off the final space.
		playerCommand = playerCommand.trim();
		
        
		for (MCDocsCommands r : records){
        	lines.clear();
        	fixedLines.clear();
        	String command = r.getCommand();
        	int page = 0;
        	String permission = "allow";
        	
        	if (playerCommand.equalsIgnoreCase(command)){
        		//Log our user using the command.
        		if (commandLogEnabled == true){
        			log.info("MCDocs: " + player.getName() + ": " + event.getMessage());
        		}
        		
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
	                
	                 //If split[lastInput] does not exist, or has a letter, page = 1
                    try{
                        page = Integer.parseInt(split[lastInput]);
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
			
		//Swaping out some variables with their respective replacement.
		for(String l : lines){
			
			//Basics
			String fixedLine = l.replace("%name", player.getDisplayName());
        	fixedLine = fixedLine.replace("%size", onlineCount());
        	fixedLine = fixedLine.replace("%world", player.getWorld().getName());
        	fixedLine = fixedLine.replace("%ip", player.getAddress().getAddress().getHostAddress());
        	
        	//Permissions related variables
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
        	
        	//iConomy
            if (this.plugin.getServer().getPluginManager().getPlugin("iConomy") != null) {
            	try{        
	                Holdings balance = com.iConomy.iConomy.getAccount(player.getName()).getHoldings();
	                fixedLine = fixedLine.replaceAll("%balance", balance.toString());
	            }
            	catch(NoClassDefFoundError e){
            		fixedLine = fixedLine.replaceAll("%balance", "Please update iConomy to v5 or higher");
            	}
              }
            
            //More Basics
        	fixedLine = fixedLine.replace("%online", onlineNames());
        	fixedLine = colourSwap(fixedLine);
        	fixedLine = fixedLine.replace("&#!", "&");
        	        	
        	//If the line currently in the for loop has "%include", we find out which file to load in by splitting the line up intesively.
        	ArrayList<String> files = new ArrayList<String>();
        	       	
			if (l.contains("%include") || l.contains("%news")){
				if (l.contains("%include")){
					String tempString = l.trim();
	    			String[] firstSplit = tempString.split(" ");
	    			for(String s : firstSplit){
	    				if(s.contains("%include")){
	    					String[] secondSplit = s.split("_");
	    					fixedLine = fixedLine.replace(s, "");
	    					files.add(secondSplit[1]);
	    				}
	    			}
	    			if(!fixedLine.equals(" ")){
	    				fixedLines.add(fixedLine);
	    			}
	    			for(String f : files){
	    				includeAdd(f, player);
	    			}
				}
				if (l.contains("%news")){
					fixedLine = fixedLine.replace("%news", "");	
					if(!fixedLine.equals(" ")){
	    				fixedLines.add(fixedLine);
	    			}
					newsLine(player);
				}
        	}
			else{
				fixedLines.add(fixedLine);
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
            header = colourSwap(header);
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
	 * includeAdd: Is used to insert more lines into the current working doc.
	 * onlineNames: Finds the current online players, and using online-players-format, applies some permissions variables.
	 * onlineGroup: Finds the current online players, check if they're in the group specified, and using online-players-format, applies some permissions variables.
	 * onlineCount: Returns the current amount of users online.
	 */
	
	private void includeAdd(String fileName, Player player){
		//Define some variables
		ArrayList<String> tempLines = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		
		//Ok, let's import our new file, and then send them into another variableSwap [ I   N   C   E   P   T   I   O   N ]
		try {
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
            scanner.close();
            fis.close();            
            //Methods inside of methods!
            variableSwap(player, tempLines);
         }	        		
         catch (Exception ex) {
        	 log.info("[MCDocs] Included file " + fileName + " not found!");
         }
	}
	
	private String onlineNames(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineNames = null;
        String nameFinal = null;
        for (Player o : online){
        	nameFinal = onlinePlayersFormat.replace("%name", o.getDisplayName());
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
        	nameFinal = colourSwap(nameFinal);
        	if (onlineNames == null){
        		onlineNames = nameFinal;
        	}
        	else{
        		onlineNames = onlineNames.trim() + "&f, " + nameFinal;
        	}
        }
        return onlineNames;
	}

	private String onlineGroup(String group){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineGroup = null;
        String nameFinal = null;
        for (Player o : online){
        	String oGroup = MCDocs.Permissions.getGroup(o.getWorld().getName(), o.getName());
        	oGroup = oGroup.toLowerCase();
        	if (oGroup.equals(group)){
        		try{
	        		nameFinal = onlinePlayersFormat.replace("%name", o.getDisplayName());
	        		nameFinal = nameFinal.replace("%group", oGroup);
	        		nameFinal = nameFinal.replace("%prefix", MCDocs.Permissions.getGroupPrefix(o.getWorld().getName(), oGroup));
	        		nameFinal = nameFinal.replace("%suffix", MCDocs.Permissions.getGroupSuffix(o.getWorld().getName(), oGroup));
	            	nameFinal = colourSwap(nameFinal);
        		}
            	catch(Exception ex){
        			log.info("[MCDocs] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
        		}
            	if (onlineGroup == null){
            		onlineGroup = nameFinal;
            	}
            	else{
            		onlineGroup = onlineGroup + "&f, " + nameFinal;
            	}
        	}
        }
        if (onlineGroup == null){
        	onlineGroup = "";
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
	
	private void newsLine(Player player){
		
		ArrayList<String> newsLinesList = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		int current = 0;
		
		try {
            FileInputStream fis = new FileInputStream(folderName + "/MCDocs/" + newsFile);
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (current < newsLines) {
                	try{
                		newsLinesList.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex){
                		newsLinesList.add(" ");
                	}
                	current++;
                }
            scanner.close();
            fis.close();  
            variableSwap(player, newsLinesList);
		}	        		
		catch (Exception ex) {
    	 log.info("[MCDocs] news-file was not found.");
		}
	}
		
    private boolean checkIfNumber(String in) {
        
        try {

            Integer.parseInt(in);
        
        } catch (NumberFormatException ex) {
            return false;
        }
        
        return true;
    }
	
    private String colourSwap(String line){
    	String[] Colours = { 	"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
						        "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
						      };
    	ChatColor[] cCode = {	ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
						        ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE,
						      };
    	
    	for (int x = 0; x < Colours.length; x++) {
    		CharSequence cChk = null;
            String temp = null;

            cChk = Colours[x];
            if (line.contains(cChk)) {
                temp = line.replace(cChk, cCode[x].toString());
                line = temp;
            }
        }
        return line;
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
