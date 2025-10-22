package snakesPackage;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemCSVMaker {
    static HashMap<String, Item> itemDatabase = new HashMap<String, Item>(); // holds EVERY Item based on ItemName, itemTierList and itemNameList are lesser
	static ArrayList<Item>[] itemTierList; // ArrayList[3] for each ItemTier, where each index is an ArrayList<Item>
	static HashMap<String, Item> itemNameList = new HashMap<String, Item>(); // exact same as itemDatabase, except some Items are excluded by ruleComplexity

    final static int TIER_AMOUNT = 6; // means total, so 5 tiers means 6 total (including T0)

    /* what is needed:
    issue: complexity
    itemTierList: used for VERY convenient loot generators
    itemNameList: has everything, used for the archive

    excluded items: check by complexity value, manually remove

    how itemGen methods work:
    - pick random tier (according to pattern), then pick random number for the item
    - first check item associated to the random number:
    -   if it's bad complexity, redo function
    -   otherwise return the item

    this approach, nothing is hard coded, complexity can be changed mid round
    downside: all items have to be checked beforehand

    when creating itemDatabase, one instance variable is the declared complexity lvl (in the main function)
    add a method to setComplexity
    all itemGen methods are inside the itemDatabase?
    - no, make something called itemGenMethods
    - itemGenSPECIFIC(pattern, filter)
    - itemGenSPECIFIC has a static instance variable called ItemComplexity, has to be changed itself as built into how items are picked anyways

    */

    public static ItemCSVMaker createDatabase(ArrayList<String[]> itemList) {
        // goal: create the itemDatabase, itemTierList and itemNameList

        itemTierList = new ArrayList[TIER_AMOUNT]; // 0 = exclusive, 1-3 = tier 1-3, 4 = artifacts
		for (int i = 0; i < TIER_AMOUNT; i++) { // Initialize every ArrayList in itemTierList
			itemTierList[i] = new ArrayList<>();
		}

        Item itemImport = null;

        for (String[] csvArray: itemList) {

            itemImport = new Item (
					csvArray[0], // name
					Integer.parseInt(csvArray[1]), // cost
					csvArray[2].split("<br>"), // description
					csvArray[3], // blurb
					csvArray[4], // range
					csvArray[5], // type
					csvArray[6].split("/"),// theme
					Integer.parseInt(csvArray[7]), // tier
					csvArray[8], // version
					Boolean.parseBoolean(csvArray[9].toLowerCase()), //isStocked
                    Integer.parseInt(csvArray[10]) // complexity
                    );

            itemNameList.put(itemImport.getName().toLowerCase(), itemImport);
            itemTierList[itemImport.getTier()].add(itemImport);
        }
        return null;
    }

    public static HashMap<String, Item> getNameList(){
        return itemNameList;
    }

    public static ArrayList<Item>[] getTierList(){
        return itemTierList;
    }

    /* required functions:
    something to actually access the itemDatabase
    - must be discernable between ItemNameList and ItemTierList

     */

}
