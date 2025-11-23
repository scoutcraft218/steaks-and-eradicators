package snakesPackage;

import universalThings.uniMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.Random;

public class ItemGenMethods {
    private static int rule_allowedComplexity;
    private static boolean rule_simpleTheme;

    static ArrayList<Item>[] itemTierList;
    static HashMap<String, Item> itemNameList;
//    private static ArrayList<Item>[] copy_itemTierList;
    private static final Random randGen = new Random(); // nextInt(min include, max exclude)
    private static final String[] winningTypeRaw =
        {"Item", // 1
        "Hazard", // 2
        "Building", // 3
        "Artifact", // 4
        "Passive", // 5
        "Power",// 6
        "Modifier",// 7
        "Curse"}; // 8


    public static void activateItemGenMethods(ArrayList<String[]> csvArray) {
        ItemCSVMaker itemDatabase = ItemCSVMaker.createDatabase(csvArray);
        itemTierList = ItemCSVMaker.getTierList();
        itemNameList = ItemCSVMaker.getNameList();
    }

    public static void setComplexity(int newComplexity){
        rule_allowedComplexity = newComplexity;
    }
    public static int getComplexity() { return rule_allowedComplexity; }



    /* generateItem requires a predicate, since it either calls singleItemGen or customItemGen

    BoundItemGen requires a pattern and a filter, assuming the filter is static
    - if the filter is dynamic, it needs the ingredients (weightedProb, weightedType) and the instructions (typeItemGen)
    - this is because running generateItem() must compile the final typeFilter during every instance of generateItem()

     */

    /*
    --------------------------------------------------------------------------------------------------
     */


	/* Method which randomly generates an Item that WILL fit
	 * the criteria (Predicate).
	 *
	 * @param itemTier/weightedProbability
	 * Based on the itemTier or weightedProbability, return a randomly
	 * generated item
	 *
	 * @param filter
	 * Predicate which is basically a lambda expression AKA a portable mini
	 * method, used as the requirement for what to EXCLUDE from generated item
	 */

    /* This is the Side Branch

     */
	private static Item tunnel_customItemGen(int itemTier, Predicate<Item> filter, ArrayList<Item> tiered_copy_itemTierList) { // side branch
//		System.out.println("customItemGen(itemTier, filter) was called");
		Item winningItem;

        if (tiered_copy_itemTierList == null){ // if no map has been provided (called by Side Branch)
            tiered_copy_itemTierList = (ArrayList<Item>) itemTierList[itemTier].clone();
        }

        // keep generating until it returns success or null

		do {
			winningItem = tunnel_singleItemGen(itemTier, tiered_copy_itemTierList); // generate an item but record itemTierList
            // keep generating singleItemGen until the filter is met OR it generates null

//            System.out.println("customItemGen winningItem = " + winningItem);
            uniMethod.overload(500);
		} while (winningItem != null && !filter.test(winningItem));
//        System.out.println("customItemGen has PASSED as:" + winningItem);
        uniMethod.overloadReset();
		return winningItem;

//        example: item -> (!item.getType().equals("Passive")) && !item.getType().equals("Power"))
	}

    /* This is the Master Branch

     */
	public static Item customItemGen(int[] weightedProbability, Predicate<Item> filter) { // master branch
        ArrayList<Item>[] copy_itemTierList = (ArrayList<Item>[])itemTierList.clone(); // create the tierList
//        uniMethod.printArray(copy_itemTierList);

//        System.out.println("customItemGen(weightedProbability, filter) was called");


        int[] copy_weightedProbability = weightedProbability.clone();
        int winningTier;
        ArrayList<Item> tiered_copy_itemTierList;
        Item winningItem;

        int tier_runtime = 0;

        for (int tier : copy_weightedProbability){
            if (tier != 0){
                tier_runtime++;
            }
        }

        for (int i = 0; i < tier_runtime; i++) { // this will run a total number of times for every possibleTier possibility
            winningTier = weightedProb(copy_weightedProbability); // generate a tier
//            System.out.println("customItemGen winningTier: " + winningTier);
            tiered_copy_itemTierList = (ArrayList<Item>) itemTierList[winningTier].clone();

            // this either succeeds total using next Branch or fails
            winningItem = tunnel_customItemGen(winningTier, filter, tiered_copy_itemTierList); // call the V:tier BELOW for an item


            if (winningItem != null){ // if customItemGen Side Branch succeeds (filter passes)
                return winningItem; // the whole Master Branch succeeded
            }
            // otherwise, the only other case is it failed, which pins blame on weightedProbability

            // otherwise it's not a winningItem

//            do { // generate 1 item
//
//            } while (winningItem != null); // if the master branch breaks, the whole function is broken
//            // this means winningItem is a null

//            System.out.println("customItemGen, winningItem is null, Tier: " + winningTier + " must be impossible");
//            System.out.println("Now I must update the weightedProbability and pick again");
            copy_weightedProbability[winningTier] = 0;
        }

        // if the for loop didn't run, there are no tiers left to generate
//        System.out.println("customItemGen(weightedProbability, filter) has failed. ABORTING");
        return null;

	}

    public static Item customItemGen(int itemTier, Predicate<Item> filter){
        return tunnel_customItemGen(itemTier, filter, null);
    }

    public static Item customItemGen(BoundItemGen itemBound){
        return itemBound.boundGenerate();
    }


    /* This is the Side Branch

     */
    private static Item tunnel_singleItemGen(int itemTier, ArrayList<Item> tiered_copy_itemTierList) {
//        ArrayList<Item> tiered_copy_itemTierList = (ArrayList<Item>) copy_itemTierList[winningTier].clone();

//        System.out.println("singleItemGen itemTier was called");
		int winningTierSize;

        int winningItemIndex;
        Item winningItem;

        if (tiered_copy_itemTierList == null){ // if no map has been provided (called by Side Branch)
            tiered_copy_itemTierList = (ArrayList<Item>) itemTierList[itemTier].clone();
        }

        do {
            winningTierSize = tiered_copy_itemTierList.size(); // get the size of the tier (dynamic)
            if (winningTierSize == 0){ // if there are physically no more items left in the tier
//                System.out.println("singleItemGen: tierSize is 0, returning null");
                return null;
            }
//            uniMethod.overload(250);
//            uniMethod.printArray(tiered_copy_itemTierList);
            winningItemIndex = randGen.nextInt((winningTierSize)); // pick an index for an item
//            System.out.println("getting winningItemIndex: " + winningItemIndex);
            winningItem = tiered_copy_itemTierList.get(winningItemIndex); // get the item
//            System.out.println("getting winningItem: " + winningItem);
            tiered_copy_itemTierList.remove(winningItemIndex); // delete the index
//            System.out.println("deleting: " + winningItemIndex);

//            System.out.println("singleItemGen special attempt gen: " + winningItem);
        } while ((winningItem.getComplexity() > rule_allowedComplexity) || (!winningItem.getStocked())); // continue until it's valid

//        System.out.println("singleItemGen successfully generated: " + winningItem);
		return specializedItemCheck(winningItem);
	}

	/* Most common method used for generating 1 single
	 * random Item based on weightedArray.
	 *
	 * @param weightedProbability
	 * Uses the weightedArray to generate a random Item Tier and then
	 * generates a random Item that fits the Item Tier.
	 *
	 * this is the master branch
	 */
	public static Item singleItemGen(int[] weightedProbability) {
//		System.out.println("singleItemGen weightedProb was called");
		int winningTier = weightedProb(weightedProbability); // generate the winning itemTier
        return tunnel_singleItemGen(winningTier, null); // pick a tier, here is the tierList for that tier specifically
        // if it fails (return null), that means the entire tier is wrong, so customItemGen will know that

	}
    public static Item singleItemGen(int itemTier){
        return tunnel_singleItemGen(itemTier, null);
    }

    /*
    --------------------------------------------------------------------------------------------------
     */

    public static Item typeItemGen(int itemTier, Predicate<Item> filter, String itemType ){
        int winningTypeIndex = -1;

        for (int i = 0; i < winningTypeRaw.length; i++){
            if (winningTypeRaw[i].equals(itemType)){
                winningTypeIndex = i;
                break;
            }
        }

        if (winningTypeIndex == -1){
            return null;
        }
        else {
            return tunnel_typeItemGen(itemTier, filter, winningTypeIndex, null);
        }
    }

    public static Item typeItemGen(int[] weightedProb, Predicate<Item> filter, int winningTypeIndex){
        int[] weightedType = new int[8]; //(wip)

        weightedType[winningTypeIndex] = 1;

        return typeItemGen(weightedProb, filter, weightedType);
    }

    public static Item typeItemGen(int itemTier, Predicate<Item> filter, int[] weightedType){
        int[] weightedProb = new int[5]; //(wip)

        weightedProb[itemTier] = 1;

        return typeItemGen(weightedProb, filter, weightedType);
    }

    public static Item typeItemGen(int itemTier, Predicate<Item> filter, int winningTypeIndex){
        return tunnel_typeItemGen(itemTier, filter, winningTypeIndex, null);
    }

    /* This is the master branch

     */
    public static Item typeItemGen(int[] weightedProb, Predicate<Item> filter, int[] weightedType) { // master branch
//        System.out.println("typeItemGen(weightedProb, filter, weightedType) has run");
        int winningTier;
        Item winningItem;
        String winningType;
        int winningTypeIndex;
        Predicate<Item> winningTypeFilter;
//        ArrayList<String>[] blacklist = new ArrayList[5]; { // 5 tiers (wip)
//            blacklist[0] = new ArrayList<String>();
//            blacklist[1] = new ArrayList<String>();
//            blacklist[2] = new ArrayList<String>();
//            blacklist[3] = new ArrayList<String>();
//            blacklist[4] = new ArrayList<String>();
//        }

        int[] tracker_weightedProb = weightedProb.clone();
        int[][] tracker_weightMap = prepTrackerMap(weightedProb, weightedType);

        do {
            // generate a brand new winningTier and winningTypeFilter
            winningTier = weightedProb(tracker_weightedProb); // pick a tier
            if (winningTier == -1){ // if it's impossible to generate any tier
//                System.out.println("typeItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!!!!!!!!!");
//                System.out.println("typeItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!");
                return null; // the entire Master Branch breaks
            }

            winningTypeIndex = weightedProb(tracker_weightMap[winningTier]); // pick a type index

            // generate an item, this either succeeds or is null
            winningItem = tunnel_typeItemGen(winningTier, filter, winningTypeIndex, null);

            if (winningItem == null){ // if it's null, figure out why
//                System.out.println("typeItemGen: winningItem is null, why");
//                System.out.println("failure: " + winningTier + " and: " + winningTypeRaw[winningTypeIndex]);
//                uniMethod.printArray(tracker_weightMap[winningTier]);
//                blacklist[winningTier].add(winningType);

                // the generatedType at the specific itemTier is impossible
                tracker_weightMap[winningTier][winningTypeIndex] = 0; // ban the specific tier+type combination
//                uniMethod.printArray(tracker_weightMap[winningTier]);

                // check if the whole tier is null or not
                boolean weightType_fine = false;
                for (int x : tracker_weightMap[winningTier]){ // count to see if the whole typeProb is invalid
                    if (x != 0){ // there's a valid index in tracker_weightType
//                        System.out.println("tracker_weightType is fine, break");
                        weightType_fine = true;
                        break;
                    }
                }

                if (!weightType_fine){ // if the whole tier is null
//                    System.out.println("tracker_weightType is all 0's");

                    tracker_weightedProb[winningTier] = 0; // modify weightedProb to be impossible
//                    uniMethod.printArray(tracker_weightedProb);
                }
            }

        } while (winningItem == null); // continue until the item succeeds

        // this guarantees to return an item that works without breaking, unless every weightedType outcome is rigged
//        System.out.println("typeItemGen has finished with: " + winningItem);
        return winningItem;
    }


    /* This is the Side Branch

     */

    private static Item tunnel_typeItemGen(int itemTier, Predicate<Item> filter, int itemTypeIndex, ArrayList<Item>[] copy_itemTierList){
        // this just means customItemGen 1 item with just an itemTier and itemType
        Predicate<Item> winningTypeFilter = typeFilter(itemTier, itemTypeIndex);


        if (copy_itemTierList == null){ // if no map has been provided (called by Side Branch)
            copy_itemTierList = itemTierList.clone();
        }

        // step 1, create a map
        ArrayList<Item> tiered_copy_itemTierList = (ArrayList<Item>) copy_itemTierList[itemTier].clone();



        // attempt to generate the item (using customItemGen)
            if (filter != null){ // if calling typeItemGen(weightedProb, filter, weightedType)
                // generate the winningItem based on the given tier and typeFilter
//                System.out.println("running typeItemGen filter != null");
                winningTypeFilter = filter.and(winningTypeFilter);
//                return tunnel_customItemGen(itemTier, filter.and(winningTypeFilter), tiered_copy_itemTierList);
            }
//            else { // otherwise you called typeItemGen(weightedProb, null, weightedType)
//                System.out.println("running typeItemGen filter == null");
//
//            }
            return tunnel_customItemGen(itemTier, winningTypeFilter, tiered_copy_itemTierList);

        /* this is just running it once, either works or doesn't work.
        If it doesn't work, then the itemTier and itemTypeIndex doesn't work. That's it.

         */
    }

    private static int[] typeAntiSoftlock (int winningTier, int[] weightedType){
        int[] modified_weightedType = weightedType.clone(); // generate a copy

        // anti softlock tier mechanism
        if (winningTier == 1 || winningTier == 2 || winningTier == 3) { // if it's T1,T2 or T3
            // artifacts can't be generated
            modified_weightedType[3] = 0; // artifact
        }
        else if (winningTier == 4){ // if it's T4
            // items, hazards, and buildings can't be generated
            modified_weightedType[0] = 0;
            modified_weightedType[1] = 0;
            modified_weightedType[2] = 0;

            // make artifacts slightly more common as passives/powers rarely generate
            modified_weightedType[3] += 3; // artifact
            modified_weightedType[4] = 0;
            modified_weightedType[6] = 0;
        }

        return modified_weightedType;
    }

    private static Predicate<Item> typeFilter(int winningTier, int itemTypeIndex){
        // afterwards, THEN generate the winningType from the modified weightedProbability
        String winningType = winningTypeRaw[itemTypeIndex]; // generate the winning index of typeProbability

        Predicate<Item> typeFilter = item -> (item.getType().equals(winningType));
        return typeFilter;
    }

    private static Predicate<Item> typeFilter(int winningTier, int[] weightedType){
        int[] modified_weightedType = typeAntiSoftlock(winningTier, weightedType); // don't mess up weightedType

        // afterwards, THEN generate the winningType from the modified weightedProbability
        String winningType = winningTypeRaw[weightedProb(modified_weightedType)]; // generate the winning index of typeProbability
        Predicate<Item> typeFilter = item -> (item.getType().equals(winningType));
        return typeFilter;
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
	 * shopList[itemSlot - 1] = purchaseItem(shopList, weightedShopArray[shopTier]); // switch out the old item
	 * Usually the item that is generated is put into the same shopList/itemArray array.
	 *
	 */

//    public static <T> Item noDupeItemGen(Item[] itemArray, T pattern, Predicate<Item> filter, int[] weightedType) {
//        /*
//        if only pattern, singleItemGen
//        else if pattern and filter, customItemGen
//        else if pattern and weightedType, typeItemGen
//        else if pattern, filter and weightedType, typeItemGen but add the predicate to customItemGen
//         */
//
//        ItemGenMethod itemGen;
//
//        if (filter == null && weightedType == null) {
//            // singleItemGen
//            itemGen = singleGen;
//        }
//        else if (weightedType == null){
//            // customItemGen
//            itemGen = customGen;
//        }
//        else if (filter == null){
//            // typeItemGen
//            itemGen = typeGen;
//            itemGen.runGenerate(pattern, weightedType);
//        }
//        else {
//            // custom/typeItemGen?
//            // runGenerate needs to find typeFilter and combine it with customGen
//            itemGen = singleGen;
//        }
//
//        /*next things to do
//        get rid of itemGenMethods (now rely on just pure method overloading, convenient to user not code)
//        remove typeGen/old one
//        itemGen.generateItem(pattern, filter.and(typeFilter(weightedType)))
//        maybe check if no filter, then and() is base True and x or breaks or something, place if statements
//        BoundItemGen stays the same
//        itemGenMethods only exist for convenient item generation, not as a parameter
//         */
//
//        /*
//        if singleGen(pattern, filter)
//        if customGen(pattern, filter)
//        if typeGen(int[] pattern, int[] typeProb) -> typeGen(int pattern, filter)
//
//        goal:
//        runGenerate(pattern) = singleGen
//        runGenerate(pattern, filter) = customGen
//        runGenerate(pattern, typeProb)
//         */
//
//
//    }


    /* If itemArray is the following:
    - 0 length, it just generates and doesn't compare
    - null is in itemArray[i] = ignore, is not a duplicate


     */

    /* This is the Side Branch

     */
    private static Item tunnel_noDupeItemGen(int itemTier, Predicate<Item> filter, int itemTypeIndex, Item[] toReference_itemArray, ArrayList<Item>[] copy_itemTierList) {
        // this just means customItemGen 1 item with just an itemTier and itemType

        if (copy_itemTierList == null){ // if no map has been provided (called by Side Branch)
            copy_itemTierList = itemTierList.clone();
        }

        // go through itemArray, remove every instance from copy_itemTierList
        for (Item dupe_item : toReference_itemArray){
            if (!dupe_item.getType().equals("Hazard")){ // if there's a (3/3) Hazard
                copy_itemTierList[dupe_item.getTier()].remove(dupe_item);
            }
            else {
                String dupe_item_name = dupe_item.getName();
                String better_name = dupe_item_name.substring(0,dupe_item_name.length()-6);
//                System.out.println("DUPE DETECTED OF: " + better_name);
                Item correct_item = itemNameList.get(better_name);
                copy_itemTierList[dupe_item.getTier()].remove(correct_item);
            }

//            System.out.println("noDupeItemGen-side removed: " + dupe_item);
        }

        return tunnel_typeItemGen(itemTier, filter, itemTypeIndex, copy_itemTierList);

    }

    public static Item noDupeItemGen(Item[] toReference_itemArray, BoundItemGen itemBound){
        return noDupeItemGen(toReference_itemArray, itemBound.getPattern(), itemBound.getFilter(), itemBound.getWeightedType());

        /* get raw variables of itemBound


        call tunnel_itemGen(raw1, raw2, raw2, etc)

         */

        /* honestly BoundItemGen only exists to make noDupeItemGen and multiItemGen more
        convenient by prepping it with premade typeItemGen variables.
        - you could use these methods with singleItemGen and customItemGen while working,
        but it's inefficient and generally no point.

         */
    }

    /* This is the Master Branch

     */
	public static Item noDupeItemGen(Item[] toReference_itemArray, int[] weightedProb, Predicate<Item> filter, int[] weightedType){
//        System.out.println("noDupeItemGen has been called");
		// generate a new Item based on shopList for no duplicates
		Item winningItem;
        int winningTier;
        int winningTypeIndex;

		boolean yesDupes;
        int[] tracker_weightedProb = weightedProb.clone();
        int[][] tracker_weightMap = prepTrackerMap(weightedProb, weightedType);

		do {
            // generate a brand new winningTier and winningTypeFilter
            winningTier = weightedProb(tracker_weightedProb); // pick a tier
            if (winningTier == -1){ // if it's impossible to generate any tier
//                System.out.println("noDupeItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!!!!!!!!!");
//                System.out.println("noDupeItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!");
                return null; // the entire Master Branch breaks
            }

            winningTypeIndex = weightedProb(tracker_weightMap[winningTier]); // pick a type index

            // generate an item, this either succeeds (no duplicates in itemArray) or is null
            winningItem = tunnel_noDupeItemGen(winningTier, filter, winningTypeIndex, toReference_itemArray, null);

            if (winningItem == null){ // if it's null, figure out why
//                System.out.println("noDupeItemGen: winningItem is null, why");
//                System.out.println("failure: " + winningTier + " and: " + winningTypeRaw[winningTypeIndex]);
//                uniMethod.printArray(tracker_weightMap[winningTier]);
//                blacklist[winningTier].add(winningType);

                // the generatedType at the specific itemTier is impossible
                tracker_weightMap[winningTier][winningTypeIndex] = 0; // ban the specific tier+type combination
//                uniMethod.printArray(tracker_weightMap[winningTier]);

                // check if the whole tier is null or not
                boolean weightType_fine = false;
                for (int x : tracker_weightMap[winningTier]){ // count to see if the whole typeProb is invalid
                    if (x != 0){ // there's a valid index in tracker_weightType
//                        System.out.println("tracker_weightType is fine, break");
                        weightType_fine = true;
                        break;
                    }
                }

                if (!weightType_fine){ // if the whole tier is null
//                    System.out.println("tracker_weightType is all 0's");

                    tracker_weightedProb[winningTier] = 0; // modify weightedProb to be impossible
//                    uniMethod.printArray(tracker_weightedProb);
                }
            }

        } while (winningItem == null); // continue until the item succeeds

        // this guarantees to return an item that works without breaking, unless every weightedType outcome is rigged
//        System.out.println("typeItemGen has finished with: " + winningItem);
        return winningItem;
	} // ARRAY

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
	 *  @param Predicate<Item> filter
	 *
	 *  another version uses this:
	 *
	 *  @param itemArray
	 *
	 *  @param boundArray
	 *
	 *  Bound version is only used if the itemGen methods, filters and patterns
	 *  are always going to be the same.
	 *
	 *  Manual version is used if the itemGen methods change alot
	 */

    public static Item[] multiItemGen(Item[] toFill_itemArray, BoundItemGen[] boundArray){
//        System.out.println("multiItemGen-master has been called");

        // generate a new Item based on shopList for no duplicates
		Item winningItem;
        int winningTier;
        int winningTypeIndex;

        int[] tracker_weightedProb;
        int[][] tracker_weightMap;

        Predicate<Item> filter;

        Item[] tempArray;

		for (int i = 0; i < toFill_itemArray.length; i++) { // generate itemArray.length number of Items
            tracker_weightedProb = boundArray[i].getPattern().clone();
            tracker_weightMap = prepTrackerMap(tracker_weightedProb, boundArray[i].getWeightedType().clone());
            filter = boundArray[i].getFilter();

			do { // generate the next item and check if it's a dupe

                // generate a brand new winningTier and winningTypeFilter
                winningTier = weightedProb(tracker_weightedProb); // pick a tier
                if (winningTier == -1) { // if it's impossible to generate any tier
//                    System.out.println("multiItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!!!!!!!!!");
//                    System.out.println("multiItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!");

                    for (int j = i; j < toFill_itemArray.length; j++) { // the entire Master Branch breaks (itemArray)
                        toFill_itemArray[j] = null;
                    }

                    return toFill_itemArray;
                }

                winningTypeIndex = weightedProb(tracker_weightMap[winningTier]); // pick a type index
                tempArray = Arrays.copyOfRange(toFill_itemArray, 0, i); // prep the array

                // generate an item, this either succeeds (no duplicates in itemArray) or is null
                winningItem = tunnel_noDupeItemGen(winningTier, filter, winningTypeIndex, tempArray, null);

                if (winningItem == null) { // if it's null, figure out why
//                    System.out.println("multiItemGen: winningItem is null, why");
//                    System.out.println("failure: " + winningTier + " and: " + winningTypeRaw[winningTypeIndex]);
//                    uniMethod.printArray(tracker_weightMap[winningTier]);
//                blacklist[winningTier].add(winningType);

                    // the generatedType at the specific itemTier is impossible
                    tracker_weightMap[winningTier][winningTypeIndex] = 0; // ban the specific tier+type combination
//                    uniMethod.printArray(tracker_weightMap[winningTier]);

                    // check if the whole tier is null or not
                    boolean weightType_fine = false;
                    for (int x : tracker_weightMap[winningTier]) { // count to see if the whole typeProb is invalid
                        if (x != 0) { // there's a valid index in tracker_weightType
//                            System.out.println("tracker_weightType is fine, break");
                            weightType_fine = true;
                            break;
                        }
                    }

                    if (!weightType_fine) { // if the whole tier is null
//                        System.out.println("tracker_weightType is all 0's");

                        tracker_weightedProb[winningTier] = 0; // modify weightedProb to be impossible
//                        uniMethod.printArray(tracker_weightedProb);
                    }
                }

            } while (winningItem == null); // if there's dupes, generate an Item again

            toFill_itemArray[i] = winningItem; // add the new item to the next itemArray index

        }
		return toFill_itemArray;
    }

    public static Item[] multiItemGen(Item[] itemArray, BoundItemGen bound){
        return multiItemGen(itemArray, bound.getPattern(), bound.getFilter(), bound.getWeightedType());
    }

    public static Item[] multiItemGen(Item[] toFill_itemArray, int itemTier, Predicate<Item> filter, int[] weightedType){
        return multiItemGen(toFill_itemArray, patternConverter(itemTier), filter, weightedType);
    }

    /* This is the Master Branch

     */
	public static Item[] multiItemGen(Item[] toFill_itemArray, int[] weightedProb, Predicate<Item> filter, int[] weightedType) {
//		System.out.println("multiItemGen-master has been called");

        // generate a new Item based on shopList for no duplicates
		Item winningItem;
        int winningTier;
        int winningTypeIndex;

        int[] tracker_weightedProb = weightedProb.clone();
        int[][] tracker_weightMap = prepTrackerMap(weightedProb, weightedType);

        Item[] tempArray;

		for (int i = 0; i < toFill_itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe

                // generate a brand new winningTier and winningTypeFilter
                winningTier = weightedProb(tracker_weightedProb); // pick a tier
                if (winningTier == -1) { // if it's impossible to generate any tier
//                    System.out.println("multiItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!!!!!!!!!");
//                    System.out.println("multiItemGen: ALERT ALERT EVERY TIER IS IMPOSSIBLE!!!!!!!!!!!!!");

                    for (int j = i; j < toFill_itemArray.length; j++) { // the entire Master Branch breaks (itemArray)
                        toFill_itemArray[j] = null;
                    }

                    return toFill_itemArray;
                }

                winningTypeIndex = weightedProb(tracker_weightMap[winningTier]); // pick a type index
                tempArray = Arrays.copyOfRange(toFill_itemArray, 0, i); // prep the array

                // generate an item, this either succeeds (no duplicates in itemArray) or is null
                winningItem = tunnel_multiItemGen(winningTier, filter, winningTypeIndex, tempArray, null);

                if (winningItem == null) { // if it's null, figure out why
//                    System.out.println("multiItemGen: winningItem is null, why");
//                    System.out.println("failure: " + winningTier + " and: " + winningTypeRaw[winningTypeIndex]);
//                    uniMethod.printArray(tracker_weightMap[winningTier]);
//                blacklist[winningTier].add(winningType);

                    // the generatedType at the specific itemTier is impossible
                    tracker_weightMap[winningTier][winningTypeIndex] = 0; // ban the specific tier+type combination
//                    uniMethod.printArray(tracker_weightMap[winningTier]);

                    // check if the whole tier is null or not
                    boolean weightType_fine = false;
                    for (int x : tracker_weightMap[winningTier]) { // count to see if the whole typeProb is invalid
                        if (x != 0) { // there's a valid index in tracker_weightType
//                            System.out.println("tracker_weightType is fine, break");
                            weightType_fine = true;
                            break;
                        }
                    }

                    if (!weightType_fine) { // if the whole tier is null
//                        System.out.println("tracker_weightType is all 0's");

                        tracker_weightedProb[winningTier] = 0; // modify weightedProb to be impossible
//                        uniMethod.printArray(tracker_weightedProb);
                    }
                }

            } while (winningItem == null); // if there's dupes, generate an Item again

            toFill_itemArray[i] = winningItem; // add the new item to the next itemArray index

        }
		return toFill_itemArray;

	} // multiItemGen ARRAY

    /* This is the Side Branch

     */
    public static Item tunnel_multiItemGen(int itemTier, Predicate<Item> filter, int itemTypeIndex, Item[] toReference_itemArray, ArrayList<Item>[] copy_itemTierList) {
        // this just means customItemGen 1 item with just an itemTier and itemType

        if (copy_itemTierList == null){ // if no map has been provided (called by Side Branch)
            copy_itemTierList = itemTierList.clone();
        }

        return tunnel_noDupeItemGen(itemTier, filter, itemTypeIndex, toReference_itemArray, copy_itemTierList);

    }


    private static int[][] prepTrackerMap(int[] weightedProb, int[] weightedType){
        int[][] tracker_weightMap = new int[weightedProb.length][5]; // 5 tiers

        for (int i = 0; i < weightedProb.length; i++){ // prepare tracker_weightMap
//            tracker_weightMap[i] = typeAntiSoftlock(i, weightedType.clone()); // apply antiSoftlock on all prob
            tracker_weightMap[i] = weightedType.clone(); // apply antiSoftlock on all prob
        }

        return tracker_weightMap;
    }

    /* weightedProb is a function that takes in an int[] array and chooses+returns a
	 * random index number by treating each index value as a probability.
	 *
	 * @param weightedArray
	 * Basically it adds up all the numbers in weightedArray and generates a
	 * random number. If the random number fits in the range of the weightedArray
	 * indexes, then that's the winning index which is returned.
	 *
	 * (!)Failure: Return -1 if every index is 0 or there is 1 negative number,
	 *
	 *
	 */

	public static int weightedProb(int[] weightedArray) {
		int denoSum = 0; // used for variable type denominators, so denominator doesn't have to add up to 100

		// get the total denominator and scan through the array
		for (int i = 0; i < weightedArray.length; i++) {
			denoSum += weightedArray[i];
            if (weightedArray[i] < 0) {
                return -1;
            }
		}

        if (denoSum == 0){ // if everything is 0, return -1
            return -1;
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

    public static Item specializedItemCheck(Item targetItem) {
        String itemName = targetItem.getName();
        String itemType = targetItem.getType();

        if (itemName.equals(itemNameList.get("# in a bottle").getName())){
            int diceBottleNumber = randGen.nextInt(6) + 1;

            return itemNameList.get(diceBottleNumber + itemName.substring(1));
        }
        else if (itemType.equals("Hazard")){
            Item modifiedHazard = new Item(targetItem);

            // change quantity of items, get cost from that

            modifiedHazard.setName(modifiedHazard.getName() + " (3/3)");
            modifiedHazard.setCost(modifiedHazard.getCost() * 3);
            return modifiedHazard;
        }
        else {
            return targetItem;
        }

    }

    public static int[] patternConverter(int itemTier){
        int[] weightedProb = new int[5];
        weightedProb[itemTier] = 1;
        return weightedProb;
    }

    public static int[] typeConverter(int weightedTypeIndex){
        int[] weightedType = new int[8];
        weightedType[weightedTypeIndex] = 1;
        return weightedType;
    }

}

		/* ItemGenMethod is an abstract Interface where the lambda method
		 * MUST use the following parameters:
		 *
		 * @T pattern
		 * <T> is a type parameter which is similar to an Object but
		 * has more restrictions.
		 * In this case, pattern is usually an int (for itemTier) or
		 * int[] (for weightedProbability[])
		 *
		 * @Predicate<Item> filter
		 * Predicate<Item> is a lambda expression/mini method which you
		 * put some random filters that MUST return an Item.
		 *
		 * In this case, the parameter is usually singleGen or
		 * customGen variable which returns singleItemGen or customItemGen
		 * respectively
		 *
		 * singleGen and customGen are 2 ItemGenMethods or lambda expressions
		 * that MUST use both the pattern and filter within its parameters.
		 *
		 * When singleGen is called, it returns singleItemGen(pattern)
		 * When customGen is called, it returns customItemGen(pattern, filter)
		 *
		 */

		/* get rid of the use of singleGen and customGen
		 *
		 * function which only has a pattern and filter
		 * custom is used if filter
		 * otherwise only single
		 *
		 * each pattern has both an int and int[]
		 *
		 * ItemGenMethod is used for manual programming
		 * BoundItemGen is premade/preset item generation types
		 *
		 */