package snakesPackage;

import universalThings.uniMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.Random;

public class ItemGenMethods {
    private static int rule_allowedComplexity;
    private static boolean rule_simpleTheme;

    static ArrayList<Item>[] itemTierList;
    static HashMap<String, Item> itemNameList;
    private static final Random randGen = new Random(); // nextInt(min include, max exclude)

    public static void activateItemGenMethods(ArrayList<String[]> csvArray) {
        ItemCSVMaker itemDatabase = ItemCSVMaker.createDatabase(csvArray);
        itemTierList = ItemCSVMaker.getTierList();
        itemNameList = ItemCSVMaker.getNameList();
    }

    public static void setComplexity(int newComplexity){
        rule_allowedComplexity = newComplexity;
    }
    public static int getComplexity() { return rule_allowedComplexity; }


    /* Used to pre-define the singleGen and customGen as actual variables,
    therefore you can put them as parameters in a method which basically says:
    "which itemGen am I using", and directly place the pattern and filter into
    singleGen/customGen which then runs singleItemGen or customItemGen.

     */

    public interface ItemGenMethod<T> {
		Item runGenerate(T pattern, Predicate<Item> filter);
	}

    public static final ItemGenMethod<Object> singleGen = (pattern, filter) -> {
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

    public static final ItemGenMethod<Object> customGen = (pattern, filter) -> {
        if (pattern instanceof Integer) {
            return customItemGen((int)pattern, filter);
        }
        else if (pattern instanceof int[]){
            return customItemGen((int[])pattern, filter);
        }
        else {
            throw new IllegalArgumentException("ItemGenMethod customGen, Invalid pattern type: " + pattern.getClass());
        }
    };

    	/* Method which has 2 overloaded versions,
	 * takes in a pattern and filter and passes it to
	 * customItemGen or singleItemGen respectively.
	 *
	 * Fundamentally to generate an item, you either create a BoundItemGen
	 * and do bound1.runGenerate(), OR you don't create a BoundItemGen and just run
	 * generateItem(pattern, filter). Because a BoundItemGen requires the same
	 * parameters either way, generateItem is just a generic convenient method to use,
	 * while BoundItemGen is basically a premade object that has both a pattern and filter
	 * instance variable.
	 *
	 * @param T pattern
	 * The probability on how to generate an item.
	 * It's usually either:
	 * int itemTier, guarantees an item probability
	 * int[5] weightedProbability, each index represents a tier to select and then generate.
	 *
	 * @param Predicate<Item> filter
	 * A mini method which takes in an Item and returns something.
	 * Most methods require this to return a boolean, specifically
	 * as a filter for generating items in customItemGen
	 *
	 */

	public static <T> Item generateItem(T pattern, Predicate<Item> filter) {
		if (pattern instanceof Integer) { // generate a guaranteed Tier
			return customItemGen((int)pattern, filter);
		}
		else if (pattern instanceof int[]){ // generate based on Probability
			return customItemGen((int[])pattern, filter);
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
	 * @param filter
	 * Predicate which is basically a lambda expression AKA a portable mini
	 * method, used as the requirement for what to EXCLUDE from generated item
	 */
	public static Item customItemGen(int itemTier, Predicate<Item> filter) {
//		System.out.println("customItemGen itemTier was called");
		Item itemTemp;

		do {
			itemTemp = singleItemGen(itemTier);
			uniMethod.overload(999);
		} while (!filter.test(itemTemp)); // keep generating until the filter is met

		uniMethod.overloadReset();
		return itemTemp;
	}

	public static Item customItemGen(int[] weightedProbability, Predicate<Item> filter) {
//		System.out.println("customItemGen weightedProb was called");
		Item itemTemp;

		do {
			// generate a single item
			itemTemp = singleItemGen(weightedProbability);

			// record the item
			uniMethod.overload(999); // just in case it gets stuck in an infinite loop
		} while (!filter.test(itemTemp)); // keep generating until the filter is NOT met

		uniMethod.overloadReset();
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
		int winningTier = weightedProb(weightedProbability); // generate the winning itemTier
        int winningTierSize = itemTierList[winningTier].size();

        int winningItemIndex;
        Item winningItem;
        do {
             winningItemIndex = randGen.nextInt((winningTierSize));
             winningItem = itemTierList[winningTier].get(winningItemIndex);
        } while ((winningItem.getComplexity() > rule_allowedComplexity) || (!winningItem.getStocked()));

		return specializedItemCheck(winningItem);
	}

	/* Specialized version of above which is the same thing
	 * but the Item Tier is pre-picked
	 *
	 * @param itemTier
	 * Is a number which is the Tier of Item, only goes from 0-4.
	 */

	public static Item singleItemGen(int itemTier) {
//		System.out.println("singleItemGen itemTier was called");

		int winningTierSize = itemTierList[itemTier].size();

        int winningItemIndex;
        Item winningItem;
        do {
             winningItemIndex = randGen.nextInt((winningTierSize));
             winningItem = itemTierList[itemTier].get(winningItemIndex);
        } while ((winningItem.getComplexity() > rule_allowedComplexity) || (!winningItem.getStocked()));

		return specializedItemCheck(winningItem);
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

	public static <T> Item noDupeItemGen(Item[] itemArray, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> filter){

		// generate a new Item based on shopList for no duplicates
		Item itemHolder;
		boolean yesDupes;

		do {
			yesDupes = false; // assume there's dupes

			// generate an item
//			itemHolder = singleItemGen(weightedProbability);

			/* customItemGen
			 * itemGen = (int, Predicate<Item>) -> customItemGen(itemTier, filter)
			 */
			itemHolder = itemGen.runGenerate(pattern, filter); // generate a random item but use itemGen to do so

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

			// use BoundItemGen to generate an Item
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

//	public static <T> Item noDupeItemGen(ArrayList<Item> itemArray, T pattern, ItemGenMethod<T> itemGen, Predicate<Item> filter){
//
//		// generate a new Item based on shopList for no duplicates
//		Item itemHolder;
//		boolean yesDupes;
//
//		do {
//			yesDupes = false; // assume there's dupes
//
//			/* customItemGen
//			 * itemGen = (int, Predicate<Item>) -> customItemGen(itemTier, filter)
//			 */
//
//			// generate an item
////			itemHolder = singleItemGen(weightedProbability);
//			itemHolder = itemGen.runGenerate(pattern, filter); // generate a random item but use itemGen to do so
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

	public static <T> Item[] multiItemGen(Item[] itemArray, ItemGenMethod<T> itemGen, T pattern, Predicate<Item> filter) {
		Item winningItem = null; // used to store items
		boolean yesDupes = true;

		/* Generate the first Item
		 *
		 */
//		System.out.println("multiItemGen Array[] was called");

		itemArray[0] = itemGen.runGenerate(pattern, filter);// generate the first item
//		System.out.println("first item is: " + itemArray[0]);


		for (int i = 1; i < itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe
				yesDupes = false; // assume that there's no dupes but check anyways
				winningItem = itemGen.runGenerate(pattern, filter); // generate a random item
//				System.out.println("generated: " + itemHolder);

				for (int j = 0; j < i; j++) { // iterate through itemArray
					if (winningItem == itemArray[j]) { // if the generated Item is a dupe
//						System.out.println("DUPE DETECTED");
						yesDupes = true; // there actually are dupes
						break; // stop checking for dupes and let the while loop reset the item generation
					}
				} // for loop

			} while (yesDupes); // if there's dupes, generate an Item again

			itemArray[i] = winningItem; // add the new item to the next itemArray index
		}

		return itemArray;

	} // multiItemGen ARRAY

	public static <T> Item[] multiItemGen(Item[] itemArray, BoundItemGen[] genArray) {
		Item winningItem = null; // used to store items
		boolean yesDupes = true;

		// check if itemArray and filterArray are the same size

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
				winningItem = genArray[i].runGenerate(); // generate a random item
//				System.out.println("generated: " + itemHolder);

				for (int j = 0; j < i; j++) { // iterate through itemArray
					if (winningItem == itemArray[j]) { // if the generated Item is a dupe
//						System.out.println("DUPE DETECTED");
						yesDupes = true; // there actually are dupes
						break; // stop checking for dupes and let the while loop reset the item generation
					}
				} // for loop

			} while (yesDupes); // if there's dupes, generate an Item again

			itemArray[i] = winningItem; // add the new item to the next itemArray index
		}

		return itemArray;

	} // multiItemGen ARRAY

	public static <T> Item[] multiItemGen(Item[] itemArray, BoundItemGen genType) {
		Item itemHolder = null; // used to store items
		boolean yesDupes = true;

		// check if itemArray and filterArray are the same size


//		System.out.println("multiItemGen Array[] was called");

//		itemArray[0] = genType.runGenerate();// generate the first item
//		System.out.println("first item is: " + itemArray[0]);


		for (int i = 0; i < itemArray.length; i++) { // generate itemArray.length number of Items
			do { // generate the next item and check if it's a dupe
				yesDupes = false; // assume that there's no dupes but check anyways
				itemHolder = genType.runGenerate(); // generate a random item
//				System.out.println("generated: " + itemHolder);

				for (int j = 0; j < i; j++) { // iterate through itemArray
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