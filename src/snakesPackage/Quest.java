package snakesPackage;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import freemarker.template.*;

import static snakesPackage.ColorData.*;

public class Quest {
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
			temp = cfg.getTemplate("quest_template.ftl");
		} catch (Exception e) { // catch the error so the above code can run
			e.printStackTrace();
		}
	}
	private static StringWriter out = new StringWriter();
	private static Map root = new HashMap();

	private String QuestName;
	private String QuestDesc;
	private int QuestTier;
    private String QuestReward;
    private int QuestFee;

    private String QuestHtml;
    private String QuestPlain;

	public Quest(String QuestName, String QuestDesc, int QuestTier) {
		this.QuestName = QuestName;
		this.QuestDesc = QuestDesc;
		this.QuestTier = QuestTier;
	}

    public Quest(Quest toClone){
        QuestName = toClone.getName();
		QuestDesc = toClone.getDesc();
		QuestTier = toClone.getTier();
    }
	
	public String toString() {
		return QuestName;
	}
	
	public void fullPrint() {
		/* name + tier
		 * desc
		 * extra line
		 * reward
		 * cost
		 * 
		 */
		System.out.println(QuestName + ANSI_TIER[QuestTier] + " [Tier " + QuestTier + "]" + ANSI_RESET);
		System.out.println("- Description:");
		System.out.println("\t- " + QuestDesc);

        if (QuestReward != null){
            System.out.println("- Reward: " + QuestReward);
        }

		System.out.println("- Cancellation Fee: " + QuestTier * 3); //(WIP)
		
	}

    public void createPlain(){
        String plainHolder = "";
        plainHolder += QuestName + " [Tier " + QuestTier + "]" + "\r\n";
		plainHolder += "- Description:" + "\r\n";
		plainHolder += "\t- " + QuestDesc + "\r\n";

        if (QuestReward != null){
            plainHolder += "- Reward: " + QuestReward + "\r\n";
        }

		plainHolder += "- Cancellation Fee: " + QuestTier * 3; //(WIP)

        QuestPlain = plainHolder;
    }

    public void createHtml(){
        try {
            root.put("QuestName", QuestName);
            root.put("QuestTier", QuestTier);
            root.put("QuestFee", QuestTier * 3);
            root.put("QuestDesc", QuestDesc);
            root.put("QuestReward", QuestReward);
            root.put("QuestTierColor", HEX_TIER[QuestTier]);
            temp.process(root, out);
            QuestHtml = out.toString();
//            return out.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
//        	return "[HTML GENERATION ERROR]";
        } finally {
        	root.clear(); // reset the root regardless
        	out.getBuffer().setLength(0); // reset StringWriter as it accumulates
        }
    }

    public String getHtml(){
        return QuestHtml;
    }

    public String getPlain(){
        return QuestPlain;
    }

	// getter method
	public String getName() {
		return QuestName;
	}
	
	public int getTier() {
		return QuestTier;
	}
	
	public String getDesc() {
		return QuestDesc;
	}

    public String getReward() { return QuestReward; }

    public void setReward(String reward) { QuestReward = reward; }

    public int getFee() { return QuestFee;}

    public void setFee(int fee) { QuestFee = fee; }

    public void setDesc(String desc) {QuestDesc = desc;}

}
