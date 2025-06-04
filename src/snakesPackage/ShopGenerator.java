package snakesPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import universalThings.uniMethod;
import java.util.Random;
import java.io.File;
import java.util.Scanner;
import java.util.function.Predicate;

import java.awt.*;
import java.awt.datatransfer.*;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class ShopGenerator {
	static Random randGen = new Random(); // nextInt(min include, max exclude)
	static ArrayList<Item>[] itemTierList; // composed of 5 ArrayList, each holding every ID of the Tier items
	static ArrayList<Item> itemIDList = new ArrayList<Item>(); // holds every Item based on ID
	static HashMap<String, Item> itemNameList = new HashMap<String, Item>(); // holds every Item based on name, used in Goal 1
	static HashMap<String, Item> itemDatabase = new HashMap<String, Item>();
	static int parameterCounter; // used as a safety net if the parameter generation messes up
	
//	final static String[] HEX_ARRAY = new String[5];
//	static {
//		HEX_ARRAY[0] = "#ffd966";
//		HEX_ARRAY[1] = "#b1d0a4";
//		HEX_ARRAY[2] = "#a4c2f4";
//		HEX_ARRAY[3] = "#dd7e6b";
//		HEX_ARRAY[4] = "#c585d1";
//	}
//	
//	final static String[] ANSI_ARRAY = new String[5];
//	static {
//		for (int i = 0; i < HEX_ARRAY.length; i++) {
//			ANSI_ARRAY[i] = hexToAnsi(HEX_ARRAY[i]);
//		}
//	}
//	
//	final static String ANSI_RESET = "\033[0m";
//	final static String RGB_RESET = "[255, 255, 255]";
//	
//	final static HashMap<String, String> HEX_THEME = new HashMap<String, String>();
//	static {
//		HEX_THEME.put("Item", "#ea9999");
//		HEX_THEME.put("Hazard", "#f9cb9c");
//		HEX_THEME.put("Building", "#ffe599");
//		HEX_THEME.put("Artifact", "#b6d7a8");
//		HEX_THEME.put("Passive", "#a2c4c9");
//		HEX_THEME.put("Curse", "#b4a7d6");
//		
//		HEX_THEME.put("Gold", "#f1c232");
//		HEX_THEME.put("Steal", "#ee560b");
//		HEX_THEME.put("Gain", "#c4d313");
//		HEX_THEME.put("Mobility", "#43e243");
//		HEX_THEME.put("Reverse", "#d5a6bd");
//		HEX_THEME.put("Hinder", "#a6c29b");
//		HEX_THEME.put("Relocate", "#d33ed2");
//		HEX_THEME.put("Creation", "#00ffff");
//		HEX_THEME.put("Change", "#3d3df0");
//		HEX_THEME.put("Opportunity", "#4ca7fa");
//		HEX_THEME.put("Destruction", "#73a6b3");
//		HEX_THEME.put("Scale", "#d9d9d9");
//		HEX_THEME.put("Reroll", "#d9ead3");
//		HEX_THEME.put("Constant", "#c9daf8");
//	}
//	
//	final static HashMap<String, String> ANSI_THEME = new HashMap<String, String>();
//	static {
//		HEX_THEME.forEach(
//				(key, value)
//				-> ANSI_THEME.put(key, hexToAnsi(value))
//				);
//	}
	
	final static HashMap<String, String> ANSI_THEME = Item.getAnsiTheme();
	final static String ANSI_RESET = Item.getAnsiReset();
	final static String[] ANSI_TIER = Item.getAnsiTier();
	
	public interface ItemGenMethod<T> {
		Item runGenerate(T pattern, Predicate<Item> parameter);
	}
	
	
	/* BoundItemGen is really just a preset on how XYZ will
	 * generate an item, for example:
	 * 
	 * chestBound = bind(chestProb), uses singleItemGen with only a weighted probability
	 * shopBound = bind(weightedShopArray), uses singleItemGen also
	 * startBound = bind(1, item -> (!item.getType().equals("Passive"))), uses customItemGen, T1 with NO passives
	 * 
	 */
	@FunctionalInterface
	public interface BoundItemGen {
	    Item runGenerate();
	}

	public static <T> BoundItemGen bind(T pattern, Predicate<Item> parameter) {
	    return () -> generateItem(pattern, parameter);
	}
	
	public static <T> BoundItemGen bind(T pattern) {
	    return () -> generateItem(pattern);
	}

	
	public static void main(String[] args) throws Exception{
		/* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDirectoryForTemplateLoading(new File("templates")); // import template file
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        /* ------------------------------------------------------------------------ */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */
		
        Map root = new HashMap();
        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("item_template.ftl");

        /* Merge data-model with template */
//        Writer out = new OutputStreamWriter(System.out);
        StringWriter out = new StringWriter();  // Change this from OutputStreamWriter
        
        // import the system Clipboard
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		ItemTransfer itemTransTemp = new ItemTransfer();
 		
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
        
        /* all primary variables
         * 
         * {ItemName}
         * {ItemTierColor}
         * {ItemTier}
         * {ItemCost}
         * {ItemDesc + lines}
         * {ItemRange}
         * {ItemType}
         * {ItemTheme}
         * {ItemVersion}
         * 
         */
        
// 		System.out.println(ANSI_THEME.get("Change") + "change text");
 		
		final String ITEMFileName = "Snakes items list_testing.csv";
		final String QUESTFileName = "Snakes items list_Quest List.csv";
		
		final double DISCOUNTRate = 0.66; // the new price of the original item
		final int ruleComplexity = 3; // what items to include based on complexity
		
		/* testing: "Snakes items list_testing.csv"
		 * official: "Snakes items list_Snake Game item list.csv"
		 */
		
		/* ItemGenMethod is an abstract Interface where the lambda method
		 * MUST use the following parameters:
		 * 
		 * @T pattern
		 * <T> is a type parameter which is similar to an Object but
		 * has more restrictions.
		 * In this case, pattern is usually an int (for itemTier) or
		 * int[] (for weightedProbability[])
		 * 
		 * @Predicate<Item> parameter
		 * Predicate<Item> is a lambda expression/mini method which you
		 * put some random parameters that MUST return an Item.
		 * 
		 * In this case, the parameter is usually singleGen or
		 * customGen variable which returns singleItemGen or customItemGen
		 * respectively
		 * 
		 * singleGen and customGen are 2 ItemGenMethods or lambda expressions
		 * that MUST use both the pattern and parameter within its parameters.
		 * 
		 * When singleGen is called, it returns singleItemGen(pattern)
		 * When customGen is called, it returns customItemGen(pattern, parameter)
		 *  
		 */
		
		/* get rid of the use of singleGen and customGen
		 * 
		 * function which only has a pattern and parameter
		 * custom is used if parameter
		 * otherwise only single
		 * 
		 * each pattern has both an int and int[]
		 * 
		 * ItemGenMethod is used for manual programming
		 * BoundItemGen is premade/preset item generation types
		 * 
		 */
		
		ItemGenMethod<Object> singleGen = (pattern, parameter) -> {
			if (pattern instanceof Integer) {
				return singleItemGen((int)pattern);
			}
			else if (pattern instanceof int[]){
				return singleItemGen((int[])pattern);
			}
			else {
				throw new IllegalArgumentException("ItemGenMethod singleGen, Invalid pattern type: " + pattern.getClass());
			}
		};
		
		ItemGenMethod<Object> customGen = (pattern, parameter) -> {
			if (pattern instanceof Integer) {
				return customItemGen((int)pattern, parameter);
			}
			else if (pattern instanceof int[]){
				return customItemGen((int[])pattern, parameter);
			}
			else {
				throw new IllegalArgumentException("ItemGenMethod customGen, Invalid pattern type: " + pattern.getClass());
			}
		};
		
		File itemFile = new File(ITEMFileName); // holds the File for all the items
	
		Scanner fileScanner = new Scanner(itemFile); // Scanner for the itemFile
		
		// temporary variables
		int weightedTemp;
		Item itemTemp = null; // temporary Item variable used in the Menu
		String strTemp = null;
		String strTemp2 = null;
		String[] strTempFinal = new String[1]; // used specifically in THEMED chest loot since local variables in 
		
		int intTemp = 0;
		int intTemp2 = 0;
		
		// Goal 1: import all items
		String[] csvArray; // csvArray holds the split
		
		itemTierList = new ArrayList[5]; // 0 = exclusive, 1-3 = tier 1-3, 4 = artifacts
		for (int i = 0; i < 5; i++) { // initiate every ArrayList in itemIDList
			itemTierList[i] = new ArrayList<>();
		}
		
		fileScanner.nextLine(); // skip Line no.1 which contains the headers
		while (fileScanner.hasNextLine()) { // for each line in the txt document
			strTemp = fileScanner.nextLine(); // stores the raw line in string
			
			csvArray = strTemp.split(";"); // splits the stored line as an array
			
			itemTemp = new Item (
					Integer.parseInt(csvArray[0]), // ID
					csvArray[1], // name
					Integer.parseInt(csvArray[2]), // cost
					csvArray[3].split("<br>"), // description
					csvArray[4], // blurb
					csvArray[5], // range
					csvArray[6], // type
					csvArray[7].split("/"),// theme
					Integer.parseInt(csvArray[8]), // tier
					csvArray[9], // version
					Integer.parseInt(csvArray[10]) // complexity
					);
			
			uniMethod.printArray(csvArray);
			
			itemIDList.add(itemTemp);
			itemDatabase.put(itemTemp.getName().toLowerCase(), itemTemp);
			
			// exclude the items from the actual lootpool
			if (itemTemp.getComplexity() <= ruleComplexity) {
				// place in ItemNameList HashMap
				itemNameList.put(itemTemp.getName().toLowerCase(), itemTemp);
				
				// based on itemTemp's Tier, place in itemTierList
				itemTierList[itemTemp.getTier()].add(itemTemp);
			}
			
		} // while loop
		
		fileScanner.close();
		// Now: allItems now holds every Item
		
		// Goal 2: Create an ID array based on every Type and Theme
		// Next: Count all the Item Types
		
				
//		for (int i = 0; i < Item.getTotal(); i++) { // repeat for every possible item
//			// check for ruleComplexity
//			
//			if (itemTemp.getComplexity() <= ruleComplexity) {
//				itemTemp = itemIDList.get(i); // get ID no.i
//				// based on item Tier, place itemTemp
//				
//			}			
//		}
		// Now: itemTypeTemp contains the ID for each of the special items
		
		// Goal 3: Create the quests and place them in an Array
		File questFile = new File(QUESTFileName);
		HashMap<String, Quest> questNameList = new HashMap<String, Quest>(); // stores Quests based on Name (lowercase)
		Scanner questScanner = new Scanner(questFile);
		Quest questTempLoad;
		
		// initialize questTierList
		ArrayList<Quest>[] questTierList = new ArrayList[3]; // holds Quests based on its Tiers
		for (int i = 0; i < 3; i++) { // initiate every ArrayList in itemIDList
			questTierList[i] = new ArrayList<>();
		}
		
		questScanner.nextLine(); // skip first row
		while (questScanner.hasNextLine()) {
			strTemp = questScanner.nextLine(); // stores the raw csv line as String
			csvArray = strTemp.split(";"); // splits the csv line as an Array
			
			questTempLoad = new Quest(
					Integer.parseInt(csvArray[0]), // QuestID
					csvArray[1], // QuestName
					csvArray[2], // QuestReward
					Integer.parseInt(csvArray[3]) // QuestTier
					);
	
			questNameList.put(questTempLoad.getName().toLowerCase(), questTempLoad);
			questTierList[questTempLoad.getTier() - 1].add(questTempLoad); // each ArrayList represents a Tier of Quests
		} // while loop
		// Now: questArray contains the raw strings of each quest
		
		
		
		// Goal: load the menu
		Scanner myScanner = new Scanner(System.in);
		int intInput; // scanner int input
		String strInput; // scanner String input
		
//		ArrayList<Item> shopList = new ArrayList<Item>(); // the items stored in the shop
		Item[] shopList = new Item[5];
		int refreshCounter = 0; // used in 4. refresh shop
		
		int firstTile; // used in 8. distance calculator
		int secondTile;
		
//		Item[] chestArray = new Item[3]; // used for weighted chest
//		int chestTotal = 4; // number of items in the chest
		
		int shopTier = 0; // the tier of the Shop, used to unlock more items
		
		boolean yesDupes; // used in lots of generate multiple things with no dupe
		boolean discountPresent = true; // tracks when the Discount shop item is purchased
		
//		// blurb printing variables
//		int cutOff = 75; // cutoff range (CHANGEABLE)
//		
//		String itemBlurb;
//		int blurbLength; // holds the length of the blurb
//		String blurbSlice = null; // holds blurb substring
//		int startIndex = 0; // starting index of substring
//		int endIndex; // end index of substring
//		int maxDivisions; // number of times blurb can get divided
		
		int[] chestProb = {0,20,60,20,0}; // used for generating chests
		int[] questProbTemp;
		BoundItemGen chestBound = bind(chestProb);
		
		// probability of generating a Random Tiered Quest based on shopTier
		int[][] questProbTier = new int[4][3];{
			questProbTier[0] = new int[]{0,0,1}; // the fallback is all T3 quests
			questProbTier[1] = new int[]{8,2,0}; 
			questProbTier[2] = new int[]{12,5,1};
			questProbTier[3] = new int[]{8,5,3};
		}
		
		// this is 0 indexed
		String questRewardList[][] = new String[4][];{
			// questRewardT1
			questRewardList[0] = new String[] {
					"FALLBACK REWARD T0"
			};
				
			questRewardList[1] = new String[]{ // questRewardT1
					"20-30 Gold", // 1
					"3 Random T1 Items", // 2
					"2 Random T2 Item", // 3
					"Any 1 T1 Item", // 4
					"1 Random T3 Item", // 5
					"1 Lockbox Item", // 6
					"Any Normal Dice Face up to D6", // 7
					"Free D8", // 8
					"Free D10" // 9
			};
			
			// questRewardT2
			questRewardList[2] = new String[]{
					"30-50 Gold", // 1
					"Any 3 Unique T1 Items", // 2
					"Any 2 T2 Items", // 3
					"2 Random T3 Item", // 4
					"2 Lockbox Items", // 5
					"Any Normal Dice Face up to D8", // 6
					"D10.5 Face", // 7
					"D12 Face", // 8
					"Free D10", // 9
					"Free D12", // 10
			};
			
			// questRewardT3
			questRewardList[3] = new String[]{
				"75-100 Gold", // 1
				"Any 4 Unique Items", // 2
				"Any Normal Dice Face up to D20", // 3
				"D20.5 Face", // 4
				"Free D20", // 5
				"Random Artifact" // 6
			};
		}
		
		
		int questRewardProb[][] = new int[4][]; { // create a local code block
			// access questRewardProb which exists at a higher scope, then update the variable
			
			questRewardProb[0] = new int[] {1}; // fallback
			questRewardProb[1] = new int[] {15,15,13,10,10,8,8,5,1}; // probability of picking a REWARD for a T1 Quest
			questRewardProb[2] = new int[] {15,14,14,14,12,5,5,5,2,2}; // probability of picking a REWARD for a T1 Quest
			questRewardProb[3] = new int[] {15,15,12,8,1,1}; // probability of picking a REWARD for a T1 Quest
		}
		
		int[][] weightedShopArray = new int[4][];{ // holds each 4 shopTiers of probability
			weightedShopArray[0] = new int[]{1,0,0,0,0}; // fallback
			weightedShopArray[1] = new int[]{0,85,15,0,0};
			weightedShopArray[2] = new int[]{0,43,42,15,0};
			weightedShopArray[3] = new int[]{0,5,5,5,1};
		}
		
		BoundItemGen[] shopBound = new BoundItemGen[4]; {
			for (int i = 0; i < 4; i++) {
				shopBound[i] = bind(weightedShopArray[i]);
			}
		}
		
		Quest questTemp;
		int questTierTemp;
		Quest[] questBoard = new Quest[4]; // 4 quests in the quest board
		String[] questReward = new String[4]; // holds the quest reward OUTCOMES
		
		int quantityReward; // number of RANDOM items
		int tierReward; // tier of RANDOM items
		
		String[] bonusReward = new String[4];
		String questDescTemp;
		
		String bonusSpace = // bonusSpace used for nice Quest formatting
				"                                                            ";
		
		
		// starting item generation
		Item[][] startArray = new Item[3][4]; // 3 players, 4 length/starting items
		boolean[][] startRefreshed = new boolean[3][4]; // boolean for already refreshed items
		
		/* boundArray holds the ItemGenType, pattern and parameter
		 * for how to generate an Item for each item in startArray[player X]
		 * 
		 * While generating an Item, call boundArray[i].runGenerate() which calls:
		 * bound1.runGenerate();                // You call the interface method (no args)
 			-> gen.runGenerate(pattern, parameter); 
         			-> executes singleGenLogic
              			-> calls singleItemGen(...)
		 * 
		 */
		
		// 
		BoundItemGen[] startBound = new BoundItemGen[4]; {
			startBound[0] = bind(1, item -> (!item.getType().equals("Passive")));
			startBound[1] = bind(1, item -> (!item.getType().equals("Passive")));
			startBound[2] = bind(2, item -> (!item.getType().equals("Passive")));
			startBound[3] = bind(2, item -> (!item.getType().equals("Passive")));
		}
		

		// Catch uncaught exceptions
//		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
//		    System.err.println("Uncaught exception in thread " + thread.getName() + ": " + throwable);
//		    System.out.println("code crashjed");
//		    myScanner.close();
//		    questScanner.close();
////		    saveCode(); // or your save/backup logic
//		});
//		
//		// Run on shutdown (even clean ones)
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("Shutdown hook triggered");
////            saveCode();
//        }));
		
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        executor.scheduleAtFixedRate(() -> {
//            saveCode();
//        }, 1, 30, TimeUnit.SECONDS); // saves every 30 seconds
        
		System.out.println("____   ____                  .__                       ____       ______             __________        .__                                ____ ___            .___       __          \r\n"
				+ "\\   \\ /   /___________  _____|__| ____   ____   ___  _/_   |     /  __  \\            \\______   \\_____  |  | _____    ____   ____  ____   |    |   \\______   __| _/____ _/  |_  ____  \r\n"
				+ " \\   Y   // __ \\_  __ \\/  ___/  |/  _ \\ /    \\  \\  \\/ /|   |     >      <    ______   |    |  _/\\__  \\ |  | \\__  \\  /    \\_/ ___\\/ __ \\  |    |   /\\____ \\ / __ |\\__  \\\\   __\\/ __ \\ \r\n"
				+ "  \\     /\\  ___/|  | \\/\\___ \\|  (  <_> )   |  \\  \\   / |   |    /   --   \\  /_____/   |    |   \\ / __ \\|  |__/ __ \\|   |  \\  \\__\\  ___/  |    |  / |  |_> > /_/ | / __ \\|  | \\  ___/ \r\n"
				+ "   \\___/  \\___  >__|  /____  >__|\\____/|___|  /   \\_/  |___| /\\ \\______  /            |______  /(____  /____(____  /___|  /\\___  >___  > |______/  |   __/\\____ |(____  /__|  \\___  >\r\n"
				+ "              \\/           \\/               \\/               \\/        \\/                    \\/      \\/          \\/     \\/     \\/    \\/            |__|        \\/     \\/          \\/ ");
		System.out.println("99. test special");
		System.out.println("0. print the menu again");
		System.out.println("---------------");
		System.out.println("1. set shop privilege");
		System.out.println("2. create a new shop");
		System.out.println("3. open the shop");
		System.out.println("4. purchase an item from shop");
		System.out.println("5. refresh shop");
		System.out.println("6. view tier prices");
		System.out.println("7. purchase specific dice face");
		System.out.println("8. view all dice");
		System.out.println("9. generate quest board");
		System.out.println("10. easily print a quest");
		System.out.println("---------------");
		System.out.println("11. generate random number 144");
		System.out.println("12. calculate distance");
		System.out.println("---------------");
		System.out.println("13. gen chest loot");
		System.out.println("14. gen THEMED chest loot");
		System.out.println("15. gen all Tier items");
		System.out.println("---------------");
		System.out.println("16. inquire an item");
		System.out.println("17. html for most recent itemTemp");
		System.out.println("18. what to do in turn");
		System.out.println("---------------");
		System.out.println("19. generate starter items");
		System.out.println("20. print the starter items");
		System.out.println("21. refresh a specific item");
		System.out.println("22. Clipboard the starting items");
		System.out.println("---------------");
		System.out.println("23. print all artifacts");
		System.out.println("24. Clipboard all artifacts");
		System.out.println("25. generate tutorial items");
		
		
		while (true) { // menu loop
			intInput = myScanner.nextInt();
			
			if (intInput == 99) { // do something specific
				Item[] itemArray = new Item[3];
				
				multiItemGen(itemArray, singleGen, 3, null);
				
				/* 2 solutions:
				 * @ multiItemGen(itemArray, 3);
				 * ok here's an itemArray, oh 3? oh right that
				 * obviously means singleItemGen
				 * 
				 * requires: (VERY ANNOYING)
				 * multiItemGen(Item[] itemArray, int itemTier)
				 * multiItemGen(Item[] itemArray, int[] weightedProb)
				 * 
				 * @ BoundItemGen boundTemp = bind(3); // represents a singleItemGen of 3
				 * @ multiItemGen(itemArray, boundTemp)
				 * boundTemp is a wrapper class which can contain either an
				 * itemTier or weightedProb.
				 * 
				 * To run multiItemGen, you pass an itemArray and "something"
				 * called BoundItemGen, but each BoundItemGen contains an generateItem()
				 * which has 2 types:
				 * generateItem(T pattern) -- goes to --> singleItemGen(specific pattern)
				 * generateItem(T pattern, Predicate<Item> parameter) -- goes to --> customItemGen(specific pattern, parameter)
				 *
				 * @ multiItemGen(itemArray, singleGen(class), 3, null)
				 * This says to take singleGen, and do singleGen.test(3, null)
				 * which then automatically points to singleItemGen(3)
				 * 
				 * the 1st method requires lots of multiItemGen overloading which
				 * is inconvenient
				 * 
				 * the 2nd method is good for presets of how to generate items
				 * (chest, shop) but doing a unique item generation is ANNOYING as you have
				 * to make a new class of BoundItemGen for 1 specific unique usage.
				 * 
				 * the 3rd method is lengthy, but in terms of the code, there's really
				 * only 1 multiItemGen (no overloading), where ItemGenMethod takes
				 * care of everything.
				 * 
				 * The 2 best methods is a combination of 2nd and 3rd, since 2nd is convenient
				 * for item generations that DO NOT CHANGE, while the 3rd is like programmable.
				 * 
				 * Fundamentally, this code runs by either having ItemGenMethod OR generateItem()
				 * route the pattern/parameter manually, where both BoundItemGen and ItemGenMethod
				 * just do a "runGenerate()" to make the item. In this case, ItemGenMethod uses
				 * both (pattern, parameter) as its parameters, while BoundItemGen uses
				 * runGenerate() where the bind() process is EITHER bind(pattern) or bind(pattern, parameter)
				 * 
				 * Also generateItem is overloaded to have 2 methods, with 4 outcomes total:
				 * customItemGen() with int and int[]
				 * singleItemGen() with int and int[]
				 * 
				 */
				
				for (Item element : itemArray) {
					element.fullPrint();
				}
				
//				chestOpen(3, 0, customGen, item -> (!item.getType().equals("Artifact")));
//				chestOpen(3, new int[]{20,20,60,20,0}, customGen, item -> (item.getVersion().equals("v1.1") && !item.getType().equals("Artifact")));
//				System.out.println(ANSI_T1 + "[Tier 1]");
				
			
			}
			
			else if (intInput == 0) {
				System.out.println("99. test special");
				System.out.println("0. print the menu again");
				System.out.println("---------------");
				System.out.println("1. set shop privilege");
				System.out.println("2. create a new shop");
				System.out.println("3. open the shop");
				System.out.println("4. purchase an item from shop");
				System.out.println("5. refresh shop");
				System.out.println("6. view tier prices");
				System.out.println("7. purchase specific dice face");
				System.out.println("8. view all dice");
				System.out.println("9. generate quest board");
				System.out.println("10. easily print a quest");
				System.out.println("---------------");
				System.out.println("11. generate random number 144");
				System.out.println("12. calculate distance");
				System.out.println("---------------");
				System.out.println("13. gen chest loot");
				System.out.println("14. gen THEMED chest loot");
				System.out.println("15. gen all Tier items");
				System.out.println("---------------");
				System.out.println("16. inquire an item");
				System.out.println("17. html for most recent itemTemp");
				System.out.println("18. what to do in turn");
				System.out.println("---------------");
				System.out.println("19. generate starter items");
				System.out.println("20. print the starter items");
				System.out.println("21. refresh a specific item");
				System.out.println("22. Clipboard the starting items");
				System.out.println("---------------");
				System.out.println("23. print all artifacts");
				System.out.println("24. Clipboard all artifacts");
				System.out.println("25. generate tutorial items");
			} // input 0
			
			if (intInput == 1) { // set shop privilege
				System.out.println("input the tier privilege:");
				shopTier = myScanner.nextInt();
				System.out.println("remember to create a store now");
				
//				// update the shopSize the moment Shop Tier is bought/upgraded
//				if (shopTier == 1) { // tier 1 shop
//					shopSize = 5;
//				}
//				else if (shopTier == 2) { // tier 2 shop
//					shopSize = 6;
//				}
//				else if (shopTier == 3) {
//					shopSize = 7;
//				}
////				else { // tier 0 shop
////					// do NOT change the shopSize
////					
////					//accessing tier 0 shop would be set Tier to 1-3, and then 0 AFTER
////				}
			} // input 1
			
			else if (intInput == 2) { // create a new shop
				discountPresent = true; // re-add the discount for last item
				multiItemGen(shopList, shopBound[shopTier]);
				
//				multiItemGen(shopSize, shopList, weightedShopArray[shopTier], singleGen, null); // fill the shopList with items
				refreshCounter = 1; // set the initial refresh cost
				System.out.println("shop successfully created");
			} // input 2
			
			else if (intInput == 3) { // open a new shop
				// Next: Print the shop items
				System.out.println("\r\n"
						+ "   __    __  .__              _   /\\         .__                     __   \r\n"
						+ "  / /  _/  |_|  |__   ____   / \\ / /    _____|  |__   ____ ______    \\ \\  \r\n"
						+ " / /   \\   __\\  |  \\_/ __ \\  \\_// /_   /  ___/  |  \\ /  _ \\\\____ \\    \\ \\ \r\n"
						+ " \\ \\    |  | |   Y  \\  ___/    / // \\  \\___ \\|   Y  (  <_> )  |_> >   / / \r\n"
						+ "  \\_\\   |__| |___|  /\\___  >  / / \\_/ /____  >___|  /\\____/|   __/   /_/  \r\n"
						+ "                  \\/     \\/   \\/           \\/     \\/       |__|           \r\n"
						+ "");
				
				System.out.println("⋘ ─────────────────────── < ($) > ─────────────────────── ⋙");
				
				// print each Item in ShopList in Shop format
				fullPrintShop(shopList, discountPresent, DISCOUNTRate);
				
				// Next: Print the Refresh cost
				System.out.println("Refresh Cost: " + fib(refreshCounter - 1));
				
			} // input 3
			
			else if (intInput == 4) { // purchase an item from shop
				System.out.println("which item: ");
				intInput = myScanner.nextInt();
				
				// get the respective item
				try { // print out the purchased item
					itemTemp = shopList[intInput - 1];		
					System.out.println("-- Thank you for your purchase ~ --");
					itemTemp.fullPrint();
					
					// switch the old purchased item index for a new item
					shopList[intInput - 1] = noDupeItemGen(shopList, shopBound[shopTier]);
				} catch (Exception e) {
					System.out.println("Unfortunately that item isn't available.");
				}
				
				// if the FIRST item was purchased
				if (intInput == 1) {
					// the 50% discount has been used up
					discountPresent = false;
				}
				
			} // input 4
			
			else if (intInput == 5) { // refresh the shop
				System.out.println("Shop has been REFRESHED");
				multiItemGen(shopList, shopBound[shopTier]); // fill the shopList with items
				refreshCounter++;
				discountPresent = true;
			} // input 5
			
			else if (intInput == 6) { // view extra tier prices
				System.out.println("--- Shop Tier 1 ---");
				System.out.println("All players start here");
				System.out.println("Shop:");
				System.out.println("- 92% for T1 items");
				System.out.println("- 8% for T2 items");
				System.out.println("Access to D2, D4, D6 and D8");
				System.out.println("Higher chance for T1 Quests");
				System.out.println();
				System.out.println("--- Shop Tier 2 (15 Gold) ---");
				System.out.println("Shop:");
				System.out.println("- 46% for T1 items");
				System.out.println("- 46% for T2 items");
				System.out.println("- 8% for T3 items");
				System.out.println("Access to D10 and D12");
				System.out.println("Higher chance for T2 Quests");
				System.out.println();
				System.out.println("--- Shop Tier 3 (30 Gold)---");
				System.out.println("Shop:");
				System.out.println("- 31% for T1 items");
				System.out.println("- 31% for T2 items");
				System.out.println("- 31% for T3 items");
				System.out.println("- 6% for Artifacts items");
				System.out.println("Access to D20");
				System.out.println("Higher chance for T3 Quests");
				System.out.println();
				
			} // input 6
			
			else if (intInput == 7) { // purchase specific dice face
				System.out.println("Input a specific Dice Face: ");
				intInput = myScanner.nextInt();
				System.out.println(intInput + " face = " + 2 * intInput + " gold");
				System.out.println("-" + intInput + " face = " + intInput + " gold");
				System.out.println(intInput + 0.5 + " face = " + intInput * 3 + " gold");
			} // input 7
			
			else if (intInput == 8) { // purchase a dice
				System.out.println("--- Tier 1 required ---");
				System.out.println("D2: 20 gold");
				System.out.println("D4: 20 gold");
				System.out.println("D6: 20 gold");
				System.out.println("D8: 25 gold");
				System.out.println("--- Tier 2 required ---");
				System.out.println("D10: 30 gold");
				System.out.println("D12: 40 gold");
				System.out.println("--- Tier 3 required ---");
				System.out.println("D20: 50 gold");
				System.out.println("--- Extra modifications ---");
				System.out.println("Dice Modification (Impossibility): 12 Gold");
				System.out.println("Dice Modification (Weighted): 12 Gold");
				System.out.println("Dice Face Removal (Returns a Dice Face as an Item): 10 Gold ");
			} // input 8
			
			else if (intInput == 9) { // generate quests
				
				try { // questProbTemp holds the questT#Prob from questProbList
					questProbTemp = questProbTier[shopTier];
				} catch (Exception e) { // if something screws up, use questT0Prob as fallback
					questProbTemp = questProbTier[0];
				}
				
				
				// Goal: FIRST Generate 4 Unique Quests
				
				// generate the VERY FIRST quest (bypass no dupe Quest gen)
				questTierTemp = weightedProb(questProbTemp); // generate a random quest's TIER
				
				// choose a random quest based on the chosen TIER
				questTemp = questTierList[questTierTemp].get(randGen.nextInt(questTierList[questTierTemp].size())); // get the random associated quest based on winning quest tier
				questBoard[0] = questTemp; // store the first quest in questBoard
				
				// Next:
				for (int i = 1; i < questBoard.length; i++) { // fill the rest of the quests
					do { // repeat until there's no dupes
						yesDupes = false; // state that there's no dupes first
						
						// generate a random quest
						questTierTemp = weightedProb(questProbTemp); // generate the random winning quest tier
						questTemp = questTierList[questTierTemp].get(randGen.nextInt(questTierList[questTierTemp].size())); // get the random associated quest based on winning quest tier
						

						// check if there's dupes
						for (int j = 0; j < i; j++) { // iterate through questBoard
							if (questBoard[j] == questTemp) { // if questTemp is a dupe
								yesDupes = true; // there are dupes so continue generating
								break; // end the for loop and generate another quest
							}
						}
						
						
					} while (yesDupes); // use do while, assume there's no dupes first and confirm in the loop itself
					
					// if there are no dupes
					questBoard[i] = questTemp; // set the next questBoard index as unique Quest
					
					
				} // for loop
				// Now: questBoard has 4 Unique Quests
				
				// Next: SECOND Create the 4 Rewards for each Quest AND print them
				
				System.out.println("\r\n"
						+ "   ,----.            ,-----.      ___    _     .-''-.     .-'''-. ,---------.          _______       ,-----.       ____    .-------.     ______             .----,    \r\n"
						+ "   )  ,-'          .'  .-,  '.  .'   |  | |  .'_ _   \\   / _     \\\\          \\        \\  ____  \\   .'  .-,  '.   .'  __ `. |  _ _   \\   |    _ `''.         `-,  (    \r\n"
						+ "  / _/_           / ,-.|  \\ _ \\ |   .'  | | / ( ` )   ' (`' )/`--' `--.  ,---'        | |    \\ |  / ,-.|  \\ _ \\ /   '  \\  \\| ( ' )  |   | _ | ) _  \\          _\\_ \\   \r\n"
						+ " / ( ` )         ;  \\  '_ /  | :.'  '_  | |. (_ o _)  |(_ o _).       |   \\           | |____/ / ;  \\  '_ /  | :|___|  /  ||(_ o _) /   |( ''_'  ) |         ( ' ) \\  \r\n"
						+ ") (_{;}_)        |  _`,/ \\ _/  |'   ( \\.-.||  (_,_)___| (_,_). '.     :_ _:           |   _ _ '. |  _`,/ \\ _/  |   _.-`   || (_,_).' __ | . (_) `. |        (_{;}_) ( \r\n"
						+ " \\ (_,_)         : (  '\\_/ \\   ;' (`. _` /|'  \\   .---..---.  \\  :    (_I_)           |  ( ' )  \\: (  '\\_/ \\   ;.'   _    ||  |\\ \\  |  ||(_    ._) '         (_,_) /  \r\n"
						+ "  \\  \\            \\ `\"/  \\  )  \\| (_ (_) _) \\  `-'    /\\    `-'  |   (_(=)_)          | (_{;}_) | \\ `\"/  \\  ) / |  _( )_  ||  | \\ `'   /|  (_.\\.' /            /  /   \r\n"
						+ "   )  `-.          '. \\_/``\"/)  )\\ /  . \\ /  \\       /  \\       /     (_I_)           |  (_,_)  /  '. \\_/``\".'  \\ (_ o _) /|  |  \\    / |       .'          .-`  (    \r\n"
						+ "   `----'            '-----' `-'  ``-'`-''    `'-..-'    `-...-'      '---'           /_______.'     '-----'     '.(_,_).' ''-'   `'-'  '-----'`            '----`    \r\n"
						+ "                                                                                                                                                                      \r\n"
						+ "");
				System.out.println(bonusSpace.substring(6) + "┍━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━»•» 🌸 «•«━━━━━┑");
				
				
				for (int i = 0; i < 4; i++) { // for each Quest
					if (i != 0) { // don't print the line break at beginning
						System.out.println(bonusSpace + "         " + "•❅────────────────────────✧❅✦❅✧────────────────────────❅•");
					}
					
					questTierTemp = questBoard[i].getTier(); // hold the questTier
					questDescTemp = questBoard[i].getDesc(); // hold the questDesc
					
					weightedTemp = weightedProb(questRewardProb[questTierTemp]); // holds the index of quest's Reward
					questReward[i] = questRewardList[questTierTemp][weightedTemp]; // get the quest's Reward based on index
					
					// Next: Add bonus info based on Description
					/* X Item: Scavenger Hunt
					 * X/Y Space: Movement
					 */
					
					// Next: Add bonus info based on Reward
					if (questReward[i].contains("Gold")) { // if it's gold reward
						// find the index of the space and '-'		
						bonusReward[i] = Integer.toString(randGen.nextInt( // generate random int
								Integer.parseInt(questReward[i].substring(0,questReward[i].indexOf('-'))),
								Integer.parseInt(questReward[i].substring(questReward[i].indexOf('-') + 1, questReward[i].indexOf(' '))
								)));
					} // if gold reward
					
					else if (questReward[i].contains("Random") && questReward[i].contains("Item")) { // if it's random item reward
						// find quantity of items and the tier
						quantityReward = Integer.parseInt(questReward[i].substring(0,1)); // holds the quantity of item Reward
						tierReward = Integer.parseInt(questReward[i].substring(questReward[i].indexOf('T') + 1, questReward[i].indexOf('T') + 2)); // tier of items

						// reset bonusReward as it used to contain the previous Quest's Gold bonusReward
						bonusReward[i] = "";
						
						for (int j = 0; j < quantityReward; j++) { // generate quantity number of random items
							// generate random item Rewards based on the item Tier
							if (tierReward  == 1) {
								itemTemp = singleItemGen(1);
							}
							else if (tierReward == 2) {
								itemTemp = singleItemGen(2);
							}
							else { // tier 3 item
								itemTemp = singleItemGen(3);
							}
							
							// place itemTemp in bonusReward multiple times for each Random Reward item
							bonusReward[i] += itemTemp.getName();
							
							if (j != quantityReward - 1) { // not last iteration
								bonusReward[i] += ", ";
							}
							
							// funny thing vvvv
//							itemTemp = itemIDList.get(itemTierList[Integer.parseInt(strTemp.substring(strTemp.indexOf('T') + 1, strTemp.indexOf('T') + 1))].get(randGen.nextInt(itemTierList[Integer.parseInt(strTemp.substring(strTemp.indexOf('T') + 1, strTemp.indexOf('T') + 1))].size())));
						} // for loop j
						
						
					} // else if random
					
					else if (questReward[i].contains("Artifact")) { // if it contains artifact
						itemTemp = singleItemGen(4); // generate a random artifact
						bonusReward[i] = ""; // reset bonusReward
						bonusReward[i] += itemTemp.getName(); // add Random Artifact to bonusReward
						
					}
					// Now: bonusReward now contains extra information based on questReward
					
					
					// Next: Modify the quest description if needed
					if (questDescTemp.contains("Space X")){ // if description itself is related to Space X
						questDescTemp = questDescTemp.substring(0,questDescTemp.indexOf("X")) + (randGen.nextInt(144) + 1); // get rid of the "X"
					}
					// Now: questDesc has been overwritten
					
					
					
					// Next: Display the full information of the Quests and bonus Info
					System.out.println();
					System.out.println(bonusSpace + "Name: " + questBoard[i].getName() + " (Tier " + questBoard[i].getTier() + ")");
					System.out.println(bonusSpace + "Description: ");
					System.out.println(bonusSpace + "       " + "- " + questDescTemp);
					
					if (questReward[i].contains("Gold") || questReward[i].contains("Random")) { // if there's a bonus reward
						System.out.println(bonusSpace + "Reward: " + questReward[i] + " (" + bonusReward[i] + ")");
					}
					
					else { // otherwise print it normal
						System.out.println(bonusSpace + "Reward: " + questReward[i]);
					}
					
					System.out.println(bonusSpace + "Cost: " + (questBoard[i].getTier() * 5));
					System.out.println();
//					bonusReward = ""; // reset the bonus reward
					// Now: a Quest has been fully printed
					
				} // for loop i
				
				System.out.println(bonusSpace.substring(6) + "┕━━━━━»•» 🌸 «•«━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┙");
				
			} // input 9
			
			else if (intInput == 10) { // cleanly print a selected quest
				System.out.println("Which quest do you choose?: ");
				intTemp = myScanner.nextInt();
				
				questBoard[intTemp - 1].fullPrint(questReward[intTemp - 1], bonusReward[intTemp - 1]);
				
			}
			
			else if (intInput == 11) { // generate 3 random numbers from 144
				for (int i = 0; i < 3; i++) {
					System.out.println(randGen.nextInt(144) + 1);
				}
				System.out.println();
			} // input 10
			
			else if (intInput == 12) { // calculate the distance
				System.out.println("first tile?: ");
				firstTile = myScanner.nextInt();
				System.out.println("second tile?: ");
				secondTile = myScanner.nextInt();
				
				calculateDistance(firstTile, secondTile);
			}// input 11
			
			else if (intInput == 13) { // generate chest drops
				System.out.println("How many chest items?: ");
				intTemp = myScanner.nextInt();
				
				/* multiItemGen(custom array length, weightedProb, singleItemGen)
				 * except 
				 * 
				 * maybe instead, make a new Item[intTemp] inside the parameter
				 * itself, then it'll return the same Item[] and i do things with it?
				 * 
				 * or maybe use an ArrayList lol
				 */
				
				
				chestOpen(intTemp, chestBound);
				// turn this into lambda
//				for (Item chestLoot : ) {
//					chestLoot.fullPrint();
//				}
			} // else if input 12
			
			else if (intInput == 14) { // generate THEMED chest loot (UPDATE THIS)
				System.out.println("how many items?: ");
				intTemp = myScanner.nextInt();
				
				myScanner.nextLine(); // refresh myScanner
				System.out.println("choose a Type: ");
				
				final String TempParameter = myScanner.nextLine(); // make brand new final variable
				
				System.out.println("chestTotal: " + intTemp);
				System.out.println("item Theme: " + TempParameter);
				
				/* problem:
				 * ItemThemes are now String[], not String
				 * 
				 */
				
				// NOTE: strTempFinal[0] instead of strTemp is probably a RACE condition FIX
				chestOpen(intInput, chestProb, customGen, item -> (
						Arrays.stream(item.getTheme()).anyMatch(theme -> theme.equals(TempParameter)
							
						
						)
						
						));
				
				/*(
						
						for (String theme : TempParameter) {
							if (item.getTheme().equals(TempParameter)) {
								return true;
							}
						}
						
						return false;
						
						));
				 * 
				 */
				
			} // input 13
			
			else if (intInput == 15) {
				for (int i = 0; i < 4; i++) {
					(itemTemp = singleItemGen(i)).fullPrint();
				}
			}
			
			else if (intInput == 16) { // inquire an item
				myScanner.nextLine(); // refresh myScanner
				System.out.println("type an item id: ");
				strInput = myScanner.nextLine().toLowerCase();
				
				try {
					// FIX THE COMPLEXITY DATABSE LATER
					itemTemp = itemDatabase.get(strInput);
//					itemTemp = itemNameList.get(strInput); // if this messes up, it equal null
					itemTemp.fullPrint();
				} catch (Exception e){
					System.out.println("No such item exists.");
				}

			} // input 15
			
			else if (intInput == 17) { // save html format to Clipboard (for google docs)
//				root.clear(); // reset the root
//				
//		        /* Create a data-model */
//		        root.put("ItemName", itemTemp.getName()); // variable name, contents
//		        root.put("ItemTier", itemTemp.getTier());
//		        root.put("ItemTierColor", HEX_ARRAY[itemTemp.getTier()]);
//		        root.put("ItemCost", itemTemp.getCost());
//		        root.put("ItemDesc", itemTemp.getDesc());
//		        root.put("ItemRange", itemTemp.getRange());
//		        root.put("ItemType", itemTemp.getType());
//		        root.put("ItemTypeColor", HEX_THEME.get(itemTemp.getType()));
//		        root.put("ItemTheme", itemTemp.getTheme());
//		        root.put("ItemThemeColor", HEX_THEME.get(itemTemp.getTheme()));
//		        root.put("ItemVersion", itemTemp.getVersion());
//		        
//		        temp.process(root, out); // put this into StringWriter
				
				try {
					System.out.println("PRINT PLAIN:");
					System.out.println(itemTemp.getPlain());
					
					System.out.println("PRINT HTML:");
					System.out.println(itemTemp.getHtml());
					
					itemTransTemp.setHtml(itemTemp.getHtml());
					itemTransTemp.setPlain(itemTemp.getPlain());
					
					clipboard.setContents(itemTransTemp, null);
					System.out.println("successfully copied to clipboard");
				} catch (Exception e) {
					System.out.println("Something went WRONG with the clipboard print.");
					e.printStackTrace();
				}
				
				
//				System.out.println("second");
//				System.out.println("Item Name: Grenade[38;2;177;208;164m [Tier 1][0m\r\n"
//						+ "- Cost: 8\r\n"
//						+ "- Description: \r\n"
//						+ "	- Throw a Grenade which Explodes 3x3 at the Start of your Next Turn.\r\n"
//						+ "	- All Players who are inside the Explosion Lose 15 Gold.\r\n"
//						+ "- Range: Line of Sight 7, All\r\n"
//						+ "- Type: Item\r\n"
//						+ "- Theme: Steal/Creation\r\n"
//						+ "- Version: v1.2");
				
				
				
				
			}
			
			else if (intInput == 18) { // possible things to do on a turn
				System.out.println("Open the Shop\r\n"
						+ "- Buy Dice\r\n"
						+ "- Inventory Upgrade\r\n"
						+ "- Shop Tier (15 Gold, 30 Gold)\r\n"
						+ "- Access the Yard Sale (requires Yard Sale Permit)\r\n"
						+ "- Upgrade Inventory 15 Gold (max 2 times for 7 total)\r\n"
						+ "\r\n"
						+ "Upgrade Artifact Tier\r\n"
						+ "- Artifact T2 = 15 Gold\r\n"
						+ "- Artifact T3 = 30 Gold");
			} // input 10
			
			else if (intInput == 19) { // generate ALL start items
				
				for (int i = 0; i < 3; i++) { // for each player
					// generate a custom item in startArray
					startArray[i] = multiItemGen(startArray[i], startBound);
					
					
					// for each startArray[player X] Item, call customItemGen(pattern, parameter) to generate a new item
//					startArray[i][0] = customItemGen(1, item -> (!item.getType().equals("Passive")));
//					startArray[i][1] = customItemGen(1, item -> (!item.getType().equals("Passive")));
//					startArray[i][2] = customItemGen(2, item -> (!item.getType().equals("Passive")));
//					startArray[i][3] = customItemGen(2, item -> (!item.getType().equals("Passive")));
				}
				System.out.println("Successfully generated all starting items.");
				
			} // input 15
			
			else if (intInput == 20) { // print the starting items
				System.out.println("which player? (0 for all)");
				try {
					intTemp = myScanner.nextInt(); // intTemp usually holds the player (not 0 index)
				} catch (Exception e) {
					System.out.println("You didn't type a valid Player, try again.");
				}
				
				if (intTemp == 0) { // print every player
					for (int i = 0; i < 3; i++) { // for each player
						System.out.println("--- PLAYER " + (i + 1) + "'s Starting Inventory ---");
						for (int j = 0; j < 4; j++) { // for each player's inventory
							if (startRefreshed[i][j]) { // can be refreshed
								System.out.println("(CANNOT be refreshed)");
							}
							else {
								System.out.println("(Can be refreshed)");
							}
							startArray[i][j].fullPrint();
						}
					}
				}
				else { // print a specific player
					System.out.println("--- PLAYER " + (intTemp) + "'s Starting Inventory ---");
					intTemp--;
					for (int i = 0; i < 4; i++) {
						if (startRefreshed[intTemp][i]) { // can be refreshed
							System.out.println("(CANNOT be refreshed)");
						}
						else {
							System.out.println("(Can be refreshed)");
						}
						startArray[intTemp][i].fullPrint();
					}
				}
			}
			
			else if (intInput == 21) { // refresh a starter item
				try {
					System.out.println("Which player?: ");
					intTemp = myScanner.nextInt(); // player (1-3 index)
					
					System.out.println("Which item slot?: ");
					intTemp2 = myScanner.nextInt(); // item slot (1-4 index)
				} catch (Exception e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine(); // reset in case String typed so doesn't break nextInt()
					continue;
				}
				
				
				
				if (!startRefreshed[intTemp - 1][intTemp2 - 1]) { // if you can refresh it (true)
					intTemp--; // make it 0 indexed (intTemp = 1 means player 1 at index no.0)
					intTemp2--;
					
					if (intTemp2 == 0 || intTemp2 == 1) { // if it item no.1 or 2
						System.out.println("refreshing T1");
						// generate a T1 which is impossible to be a Passive
						itemTemp = noDupeItemGen(startArray[intTemp], 1, customGen, item -> (!item.getType().equals("Passive")));
					}
					else {
						// generate a T2 that's NOT a Passive
						System.out.println("refreshing T2");
						itemTemp = noDupeItemGen(startArray[intTemp], 2, customGen, item -> (!item.getType().equals("Passive")));
					}
					startRefreshed[intTemp][intTemp2] = true; // set startRefreshed to true as item was refreshed
					
					startArray[intTemp][intTemp2] = itemTemp; // replace the old refreshed item
					
					System.out.println("(CANNOT be refreshed)");
					itemTemp.fullPrint(); // print the new item
					
					// copy the item WITH the (refresh) into clipboard
					strTemp = "<html><body>" + "(CANNOT be refreshed)" + "</body></html>\n" + itemTemp.getHtml();
					itemTransTemp.setHtml(strTemp);
					
					strTemp = "(CANNOT be refreshed)\n" + itemTemp.getPlain();
					itemTransTemp.setPlain(strTemp);
					
//					System.out.println("PLAIN------------");
//					System.out.println(itemTransTemp.getPlain());
//					
//					System.out.println("HTML------------");
//					System.out.println(itemTransTemp.getHtml());
					
					clipboard.setContents(itemTransTemp, null);
					System.out.println("Successfully copied to clipboard.");
					
				}
				else { // otherwise you've already refreshed (false)
					System.out.println("Player " + intTemp +  " has already refreshed item no." + intTemp2);
				}
			}
			
			else if (intInput == 22) { // clipboard the Starter items
				System.out.println("Which player?: ");
				intInput = myScanner.nextInt() - 1;
				
				System.out.println("Type something to include the (refresh status): ");
				myScanner.nextLine();
				strInput = myScanner.nextLine();
				
				/* strTemp holds
				 * strTemp2 holds
				 * 
				 * 
				 */
				
				// reset strTemp
				strTemp = "";
				strTemp2 = "";
				
				
				// for every starting item
				if (strInput.isEmpty()) { // copy WITHOUT the (refresh)
					// store every Item's html in strTemp
					for (Item element : startArray[intInput]) { // for every Item in startArray[player]
						element.fullPrint();
						strTemp += element.getHtml(); // store Item.getHtml() in strTemp2
						strTemp2 += element.getPlain() + "\n";
					}
					
				} // if copy without (refresh status)
				
				else { // copy with the (refresh)
					String refreshHoldPlain;
					String refreshHoldHtml;
					
					for (int i = 0; i < startArray[intInput].length; i++) {
						itemTemp = startArray[intInput][i];
						
						if (startRefreshed[intInput][i]) {
							System.out.println("(CANNOT be refreshed)");
							refreshHoldHtml = "<html><body>" + "(CANNOT be refreshed)" + "</body></html>\n";
							refreshHoldPlain = "(CANNOT be refreshed)";
						}
						else {
							System.out.println("(Can be refreshed)");
							refreshHoldHtml = "<html><body>" + "(Can be refreshed)" + "</body></html>\n";
							refreshHoldPlain = "(Can be refreshed)";
						}
						itemTemp.fullPrint();
						
						// strTemp holds both the Item Html AND (refresh status)
						strTemp += refreshHoldHtml + itemTemp.getHtml();
						
						// strTemp2 holds both the Item Plain AND (refresh status)
						strTemp2 += refreshHoldPlain + itemTemp.getPlain() + "\n";
					}
				} // else copy with (Refresh status)
				
				
				
				itemTransTemp.setPlain(strTemp2);
				itemTransTemp.setHtml(strTemp);
				clipboard.setContents(itemTransTemp, null);
				System.out.println("Successfully added all Starting items to plain.");
			}
			
			else if (intInput == 23) { // print artifacts
				itemNameList.values().forEach(value -> {
					if (value.getTier() == 4) {
						value.fullPrint();
					}
				});
				
			} // input 16
			
			else if (intInput == 24) { // copy all Artifacts to Clipboard
				// reset strTemp
				strTemp = "";
				strTemp2 = "";
				
				// loop through every Artifact
				for (Item element : itemNameList.values()) {
					if (element.getTier() == 4) {
						
						element.fullPrint();
						// repeatedly add the Plain
						strTemp += element.getHtml();
						strTemp2 += element.getPlain() + "\n";
					}
				}
				
				itemTransTemp.setPlain(strTemp2);
				itemTransTemp.setHtml(strTemp);
				
				clipboard.setContents(itemTransTemp, null);
				System.out.println("Successfully added all Artifacts to Clipboard.");
			} // else if
			
			else if (intInput == 25) { // generate tutorial items
				
			}
			
		}// while loop

	} // main
	
	/* An Object which contains the String format of an html and plain text, as
	 * well as an array which has labels.
	 * 
	 * The ItemTransfer class is really only used for importing html and plain
	 * text to the clipboard.
	 * 
	 */
	
	static class ItemTransfer implements Transferable {
		
//		Transferable.getTransferDataFlavors()
//		TTransferable.getTransferData(DataFlavor)
//		TTransferable.isDataFlavorSupported(DataFlavor)
		
		// instance variables
		private static final DataFlavor[] flavors = {
			DataFlavor.stringFlavor,
            new DataFlavor("text/html;class=java.lang.String", "HTML Format")
		};
		
		private String plain;
		private String html;
		
		public ItemTransfer(String html, String plain) {
            this.html = html;
            this.plain = plain;
        }
		
		public ItemTransfer() {
			
		}
		
		// special methods
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (flavor.getMimeType().startsWith("text/html")) {
                return html;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return plain;
            }
            throw new UnsupportedFlavorException(flavor);
		}
		
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			for (DataFlavor f : flavors) {
				if (f.equals(flavor)) return true;
			}
			return false;
		}
		
		// getter+setter
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}
		
		public String getHtml() {
			return html;
		}
		
		public String getPlain() {
			return plain;
		}
		
		public void setHtml(String html) {
			this.html = html;
		}
		
		public void setPlain(String plain) {
			this.plain = plain;
		}
		
		
	}
	
	/* All Methods:
	 * 
	 * Specialized Methods:
	 * chestOpen
	 * fullPrintShop
	 * 
	 * Fundamental Methods:
	 * noDupeItemGen
	 * - based on this item array, generate an item with no duplicates
	 * multiItemGen
	 * - fill up item array with generated items, no duplicates
	 * 
	 * Important Methods:
	 * customItemGen
	 * - generate item but make sure it's this
	 * singleItemGen
	 * - generate item
	 * weightedProb
	 * 
	 * Other:
	 * calulateDistance
	 * parameterCounter
	 * parameterCounterReset
	 * ansiToRGB
	 * 
	 */
	
	
	/* Function where you put in how many items you want in the chest (chestTotal)
	 * and runs multiItemGen for you while returning a brand new Item[]
	 * 
	 * @param int chestTotal
	 * How many chest items are made.
	 * 
	 * @param T pattern
	 * Generates the random item based off of the pattern,
	 * either the weightedProbability or a single itemTier.
	 * 
	 * @param ItemGenMethod<T> itemGen
	 * The method of generating an item.
	 * 
	 * @param Predicate<Item> parameter
	 * If using customItemGen, what the parameters are for
	 * generating the item.
	 * 
	 */
	
	public static <T> Item[] chestOpen(int chestTotal, BoundItemGen chestBound) {
		Item[] chestArray = new Item[chestTotal];
		multiItemGen(chestArray, chestBound);
		
		System.out.println("CHEST OPENING TIME");
		for (int i = 0; i < chestArray.length; i++) { // display all the items
			chestArray[i].fullPrint();
		}
		
		return chestArray;
	}
	
	public static <T> Item[] chestOpen(int chestTotal, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> parameter) {
		Item[] chestArray = new Item[chestTotal];
		multiItemGen(chestArray, itemGen, pattern, parameter);
		
		System.out.println("CHEST OPENING TIME");
		for (int i = 0; i < chestArray.length; i++) { // display all the items
			chestArray[i].fullPrint();
		}
		
		return chestArray;
	}
	
	/* A method which is similar to fullPrint() but
	 * specialized for the Shop format, like having a discount
	 * 
	 * @param shopList
	 * Array which holds all the Items in the Shop.
	 * 
	 * @param boolean discountPresent
	 * Whether the Discount item was bought before the
	 * Shop has reset, and either does or doesn't print the
	 * DISCOUNT tag on the 1st item.
	 * 
	 * @param double discountRate
	 * How much the Shop Discount item NOW costs.
	 * 
	 */
	
	public static void fullPrintShop(Item[] shopList, boolean discountPresent, double discountRate) {
		Item itemTemp;
		
		for (int i = 0; i < shopList.length; i++) { // for each item
			itemTemp = shopList[i]; // store the current shopList item within itemTemp
			
			// print item and cost with discount (1st item always discounted)
			if (i == 0 && discountPresent) { // if the FIRST  discounted item is purchased
				System.out.println("Item (" + (i + 1) + "): " + itemTemp + "(DISCOUNT)"  + ANSI_TIER[itemTemp.getTier()] + " [Tier " + itemTemp.getTier() + "]" + ANSI_RESET);
				System.out.println("- Cost: " + (int)(itemTemp.getCost()*discountRate) + " originally (" + itemTemp.getCost()+")");
			}
			else {
				System.out.println("Item (" + (i + 1) + "): " + itemTemp + ANSI_TIER[itemTemp.getTier()] + " [Tier " + itemTemp.getTier() + "]" + ANSI_RESET);
				System.out.println("- Cost: " + itemTemp.getCost());
			}
			
			// print description header
			System.out.println("- Description: ");
			
			for (String ItemDescLine : itemTemp.getDesc()) {
				System.out.println("\t- " + ItemDescLine);
			}
			
			// print range and type
			System.out.println("- Range: " + itemTemp.getRange());
			System.out.println("- Type: " + ANSI_THEME.get(itemTemp.getType()) + itemTemp.getType() + ANSI_RESET);
			
			// print Theme with COLORS
			System.out.print("- Theme: ");
			
			for (int j = 0; j < itemTemp.getTheme().length; j++) { // go through strTemp FOR Themes
				System.out.print(ANSI_THEME.get(itemTemp.getTheme()[j]) + itemTemp.getTheme()[j] + ANSI_RESET); // print the colored Theme
				
				if (j != itemTemp.getTheme().length - 1) { // if it's not the last Theme, print the dividing bracket
					System.out.print(" / ");
				}	
				
			}
			
			System.out.println(); // conclude the line
			
			// print Version and Complexity
			System.out.println("- Version: " + itemTemp.getVersion());
//			System.out.println("- Complexity: " + itemTemp.getComplexity());
			System.out.println("⋘ ─────────────────────── < ($) > ─────────────────────── ⋙");
		} // for loop item
	}
	
	/* Method which takes an Item array and generates a new Item (based on weightedProbability)
	 * that is NOT a duplicate based on the Item array.
	 * 
	 * @param itemArray
	 * The Item array which stores all the items and also which item is missing.
	 * 
	 * @param weightedProbability
	 * Used to generate a random Item based on the weightedProbability for
	 * the Item Tier.
	 * 
	 * @secret param
	 * shopList[intInput - 1] = purchaseItem(shopList, weightedShopArray[shopTier]); // switch out the old item
	 * Usually the item that is generated is put into the same shopList/itemArray array.
	 * 
	 */
	
	public static <T> Item noDupeItemGen(Item[] itemArray, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> parameter){
		
		// generate a new Item based on shopList for no duplicates
		Item itemHolder;
		boolean yesDupes;
		
		do {
			yesDupes = false; // assume there's dupes
			
			// generate an item
//			itemHolder = singleItemGen(weightedProbability);
			
			/* customItemGen
			 * itemGen = (int, Predicate<Item>) -> customItemGen(itemTier, parameter)
			 */
			itemHolder = itemGen.runGenerate(pattern, parameter); // generate a random item but use itemGen to do so
			
			// make sure there's no duplicates (including the original item)
			for (int i = 0; i < itemArray.length; i++) { // iterate through itemArray
				if (itemHolder == itemArray[i]) { // there's a dupe
					yesDupes = true;
					break;
				}
				// otherwise yesDupes is still false
			} // for loop
			
		} while (yesDupes); // while loop
		
		return itemHolder;
	} // ARRAY
	
	public static <T> Item noDupeItemGen(Item[] itemArray, BoundItemGen boundArray){
		
		// generate a new Item based on shopList for no duplicates
		Item itemTemp;
		boolean yesDupes;
		
		do {
			yesDupes = false; // assume there's dupes
			
			// generate an item
//			itemHolder = singleItemGen(weightedProbability);
			
			
			/* customItemGen
			 * itemGen = (int, Predicate<Item>) -> customItemGen(itemTier, parameter)
			 */
			itemTemp = boundArray.runGenerate(); // generate a random item but use itemGen to do so
			
			// make sure there's no duplicates (including the original item)
			for (int i = 0; i < itemArray.length; i++) { // iterate through itemArray
				if (itemTemp == itemArray[i]) { // there's a dupe
					yesDupes = true;
					break;
				}
				// otherwise yesDupes is still false
			} // for loop
			
		} while (yesDupes); // while loop
		
		return itemTemp;
	} // ARRAY
	
//	public static <T> Item noDupeItemGen(ArrayList<Item> itemArray, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> parameter){
//		
//		// generate a new Item based on shopList for no duplicates
//		Item itemHolder;
//		boolean yesDupes;
//		
//		do {
//			yesDupes = false; // assume there's dupes
//			
//			/* customItemGen
//			 * itemGen = (int, Predicate<Item>) -> customItemGen(itemTier, parameter)
//			 */
//			
//			// generate an item
////			itemHolder = singleItemGen(weightedProbability);
//			itemHolder = itemGen.runGenerate(pattern, parameter); // generate a random item but use itemGen to do so
//			
//			// make sure there's no duplicates (including the original item)
//			for (int i = 0; i < itemArray.size(); i++) { // iterate through itemArray
//				if (itemHolder == itemArray.get(i)) { // there's a dupe
//					yesDupes = true;
//					break; // stop checking for dupes and let the while loop reset the item generation
//				}
//				// otherwise yesDupes is still false
//			} // for loop
//			
//		} while (yesDupes); // while loop
//		
//		return itemHolder; // return the NOT dupe item
//	} // ARRAY
	
	
	/*  Method which takes an Item array and fills it up
	 *  with no duplicates based on a weightedArray
	 *  
	 *  @param Item[] itemArray
	 *  Item array which stores all the generated items.
	 *  
	 *  @param T pattern
	 *  Weighted probability array where the index corresponds to the
	 *  Item Tier, and selects a random item from the selected Tier.
	 *  
	 *  @param ItemGenMethod<T> itemGen
	 *  
	 *  @param Predicate<Item> parameter
	 *  
	 *  another version uses this:
	 *  
	 *  @param itemArray
	 *  
	 *  @param boundArray
	 *  
	 *  Bound version is only used if the itemGen methods, parameters and patterns
	 *  are always going to be the same.
	 *  
	 *  Manual version is used if the itemGen methods change alot
	 */
	
	public static <T> Item[] multiItemGen(Item[] itemArray, ItemGenMethod<T> itemGen, T pattern, Predicate<Item> parameter) {
		Item itemHolder = null; // used to store items
		boolean yesDupes = true;
		
		/* Generate the first Item
		 * 
		 */
//		System.out.println("multiItemGen Array[] was called");
		
		itemArray[0] = itemGen.runGenerate(pattern, parameter);// generate the first item
//		System.out.println("first item is: " + itemArray[0]);
		
		
		for (int i = 1; i < itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe
				yesDupes = false; // assume that there's no dupes but check anyways
				itemHolder = itemGen.runGenerate(pattern, parameter); // generate a random item
//				System.out.println("generated: " + itemHolder);
				
				for (int j = 0; j < itemArray.length; j++) { // iterate through itemArray
					if (itemHolder == itemArray[j]) { // if the generated Item is a dupe
//						System.out.println("DUPE DETECTED");
						yesDupes = true; // there actually are dupes
						break; // stop checking for dupes and let the while loop reset the item generation
					}
				} // for loop
				
			} while (yesDupes); // if there's dupes, generate an Item again
			
			itemArray[i] = itemHolder; // add the new item to the next itemArray index
		}
		
		return itemArray;
		
	} // multiItemGen ARRAY
	
	public static <T> Item[] multiItemGen(Item[] itemArray, BoundItemGen[] genArray) {
		Item itemHolder = null; // used to store items
		boolean yesDupes = true;
		
		// check if itemArray and parameterArray are the same size
		
		if (itemArray.length != genArray.length) {
			throw new IllegalArgumentException(
				"itemArray (" + itemArray.length + "),"
				+ "and genArray (" + genArray.length + "),"
				+ " are differing lengths.");
		}
		
//		System.out.println("multiItemGen Array[] was called");
//		itemArray[0] = genArray[0].runGenerate();// generate the first item
//		System.out.println("first item is: " + itemArray[0]);
		
		
		for (int i = 0; i < itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe
				yesDupes = false; // assume that there's no dupes but check anyways
				itemHolder = genArray[i].runGenerate(); // generate a random item
//				System.out.println("generated: " + itemHolder);
				
				for (int j = 0; j < itemArray.length; j++) { // iterate through itemArray
					if (itemHolder == itemArray[j]) { // if the generated Item is a dupe
//						System.out.println("DUPE DETECTED");
						yesDupes = true; // there actually are dupes
						break; // stop checking for dupes and let the while loop reset the item generation
					}
				} // for loop
				
			} while (yesDupes); // if there's dupes, generate an Item again
			
			itemArray[i] = itemHolder; // add the new item to the next itemArray index
		}
		
		return itemArray;
		
	} // multiItemGen ARRAY
	
	public static <T> Item[] multiItemGen(Item[] itemArray, BoundItemGen genType) {
		Item itemHolder = null; // used to store items
		boolean yesDupes = true;
		
		// check if itemArray and parameterArray are the same size
		
		
//		System.out.println("multiItemGen Array[] was called");
		
//		itemArray[0] = genType.runGenerate();// generate the first item
//		System.out.println("first item is: " + itemArray[0]);
		
		
		for (int i = 0; i < itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe
				yesDupes = false; // assume that there's no dupes but check anyways
				itemHolder = genType.runGenerate(); // generate a random item
//				System.out.println("generated: " + itemHolder);
				
				for (int j = 0; j < itemArray.length; j++) { // iterate through itemArray
					if (itemHolder == itemArray[j]) { // if the generated Item is a dupe
//						System.out.println("DUPE DETECTED");
						yesDupes = true; // there actually are dupes
						break; // stop checking for dupes and let the while loop reset the item generation
					}
				} // for loop
				
			} while (yesDupes); // if there's dupes, generate an Item again
			
			itemArray[i] = itemHolder; // add the new item to the next itemArray index
		}
		
		return itemArray;
		
	} // multiItemGen ARRAY
	
	/* version that generates a new array instead of filling one up manually
	 * 
	 */
	
//	public static <T> void multiItemGen(int arraySize, ArrayList<Item> itemArray, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> parameter) {
//		// this version specifically is for ArrayLists, which is really only used for the shop
//		
//		Item itemHolder = null; // used to store items
//		boolean yesDupes = true;
////		System.out.println("multiItemGen ArrayList was called");
//		/* Generate the first Item
//		 * 
//		 */
//		itemArray.clear(); // clear the ArrayList BECAUSE multiItemGen is supposed to fill an array
//		itemArray.add(itemGen.runGenerate(pattern, parameter));// generate the 0th item
////		System.out.println("first item is: " + itemArray[0]);
//		
//		
//		for (int i = 1; i < arraySize; i++) { // start at 1st item, generate itemArray.length number of Items
//			do { // generate the next item and check if it's a dupe
//				yesDupes = false; // assume that there's no dupes but check anyways
//				itemHolder = itemGen.runGenerate(pattern, parameter); // generate a random item
////				System.out.println("generated: " + itemHolder);
//				
//				for (int j = 0; j < itemArray.size(); j++) { // iterate through itemArray
//					if (itemHolder == itemArray.get(j)) { // if the generated Item is a dupe
////						System.out.println("DUPE DETECTED");
//						yesDupes = true; // there actually are dupes
//						break; // stop checking for dupes and let the while loop reset the item generation
//					} // if item is dupe
//				} // for loop
//				
//			} while (yesDupes); // if there's dupes, generate an Item again
//			
//			// otherwise if the item is NOT a dupe
//			itemArray.add(itemHolder); // add the new item to the next itemArray index
//		}
//		
//	} // multiItemGen ARRAYLIST
	
	public static <T> Item generateItem(T pattern, Predicate<Item> parameter) {
		if (pattern instanceof Integer) { // generate a guaranteed Tier
			return customItemGen((int)pattern, parameter);
		}
		else if (pattern instanceof int[]){ // generate based on Probability
			return customItemGen((int[])pattern, parameter);
		}
		else {
			throw new IllegalArgumentException("generateItem customGen, Invalid pattern type: " + pattern.getClass());
		}
	}
	
	public static <T> Item generateItem(T pattern) {
		if (pattern instanceof Integer) { // generate a guaranteed Tier
			return singleItemGen((int)pattern);
		}
		else if (pattern instanceof int[]){ // generate based on Probability
			return singleItemGen((int[])pattern);
		}
		else {
			throw new IllegalArgumentException("generateItem singleGen, Invalid pattern type: " + pattern.getClass());
		}
	}
	
	/* Method which randomly generates an Item that WILL fit
	 * the criteria (Predicate).
	 * 
	 * @param itemTier/weightedProbability
	 * Based on the itemTier or weightedProbability, return a randomly
	 * generated item
	 * 
	 * @param parameter
	 * Predicate which is basically a lambda expression AKA a portable mini
	 * method, used as the requirement for what to EXCLUDE from generated item
	 */
	public static Item customItemGen(int itemTier, Predicate<Item> parameter) {
//		System.out.println("customItemGen itemTier was called");
		Item itemTemp;
		
		do {
			itemTemp = singleItemGen(itemTier);
			parameterCounter();
		} while (!parameter.test(itemTemp)); // keep generating until the parameter is met

		parameterCounterReset();
		return itemTemp;
	}
	
	public static Item customItemGen(int[] weightedProbability, Predicate<Item> parameter) {
//		System.out.println("customItemGen weightedProb was called");
		Item itemTemp;
		
		do {
			// generate a single item
			itemTemp = singleItemGen(weightedProbability);
			
			// record the item
			parameterCounter();
		} while (!parameter.test(itemTemp)); // keep generating until the parameter is NOT met
		
		parameterCounterReset();
		return itemTemp;
	}
	
	
	/* Most common method used for generating 1 single
	 * random Item based on weightedArray.
	 * 
	 * @param weightedProbability
	 * Uses the weightedArray to generate a random Item Tier and then
	 * generates a random Item that fits the Item Tier.
	 * 
	 */
	public static Item singleItemGen(int[] weightedProbability) {
//		System.out.println("singleItemGen weightedProb was called");
		Item itemHolder; // used to store items
		
		int winningTier; // the tier that won from the weightedArray
		
		/* Generate the first Item
		 * 
		 */
		
		winningTier = weightedProb(weightedProbability); // generate the winning itemTier
			
		// based on winningTier, get the corresponding Item
		itemHolder = itemTierList[winningTier].get(randGen.nextInt((itemTierList[winningTier].size())));
		
		
		return itemHolder;
	}
	
	/* Specialized version of above which is the same thing
	 * but the Item Tier is pre-picked
	 * 
	 * @param itemTier
	 * Is a number which is the Tier of Item, only goes from 0-4.
	 */
	
	public static Item singleItemGen(int itemTier) {
//		System.out.println("singleItemGen itemTier was called");
		return itemTierList[itemTier].get(randGen.nextInt((itemTierList[itemTier].size())));
	}
	
	/* Method that takes an array with various numbers and generates
	 * a winning index.
	 * 
	 * @param weightedArray
	 * Basically it adds up all the numbers in weightedArray and generates a
	 * random number. If the random number fits in the range of the weightedArray
	 * indexes, then that's the winning index which is returned.
	 * 
	 */
	
	public static int weightedProb(int[] weightedArray) {
		int denoSum = 0; // used for variable type denominators, so denominator doesn't have to add up to 100
		
		// get the total denominator
		for (int i = 0; i < weightedArray.length; i++) {
			denoSum += weightedArray[i];
		}
		
		int randomWeight = randGen.nextInt(denoSum); // generate a random number in denoSum
		int prevDeno = 0; // the previous denominator, used to check if randomWeight is in the next probability
		int winningIndex = -1; // the index that won the probability
		
//		System.out.println("randomWeight is: " + randomWeight);
		
		for (int i = 0; i < weightedArray.length; i++) { // for each element in weightedArray
			
			if (i == 0) { // for the first weight
//				System.out.println("is it between 0 and " + (weightedArray[0] - 1));
				if (0 <= randomWeight && randomWeight <= weightedArray[0] - 1 && weightedArray[0] != 0) { // 0 to lowest = low
//					System.out.println("yes");
					winningIndex = 0; // it managed to pick 0
					break;
				}
				prevDeno += weightedArray[i];
			}
			
			else { // for each subsequent weight
//				System.out.println("is it between " + prevDeno + " and " + (prevDeno + weightedArray[i] - 1));
				if (prevDeno <= randomWeight && randomWeight <= prevDeno + weightedArray[i] - 1  && weightedArray[i] != 0) {
//					System.out.println("yes");
					winningIndex = i; // keep track of the winning Tier
					break;
				}
				prevDeno += weightedArray[i];
			}
			
		} // for loop
		
		return winningIndex;
		
	}
	
	
	/* Method that calculates the distance between 2 points, ACCOUNTING
	 * for weird Snakes and Ladders board where each second Row is mirrored.
	 * 
	 * @param firstTile
	 * The Space number which is converted to a Point.
	 * 
	 * @param secondTile
	 * Also the Space number which is converted to a Point.
	 */
	
	public static void calculateDistance(int firstTile, int secondTile) {
		/* method which takes the firstTile and secondTile number and calculates the distance between them
		 * it also assumes that each row on the grid does a squiggly like a snake and not each row start on the beginning
		 * 
		 */
		int interval = 12;
		
		int maximumRow;
		int intervalCheck;
		
		// magic machine which returns 1 if divisible by interval: System.out.println(Math.abs((changes % 12) - 12)/12);
		
		// basically tells whenever it's on another row, ACCOUNTING FOR 12, 24, etc being on the same row
		int firstChange = ((firstTile / interval)  - Math.abs((firstTile % interval) - interval)/interval)%2;
		int secondChange = ((secondTile / interval) - Math.abs((secondTile % interval) - interval)/interval)%2; // subtract by 1 ONLY if it's divisible by 12
		
		// 13 = 24, 1 = 12
		// 14 = 23, 2 = 11
		// 15 = 22, 3 = 10
		
		/* 13 = 1
		 * 25 = 2
		 * 37 = 3
		 * 
		 */
		
		
		System.out.println("firstChange is: " + firstChange);
		System.out.println("secondChange is: " + secondChange);
		
		System.out.println("firstTile is: " + firstTile);
		System.out.println("secondTile is: " + secondTile);
		
		// first part
		int firstX;
		int firstY;
		
		maximumRow = ((firstTile / (interval + Math.abs((firstTile % interval) - interval)/interval%2)) + 1) * interval;
		intervalCheck = Math.abs((firstTile % interval) - interval)/interval;
		
		if (firstChange == 1) { // reversed row
			firstX = Math.abs(firstTile - maximumRow - 1); // (firstTile / interval)
		}
		else {
			firstX = firstTile - (maximumRow - interval); // maximumRow but minus 1 interval (24 - 25) instead of (36 - 25)
		}
		
		firstY = (firstTile / (interval + intervalCheck)) + 1;
		
		System.out.println("normal first: (" + firstX + "," + firstY + ")"); // regular coordinates
		
		// second part
		int secondX;
		int secondY;
		
		maximumRow = ((secondTile / (interval + Math.abs((secondTile % interval) - interval)/interval%2)) + 1) * interval;
		intervalCheck = Math.abs((secondTile % interval) - interval)/interval;
		
		if (secondChange == 1) { // reversed row
			secondX = Math.abs(secondTile - maximumRow - 1); // (firstTile / interval)
		}
		else {
			secondX = secondTile - (maximumRow - interval); // maximumRow but minus 1 interval (24 - 25) instead of (36 - 25)
		}
		
		secondY = (secondTile / (interval + intervalCheck)) + 1;
		
		System.out.println("normal second: (" + secondX + "," + secondY + ")"); // regular coordinates
		
		// final printing
		double xDifferenceSquare = Math.pow((firstX - secondX),2);
		double yDifferenceSquare = Math.pow((firstY - secondY),2);
		
		
		double distanceBetween = Math.sqrt(xDifferenceSquare + yDifferenceSquare);
		
		System.out.println(xDifferenceSquare);
		System.out.println(yDifferenceSquare);
		
		System.out.println(distanceBetween);
	}
	
	// specific methods
	public static void parameterCounter() {
		parameterCounter++;
		
		if (parameterCounter >= 999) {
			throw new IllegalArgumentException("You generated 999 items, you prob messed up your parameter");
		}
	}
	
	public static void parameterCounterReset() {
		parameterCounter = 0;
	}
	
	public static String hexToAnsi(String hex) {
	    int r = Integer.valueOf(hex.substring(1, 3), 16);
	    int g = Integer.valueOf(hex.substring(3, 5), 16);
	    int b = Integer.valueOf(hex.substring(5, 7), 16);

	    // Actual ANSI code used for coloring
	    String ansi = "\u001B[38;2;" + r + ";" + g + ";" + b + "m";

	    // Printable representation
//	    String visible = "\\u001B[38;2;" + r + ";" + g + ";" + b + "m";
//	    System.out.println("Visible ANSI string: " + visible);

	    return ansi;
	}
	
	public static int[] ansiToRGB(String ansiCode) {
	    // Example input: "\u001B[38;2;177;208;164m"
	    // Strip prefix and suffix
	    ansiCode = ansiCode.replace("\u001B[", "").replace("m", "");
	    String[] parts = ansiCode.split(";");

	    // Check if it's a true color sequence
	    if (parts.length == 5 && (parts[0].equals("38") || parts[0].equals("48")) && parts[1].equals("2")) {
	        try {
	            int r = Integer.parseInt(parts[2]);
	            int g = Integer.parseInt(parts[3]);
	            int b = Integer.parseInt(parts[4]);
	            return new int[]{r, g, b};
	        } catch (NumberFormatException e) {
	            return null; // Invalid numbers
	        }
	    }
	    return null; // Not a valid true color ANSI code
	}
	
	public static int fib(int x) {
		if (x <= 1) {
			return 1;
		}
		return fib(x - 1) + fib(x - 2);
		
		
	}
}
