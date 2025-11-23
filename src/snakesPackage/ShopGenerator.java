package snakesPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import freemarker.cache.ConditionalTemplateConfigurationFactory;
import freemarker.core.TemplateConfiguration;
import universalThings.uniMethod;

import static snakesPackage.ColorData.*;
import static snakesPackage.ItemGenMethods.*;


import java.io.File;
import java.util.Scanner;
//import java.util.concurrent.Executors; // used for the save thread that activates overtime
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import java.awt.*;
import java.awt.datatransfer.*;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class ShopGenerator {
	static Random randGen = new Random(); // nextInt(min include, max exclude)

    static ArrayList<Item>[] itemTierList; // ArrayList[3] for each ItemTier, where each index is an ArrayList<Item>
	static HashMap<String, Item> itemNameList = new HashMap<String, Item>(); // exact same as itemDatabase, except some Items are excluded by ruleComplexity

    static ArrayList<Quest>[] questTierList;
    static HashMap<String, Quest> questNameList;

	static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	static ItemTransfer itemTrans = new ItemTransfer();
	
//	final static HashMap<String, String> ANSI_THEME = Item.getAnsiTheme();
//	final static String ANSI_RESET = Item.getAnsiReset();
//	final static String[] ANSI_TIER = Item.getAnsiTier();

    // Important variables
    final static int TOTAL_PLAYERS = 3;
    final static int START_INV_SLOTS = 4;
    final static int TOTAL_ITEM_TIER = 5;
    final static double Discount_Rate = 0.66; // the discount applied to the Shop
    final static int QUEST_BOARD_TOTAL = 4;
    final static int QUEST_CANCELLATION_FEE = 4;

	public static void main(String[] args) throws Exception{
		// File name paths
		final String ITEM_FileName = "Snakes items list_testing.csv";
		final String QUEST_FileName = "Snakes items list_Quest List.csv";
		final File TEMPLATE_Folder = new File("templates");

        File itemFile = new File(ITEM_FileName);
        File questFile = new File(QUEST_FileName);

		/* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        try {
    		if (!(TEMPLATE_Folder.exists() && TEMPLATE_Folder.isDirectory())) { // if the template doesn't exist
    			throw new FileNotFoundException("templateFile Folder not found"); // throw an error
    		}
        	cfg.setDirectoryForTemplateLoading(TEMPLATE_Folder); // import template file
		} catch (Exception e) { // catch that error and then print it without crashing the code
			e.printStackTrace();  // or handle however you'd like
		}
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        /* ------------------------------------------------------------------------ */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */

        Template itemTemplate = cfg.getTemplate("item_template.ftl");
        Template questTemplate = cfg.getTemplate("quest_template.ftl");

 		
//		 Threads which somehow trigger on uncaught exceptions
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
////            saveCode();
//            System.out.println("hi");
//        }, 1, 5, TimeUnit.SECONDS); // saves every 30 seconds


        ArrayList<String[]> itemList = ItemCSVPrepper.readFile(itemFile); // parse csv_reader in itemList
        ItemCSVMaker.createDatabase(itemList); // parse itemNameList and itemTierList
        itemNameList = ItemCSVMaker.getNameList();
        itemTierList = ItemCSVMaker.getTierList();
        ItemGenMethods.activateItemGenMethods(itemList);
        ItemGenMethods.setComplexity(3);


		// Load all Quests and place them in an Array
        ArrayList<String[]> questList = QuestCSVPrepper.parseQuests(questFile);
        QuestCSVMaker.createDatabase(questList);
        questNameList = QuestCSVMaker.getNameList(); // stores Quests based on QuestName (lowercase)
        questTierList = QuestCSVMaker.getTierList();


		// Goal: load the menu
		Scanner myScanner = new Scanner(System.in);
		int menuID = -1; // set to -1 to be initialized

		
		// Shop variables
		Item[] shopList = new Item[5];
		int refreshCounter = 0; // used in 4. refresh shop
		int shopTier = 0; // the tier of the Shop, used to unlock more items
		boolean discountPresent = true; // tracks when the Discount shop item is purchased

		int[][] weight_array_ShopProb = new int[4][];{ // holds each 4 shopTiers of probability
			weight_array_ShopProb[0] = new int[]{1,0,0,0,0}; // fallback
			weight_array_ShopProb[1] = new int[]{0,85,15,0,0};
			weight_array_ShopProb[2] = new int[]{0,43,42,15,0};
			weight_array_ShopProb[3] = new int[]{0,4,5,6,1};
		}

        int[][] weight_array_ShopType = new int[4][]; {
            weight_array_ShopType[0] = new int[] {1,0,0,0,0,0,0,0};
            weight_array_ShopType[1] = new int[] {8,5,4,0,1,1,1,0};
            weight_array_ShopType[2] = new int[] {8,5,4,0,1,1,1,0};
            weight_array_ShopType[3] = new int[] {7,5,4,1,1,1,1,0}; // need to add t4 passives and powers first
        }

        /* How to run this?
        need to bind every single one.
        Next run multiItemGen(itemArray, shopBound)
        - in multiItemGen, fine with generating items. the issue is detecting if an item is physically impossible.
        - multiItemGen uses customItemGen, but to detect whether an item is physically impossible

        first each shopBound is contained of a weightProb and weightType.
        - 1. store the raw of weightProb and weightType.
            - issue is that boundItemGen exists so that you don't use it
        - 2. figure out polish, itemTier of weightProb is and then find the itemType of weightType is
        - 3. feed it to customItemGen. it either returns a valid item, duplicate item, or null (impossible).
            - 3.1. if it return valid, good.
            - 3.2. if it return dupe, redo the polish.
            - 3.3. if it return null, the polish is impossible, so redo it again.


         */
		
		BoundItemGen[] shopBound = new BoundItemGen[4]; {
            for (int i = 0; i < 4; i++) {
				shopBound[i] = new BoundItemGen(weight_array_ShopProb[i], null, weight_array_ShopType[i]);
			}
		}


		// Chest variables
		int[] chestProb = {0,1,6,3,0}; // used for generating chests
        int[] chestType = {3,4,4,0,2,2,1,0};
		BoundItemGen chestBound = new BoundItemGen(chestProb, null, chestType);


		// probability of generating a Random Tiered Quest based on shopTier
		int[][] questProbList = new int[4][3];{
			questProbList[0] = new int[]{1,0,0,0}; // the fallback is all T0 quests
			questProbList[1] = new int[]{0,8,2,0};
			questProbList[2] = new int[]{0,12,5,1};
			questProbList[3] = new int[]{0,8,5,3};
		}
		
		// this is 0 indexed
		String[][] questRewardList = new String[4][];{
			// questRewardT1
			questRewardList[0] = new String[] {
					"FALLBACK REWARD T0"
			};
				
			questRewardList[1] = new String[]{ // questRewardT1
					"18-25 Gold", // 1
                    "2 Random T2 Items", // 2
                    "2 Random T3 Items", // 3
                    "1 Random T2 Power", // 4
					"2 Random T2 Modifiers", // 5
					"1 Lockbox Item", // 6
                    "D10.5 Face", // 7
					"Free D8", // 8
					"Free D10" // 9
			};
			
			// questRewardT2
			questRewardList[2] = new String[]{
					"25-40 Gold", // 1
					"3 Random T3 Item", // 2
					"2 Random T3 Modifiers", // 3
                    "1 Random T3 Power", // 4
                    "2 Lockbox Items", // 5
                    "D20.5 Face", // 6
					"Free D12", // 7
                    "Free D16", // 8
                    "Random Artifact" // 9
			};
			
			// questRewardT3
			questRewardList[3] = new String[]{
					"40-65 Gold", // 1
                    "Random Artifact", // 2
					"Free D2", // 3
                    "Free D20", // 4
                    "Mirror", // 5
                    "Crown" // 6
			};
		}
		
		int[][] questRewardProb = new int[4][]; { // probability of picking a REWARD for a T1,T2,T3 Quest
			// access questRewardProb which exists at a higher scope, then update the variable
			
			questRewardProb[0] = new int[] {1}; // fallback
			questRewardProb[1] = new int[] {7,8,8,5,5,5,4,1,1};
			questRewardProb[2] = new int[] {8,9,7,7,6,3,2,2,1};
			questRewardProb[3] = new int[] {4,4,3,3,2,2};
		}
        Quest[] questBoard = new Quest[QUEST_BOARD_TOTAL]; // raw
        Quest[] currentQuestBoard = new Quest[QUEST_BOARD_TOTAL]; // with the modified rewards
		
		// Starting item variables
		Item[][] startArray = new Item[TOTAL_PLAYERS][START_INV_SLOTS]; // 3 players, 4 length/starting items
		boolean[][] startRefreshed = new boolean[3][4]; // boolean for already refreshed items
        int[] startType = {5,4,4,0,0,0,0,0};

		BoundItemGen[] startBound = new BoundItemGen[4]; { // (wip)NOTE: the BoundItemGen is kind of fixed compared to START_INV_SLOTS
			startBound[0] = new BoundItemGen(1, null, startType);
			startBound[1] = new BoundItemGen(1, null, startType);
			startBound[2] = new BoundItemGen(2, null, startType);
			startBound[3] = new BoundItemGen(2, null, startType);
		}

        int[] gachaType = {1,1,1,0,1,1,1,0};

		Item[] tutorialArray = new Item[]{
				specializedItemCheck(itemNameList.get("# in a bottle")),
                specializedItemCheck(itemNameList.get("banana peel")),
				itemNameList.get("directional sign post"),
				specializedItemCheck(itemNameList.get("prickly pear"))
		};
		
		System.out.println();
		String menu =
                """
                ____   ____                  .__                       ____    ________            ________                              .__                                   .___    _____                        \r
                \\   \\ /   /___________  _____|__| ____   ____   ___  _/_   |  /   __   \\           \\______ \\ ___.__. ____ _____    _____ |__| ____   ______ _____    ____    __| _/   /     \\ _____    ____ _____   \r
                 \\   Y   // __ \\_  __ \\/  ___/  |/  _ \\ /    \\  \\  \\/ /|   |  \\____    /   ______   |    |  <   |  |/    \\\\__  \\  /     \\|  |/ ___\\ /  ___/ \\__  \\  /    \\  / __ |   /  \\ /  \\\\__  \\  /    \\\\__  \\  \r
                  \\     /\\  ___/|  | \\/\\___ \\|  (  <_> )   |  \\  \\   / |   |     /    /   /_____/   |    `   \\___  |   |  \\/ __ \\|  Y Y  \\  \\  \\___ \\___ \\   / __ \\|   |  \\/ /_/ |  /    Y    \\/ __ \\|   |  \\/ __ \\_\r
                   \\___/  \\___  >__|  /____  >__|\\____/|___|  /   \\_/  |___| /\\ /____/             /_______  / ____|___|  (____  /__|_|  /__|\\___  >____  > (____  /___|  /\\____ |  \\____|__  (____  /___|  (____  /\r
                              \\/           \\/               \\/               \\/                            \\/\\/         \\/     \\/      \\/        \\/     \\/       \\/     \\/      \\/          \\/     \\/     \\/     \\/\s
                              """ +

                """
                99. test special
                ---------------
                0. print the menu again
                ---------------
                1. set shop privilege
                2. create a new shop
                3. open the shop
                4. purchase an item from shop
                5. refresh shop
                6. overwrite a shop item
                ---------------
                7. print menu_dice
                8. purchase specific dice face
                9. generate/view quest board
                10. clipboard a quest
                ---------------
                11. generate random number 144
                12. calculate distance
                ---------------
                13. gen chest loot
                14. gen THEMED chest loot
                15. gen all Tier items
                16. custom gen amount
                17. inquire an item
                ---------------
                18. generate starter items
                19. print the starter items
                20. refresh a specific item
                21. Clipboard the starting items
                ---------------
                22. print and Clipboard all artifacts
                23. generate tutorial items
                ---------------
                24. Change rule_itemComplexity
                25. Change rule_simpleTheme
                Open the Shop
                - Buy Dice or Dice faces
                - Open Quest Board
                - Upgrade Max Mana capacity by 25 for 15 Gold
                - Upgrade Artifact/Shop Tier
                \t- T2 = 14 Gold
                \t- T3 = 24 Gold
                """;

        String menu_dice = """
                ----- Tier 1 required ------
                D4: 18 Gold
                D6: 15 Gold
                D8: 18 Gold
                ----- Tier 2 required ------
                D10: 20 Gold
                D12: 30 Gold
                ----- Tier 3 required ------
                D16: 35 Gold
                D20: 45 Gold
                D2: 20 Gold
                ----- Dice Face prices ------
                - X Face = X Gold
                - -X Face = 0.5 \\* X Gold
                - X.5 Face = 1.5 \\* X Gold
                ----- Extra Modifications ------
                Weighted Dice Face: 2 Gold per Roll
                - Choose 1 face more likely to land, roll twice and use chosen Face if rolled at least once.
                Impossible Dice Face: 2 Gold per Roll
                - Choose 1 face to deactivate, Rerolling it whenever it lands.
                Dice Face Customization: 5 Gold total
                - Choose as many applied Dice Faces to remove.""";

        System.out.println(menu);
		
		while (true) { // menu loop
			try {
				System.out.println("Type the MenuID: (last menuID was: " + menuID + ")");
				menuID = myScanner.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Looks like you mistyped something, try again.");
				myScanner.nextLine();
				continue;
			}

            if (menuID == -1){
                break;
            }

			if (menuID == 99) { // do something specific
                /*
                singleItemGen(weightedProbability or itemTier) -> item
                customItemGen(weightedProbability, condition) --looped for condition--> singleItemGen(weightedProbability)

                singleItemGen fundamentally just makes an item. that's it.
                customItemGen has very specific filters
                typeItemGen would call singleItemGen with a general itemType filter

                {Item, Hazard, Building, Artifact, Passive, Curse, Power}

                 */

			}
			
			else if (menuID == 0) {
                System.out.println(menu);
			} // input 0
			
			if (menuID == 1) { // set shop privilege
				try {
					System.out.println("Input the Shop's tier:");
					shopTier = myScanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine();
					continue;
				}
				
				if (shopTier <= -1 || shopTier >= 4) {
					System.out.println("Unfortunately that shopTier doesn't exist.");
					continue;
				}
				
				System.out.println("shopTier is now (" + shopTier + "), remember to create a store now.");
				
			} // input 1
			
			else if (menuID == 2) { // create a new shop
				discountPresent = true; // re-add the discount for last item
				multiItemGen(shopList, shopBound[shopTier]);
				
				refreshCounter = 1; // reset the initial refresh cost
				System.out.println("shop successfully created");
			} // input 2
			
			else if (menuID == 3) { // open a new shop
				// Next: Print the shop items
                uniMethod.printArray(shopList);
				System.out.println("""
                        \r
                           __    __  .__              _   /\\         .__                     __   \r
                          / /  _/  |_|  |__   ____   / \\ / /    _____|  |__   ____ ______    \\ \\  \r
                         / /   \\   __\\  |  \\_/ __ \\  \\_// /_   /  ___/  |  \\ /  _ \\\\____ \\    \\ \\ \r
                         \\ \\    |  | |   Y  \\  ___/    / // \\  \\___ \\|   Y  (  <_> )  |_> >   / / \r
                          \\_\\   |__| |___|  /\\___  >  / / \\_/ /____  >___|  /\\____/|   __/   /_/  \r
                                          \\/     \\/   \\/           \\/     \\/       |__|           \r
                        """);
				
				System.out.println("⋘ ─────────────────────── < ($) > ─────────────────────── ⋙");
				
				// print each Item in ShopList in Shop format
				fullPrintShop(shopList, discountPresent, Discount_Rate);
				
				// Next: Print the Refresh cost
				System.out.println("Refresh Cost: " + fib(refreshCounter - 1));
				
			} // input 3
			
			else if (menuID == 4) { // purchase an item from shop
				int itemSlot = -1;
				Item purchasedItem;
				
				// Retrieve the respective item from the Shop
				try { // print out the purchased item
					System.out.println("which item: ");
					itemSlot = myScanner.nextInt() - 1; // 0 index the itemSlot
				} catch (InputMismatchException e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine();
					continue;
				}
				
				if (itemSlot < 0 || itemSlot > 5) {
					System.out.println("Unfortunately that itemSlot isn't available.");
					continue;
				}
				
				purchasedItem = shopList[itemSlot];
				System.out.println("-- Thank you for your purchase ~ --");
				purchasedItem.nicePrint();
				copyClipboard(purchasedItem);
				
				// switch the old purchased item index for a new item
				shopList[itemSlot] = noDupeItemGen(shopList, shopBound[shopTier]);
				
				// if the FIRST item was purchased
				if (itemSlot == 0) {
					discountPresent = false;
				}
			} // input 4
			
			else if (menuID == 5) { // refresh the shop
				System.out.println("Shop+Quest Board has been REFRESHED");
				
				// fill the shopList with items
				multiItemGen(shopList, shopBound[shopTier]);
				refreshCounter++; // increase the Refresh cost
				discountPresent = true; // add back the Discount
			} // input 5
			
			else if (menuID == 6) { // overwrite a Shop item
				int itemSlot;
				String itemName;
				Item selectedItem;
				
				try { // catch the inputMismatchException
					System.out.println("Which item slot?: ");
					itemSlot = myScanner.nextInt() - 1;
					myScanner.nextLine(); // refresh the Scanner
					
					System.out.println("Replace with what item?: ");
					itemName = myScanner.nextLine();
				} catch (InputMismatchException e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine();
					continue;
				}
				
				if (itemSlot < 0 || itemSlot > 5) { // catch the invalid itemSlot
					System.out.println("Unfortunately that itemSlot isn't available.");
					continue;
				}
				
				// attempt to retrieve selectedItem from itemDatabase
				selectedItem = itemNameList.get(itemName.toLowerCase());
				
				if (selectedItem == null) { // if itemDatabase returns null
					System.out.println("That item does not exist.");
				}
				else {
					shopList[itemSlot] = selectedItem; // Based on the position and Item, overwrite shopList
					System.out.println("Succesfully replaced with " + selectedItem);
				}
				
			} // input 6
			
			else if (menuID == 7) { // print menu_dice
				System.out.println(menu_dice);
			} // input 8

            else if (menuID == 8) { // purchase specific dice face
				int diceFace;

				System.out.println("Input a specific Dice Face: ");
				diceFace = myScanner.nextInt();

				// Technically using magic numbers, may change later if it needs to
				System.out.println(diceFace + " face = " + 2 * diceFace + " gold");
				System.out.println("-" + diceFace + " face = " + diceFace + " gold");
				System.out.println(diceFace + 0.5 + " face = " + diceFace * 3 + " gold");
			} // input 7
			
			else if (menuID == 9) { // generate quests/questboard
				String includeRefreshStatus;

                System.out.println("Type something to refresh questBoard: ");
				myScanner.nextLine(); // reset the scanner
				includeRefreshStatus = myScanner.nextLine();

                // for every starting item
				if (!includeRefreshStatus.isEmpty()) { // copy WITHOUT the (refresh)
					System.out.println("Generating questBoard");
                    int[] questProb;

                    try { // questProbTemp holds the questT#Prob from questProbList
                        questProb = questProbList[shopTier];
                    } catch (Exception e) { // if something screws up, use questT0Prob as fallback
                        System.out.println(e); // DEBUG
                        questProb = questProbList[0];
                    }

                    // Goal: FIRST Generate 4 Unique Quests

                     // get the random associated quest based on winning quest tier
                    questBoard[0] = singleQuestGen(questProb); // store the first quest in questBoard

                    multiQuestGen(questBoard, questProb);
                    // Now: questBoard has 4 Unique Quests

                    for (int i = 0; i < questBoard.length; i++) {
                        currentQuestBoard[i] = new Quest(questBoard[i]);
                    }

                    // Next: SECOND Create the 4 Rewards for each Quest AND print them

                    updateQuestRewards(currentQuestBoard, questRewardProb, questRewardList);

                    // print quest board

                    // else generate teh quest board and print it

				} // if copy without (refresh status)

                questBoardPrint(currentQuestBoard);

			} // input 9
			
			else if (menuID == 10) { // clipboard a quest
				int questSlot;
				
				System.out.println("Which quest do you choose?: ");
				questSlot = myScanner.nextInt();

                if (1 > questSlot || questSlot > 4) {
                    System.out.println("Unfortunately that is an invalid questSlot.");
                    continue;
                }

				// Cleanly display the Quest in questBoard
                Quest currentQuest = currentQuestBoard[questSlot - 1];
                currentQuest.fullPrint();
                System.out.println();
                currentQuest.createHtml();
                currentQuest.createPlain();

                copyClipboard(currentQuest);
				
			} // input 10
			
			else if (menuID == 11) { // generate 3 random numbers from 144
				for (int i = 0; i < 3; i++) {
					System.out.println(randGen.nextInt(144) + 1);
				}
				System.out.println();
			} // input 11
			
			else if (menuID == 12) { // calculate the distance
				int firstTile;
				int secondTile;
				
				System.out.println("first tile?: ");
				firstTile = myScanner.nextInt();
				System.out.println("second tile?: ");
				secondTile = myScanner.nextInt();
				
				calculateDistance(firstTile, secondTile);
			}// input 12
			
			else if (menuID == 13) { // generate chest drops
				int chestNum;
				
				System.out.println("How many chest items?: ");
				chestNum = myScanner.nextInt();
				
				/* multiItemGen(custom array length, weightedProb, singleItemGen)
				 * except 
				 * 
				 * maybe instead, make a new Item[intTemp] inside the filter
				 * itself, then it'll return the same Item[] and i do things with it?
				 * 
				 * or maybe use an ArrayList lol
				 */
				
				
				chestOpen(chestNum, chestBound);
				// turn this into lambda
//				for (Item chestLoot : ) {
//					chestLoot.fullPrint();
//				}
			} // else if input 13
			
			else if (menuID == 14) { // generate THEMED chest loot (UPDATE THIS)
				int chestTotal;
				
				System.out.println("how many items?: ");
				chestTotal = myScanner.nextInt();
				
				myScanner.nextLine(); // refresh myScanner
				System.out.println("choose a Theme (caps sensitive): ");
				
				final String TempFilter = myScanner.nextLine(); // make brand new final variable
				
				System.out.println("chestTotal: " + chestTotal);
				System.out.println("item Theme: " + TempFilter);
				
				/* problem:
				 * ItemThemes are now String[], not String
				 * 
				 */

                BoundItemGen chestThemeBound = new BoundItemGen(chestProb, item -> (
                        Arrays.asList(item.getTheme()).contains(TempFilter)), chestType);

                chestOpen(chestTotal, chestThemeBound);

			} // input 14
			
			else if (menuID == 15) { // Print 1 item for each Tier

				// Print 1 item for each Tier
				for (int i = 0; i < TOTAL_ITEM_TIER; i++) { //(WIP) magic number
					singleItemGen(i).nicePrint();
				}
			} // input 15
			
			else if (menuID == 16) { // used for gachapon
				int itemTier;
				Item[] generatedItems;
				
				try {
					System.out.println("What tier?: ");
					itemTier = myScanner.nextInt();
					
					System.out.println("How many items?: ");
					generatedItems = new Item[myScanner.nextInt()];
				} catch (InputMismatchException e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine();
					continue;
				}
				
				if (itemTier < -1  || itemTier > 6) { // (WIP) magic number
					System.out.println("Unfortunately that itemTier doesn't exist.");
					continue;
				}
				
				multiItemGen(generatedItems, itemTier, null, gachaType);
					
				for (Item target : generatedItems) {
					target.nicePrint();
				}
				
			}
			
			else if (menuID == 17) { // inquire an item
				/* Strange combination of:
				 * string similarity
				 * - https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
				 * - https://rosettacode.org/wiki/Levenshtein_distance#Java
				 * 
				 * pair things
				 * - https://stackoverflow.com/questions/67040866/arraylist-rearrange-with-another-arraylist
				 * - https://www.geeksforgeeks.org/pair-class-in-java/
				 * - https://www.geeksforgeeks.org/list-interface-java-examples/
				 */
				String itemName;
				Item selectedItem;
				List<AbstractMap.SimpleEntry<Item, Double>> pairList = new ArrayList<>(); // item search index

				myScanner.nextLine(); // refresh myScanner
				System.out.println("type the itemName: ");
				itemName = myScanner.nextLine().toLowerCase();
				
				// Search for the Item name
				selectedItem = itemNameList.get(itemName);
				
				if (selectedItem == null) {
					System.out.println("No such item exists, did you mean?: ");
					
					for (Item element : itemNameList.values()) {
						pairList.add(new AbstractMap.SimpleEntry<>(element,similarity(element.getName(), itemName)));
					}
					
					pairList.sort((p1, p2)
	                          -> Double.compare(p2.getValue(),
                                      p1.getValue()));
					
					for (int i = 0; i < 5; i++) { // 5 is first 5 results
						System.out.println(pairList.get(i));
					}
					
					pairList.clear();
				}
				else {
					selectedItem.fullPrint();
					copyClipboard(selectedItem); // the error most likely appears here
				}

			} // input 16
			
			else if (menuID == 18) { // generate ALL start items
				
				for (int i = 0; i < TOTAL_PLAYERS; i++) { // for each player
					// generate a custom item in startArray
					multiItemGen(startArray[i], startBound);
				}

				System.out.println("Successfully generated all starting items.");
				
			} // input 18
			
			else if (menuID == 19) { // print the starting items
				int selectedPlayer;
				
				try {
					System.out.println("which player? (0 for all)");
					selectedPlayer = myScanner.nextInt(); // intTemp usually holds the player (not 0 index)
					
					if (selectedPlayer < 0 || selectedPlayer > TOTAL_PLAYERS) {
						System.out.println("You didn't type a valid Player, try again.");
						continue;
					}
				} catch (InputMismatchException e){
					System.out.println("Looks like you mistyped something, try again.");
					continue;
				}
				
				// Either print every inventory or a select Player
				if (selectedPlayer == 0) { // print every player
					for (int i = 0; i < TOTAL_PLAYERS; i++) { // for each player
						System.out.println("--- PLAYER " + (i + 1) + "'s Starting Inventory ---");
						for (int j = 0; j < START_INV_SLOTS; j++) { // for each player's inventory
							if (startRefreshed[i][j]) { // can be refreshed
								System.out.println("(CANNOT be refreshed)");
							}
							else {
								System.out.println("(Can be refreshed)");
							}
							startArray[i][j].nicePrint();
						}
					}
				} // if
				
				else { // print a specific player
					System.out.println("--- PLAYER " + (selectedPlayer) + "'s Starting Inventory ---");
					selectedPlayer--; // 0 index the selectedPlayer
					for (int i = 0; i < START_INV_SLOTS; i++) {
						if (startRefreshed[selectedPlayer][i]) { // can be refreshed
							System.out.println("(CANNOT be refreshed)");
						}
						else {
							System.out.println("(Can be refreshed)");
						}
						startArray[selectedPlayer][i].nicePrint();
					}
				} // else
				
			}
			
			else if (menuID == 20) { // refresh a starter item
				int selectedPlayer;
				int itemSlot;
				
				try {
					System.out.println("Which player?: ");
					selectedPlayer = myScanner.nextInt(); // player (1-3 index)
					
					System.out.println("Which item slot?: ");
					itemSlot = myScanner.nextInt(); // item slot (1-4 index)
				} catch (InputMismatchException e) {
					System.out.println("Looks like you mistyped something, try again.");
					myScanner.nextLine(); // reset in case String typed so doesn't break nextInt()
					continue;
				}
				
				
				if (selectedPlayer <= 0 || selectedPlayer >= TOTAL_PLAYERS + 1) { // players are 1-3
					// This will trigger first over itemSlot
					System.out.println("Invalid Player, try again.");
					continue;
				}
				else if (itemSlot <= 0 || itemSlot >= 5) {
					System.out.println("Invalid itemSlot, try again.");
					continue;
				}
			
				
				// Refresh the item based on the Player and Item Slot
				if (!startRefreshed[selectedPlayer - 1][itemSlot - 1]) { // if you can refresh it (true)
					selectedPlayer--; // make it 0 indexed (intTemp = 1 means player 1 at index no.0)
					itemSlot--;
					
					Item generatedItem;
					String htmlClipboard;
					String rawClipboard;
					
					if (itemSlot == 0 || itemSlot == 1) { // if it item no.1 or 2
						System.out.println("refreshing T1");
						// generate a T1 which is impossible to be a Passive
						generatedItem = noDupeItemGen(startArray[selectedPlayer], startBound[0]);
					}
					else {
						// generate a T2 that's NOT a Passive
						System.out.println("refreshing T2");
						generatedItem = noDupeItemGen(startArray[selectedPlayer], startBound[2]);
					}
					startRefreshed[selectedPlayer][itemSlot] = true; // set startRefreshed to true as item was refreshed
					
					startArray[selectedPlayer][itemSlot] = generatedItem; // replace the old refreshed item
					
					System.out.println("(CANNOT be refreshed)");
					generatedItem.nicePrint(); // print the new item
					
					// copy the item WITH the (refresh) into clipboard
					htmlClipboard = "<html><body>" + "(CANNOT be refreshed)" + "</body></html>\n" + generatedItem.getHtml();
					itemTrans.setHtml(htmlClipboard);
					
					rawClipboard = "(CANNOT be refreshed)\n" + generatedItem.getPlain();
					itemTrans.setPlain(rawClipboard);
					
//					System.out.println("PLAIN------------");
//					System.out.println(itemTransTemp.getPlain());
//					
//					System.out.println("HTML------------");
//					System.out.println(itemTransTemp.getHtml());
					
					clipboard.setContents(itemTrans, null);
					System.out.println("Successfully copied to clipboard.");
					
				} // if
				
				else { // otherwise you've already refreshed (false)
					System.out.println("Player " + selectedPlayer +  " has already refreshed item no." + itemSlot);
				} // else
			}
			
			else if (menuID == 21) { // clipboard the Starter items
				int selectedPlayer;
				String includeRefreshStatus;
				
				System.out.println("Which player?: ");
				selectedPlayer = myScanner.nextInt() - 1;
				
				System.out.println("Type something to include the (refresh status): ");
				myScanner.nextLine(); // reset the scanner
				includeRefreshStatus = myScanner.nextLine();
				
				/* strTemp holds
				 * strTemp2 holds
				 * 
				 * 
				 */
				
				String htmlClipboard = "";
				String rawClipboard = "";
				
				// for every starting item
				if (includeRefreshStatus.isEmpty()) { // copy WITHOUT the (refresh)
					// store every Item's html in strTemp
					for (Item element : startArray[selectedPlayer]) { // for every Item in startArray[player]
						element.nicePrint();
						htmlClipboard += element.getHtml() + "<br>"; // store Item.getHtml() in strTemp2
						rawClipboard += element.getPlain() + "\r\n";
					}
					
				} // if copy without (refresh status)
				
				else { // copy with the (refresh)
					String refreshHoldPlain;
					String refreshHoldHtml;
					Item currentItem;
					
					for (int i = 0; i < startArray[selectedPlayer].length; i++) {
						currentItem = startArray[selectedPlayer][i];
						
						if (startRefreshed[selectedPlayer][i]) {
							System.out.println("(CANNOT be refreshed)");
							refreshHoldHtml = "<html><body style=\"margin:0; padding:0;\">" + "(CANNOT be refreshed)" + "</body></html>\r\n";
							refreshHoldPlain = "(CANNOT be refreshed)";
						}
						else {
							System.out.println("(Can be refreshed)");
							refreshHoldHtml = "<html><body style=\"margin:0; padding:0;\">" + "(Can be refreshed)" + "</body></html>\r\n";
							refreshHoldPlain = "(Can be refreshed)";
						}
						currentItem.nicePrint();
						
						// strTemp holds both the Item Html AND (refresh status)
						htmlClipboard += refreshHoldHtml + currentItem.getHtml() + "<br>";
//						strTemp += itemTemp.getHtml() + "\r\n";
						
						// strTemp2 holds both the Item Plain AND (refresh status)
						rawClipboard += refreshHoldPlain + currentItem.getPlain() + "\r\n";
					}
				} // else copy with (Refresh status)
				
				
				
				itemTrans.setPlain(rawClipboard);
				itemTrans.setHtml(htmlClipboard);
				clipboard.setContents(itemTrans, null);
				System.out.println("Successfully added all Starting items to clipboard.");
				
//				System.out.println(htmlClipboard); // display which item it is
			}
			
			else if (menuID == 22) { // print and clipboard artifacts
				itemNameList.values().forEach(value -> {
					if (value.getTier() == 4 && value.getComplexity() <= ItemGenMethods.getComplexity()) {
						value.nicePrint();
					}
				});
				
				copyClipboard(item -> (item.getTier() == 4 && item.getComplexity() <= ItemGenMethods.getComplexity()));
				
			} // input 16
			
			else if (menuID == 23) { // generate tutorial items
				// NOTE: Player 1 is hardcoded to have the tutorial inventory
				for (int i = 0; i < startArray[0].length; i++) {
					startArray[0][i] = tutorialArray[i];
				}
				
				uniMethod.printArray(startArray[0]);
				
				System.out.println("Successfully replaced Player 1's inventory with Tutorial");
			}

            else if (menuID == 24){
                System.out.println("type the new rule_Itemcomplexity value: ");
                int newComplexity = myScanner.nextInt();

                ItemGenMethods.setComplexity(newComplexity);
                System.out.println("rule_itemComplexity is now: " + ItemGenMethods.getComplexity());
            }

            else if (menuID == 25){
                ColorData.refactorColorTheme();
                System.out.println("rule_simpleTheme is now: " + ColorData.getSimpleTheme());
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
		
		public ItemTransfer() { // empty constructor
			
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
	 * @param Predicate<Item> filter
	 * If using customItemGen, what the parameters are for
	 * generating the item.
	 * 
	 */
	
	public static Item[] chestOpen(int chestTotal, BoundItemGen chestBound) {
		Item[] chestArray = new Item[chestTotal];
		multiItemGen(chestArray, chestBound);
		
		System.out.println("CHEST OPENING TIME");
        for (Item item : chestArray) { // display all the items
            item.nicePrint();
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
			System.out.println("- Type: " + ANSI_TYPE.get(itemTemp.getType()) + itemTemp.getType() + ANSI_RESET);
			
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
//			System.out.println("- Version: " + itemTemp.getVersion());
//			System.out.println("- Complexity: " + itemTemp.getComplexity());
			System.out.println("⋘ ─────────────────────── < ($) > ─────────────────────── ⋙");
		} // for loop item
	}



	public static void copyClipboard(Item itemTemp) {
		itemTrans.setPlain(itemTemp.getPlain());
		itemTrans.setHtml(itemTemp.getHtml());

		clipboard.setContents(itemTrans, null);

		System.out.println("Successfully copied to Clipboard");
	}

    public static void copyClipboard(Quest questTemp) {
		itemTrans.setPlain(questTemp.getPlain());
		itemTrans.setHtml(questTemp.getHtml());

		clipboard.setContents(itemTrans, null);

		System.out.println("Successfully copied to Clipboard");
	}



	public static void copyClipboard(Predicate<Item> filter) {
		// reset strTemp
		String htmlTemp = "";
		String plainTemp = "";

		// loop through every Artifact
		for (Item element : itemNameList.values()) { // search the entire itemNameList
			if (filter.test(element)) { // if the filter matches the chosen Item
//				element.fullPrint();

				// record the Item's html and plain
				htmlTemp += element.getHtml();
				plainTemp += element.getPlain() + "\n";
			}
		}

		itemTrans.setPlain(plainTemp);
		itemTrans.setHtml(htmlTemp);

		clipboard.setContents(itemTrans, null);
		System.out.println("Successfully added all Artifacts to Clipboard.");
	}


	public static void updateQuestRewards(Quest[] currentQuestBoard, int[][] questRewardProb, String[][] questRewardList){
        int winningQuestRewardIndex;
        int winningQuestTier;
        String questDesc;
        Item itemReward;
        Quest currentQuest;
        String bonusReward = null;
        String questReward;

//        String[] questReward = new String[currentQuestBoard.length];
//        String[] bonusReward = new String[currentQuestBoard.length];

        for (Quest quest : currentQuestBoard) {
            currentQuest = quest;

            winningQuestTier = currentQuest.getTier(); // hold the questTier
            questDesc = currentQuest.getDesc(); // hold the questDesc

            winningQuestRewardIndex = weightedProb(questRewardProb[winningQuestTier]); // holds the index of quest's Reward
            questReward = questRewardList[winningQuestTier][winningQuestRewardIndex]; // get the quest's Reward based on index

            // next, analyze the questReward

            // Next: Add bonus info based on Description
            /* X Item: Scavenger Hunt
             * X/Y Space: Movement
             */

            // Next: Add bonus info based on Reward
            if (questReward.contains("Gold")) { // if it's gold reward


                // find the index of the space and '-'
                bonusReward = Integer.toString(randGen.nextInt( // generate random int
                        Integer.parseInt(questReward.substring(0, questReward.indexOf('-'))),
                        Integer.parseInt(questReward.substring(questReward.indexOf('-') + 1, questReward.indexOf(' '))
                        )));
            } // if gold reward

            else if (questReward.contains("Random") && questReward.contains("Item")) { // if it's random item reward
                // find quantity of items and the tier
                int quantityReward = Integer.parseInt(questReward.substring(0, 1)); // holds the quantity of item Reward
                int tierReward = Integer.parseInt(questReward.substring(questReward.indexOf('T') + 1, questReward.indexOf('T') + 2)); // tier of items

                // reset bonusReward as it used to contain the previous Quest's Gold bonusReward
                bonusReward = "";

                for (int j = 0; j < quantityReward; j++) { // generate quantity number of random items
                    // generate random item Rewards based on the item Tier
                    itemReward = typeItemGen(tierReward, null, new int[]{1,1,1});

//                    if (tierReward == 1) {
//                        itemReward = typeItemGen(1);
//                    } else if (tierReward == 2) {
//                        itemReward = typeItemGen(2);
//                    } else { // tier 3 item
//                        itemReward = typeItemGen(3);
//                    }

                    // place itemTemp in bonusReward multiple times for each Random Reward item
                    bonusReward += itemReward.getName();

                    if (j != quantityReward - 1) { // not last iteration
                        bonusReward += ", ";
                    }

                } // for loop j
            } // else if random

            else if (questReward.contains("Modifier")) {
                int quantityReward = Integer.parseInt(questReward.substring(0, 1)); // holds the quantity of item Reward
                int tierReward = Integer.parseInt(questReward.substring(questReward.indexOf('T') + 1, questReward.indexOf('T') + 2)); // tier of items

                String bonusRewardHold;

                bonusReward = "";

                for (int i = 0; i < quantityReward; i++){
                    bonusRewardHold = typeItemGen(tierReward, null, "Modifier").getName();
                    bonusReward += bonusRewardHold;
                    if (i != quantityReward - 1){
                        bonusReward += ", ";
                    }
                }

            }

            else if (questReward.contains("Power")) {
//                int quantityReward = Integer.parseInt(questReward.substring(0, 1)); // holds the quantity of item Reward
                int tierReward = Integer.parseInt(questReward.substring(questReward.indexOf('T') + 1, questReward.indexOf('T') + 2)); // tier of items
                bonusReward = typeItemGen(tierReward, null, "Power").getName();

            }

            else if (questReward.contains("Artifact")) { // if it contains artifact
                itemReward = typeItemGen(4, null, "Artifact"); // generate a random artifact
                bonusReward = itemReward.getName(); // add Random Artifact to bonusReward
            }
            // Now: bonusReward now contains extra information based on questReward


            // Next: Modify the quest description if needed
            if (questDesc.contains("Space X")) { // if description itself is related to Space X
                questDesc = questDesc.substring(0, questDesc.indexOf("X")) + (randGen.nextInt(144) + 1); // get rid of the "X"
                currentQuest.setDesc(questDesc);
            }

            // do reward stuff here
            if (questReward.contains("Gold") || questReward.contains("Random")) { // if there's a bonus reward
                currentQuest.setReward(questReward + " (" + bonusReward + ")");
            } else { // otherwise print it normal
                currentQuest.setReward(questReward);
            }

            currentQuest.setFee(currentQuest.getTier() * QUEST_CANCELLATION_FEE);

            // Now: questDesc has been overwritten
        }



    }

	public static void questBoardPrint(Quest[] questBoard){
        ArrayList<Quest>[] questBoardPrint = new ArrayList[QUEST_BOARD_TOTAL];
        Quest currentQuest;
        String questReward;
        String questDesc;
        String bonusSpace = // bonusSpace used for nice Quest formatting
				"                                                              ";

        int winningQuestTier;

        System.out.println("""
                        \r
                           ,----.            ,-----.      ___    _     .-''-.     .-'''-. ,---------.          _______       ,-----.       ____    .-------.     ______             .----,    \r
                           )  ,-'          .'  .-,  '.  .'   |  | |  .'_ _   \\   / _     \\\\          \\        \\  ____  \\   .'  .-,  '.   .'  __ `. |  _ _   \\   |    _ `''.         `-,  (    \r
                          / _/_           / ,-.|  \\ _ \\ |   .'  | | / ( ` )   ' (`' )/`--' `--.  ,---'        | |    \\ |  / ,-.|  \\ _ \\ /   '  \\  \\| ( ' )  |   | _ | ) _  \\          _\\_ \\   \r
                         / ( ` )         ;  \\  '_ /  | :.'  '_  | |. (_ o _)  |(_ o _).       |   \\           | |____/ / ;  \\  '_ /  | :|___|  /  ||(_ o _) /   |( ''_'  ) |         ( ' ) \\  \r
                        ) (_{;}_)        |  _`,/ \\ _/  |'   ( \\.-.||  (_,_)___| (_,_). '.     :_ _:           |   _ _ '. |  _`,/ \\ _/  |   _.-`   || (_,_).' __ | . (_) `. |        (_{;}_) ( \r
                         \\ (_,_)         : (  '\\_/ \\   ;' (`. _` /|'  \\   .---..---.  \\  :    (_I_)           |  ( ' )  \\: (  '\\_/ \\   ;.'   _    ||  |\\ \\  |  ||(_    ._) '         (_,_) /  \r
                          \\  \\            \\ `"/  \\  )  \\| (_ (_) _) \\  `-'    /\\    `-'  |   (_(=)_)          | (_{;}_) | \\ `"/  \\  ) / |  _( )_  ||  | \\ `'   /|  (_.\\.' /            /  /   \r
                           )  `-.          '. \\_/``"/)  )\\ /  . \\ /  \\       /  \\       /     (_I_)           |  (_,_)  /  '. \\_/``".'  \\ (_ o _) /|  |  \\    / |       .'          .-`  (    \r
                           `----'            '-----' `-'  ``-'`-''    `'-..-'    `-...-'      '---'           /_______.'     '-----'     '.(_,_).' ''-'   `'-'  '-----'`            '----`    \r
                                                                                                                                                                                              \r
                        """);
        System.out.println(bonusSpace.substring(6) + "┍━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━»•» 🌸 «•«━━━━━┑");

        int weightedTemp;
        Item itemReward;
        String questDescTemp;

        for (int i = 0; i < questBoard.length; i++) { // for each Quest
            currentQuest = questBoard[i];
            questReward = currentQuest.getReward();
            questDesc = currentQuest.getDesc();

            if (i != 0) { // don't print the line break at beginning
                System.out.println(bonusSpace + "     " + "•❅────────────────────────✧❅✦❅✧────────────────────────❅•");
            }

            // print item and cost with discount (1st item always discounted)

            // Next: Display the full information of the Quests and bonus Info
            System.out.println();
            System.out.println(bonusSpace + "Quest Name: " + currentQuest.getName() + ANSI_TIER[currentQuest.getTier()] + " [Tier " + currentQuest.getTier() + "]"  + ANSI_RESET);
            System.out.println(bonusSpace + "- Description: ");
            System.out.println(bonusSpace + "       " + "   - " + questDesc);

            System.out.println(bonusSpace + "- Reward: " + questReward);

            System.out.println(bonusSpace + "- Cancellation Fee: " + (currentQuest.getTier() * QUEST_CANCELLATION_FEE)); //(WIP)
            System.out.println();
//					bonusReward = ""; // reset the bonus reward
            // Now: a Quest has been fully printed

        } // for loop i

        System.out.println(bonusSpace.substring(0) + "┕━━━━━»•» 🌸 «•«━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┙");


    }
	


    public static Quest[] multiQuestGen(Quest[] questArray, int[] questProb){
        Quest winningQuest;
        boolean yesDupes = false;

        // generate the first quest
        questArray[0] = singleQuestGen(questProb);

        // generate every after quest
        for (int i = 1; i < questArray.length; i++){
            do {
                yesDupes = false;
                winningQuest = singleQuestGen(questProb); // generate a quest
//                uniMethod.printArray(questArray);
                for (int j = 0; j < i; j++){ // check it
//                  System.out.println("comparing " + winningQuest + " to index" + j + " at " + questArray[j]);
                    if (winningQuest == questArray[j]){
//                        System.out.println("DUPE DETECTED");
                        yesDupes = true;
                        break;
                    }
                }

            } while (yesDupes);

            questArray[i] = winningQuest;
        }

        return questArray;
    }

    public static Quest singleQuestGen(int[] questProb){
        int winningQuestTier;
        Quest winningQuest;

        // generate the VERY FIRST quest (bypass no dupe Quest gen)
        winningQuestTier = weightedProb(questProb); // generate a random quest's TIER

        // choose a random quest based on the chosen TIER
        winningQuest = questTierList[winningQuestTier].get(randGen.nextInt(questTierList[winningQuestTier].size()));
//        System.out.println("singleQuestGen winning quest is: " + winningQuest);
        return winningQuest;
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
	
	// weird methods
	
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
	
	/* Method that returns the index of the fibonacci sequence
	 * 
	 * @param int x
	 * The term number for the fib sequence.
	 * 
	 */
	
	public static int fib(int x) {
		if (x <= 1) {
			return 1;
		}
		return fib(x - 1) + fib(x - 2);
	}
	
	public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        /* // If you have StringUtils, you can use it to calculate the edit distance:
        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                                                             (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
 
    }
	
	// Example implementation of the Levenshtein Edit Distance
    // See http://r...content-available-to-author-only...e.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
 
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
	
}

// notes:
/* 2 solutions: for custom generating items
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
 * generateItem(T pattern, Predicate<Item> filter) -- goes to --> customItemGen(specific pattern, filter)
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
 * route the pattern/filter manually, where both BoundItemGen and ItemGenMethod
 * just do a "runGenerate()" to make the item. In this case, ItemGenMethod uses
 * both (pattern, filter) as its filters, while BoundItemGen uses
 * runGenerate() where the bind() process is EITHER bind(pattern) or bind(pattern, filter)
 * 
 * Also generateItem is overloaded to have 2 methods, with 4 outcomes total:
 * customItemGen() with int and int[]
 * singleItemGen() with int and int[]
 * 
 */

/*



 */
