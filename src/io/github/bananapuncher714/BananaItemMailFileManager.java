package io.github.bananapuncher714;

import io.github.bananapuncher714.inventory.CustomInventory;
import io.github.bananapuncher714.inventory.components.ButtonComponent;
import io.github.bananapuncher714.inventory.components.InventoryPanel;
import io.github.bananapuncher714.inventory.panes.PagedStoragePane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BananaItemMailFileManager {
	BananaItemMailMain plugin;
	String ccc;
	
	public BananaItemMailFileManager( BananaItemMailMain p ) {
		plugin = p;
		ccc = p.ccc;
	}
	
	public void saveMailBoxes() {
		File saveDir = new File( plugin.getDataFolder() + File.separator + "mailboxes" );
		recursiveDelete( saveDir );
		saveDir.mkdir();
		for ( UUID u : plugin.players.keySet() ) {
			String filename = u.toString().substring( 0, 2 );
			File subDir = new File( plugin.getDataFolder() + File.separator + "mailboxes" + File.separator + filename );
			subDir.mkdir();
			File playerMailFile = new File( plugin.getDataFolder() + File.separator + "mailboxes" + File.separator + filename, u.toString() );
			try {
				playerMailFile.createNewFile();
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
			FileConfiguration playerMail = YamlConfiguration.loadConfiguration( playerMailFile );
			playerMail.set( "mailbox-contents", ( ( PagedStoragePane ) ( ( InventoryPanel ) plugin.players.get( u ).getComponent( "selectionPanel" ) ).getPanes().get( "storagePane" ) ).getAllContents() );
			boolean isPrivate = ( ( ButtonComponent ) plugin.players.get( u ).getComponent( "privateButton" ) ).getItem().getItem().getType() == Material.ENDER_CHEST;
			playerMail.set( "private", isPrivate );
			try {
				playerMail.save( playerMailFile );
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}
	}
	
	public void loadMailBoxes() {
		File saveDir = new File( plugin.getDataFolder() + File.separator + "mailboxes" );
		if ( !saveDir.exists() ) return;
		for ( File dir : saveDir.listFiles() ) {
			for ( File saveFile : dir.listFiles() ) {
				FileConfiguration playerMail = YamlConfiguration.loadConfiguration( saveFile );
				List< ItemStack > items = ( List< ItemStack > ) playerMail.get( "mailbox-contents" );
	            ItemStack[] contents = items.toArray( new ItemStack[ 0 ] );
	            CustomInventory cinv = plugin.buildInv( Bukkit.getOfflinePlayer( UUID.fromString( saveFile.getName() ) ) );
	            if ( playerMail.getBoolean( "private" ) ) {
	            	plugin.privateMailbox.add( UUID.fromString( saveFile.getName() ) );
	            	ItemStack pritem = ( ( ButtonComponent ) cinv.getComponent( "privateButton" ) ).getItem().getItem();
					ItemMeta pmeta = pritem.getItemMeta();
					pritem.setType( plugin.pribb );
					pmeta.setLore( plugin.prilb );
					pritem.setItemMeta( pmeta );
	            }
	            cinv.getInventory( true );
	            ( ( PagedStoragePane ) ( ( InventoryPanel ) cinv.getComponent( "selectionPanel" ) ).getPanes().get( "storagePane" ) ).setContents( new ArrayList< ItemStack >( Arrays.asList( contents ) ) );
	            
	            plugin.players.put( UUID.fromString( saveFile.getName() ), cinv );
			}
		}
		plugin.refreshPrivacy();
	}
	
	public void saveLocations() {
		File locFile = new File( plugin.getDataFolder(), "locations.yml" );
		locFile.delete();
		try {
			locFile.createNewFile();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
		// TODO Make this thing load!!
		FileConfiguration offlineLocations = YamlConfiguration.loadConfiguration( locFile );
		for ( UUID u : plugin.locations.keySet() ) {
			offlineLocations.set( u.toString(), toString( plugin.locations.get( u ) ) );
		}
		try {
			offlineLocations.save( locFile );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	
	public void loadLocations() {
		File locFile = new File( plugin.getDataFolder(), "locations.yml" );
		if ( !locFile.exists() ) return;
		FileConfiguration offlineLocations = YamlConfiguration.loadConfiguration( locFile );
		for ( String s : offlineLocations.getKeys( false ) ) {
			plugin.locations.put( UUID.fromString( s ), toLocation( offlineLocations.getString( s ) ) );
		}
	}
	
	public void saveDevConfig() {
		plugin.saveResource( "dev-config.yml", false );
	}
	
	public void loadDevConfig() {
		File devConfigFile = new File( plugin.getDataFolder(), "dev-config.yml" );
		if ( !devConfigFile.exists() ) return;
		plugin.getLogger().info( "[BananaItemMail] Loading the dev config!" );
		FileConfiguration dc = YamlConfiguration.loadConfiguration( devConfigFile );
		// Loading the main menu
		plugin.menuName = dc.getString( "main-menu.title" ).replaceAll( "&", ccc );
		plugin.menuRows = dc.getInt( "main-menu.rows" );
		
		// Loading the outgoing mailbox
		plugin.sendMailRows = dc.getInt( "outgoing-mail-menu.rows" );
		plugin.tp = dc.getString( "outgoing-mail-menu.prefix" ).replaceAll( "&", ccc );
		plugin.ts = dc.getString( "outgoing-mail-menu.suffix" ).replaceAll( "&", ccc );
		
		// Loading the selection panel
		plugin.sps = dc.getInt( "main-menu.panels.selection-panel.slot" );
		plugin.selx = dc.getInt( "main-menu.panels.selection-panel.x" );
		plugin.sely = dc.getInt( "main-menu.panels.selection-panel.y" );
		plugin.xpx = dc.getBoolean( "main-menu.panels.selection-panel.x-expandable" );
		plugin.xpy = dc.getBoolean( "main-menu.panels.selection-panel.y-expandable" );
		
		// Loading the pane
		plugin.skudna = dc.getString( "main-menu.panels.selection-panel.panes.send-mail-pane.item-online-display-name" ).replaceAll( "&", ccc );
		plugin.skudnb = dc.getString( "main-menu.panels.selection-panel.panes.send-mail-pane.item-offline-display-name" ).replaceAll( "&", ccc );
		plugin.skul = addColor( dc.getStringList( "main-menu.panels.selection-panel.panes.send-mail-pane.item-lore" ) );
		
		// Loading the buttons
		plugin.pbs = dc.getInt( "main-menu.buttons.send-mail.slot" );
		plugin.plab = Material.getMaterial( dc.getInt( "main-menu.buttons.send-mail.item-id" ) );
		plugin.smdn = dc.getString( "main-menu.buttons.send-mail.display-name" ).replaceAll( "&", ccc );
		plugin.sml = addColor( dc.getStringList( "main-menu.buttons.send-mail.lore" ) );
		
		plugin.mbs = dc.getInt( "main-menu.buttons.check-mailbox.slot" );
		plugin.maib = Material.getMaterial( dc.getInt( "main-menu.buttons.check-mailbox.item-id" ) );
		plugin.maidn = dc.getString( "main-menu.buttons.check-mailbox.display-name" ).replaceAll( "&", ccc );
		plugin.mail = addColor( dc.getStringList( "main-menu.buttons.check-mailbox.lore" ) );
		
		plugin.npbs = dc.getInt( "main-menu.buttons.next-page.slot" );
		plugin.nexb = Material.getMaterial( dc.getInt( "main-menu.buttons.next-page.item-id" ) );
		plugin.npdn = dc.getString( "main-menu.buttons.next-page.display-name" ).replaceAll( "&", ccc );
		plugin.npl = addColor( dc.getStringList( "main-menu.buttons.next-page.lore" ) );
		
		plugin.ppbs = dc.getInt( "main-menu.buttons.previous-page.slot" );
		plugin.preb = Material.getMaterial( dc.getInt( "main-menu.buttons.previous-page.item-id" ) );
		plugin.ppdn = dc.getString( "main-menu.buttons.previous-page.display-name" ).replaceAll( "&", ccc );
		plugin.ppl = addColor( dc.getStringList( "main-menu.buttons.previous-page.lore" ) );
		
		plugin.prbs = dc.getInt( "main-menu.buttons.toggle-privacy.slot" );
		plugin.pridn = dc.getString( "main-menu.buttons.toggle-privacy.display-name" ).replaceAll( "&", ccc );
		plugin.priba = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-privacy.item-id-off" ) );
		plugin.pribb = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-privacy.item-id-on" ) );
		plugin.prila = addColor( dc.getStringList( "main-menu.buttons.toggle-privacy.lore-on" ) );
		plugin.prilb = addColor( dc.getStringList( "main-menu.buttons.toggle-privacy.lore-off" ) );
		
		plugin.hobs = dc.getInt( "main-menu.buttons.toggle-recipients.slot" );
		plugin.visdn = dc.getString( "main-menu.buttons.toggle-recipients.display-name" ).replaceAll( "&", ccc );
		plugin.hidba = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-recipients.item-id-on" ) );
		plugin.hidbb = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-recipients.item-id-off" ) );
		plugin.visla = addColor( dc.getStringList( "main-menu.buttons.toggle-recipients.lore-on" ) );
		plugin.vislb = addColor( dc.getStringList( "main-menu.buttons.toggle-recipients.lore-off" ) );
		
		plugin.ntbs = dc.getInt( "main-menu.buttons.toggle-notifications.slot" );
		plugin.notdna = dc.getString( "main-menu.buttons.toggle-notifications.display-name-on" ).replaceAll( "&", ccc );
		plugin.notdnb = dc.getString( "main-menu.buttons.toggle-notifications.display-name-off" ).replaceAll( "&", ccc );
		plugin.notba = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-notifications.item-id-on" ) );
		plugin.notbb = Material.getMaterial( dc.getInt( "main-menu.buttons.toggle-notifications.item-id-off" ) );
		plugin.notla = addColor( dc.getStringList( "main-menu.buttons.toggle-notifications.lore-on" ) );
		plugin.notlb = addColor( dc.getStringList( "main-menu.buttons.toggle-notifications.lore-off" ) );
	}
	
	public ArrayList< String > addColor( List< String > lore ) {
		ArrayList< String > completed = new ArrayList< String >();
		for ( String l : lore ) {
			completed.add( l.replaceAll( "&", ccc ) );
		}
		return completed;
	}
	
	private void recursiveDelete( File dir ) {
	    if( dir.isDirectory() ) {
	        File[] files = dir.listFiles();
	        for( File f: files ){
	        	recursiveDelete(f);
	        } 
	    }
	    dir.delete();
	}
	
	public Location toLocation( String s ) {
		String[] ll = s.replace( ',', '.' ).split( "_" );
		Location l = new Location( plugin.getServer().getWorld( ll[ 0 ] ), Double.parseDouble( ll[ 1 ] ), Double.parseDouble( ll[ 2 ] ), Double.parseDouble( ll[ 3 ] ), Float.parseFloat( ll[ 4 ] ), Float.parseFloat( ll[ 5 ] ) );
		return l;
	}
	
	private String toString( Location l ) {
		String newLoc = l.getWorld().getName() + "_" + String.valueOf( l.getX() ) + "_" + String.valueOf( l.getY() ) + "_" + String.valueOf( l.getZ() ) + "_" + String.valueOf( l.getYaw() ) + "_" + String.valueOf( l.getPitch() );
		return newLoc.replace( '.', ',' );
	}
}
