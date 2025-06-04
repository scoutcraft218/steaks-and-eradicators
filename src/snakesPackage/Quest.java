package snakesPackage;

public class Quest {
	private String QuestName;
	private String QuestDesc;
	private int QuestTier;
	private int QuestID;
	
	public Quest(int QuestID, String QuestName, String QuestDesc, int QuestTier) {
		this.QuestID = QuestID;
		this.QuestName = QuestName;
		this.QuestDesc = QuestDesc;
		this.QuestTier = QuestTier;
	}
	
	public String toString() {
		return QuestName;
	}
	
	public void fullPrint(String questReward, String bonusReward) {
		/* name + tier
		 * desc
		 * extra line
		 * reward
		 * cost
		 * 
		 */
		System.out.println(QuestName + " (Tier " + QuestTier + ")");
		System.out.println("Description:");
		System.out.println("\t- " + QuestDesc);
		System.out.print("Reward: " + questReward);
		if (bonusReward != null) { // don't print the brackets if bonusReward is null
			System.out.print(" ("+ bonusReward + ")" + "\n");
		}
		
		System.out.println("Cost: " + (QuestTier * 7));
		
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
	
}
