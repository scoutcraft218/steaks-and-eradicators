package snakesPackage;

import java.util.HashMap;

public class ColorData {
    // used for Item Tiers (WIP) the variables probably don't exist?
	final static String[] HEX_TIER = new String[6];
    private static boolean rule_simpleTheme = false;

    static {
		HEX_TIER[0] = "#ffd966";
		HEX_TIER[1] = "#b1d0a4";
		HEX_TIER[2] = "#a4c2f4";
		HEX_TIER[3] = "#dd7e6b";
		HEX_TIER[4] = "#c585d1";
		HEX_TIER[5] = "#4eabb1";
	}

	// used for Item Tiers
	final static String[] ANSI_TIER = new String[6];
    static {
		for (int i = 0; i < HEX_TIER.length; i++) {
			ANSI_TIER[i] = hexToAnsi(HEX_TIER[i]);
		}
	}

	final static String ANSI_RESET = "\033[0m";
	final static String RGB_RESET = "[255, 255, 255]";

	final static HashMap<String, String> HEX_TYPE = new HashMap<String, String>();
	static {
		HEX_TYPE.put("Item", "#ea9999");
		HEX_TYPE.put("Hazard", "#f9cb9c");
		HEX_TYPE.put("Building", "#ffe599");
		HEX_TYPE.put("Artifact", "#b6d7a8");
		HEX_TYPE.put("Passive", "#a2c4c9");
		HEX_TYPE.put("Curse", "#b4a7d6");
		HEX_TYPE.put("Hazard/Item", "#ff9900");
		HEX_TYPE.put("Power", "#4ad3e2");
	}
    final static HashMap<String, String> HEX_THEME_COMPLEX = new HashMap<String,String>();
        static {
            HEX_THEME_COMPLEX.put("Gold", "#f1c232");
            HEX_THEME_COMPLEX.put("Steal", "#ee560b");
            HEX_THEME_COMPLEX.put("Gain", "#c4d313");
            HEX_THEME_COMPLEX.put("Mobility", "#43e243");
            HEX_THEME_COMPLEX.put("Reverse", "#d5a6bd");
            HEX_THEME_COMPLEX.put("Hinder", "#a6c29b");
            HEX_THEME_COMPLEX.put("Relocate", "#d33ed2");
            HEX_THEME_COMPLEX.put("Creation", "#00ffff");
            HEX_THEME_COMPLEX.put("Change", "#f7007a");
            HEX_THEME_COMPLEX.put("Opportunity", "#4ca7fa");
            HEX_THEME_COMPLEX.put("Destruction", "#73a6b3");
            HEX_THEME_COMPLEX.put("Scale", "#d9d9d9");
            HEX_THEME_COMPLEX.put("Reroll", "#d9ead3");
            HEX_THEME_COMPLEX.put("Constant", "#c9daf8");
        }
    final static HashMap<String, String> HEX_THEME_SIMPLE = new HashMap<String,String>();
		static { // use the simplified colors
			HEX_THEME_SIMPLE.put("Gold", "#ffff00");
			HEX_THEME_SIMPLE.put("Steal", "#ffff00");
			HEX_THEME_SIMPLE.put("Gain", "#ffff00");
			HEX_THEME_SIMPLE.put("Mobility", "#43e243");
			HEX_THEME_SIMPLE.put("Reverse", "#43e243");
			HEX_THEME_SIMPLE.put("Hinder", "#43e243");
			HEX_THEME_SIMPLE.put("Relocate", "#43e243");
			HEX_THEME_SIMPLE.put("Creation", "#00ffff");
			HEX_THEME_SIMPLE.put("Change", "#00ffff");
			HEX_THEME_SIMPLE.put("Opportunity", "#00ffff");
			HEX_THEME_SIMPLE.put("Destruction", "#00ffff");
			HEX_THEME_SIMPLE.put("Scale", "#d9d9d9");
			HEX_THEME_SIMPLE.put("Reroll", "#d9ead3");
			HEX_THEME_SIMPLE.put("Constant", "#c9daf8");
		}

    static HashMap<String, String> HEX_THEME = HEX_THEME_COMPLEX;
    static HashMap<String, String> ANSI_THEME = new HashMap<String, String>(); // based on HEX_THEME, fill ANSI_THEME using hexToAnsi()

    static {
        HEX_THEME.forEach(
				(key, value)
				-> ANSI_THEME.put(key, hexToAnsi(value))
				);
    }




    final static HashMap<String,String> ANSI_TYPE = new HashMap<String, String>();
    static {
		// based on HEX_THEME, fill ANSI_THEME using hexToAnsi()
		HEX_TYPE.forEach(
				(key, value)
				-> ANSI_TYPE.put(key, hexToAnsi(value))
				);
	}
    // constructors
    public static boolean getSimpleTheme() {
        return rule_simpleTheme;
    }

    public static void refactorColorTheme(){
        System.out.println("activating refactorColorTheme");
        rule_simpleTheme = !rule_simpleTheme;

        if (rule_simpleTheme){
            HEX_THEME = HEX_THEME_SIMPLE;
        }
        else {
            HEX_THEME = HEX_THEME_COMPLEX;
        }

        HEX_THEME.forEach(
				(key, value)
				-> ANSI_THEME.put(key, hexToAnsi(value))
				);


    }


    // helper methods
    private static String hexToAnsi(String hex) {
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


}
