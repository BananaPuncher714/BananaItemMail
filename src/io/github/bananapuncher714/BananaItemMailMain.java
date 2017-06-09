package io.github.bananapuncher714;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.bananapuncher714.inventory.CustomInventory;
import io.github.bananapuncher714.inventory.CustomMenu;
import io.github.bananapuncher714.inventory.ActionItem.ActionItem;
import io.github.bananapuncher714.inventory.ActionItem.ActionItemIntention;
import io.github.bananapuncher714.inventory.ActionItem.ButtonItem;
import io.github.bananapuncher714.inventory.components.BananaButton;
import io.github.bananapuncher714.inventory.components.BoxPanel;
import io.github.bananapuncher714.inventory.components.ButtonComponent;
import io.github.bananapuncher714.inventory.components.InventoryPanel;
import io.github.bananapuncher714.inventory.panes.ActionItemPane;
import io.github.bananapuncher714.inventory.panes.PagedOptionPane;
import io.github.bananapuncher714.inventory.panes.PagedStoragePane;
import io.github.bananapuncher714.inventory.panes.StoragePane;
import io.github.bananapuncher714.inventory.util.ICEResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class BananaItemMailMain extends JavaPlugin implements Listener {
	// Make sure BananaInvAPI is installed, although I'm not sure this actually helps
	final boolean BananaInvAPIFound = Bukkit.getPluginManager().getPlugin( "BananaInventoryAPI" ) != null;
	
	// This contains the ONLINE UUID of players
	HashMap< UUID, Integer > cooldown = new HashMap< UUID, Integer >();
	ArrayList< UUID > antiNotificationSquad = new ArrayList< UUID >();
	
	// These contain the OFFLINE UUID of players
	// players contains the CUSTOM INVENTORY of each player, online AND offline
	// They shouldn't cause a memory leak, hopefully
	ArrayList< UUID > privateMailbox = new ArrayList< UUID >();
	HashMap< UUID, CustomInventory > players = new HashMap< UUID, CustomInventory >();
	HashMap< UUID, Location > locations = new HashMap< UUID, Location >();
	
	// Blah blah blah, more messages for the config :P
	String cooldownMessage, sentMail, gotMail, notSent, diffworld, toofar, wrongWorld, noPerms, noHere;
	int cooldownTime, mailMode;
	boolean mailToSelf, mailToOffline, totallyGone;
	ArrayList< String > blacklistedWorlds = new ArrayList< String >();
	final String ccc = "§";
	
	/* Some stuff for serious modification!!!
	 * This handles the main menu
	 * The obfuscation used here was naturally done by a lazy person :/
	 * But it's ok! These are displayed in the dev-config more clearly
	 */
	int menuRows = 5;
	String menuName = ChatColor.DARK_BLUE + ChatColor.BOLD.toString() + "Mailbox";
	int sps = 9;
	int pbs = 3;
	int mbs = 5;
	int prbs = 1;
	int ntbs = 7;
	int npbs = 44;
	int ppbs = 36;
	int hobs = 40;
	Material plab = Material.MINECART;
	Material maib = Material.PAPER;
	Material notba = Material.GLOWSTONE_DUST;
	Material notbb = Material.REDSTONE;
	Material priba = Material.CHEST;
	Material pribb = Material.ENDER_CHEST;
	Material nexb = Material.ARROW;
	Material preb = Material.ARROW;
	Material hidba = Material.EYE_OF_ENDER;
	Material hidbb = Material.ENDER_PEARL;
	String pridn = ChatColor.BLUE + "Click to change mailbox privacy";
	ArrayList< String > prila = new ArrayList< String >();
	ArrayList< String > prilb = new ArrayList< String >();
	String notdna = ChatColor.BLUE + "Click to disable notifications";
	String notdnb = ChatColor.BLUE + "Click to enable notifications";
	ArrayList< String > notla = new ArrayList< String >();
	ArrayList< String > notlb = new ArrayList< String >();
	String visdn = ChatColor.GREEN + "Click to change player visibility";
	ArrayList< String > visla = new ArrayList< String >();
	ArrayList< String > vislb = new ArrayList< String >();
	String npdn = ChatColor.WHITE + ChatColor.BOLD.toString() + "Next Page";
	ArrayList< String > npl = new ArrayList< String >();
	String ppdn = ChatColor.WHITE + ChatColor.BOLD.toString() + "Previous Page";
	ArrayList< String > ppl = new ArrayList< String >();
	String skudna = ChatColor.GREEN + "%p";
	String skudnb = ChatColor.RED + "%p";
	ArrayList< String > skul = new ArrayList< String >();
	String maidn = ChatColor.AQUA + ChatColor.BOLD.toString() + "%p" + "'s mailbox";
	// Ha ha! A variable that looks like it means something!
	// Actually, it's MAIl Lore. Maybe now you can figure out what the rest means!
	ArrayList< String > mail = new ArrayList< String >();
	String smdn = ChatColor.AQUA + ChatColor.BOLD.toString() + "Send mail";
	ArrayList< String > sml = new ArrayList< String >();
	int selx = 1;
	int sely = 1;
	boolean xpx = true;
	boolean xpy = true;
	
	// This is for the inventory where you send items
	int sendMailRows = 4;
	String tp = ChatColor.DARK_BLUE.toString();
	String ts = "'s mailbox";
	
	BananaItemMailFileManager fm = new BananaItemMailFileManager( this );
	
	@Override
	public void onEnable() {
		prila.add( ChatColor.GRAY + "This mailbox is currently public" );
		prilb.add( ChatColor.GRAY + "This mailbox is currently private" );
		notla.add( ChatColor.GRAY + "Notifications are currently on" );
		notlb.add( ChatColor.GRAY + "Notifications are currently off" );
		visla.add( ChatColor.GRAY + "Currently viewing all players" );
		vislb.add( ChatColor.GRAY + "Currently viewing only online players" );
		skul.add( ChatColor.GRAY + "Click to send mail" );
		saveDefaultConfig();
		loadConfig();
		fm.loadDevConfig();
		fm.loadLocations();
		// If BananaInvAPI isn't found, the plugin just crashed. I don't think
		// the code below would ever get executed :/
		if ( !BananaInvAPIFound ) {
			getLogger().severe( "BananaInventoryAPI not found! Disabling this plugin" );
			Bukkit.getPluginManager().disablePlugin( this );
		}
		fm.loadMailBoxes();
		Bukkit.getPluginManager().registerEvents( this, this );
		Bukkit.getScheduler().scheduleSyncRepeatingTask( this, new Runnable() {
			@Override
			public void run() {
				for ( UUID u : cooldown.keySet() ) {
					int timeLeft = cooldown.get( u );
					if ( timeLeft <= 0 ) {
						cooldown.remove( u );
					} else {
						timeLeft--;
						cooldown.put( u, timeLeft );
					}
				}
			}
		}, 0, 1 );
	}
	
	@Override
	public void onDisable() {
		fm.saveMailBoxes();
		fm.saveLocations();
		players.clear();
		cooldown.clear();
		locations.clear();
		privateMailbox.clear();
		antiNotificationSquad.clear();
	}
	
	public void loadConfig() {
		FileConfiguration c = getConfig();
		cooldownTime = c.getInt( "cooldown-time" ) * 20;
		mailToSelf = c.getBoolean( "mail-to-self" );
		mailToOffline = c.getBoolean( "mail-to-offline-players" );
		totallyGone = c.getBoolean( "disable-mail-completely" );
		mailMode = c.getInt( "mail-mode" );
		blacklistedWorlds = new ArrayList< String >( c.getStringList( "blacklisted-worlds" ) );
		loadMessages( c );
	}
	
	public void loadMessages( FileConfiguration c ) {
		cooldownMessage = c.getString( "messages.cooldown-message" ).replaceAll( "&", ccc );
		sentMail = c.getString( "messages.sent-mail" ).replaceAll( "&", ccc );
		gotMail = c.getString( "messages.got-mail" ).replaceAll( "&", ccc );
		notSent = c.getString( "messages.mail-was-empty-so-not-sent" ).replaceAll( "&", ccc );
		toofar = c.getString( "messages.too-far" ).replaceAll( "&", ccc );
		wrongWorld = c.getString( "messages.wrong-world" ).replaceAll( "&", ccc );
		diffworld = c.getString( "messages.different-world" ).replaceAll( "&", ccc );
		noPerms = c.getString( "messages.no-permission" ).replaceAll( "&", ccc );
		noHere = c.getString( "messages.totally-disabled" ).replaceAll( "&", ccc );
	}
	
	@Override
	public boolean onCommand( CommandSender s, Command c, String l, String[] a ) {
		if ( c.getName().equalsIgnoreCase( "enabletheawesomedevconfigforbananaitemmail" ) ) {
			if ( ( s instanceof Player && ( ( Player ) s ).hasPermission( "bananaitemmail.superadmin" ) ) || !( s instanceof Player ) ) {
				// HERE starts the REAL CUSTOMISATION!
				fm.saveDevConfig();
				if ( s instanceof Player ) {
					( ( Player ) s ).sendMessage( ChatColor.AQUA + "[BananaItemMail]" + ChatColor.GREEN + " Dev config generated!" );
					( ( Player ) s ).sendMessage( ChatColor.GREEN + "Reload the server when done editing" );
				}
				getLogger().info( "Dev config generated! Reload the server when done editing" );
			}
		}
		if ( !( s instanceof Player ) ) return false;
		Player p = ( Player ) s;
		if ( c.getName().equalsIgnoreCase( "mail" ) ) {
			if ( !p.hasPermission( "bananaitemmail.mail" ) ) {
				p.sendMessage( noPerms );
				return false;
			}
			if ( blacklistedWorlds.contains( p.getWorld().getName() ) && !p.hasPermission( "bananaitemmail.admin" ) ) {
				p.sendMessage( noHere );
				return false;
			}
			if ( players.containsKey( getOfflineUUID( p ) ) ) {
				p.openInventory( players.get( getOfflineUUID( p ) ).getInventory( true ) );
			} else {
				CustomInventory cinv = buildInv( p );
				p.openInventory( cinv.getInventory( true ) );
				players.put( getOfflineUUID( p ), cinv );
			}
		}
		return false;
	}
	
	public CustomInventory buildInv( OfflinePlayer p ) {
		/* Now here comes the juicy parts
		 * First we create a custom menu, which implements CustomInventory
		 * Then, we add all the buttons. It's a bit unnecessary to use so much
		 * because then we could have an option pane hold all the options instead
		 * We also create a BoxPanel, to house our panes
		*/
		CustomMenu menu = new CustomMenu( menuRows, menuName );
		BoxPanel selectionPanel = new BoxPanel( "selectionPanel", sps );
		BananaButton playerButton = new BananaButton( "playerButton", pbs );
		BananaButton mailButton = new BananaButton( "mailButton", mbs );
		BananaButton notificationButton = new BananaButton( "notificationButton", ntbs );
		BananaButton privateButton = new BananaButton( "privateButton", prbs );
		BananaButton nextPage = new BananaButton( "nextPage", npbs );
		BananaButton prevPage = new BananaButton( "prevPage", ppbs );
		BananaButton hideOffline = new BananaButton( "hideOffline", hobs );
		
		/* This is the more painful part below. Unfortunately, creating
		 * itemstacks cannot be made less painful, so we will have to
		 * create each one manually for each button.
		 * After we make the itemstack, we create a ButtonItem with
		 * that itemstack, along with the name of the button, and the actions
		 */
		
		ItemStack privateItem = new ItemStack( priba );
		ItemMeta piMeta = privateItem.getItemMeta();
		piMeta.setDisplayName( pridn );
		piMeta.setLore( prila );
		privateItem.setItemMeta( piMeta );
		privateButton.setItem( new ButtonItem( "privateItem", "change privacy", ActionItemIntention.CUSTOM, privateItem ) );
		
		ItemStack notificationItem = new ItemStack( notba );
		ItemMeta niMeta = notificationItem.getItemMeta();
		niMeta.setDisplayName( notdna );
		niMeta.setLore( notla );
		notificationItem.setItemMeta( niMeta );
		notificationButton.setItem( new ButtonItem( "notificationItem", "change notifications", ActionItemIntention.CUSTOM, notificationItem ) );
		
		ItemStack hideOfflineItem = new ItemStack( hidba );
		ItemMeta hideOfflineMeta = hideOfflineItem.getItemMeta();
		hideOfflineMeta.setLore( visla );
		hideOfflineMeta.setDisplayName( visdn );
		hideOfflineItem.setItemMeta( hideOfflineMeta );
		hideOffline.setItem( new ButtonItem( "playerVis", "set vis", ActionItemIntention.NONE, hideOfflineItem ) );

		ItemStack pageTurn = new ItemStack( nexb );
		ItemMeta pageturnMeta = pageTurn.getItemMeta();
		pageturnMeta.setDisplayName( npdn );
		pageTurn.setItemMeta( pageturnMeta );
		
		ItemStack pageBack = new ItemStack( preb );
		ItemMeta pagebackMeta = pageBack.getItemMeta();
		pagebackMeta.setLore( ppl );
		pagebackMeta.setDisplayName( ppdn );
		pageBack.setItemMeta( pagebackMeta );
		
		ButtonItem nextpageItem = new ButtonItem( "nextPageButtonItem", "next page", ActionItemIntention.NEXT, pageTurn );
		nextPage.setItem( nextpageItem );
		ButtonItem prevpageItem = new ButtonItem( "prevPageButtonItem", "prev page", ActionItemIntention.PREVIOUS, pageBack );
		prevPage.setItem( prevpageItem );
		
		ItemStack pbitem = new ItemStack( plab );
		ItemMeta pbiMeta = pbitem.getItemMeta();
		pbiMeta.setDisplayName( smdn );
		pbitem.setItemMeta( pbiMeta );
		ButtonItem pbi = new ButtonItem( "playerButtonItem", "player tab", ActionItemIntention.CUSTOM, pbitem );
		playerButton.setItem( pbi );
		
		ItemStack sbitem = new ItemStack( maib );
		ItemMeta sbiMeta = sbitem.getItemMeta();
		sbiMeta.setDisplayName( maidn.replaceAll( "%p", p.getName() ) );
		sbitem.setItemMeta( sbiMeta );
		sbitem.addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
		ButtonItem sbi = new ButtonItem( "storageButtonItem", "storage tab", ActionItemIntention.CUSTOM, sbitem );
		mailButton.setItem( sbi );
		
		/* Aha!! What's this?!?!
		 * It's a pane!! Panes are the flexible auto-resizing areas of goodies that
		 * fit inside InventoryPanels, which are top-layer components when building an
		 * inventory. The ones I used for this plugin both implement the PageObject, so
		 * it can accommodate more items. Panes are also hideable, so if you want to
		 * have a muti-pane component, then you can hide certain ones while showing
		 * the rest. Panes can also add buttons, and they have their own way of managing
		 * the visibility of the button. ex. for panes that need page turners
		 */
		
		PagedOptionPane playerPane = new PagedOptionPane( "playerPane" );
		addPlayers( p, playerPane, true );
		playerPane.hide( true );
		// Here we put the panes into the BoxPanel we created earlier
		// That's because panes can only exist in an InventoryPanel
		selectionPanel.addPane( playerPane );
		
		PagedStoragePane psp = new PagedStoragePane( "storagePane" );
		psp.hide( false );
		selectionPanel.addPane( psp );
		
		playerPane.addButton( nextPage );
		playerPane.addButton( prevPage );
		if ( mailToOffline ) playerPane.addButton( hideOffline );
		
		psp.addButton( nextPage );
		psp.addButton( prevPage );
		
		// Here we edit the various properties of the selection panel
		// If it is able to expand outwards, then setting the size doesn't matter as much
		selectionPanel.setWidth( selx );
		selectionPanel.setHeight( sely );
		selectionPanel.setXpandX( xpx );
		selectionPanel.setXpandY( xpy );
		
		// And here is where we mash the all into the custom menu
		menu.addComponent( privateButton );
		menu.addComponent( notificationButton );
		if ( mailToOffline ) menu.addComponent( hideOffline );
		menu.addComponent( playerButton );
		menu.addComponent( mailButton );
		menu.addComponent( selectionPanel );
		menu.addComponent( nextPage );
		menu.addComponent( prevPage );
		return menu;
	}
	
	@EventHandler
	public void onPlayerQuitEvent( PlayerQuitEvent e ) {
		locations.put( getOfflineUUID( e.getPlayer() ), e.getPlayer().getLocation() );
	}
	
	@EventHandler
	public void onInventoryDragEvent( InventoryDragEvent e ) {
		if ( e.getInventory().getName().equalsIgnoreCase( menuName ) ) e.setCancelled( true );
	}
	
	@EventHandler
	public void onInventoryClickEvent( InventoryClickEvent e ) {
		if ( e.getInventory() == null || !e.getInventory().getName().equalsIgnoreCase( menuName ) ) return;
		if ( e.getSlot() != e.getRawSlot() ) {
			if ( e.getClick().equals( ClickType.SHIFT_LEFT ) || e.getClick().equals( ClickType.SHIFT_RIGHT ) ) e.setCancelled( true );
			return;
		}
		e.setCancelled( true );
		/* This part may get a bit tedious
		 * Here we have to determine what to do like in all other GUI managing plugins
		 * First we have to get an ICEResponse( InventoryclickEvent Response ) that will tell
		 * us about what was clicked and where. The ICEResponse returns the component, pane,
		 * and action item clicked. We mostly want to focus on the action item.
		 */
		Player p = ( Player ) e.getWhoClicked();
		CustomInventory menu = players.get( getOfflineUUID( ( Player ) e.getWhoClicked() ) );
		ICEResponse response = menu.parseICE( e );
		if ( response.getActionItem() != null || response.getItemStack() != null ) {
			if ( response.getActionItem() != null ) {
				ActionItem actionItem = response.getActionItem();
				ActionItemIntention intent = actionItem.getIntent();
				// Right here is the break for button management
				if ( actionItem instanceof ButtonItem ) {
					BoxPanel bp = ( BoxPanel ) menu.getComponent( "selectionPanel" );
					PagedOptionPane playerPane = ( PagedOptionPane ) bp.getPanes().get( "playerPane" );
					PagedStoragePane storagePane = ( PagedStoragePane ) bp.getPanes().get( "storagePane" );
					if ( actionItem.getIntent().equals( ActionItemIntention.CUSTOM ) ) {
						ItemStack playerItem = ( ( BananaButton ) menu.getComponent( "playerButton" ) ).getItem().getItem();
						ItemStack mailItem = ( ( BananaButton ) menu.getComponent( "mailButton" ) ).getItem().getItem();
						// The good thing is that we don't have to worry about the name of the item
						// Just the action and name of the Button,so we could name the item ANYTHING
						if ( actionItem.getActions().contains( "player tab" ) ) {
							if ( mailItem.containsEnchantment( Enchantment.DURABILITY ) ) mailItem.removeEnchantment( Enchantment.DURABILITY );
							if ( !playerItem.containsEnchantment( Enchantment.DURABILITY ) ) playerItem.addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
							// Here is a prime example of pane switching at is finest!
							// One pane is hidden while another is revealed
							playerPane.hide( false );
							storagePane.hide( true );
						} else if ( actionItem.getActions().contains( "storage tab" ) ) {
							if ( !mailItem.containsEnchantment( Enchantment.DURABILITY ) ) mailItem.addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
							if ( playerItem.containsEnchantment( Enchantment.DURABILITY ) ) playerItem.removeEnchantment( Enchantment.DURABILITY );
							storagePane.hide( false );
							playerPane.hide( true );
						} else if ( actionItem.getActions().contains( "change privacy" ) ) {
							ItemStack pitem = actionItem.getItem();
							ItemMeta pmeta = pitem.getItemMeta();
							if ( pitem.getType() == priba ) {
								pitem.setType( pribb );
								pmeta.setLore( prilb );
								privateMailbox.add( getOfflineUUID( p ) );
							} else {
								pitem.setType( priba );
								pmeta.setLore( prila );
								if ( privateMailbox.contains( getOfflineUUID( p ) ) ) privateMailbox.remove( getOfflineUUID( p ) );
							}
							pitem.setItemMeta( pmeta );
							// This refreshes every player's custom inventory so that only the public mailboxes are shown
							refreshPrivacy();
						} else if ( actionItem.getActions().contains( "change notifications" ) ) {
							ItemStack pitem = actionItem.getItem();
							ItemMeta pmeta = pitem.getItemMeta();
							if ( pitem.getType() == notba ) {
								pitem.setType( notbb );
								pmeta.setLore( notlb );
								pmeta.setDisplayName( notdnb );
								antiNotificationSquad.add( p.getUniqueId() );
							} else {
								pitem.setType( notba );
								pmeta.setLore( notla );
								pmeta.setDisplayName( notdna );
								if ( antiNotificationSquad.contains( p.getUniqueId() ) ) antiNotificationSquad.remove( p.getUniqueId() );
							}
							pitem.setItemMeta( pmeta );
						}
						// Here is some page turning action going on!!
					} else if ( actionItem.getIntent().equals( ActionItemIntention.NEXT ) ) {
						if ( storagePane.isHidden() ) playerPane.setPage( playerPane.getPage() + 1 );
						else storagePane.setPage( storagePane.getPage() + 1 );
					} else if ( actionItem.getIntent().equals( ActionItemIntention.PREVIOUS ) ) {
						if ( storagePane.isHidden() ) playerPane.setPage( playerPane.getPage() - 1 );
						else storagePane.setPage( storagePane.getPage() - 1 );
					} else if ( intent.equals( ActionItemIntention.NONE ) ) {
						// When dealing with itemstacks, unfortunately it will still
						// be just as tedious because BananaInventoryAPI is made for INVENTORIES
						if ( actionItem.getActions().contains( "set vis" ) ) {
							BoxPanel selPanel = ( BoxPanel ) menu.getComponent( "selectionPanel" );
							PagedOptionPane psp = ( PagedOptionPane ) selPanel.getPanes().get( "playerPane" );
							psp.getAllContents().clear();
							ItemStack toggle = actionItem.getItem();
							ItemMeta toggleMeta = toggle.getItemMeta();
							if ( actionItem.getItem().getType() == hidbb ) {
								addPlayers( p, psp, true );
								toggleMeta.setLore( visla );
								toggle.setType( hidba );
							} else {
								addPlayers( p, psp, false );
								toggleMeta.setLore( vislb );
								toggle.setType( hidbb );
							}
							toggle.setItemMeta( toggleMeta );
						}
					}
					menu.updateInventory( e.getInventory() );
				} else if ( intent.equals( ActionItemIntention.CUSTOM ) ) {
					/* Aha!! Here's the part where the player clicks something other than a button
					 * The action item they click was in a content pane, but that's just about
					 * the only difference
					 */
					if ( cooldown.containsKey( p.getUniqueId() ) && !p.hasPermission( "bananaitemmail.bypass.cooldown" ) ) {
						if ( !cooldownMessage.equalsIgnoreCase( "" ) ) p.sendMessage( cooldownMessage.replaceAll( "%t", String.valueOf( ( int ) ( cooldown.get( p.getUniqueId() ) / 20 ) ) ) );
						return;
					}
					OfflinePlayer player = Bukkit.getOfflinePlayer( actionItem.getActions().get( 0 ) );
					Location ofpl = locations.get( player.getUniqueId() );
					if ( player.isOnline() ) ofpl = ( ( Player ) player ).getLocation();
					if ( ofpl == null ) return;
					if ( !p.hasPermission( "bananaitemmail.bypass.distance" ) ) {
						if ( ofpl.distanceSquared( p.getLocation() ) > mailMode * mailMode && mailMode > 0 ) {
							p.sendMessage( toofar.replaceAll( "%p", player.getName() ) );
							return;
						}
						if ( blacklistedWorlds.contains( ofpl.getWorld().getName() ) || blacklistedWorlds.contains( p.getWorld().getName() ) ) {
							p.sendMessage( wrongWorld.replaceAll( "%p", player.getName() ) );
							return;
						}
						if ( mailMode == 0 && ofpl.getWorld() != p.getWorld() ) { 
							p.sendMessage( diffworld.replaceAll( "%p", player.getName() ) );
							return;
						}
					}
					CustomInventory cinv;
					if ( players.containsKey( player.getUniqueId() ) ) {
						cinv = players.get( player.getUniqueId() );
					} else {
						cinv = buildInv( player );
						players.put( player.getUniqueId(), cinv );
					}
					Inventory mailin = Bukkit.createInventory( null, sendMailRows * 9, tp + player.getName() + ts );
					p.openInventory( mailin );
				}
			} else {
				/* This is if they clicked on an itemstack, AKA their mailbox
				 * It just spews the item out and removes it from their mail
				 * Note how we can update the inventory with the updateInventory method.
				 * The custom menu can create a new one, fetch a premade one, or update any inventory, given
				 * its size is the same as the custom menu.
				 */
				ItemStack item = response.getItemStack();
				for ( ItemStack i : p.getInventory().addItem( item ).values() ) p.getWorld().dropItem( p.getLocation(), i );
				StoragePane s = ( StoragePane ) response.getPane();
				s.removeItem( item );
				menu.updateInventory( e.getInventory() );
			}
		}
	}
	
	@EventHandler
	public void onInventoryCloseEvent( InventoryCloseEvent e ) {
		Inventory mailInv = e.getView().getTopInventory();
		Player player = ( Player ) e.getPlayer();
		if ( !players.containsKey( getOfflineUUID( ( Player ) e.getPlayer() ) ) ) return;
		/* This next line is very important. Whenever you have a storage pane, you should
		 * call saveInventory in order to save the contents of the storage panes
		 * Of course, that's only if the player puts items inside the storage pane.
		 */
		if ( mailInv.getName().contains( tp ) && mailInv.getName().contains( ts ) ) {
			// Here's a bunch of more inventory management, and this time
			// it adds items to the player's mailbox
			OfflinePlayer p = Bukkit.getOfflinePlayer( ChatColor.stripColor( mailInv.getName().replaceFirst( tp, "" ).replaceFirst( ts, "" ) ) );
			CustomInventory menu = players.get( p.getUniqueId() );
			BoxPanel panel = ( BoxPanel ) menu.getComponent( "selectionPanel" );
			PagedStoragePane psp = ( PagedStoragePane ) panel.getPanes().get( "storagePane" );
			boolean isEmpty = true;
			for ( ItemStack i : e.getInventory().getContents() ) {
				if ( i != null ) {
					psp.addItem( i );
					isEmpty = false;
				}
			}
			menu.updateInventory( menu.getInventory( false ) );
			if ( !isEmpty && !sentMail.equalsIgnoreCase( "" ) ) player.sendMessage( sentMail.replaceAll( "%p", p.getName() ) );
			else if ( !notSent.equalsIgnoreCase( "" ) ) player.sendMessage( notSent.replaceAll( "%p", p.getName() ) );
			if ( p.isOnline() && !isEmpty && !gotMail.equalsIgnoreCase( "" ) && !antiNotificationSquad.contains( ( ( Player ) p ).getUniqueId() ) ) ( ( Player ) p ).sendMessage( gotMail.replaceAll( "%p", player.getName() ) );
			if ( !isEmpty && cooldownTime > 0 ) cooldown.put( player.getUniqueId(), cooldownTime );
			Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
				@Override
				public void run() {
					e.getPlayer().openInventory( players.get( getOfflineUUID( ( Player ) e.getPlayer() ) ).getInventory( false ) );
				}
			} );
		}
	}
	
	public UUID getOfflineUUID( Player p ) {
		return ( ( OfflinePlayer ) p ).getUniqueId();
	}
	
	public void refreshPrivacy() {
		for ( UUID u : players.keySet() ) {
			CustomInventory cinv = players.get( u );
			ButtonComponent hideOffline = ( ButtonComponent ) cinv.getComponent( "hideOffline" );
			ActionItemPane ppane = ( ActionItemPane ) ( ( InventoryPanel ) cinv.getComponent( "selectionPanel" ) ).getPanes().get( "playerPane" );
			( ( PagedOptionPane ) ppane ).getAllContents().clear();
			Material type = null;
			if ( mailToOffline ) type = hideOffline.getItem().getItem().getType();
			if ( type == hidba ) {
				addPlayers( Bukkit.getOfflinePlayer( u ), ppane, true );
			} else {
				addPlayers( Bukkit.getOfflinePlayer( u ), ppane, false );
			}
			cinv.updateInventory( cinv.getInventory( false ) );
		}
	}
	
	public void addPlayers( OfflinePlayer p, ActionItemPane playerPane, boolean offline ) {
		// This method adds all the players' heads on the server to a action pane for further
		// processing, with several options. Note that this uses offline players.
		for ( OfflinePlayer pl : Bukkit.getOfflinePlayers() ) {
			if ( pl == p && !mailToSelf ) {
				if ( !p.isOnline() || !( ( ( Player ) p ).hasPermission( "bananaitemmail.admin" ) ) ) continue;
			}
			ItemStack skull = new ItemStack( Material.SKULL_ITEM );
			if ( privateMailbox.contains( pl.getUniqueId() ) ) {
				if ( p.isOnline() ) {
					if ( !( ( Player ) p ).hasPermission( "bananaitemmail.admin" ) ) {
						continue;
					}
				}
			}
			if ( pl.isOnline() ) {
				skull.setDurability( ( short ) 3 );
				SkullMeta meta = ( SkullMeta ) skull.getItemMeta();
				meta.setOwner( pl.getName() );
				meta.setDisplayName( skudna.replaceAll( "%p", pl.getName() ) );
				meta.setLore( skul );
				skull.setItemMeta( meta );
			} else {
				skull.setDurability( (short) 0 );
				ItemMeta meta = skull.getItemMeta();
				meta.setDisplayName( skudnb.replaceAll( "%p", pl.getName() ) );
				meta.setLore( skul );
				skull.setItemMeta( meta );
			}
			ActionItem aitem = new ActionItem( pl.getName(), pl.getName(), ActionItemIntention.CUSTOM, skull );
			if ( ( ( offline && mailToOffline && locations.containsKey( pl.getUniqueId() ) ) || pl.isOnline() ) ) playerPane.addActionItem( aitem );
		}
	}
}
