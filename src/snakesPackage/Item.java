package snakesPackage;

import freemarker.template.*;
import universalThings.uniMethod;

import java.util.*;
import java.io.*;
import static snakesPackage.ColorData.*;

public class Item {
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
	
	public static int TotalItem = 0;
	private String ItemName;
	private int ItemCost;
	private String[] ItemDesc;
	private String ItemBlurb;
	private String ItemRange;
	private String ItemType;
	private String[] ItemTheme;
	private int ItemTier;
	private String ItemVersion;
	private int ItemComplexity;
    private boolean isStocked;

	private String ItemHtml;
	private String ItemPlain;
	
	/* You should do this ONLY ONCE in the whole application life-cycle:        */

    /* Create and adjust the configuration singleton */
    static Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
    
    static {
    	try {
    		cfg.setDirectoryForTemplateLoading(new File("templates")); // import template file
            // Recommended settings for new projects:
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
    	} catch (IOException e){ // catch the error so that the above code can run
    		e.printStackTrace();
    	}
    }
    
    /* ------------------------------------------------------------------------ */
    /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */
	
    
    static boolean printBlurb = false; // whether fullPrint should print the flavor text or not
    static boolean simpleTheme = false;
    
    /* -------------------------------------------------------------------------- */
    
	private static Template temp; // exists for every Item
	static {
		try {
			temp = cfg.getTemplate("item_template.ftl");
		} catch (Exception e) { // catch the error so the above code can run
			e.printStackTrace();
		}
	}
	private static StringWriter out = new StringWriter();
	private static Map root = new HashMap();
	
	// color variables
	


    //********************************************************************
	public Item(
			String ItemName, // 0
			int ItemCost, // 1
			String[] ItemDesc, // raw string, split to String[] ItemDesc after // 3
			String ItemBlurb, // 2
			String ItemRange,
			String ItemType,
			String[] ItemTheme,
			int ItemTier,
			String ItemVersion,
            boolean isStocked,
			int ItemComplexity
            ) {
		
		TotalItem++;
		this.ItemName = ItemName;
		this.ItemCost = ItemCost;
		this.ItemDesc = ItemDesc;
		this.ItemBlurb = ItemBlurb;
		this.ItemRange = ItemRange;
		this.ItemType = ItemType;
		this.ItemTheme = ItemTheme;
		this.ItemTier = ItemTier;
		this.ItemVersion = ItemVersion;
		this.ItemComplexity = ItemComplexity;
        this.isStocked = isStocked;

		this.ItemHtml = createHtml();
		this.ItemPlain = this.createPlain();
	}

    public Item(Item toClone){
        this.ItemName = toClone.getName();
		this.ItemCost = toClone.getCost();
		this.ItemDesc = toClone.getDesc();
		this.ItemBlurb = toClone.getBlurb();
		this.ItemRange = toClone.getRange();
		this.ItemType = toClone.getType();
		this.ItemTheme = toClone.getTheme();
		this.ItemTier = toClone.getTier();
		this.ItemVersion = toClone.getVersion();
		this.ItemComplexity = toClone.getComplexity();
        this.isStocked = isStocked;

		this.ItemHtml = createHtml();
		this.ItemPlain = this.createPlain();

    }

	public String toString() {
		return ItemName;
	}
	
	/* Method which prints an Item in fancy format, including:
	 * Name + Tier
	 * Blurb
	 * Cost
	 * Desc (and its lines)
	 * Type
	 * Theme (split between "/")
	 * Version
	 * 
	 * NOT included:
	 * ID
	 * Complexity
	 * Html
	 * Plain
	 */
	
	public void nicePrint() {
		
		try { // try getting the respective color based on ItemTier
			System.out.println("Item Name: " + ItemName + ANSI_TIER[ItemTier] + " [Tier " + ItemTier + "]" + ANSI_RESET);
			
		} catch (Exception e) { // if the ItemTier is invalid, don't add color
			System.out.println("Item Name: " + ItemName + " [Tier " + ItemTier + "]");
		}
		
		if (!ItemBlurb.isEmpty()) { // print the blurb if it exists
			System.out.println("- " + ItemBlurb);
		}
		
		System.out.println("- Cost: " + ItemCost);
		System.out.println("- Description: ");
		
//		// blurb printing variables
//		int cutOff = 75; // cutoff range (CHANGEABLE)
//		
//		int blurbLength; // holds the length of the blurb
//		String blurbSlice = null; // holds blurb substring
//		int startIndex = 0; // starting index of substring
//		int endIndex; // end index of substring
//		int maxDivisions; // number of times blurb can get divided
		
		// print ItemDesc
		for (String ItemDescLine : ItemDesc) {
			System.out.println("\t- " + ItemDescLine);
		}
		
		System.out.println("- Range: " + ItemRange);
		System.out.println("- Type: " + ANSI_TYPE.get(ItemType) + ItemType + ANSI_RESET);
		
		// used before if ItemType color doesn't exist (Item/Hazard)
//		if (ANSI_THEME.get(ItemType) == null) {
//			System.out.println("- Type: " + ANSI_THEME.get("Hazard") + ItemType + ANSI_RESET);
//		}
//		else {
//			
//		}
		
		System.out.print("- Theme: ");
		for (int i = 0; i < ItemTheme.length; i++) { // go through ItemTheme
            System.out.print(ANSI_THEME.get(ItemTheme[i]) + ItemTheme[i] + ANSI_RESET); // print the colored Theme
			if (i != ItemTheme.length - 1) { // if it's not the last Theme, add the dividing bracket
				System.out.print(" / ");
			}	
		}
		System.out.println(); // conclude the line
		
		// print ItemVersion
		System.out.println("- Version: " + ItemVersion);
		System.out.println();
	} // fullPrint()

    public void fullPrint(){

        try { // try getting the respective color based on ItemTier
			System.out.println("Item Name: " + ItemName + ANSI_TIER[ItemTier] + " [Tier " + ItemTier + "]" + ANSI_RESET);

		} catch (Exception e) { // if the ItemTier is invalid, don't add color
			System.out.println("Item Name: " + ItemName + " [Tier " + ItemTier + "]");
		}

		if (!ItemBlurb.isEmpty()) { // print the blurb if it exists
			System.out.println("- " + ItemBlurb);
		}

		System.out.println("- Cost: " + ItemCost);
		System.out.println("- Description: ");

//		// blurb printing variables
//		int cutOff = 75; // cutoff range (CHANGEABLE)
//
//		int blurbLength; // holds the length of the blurb
//		String blurbSlice = null; // holds blurb substring
//		int startIndex = 0; // starting index of substring
//		int endIndex; // end index of substring
//		int maxDivisions; // number of times blurb can get divided

		// print ItemDesc
		for (String ItemDescLine : ItemDesc) {
			System.out.println("\t- " + ItemDescLine);
		}

		System.out.println("- Range: " + ItemRange);
		System.out.println("- Type: " + ANSI_TYPE.get(ItemType) + ItemType + ANSI_RESET);

		// used before if ItemType color doesn't exist (Item/Hazard)
//		if (ANSI_THEME.get(ItemType) == null) {
//			System.out.println("- Type: " + ANSI_THEME.get("Hazard") + ItemType + ANSI_RESET);
//		}
//		else {
//
//		}

		System.out.print("- Theme: ");

		for (int i = 0; i < ItemTheme.length; i++) { // go through ItemTheme
			System.out.print(ANSI_THEME.get(ItemTheme[i]) + ItemTheme[i] + ANSI_RESET); // print the colored Theme
			if (i != ItemTheme.length - 1) { // if it's not the last Theme, add the dividing bracket
				System.out.print(" / ");
			}
		}
		System.out.println(); // conclude the line

		// print ItemVersion
		System.out.println("- Version: " + ItemVersion);
        System.out.println("- Complexity: " + ItemComplexity);
        System.out.println("- isStocked " + isStocked);

		System.out.println();
    }
	
	public String createPlain() { // getPlain()
		/* GOAL TO PRINT:
		 * 
		<pre>
		Item Name: Grenade<span style="color: rgb(177, 208, 164);"> [Tier 1]</span>
		- Cost: 8
		- Description: 
		- Throw a Grenade which Explodes 3x3 at the Start of your Next Turn.
		- All Players who are inside the Explosion Lose 15 Gold.
		- Range: Line of Sight 7, All
		- Type: Item
		- Theme: Steal/Creation
		- Version: v1.2
		</pre>
		 * 
		 */
		
		String plainHolder = "";
		String strTemp;
		
		// 0 = ItemName
		// print the RGB_ARRAY into span style = color format
		strTemp = "Item Name: " + ItemName;
		strTemp += " [Tier " + ItemTier + "]" + "\r\n";
		
		plainHolder += strTemp;
		
		// 0 = ItemBlurb
		if (!ItemBlurb.isEmpty()) {
			strTemp = "- " + ItemBlurb + "\r\n";
			plainHolder += strTemp;
		}
		
		// 1 = ItemCost
		strTemp = "- Cost: " + ItemCost + "\r\n";
		plainHolder += strTemp;
		
		// 2 = ItemDesc
		strTemp = "- Description: " + "\r\n";
		
		for (String ItemDescLine : ItemDesc) {
			strTemp += "\t- " + ItemDescLine + "\r\n";
		}
		
		plainHolder += strTemp;
		
		// 3 = ItemRange
		strTemp = "- Range: " + ItemRange + "\r\n";
		plainHolder += strTemp;
		
		// 4 = ItemType
		strTemp = "- Type: " + ItemType + "\r\n";
		plainHolder += strTemp;
		
		// 5 = ItemTheme array
		strTemp = "- Theme: ";
		for (int i = 0; i < ItemTheme.length; i++) {
			strTemp += ItemTheme[i];
			
			if (i != ItemTheme.length - 1) {
				strTemp += " / ";
			}
		}
		strTemp += "\r\n";
		
		plainHolder += strTemp;
		
		// 6 = ItemVersion and blank line
		strTemp = "- Version: " + ItemVersion + "\r\n";
		plainHolder += strTemp;
		
		// merge all the variableArray to strTemp
		strTemp = ""; // reset strTemp
		
		return plainHolder; // return strTemp
	} // getPlain()
	
	private String[] hexConvert(String[] ItemTheme) {
		String[] newArray = new String[ItemTheme.length];
		
		for (int i = 0; i < newArray.length; i++) {
			newArray[i] = HEX_THEME.get(ItemTheme[i]);
		}
		
		return newArray;
	}
	
	private String createHtml() {
        try {
        	/* Create a data-model */
            root.put("ItemName", ItemName); // variable name, contents
            root.put("ItemTier", ItemTier);
            root.put("ItemTierColor", HEX_TIER[ItemTier]);
            root.put("ItemCost", ItemCost);
            root.put("ItemDesc", ItemDesc);
            root.put("ItemBlurb", ItemBlurb);
            root.put("ItemRange", ItemRange);
            root.put("ItemType", ItemType);
            root.put("ItemTypeColor", HEX_TYPE.get(ItemType));

//            uniMethod.printArray(ItemTheme);
//            uniMethod.printArray(hexConvert(ItemTheme));

            root.put("ItemTheme", ItemTheme);
            root.put("ItemThemeColor", hexConvert(ItemTheme)); // holds the hex for all ItemThemes based on HEX_THEME (WIP)


            /* ItemTheme is NOT split, need to handle somehow
             * 
             * ItemTheme will be a split String[]
             * ItemThemeColor is a same length String[]
             * 
             * pass both ItemTheme and ItemThemeColor
             * Template goes through both index and pairs both colors
             * need a for loop in Template to not repeat the last bracket
             * 
             *  ItemThemeColor would be a String[] which has multiple HEX_THEME colors
             *  template would iterate through it, 
             *  
             */
            
            root.put("ItemVersion", ItemVersion);
            
            temp.process(root, out);
            return out.toString();
        } catch (IOException | TemplateException e){
            e.printStackTrace();
        	return "[HTML GENERATION ERROR]";
        	
        } finally {
        	root.clear(); // reset the root regardless
        	out.getBuffer().setLength(0); // reset StringWriter as it accumulates
        	
        }
	}
	
	// get methods
	public String getName() {
		return ItemName;
	}
	
	public int getCost() {
		return ItemCost;
	}
	
	public String[] getDesc() {
		return ItemDesc;
	}
	
	public String getRange() {
		return ItemRange;
	}
	
	public String getType() {
		return ItemType;
	}
	
	public Item getItem() {
		return this;
	}
	
	public String[] getTheme() {
		return ItemTheme;
	}
	
	public int getTier() {
		return ItemTier;
	}
	
	public String getBlurb() {
		return ItemBlurb;
	}
	
	public String getVersion() {
		return ItemVersion;
	}

    public boolean getStocked() { return isStocked;}
	
	public static int getTotal() {
		return TotalItem;
	}
	
	public int getComplexity() {
		return ItemComplexity;
	}
	
	public String getHtml() {
		return this.ItemHtml;
	}
	
	public String getPlain() {
		return this.ItemPlain;
	}
	
	public static HashMap<String, String> getAnsiTheme() {
		return ANSI_THEME;
	}
	
	public static String getAnsiReset() {
		return ANSI_RESET;
	}
	
	public static String[] getAnsiTier() {
		return ANSI_TIER;
	}
	
	// set methods
	public void setName(String ItemName) {
		this.ItemName = ItemName;
	}
	
	public void setCost(int goldCost) {
		this.ItemCost = goldCost;
	}
	
	public void setBlurb(String ItemBlurb) {
		this.ItemBlurb = ItemBlurb;
	}
	

}

//// print blurb simple
//if (!itemTemp.getBlurb().isEmpty()) { // if the itemBlurb has something
//	System.out.println("  - " + itemTemp.getBlurb());
////	System.out.print("    ");
////	uniMethod.line(itemTemp.getBlurb().length(), '—');
//}

//// print blurb complex
//if (!itemTemp.getBlurb().isEmpty()) { // if the itemBlurb has something
//	itemBlurb = itemTemp.getBlurb();
//	blurbLength = itemTemp.getBlurb().length(); // get the length of blurb
//	
//	System.out.print("  ");
//	uniMethod.line(cutOff + 7, '—');
//	System.out.print("  - ");
//	
//	if (blurbLength >= cutOff) { // if blurb is over cutOff length
//		// find the next index where it's a space for smooth cutting
//		
//		maxDivisions = blurbLength/75; // see how many cutOff length slices there are
////		System.out.println("max divisions is: " + maxDivisions);
//		endIndex = cutOff; // set the end index as 50 as base case if the next word is too long
//		
//		for (int j = 0; j < maxDivisions; j++) { // divide the lines maxDivisions number of times
//			
//			for (int k = endIndex; k < blurbLength; k++) { // for each element in blurb
////				System.out.println("is " + itemBlurb.charAt(j) + " equal to space?");
//				if (itemBlurb.charAt(k) == ' ') { // if a space is detected
////					System.out.println("YES AT INDEX: " + j);
//					endIndex = k;
//					break;
//				} // if == space
//			} // k loop
////			System.out.println("startIndex no(" + j + ") is: " + startIndex);
////			System.out.println("endIndex no(" + j + ") is: " + endIndex);
//			
//			blurbSlice = itemBlurb.substring(startIndex, endIndex); // create a slice from 50 length
//			System.out.println(blurbSlice);
////			System.out.println("blurbSlice no." + j + ": " + blurbSlice); // print the blurb slice
//			
//			// Next: modify the index variables to repeat for each blurb slice
//			startIndex = endIndex;
//			endIndex += cutOff;
//			
//			
//			
//		} // j loop
////		System.out.println("startIndex is: " + startIndex);
////		System.out.println("endIndex TEMP is: " + endIndex);
//		
//		blurbSlice = itemBlurb.substring(startIndex, itemBlurb.length()); // create a slice from 50 length
//		System.out.println("   " + blurbSlice);
//		startIndex = 0;
//		endIndex = cutOff;
//		
//	} // if cutoff
//	
//	else {
//		System.out.println("   " + itemBlurb);
//	}
//	
//	System.out.print("  ");
//	uniMethod.line(cutOff + 7, '—');
//} // if blurb empty

// print description under the blurb
//if (itemTemp.getDescription().contains("<br>")) { // if itemDescription has "<br>" aka multiple lines
//	
//	Arrays.stream(itemTemp.getDescription().split("<br>"))
//		.forEach(element -> System.out.println("      - " + element));
//	
//}
//else { // otherwise print like normal
//	 System.out.println("\t- " + itemTemp.getDescription());
//}

//public String getHtml() { // getHtml()
////System.out.println("Item Name: Grenade[38;2;177;208;164m [Tier 1][0m\r\n"
////	+ "- Cost: 8\r\n"
////	+ "- Description: \r\n"
////	+ "	- Throw a Grenade which Explodes 3x3 at the Start of your Next Turn.\r\n"
////	+ "	- All Players who are inside the Explosion Lose 15 Gold.\r\n"
////	+ "- Range: Line of Sight 7, All\r\n"
////	+ "- Type: Item\r\n"
////	+ "- Theme: Steal/Creation\r\n"
////	+ "- Version: v1.2");
//
///* GOAL TO PRINT:
//* 
//<pre>
//Item Name: Grenade<span style="color: rgb(177, 208, 164);"> [Tier 1]</span>
//- Cost: 8
//- Description: 
//- Throw a Grenade which Explodes 3x3 at the Start of your Next Turn.
//- All Players who are inside the Explosion Lose 15 Gold.
//- Range: Line of Sight 7, All
//- Type: Item
//- Theme: Steal/Creation
//- Version: v1.2
//</pre>
//* 
//*/
//
///* fix bullet points
//* https://www.htmlgoodies.com/getting-started/so-you-want-indents-and-lists-huh/
//* 
//*/
//
//String[] variableArray = new String[7];
//String strTemp;
//String[] strArrayTemp;
//
//// 0 = ItemName
//// print the RGB_ARRAY into span style = color format
//strTemp = "Item Name: " + ItemName + "<span style=\"color: " + HEX_ARRAY[ItemTier] + ";\"> ";
//strTemp += "[Tier " + ItemTier + "] </span>" + "\r\n";
//
//variableArray[0] = strTemp;
//
//// 1 = ItemCost
//strTemp = "\t" + "- Cost: " + ItemCost + "\r\n";
//variableArray[1] = strTemp;
//
//
//// 2 = ItemDesc
//strTemp = "\t" + "- Description: " + "\r\n";
//
////if (ItemDesc.contains("<br>")) { // if ItemDesc has "<br>" aka multiple lines
////strArrayTemp = ItemDesc.split("<br>");
////
////for (String element : strArrayTemp) {
////	strTemp += "\t" + "\t- " + element + "\r\n";
////}
////
////}
////else { // otherwise print like normal
//// strTemp += "\t" + "\t- " + ItemDesc + "\r\n";
////}
//
//for (String ItemDescLine : ItemDesc) {
//strTemp += "\t" + "\t- " + ItemDescLine + "\r\n";
//}
//
//variableArray[2] = strTemp;
//
//// 3 = ItemRange
//strTemp = "\t" + "- Range: " + ItemRange + "\r\n";
//variableArray[3] = strTemp;
//
//// 4 = ItemType
//strTemp = "\t" + "- Type: " + ItemType + "\r\n";
//variableArray[4] = strTemp;
//
//// 5 = ItemTheme		
//strTemp = "\t" + "- Theme: " + ItemTheme + "\r\n";
//variableArray[5] = strTemp;
//
//// 6 = ItemVersion and blank line
//strTemp = "\t" + "- Version: " + ItemVersion + "\r\n";
//variableArray[6] = strTemp;
//
//// merge all the variableArray to strTemp
//strTemp = "<pre>" + "\r\n"; // reset strTemp
//
//for (String element : variableArray) { // merge variableArray to strTemp
//strTemp += element; // merge all the elements
//}
//strTemp += "</pre>";
//
////System.out.println("PRINTING HTML");
////System.out.println(strTemp);
//
//return strTemp; // return strTemp
//} // getHtml()
