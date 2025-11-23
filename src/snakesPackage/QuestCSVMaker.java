package snakesPackage;

import universalThings.uniMethod;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestCSVMaker {
    static HashMap<String, Quest> questNameList = new HashMap<String,Quest>();
    static ArrayList<Quest>[] questTierList;

    final static int QUEST_TIER_AMOUNT = 4;
    
    public static void createDatabase(ArrayList<String[]> questList) {
        Quest questTempLoad;

        questTierList = new ArrayList[QUEST_TIER_AMOUNT]; // 0 = exclusive, 1-3 = tier 1-3, 4 = artifacts
		for (int i = 0; i < QUEST_TIER_AMOUNT; i++) { // Initialize every ArrayList in itemTierList
			questTierList[i] = new ArrayList<>();
		}

        for (String[] csvArray : questList) {
            questTempLoad = new Quest(
					csvArray[0], // QuestName
					csvArray[1], // QuestReward
					Integer.parseInt(csvArray[2]) // QuestTier
					);

            uniMethod.printArray(csvArray);
//            System.out.println(questTempLoad);

			questNameList.put(questTempLoad.getName().toLowerCase(), questTempLoad);
			questTierList[questTempLoad.getTier()].add(questTempLoad); // each ArrayList represents a Tier of Quests

        }


    }

    public static HashMap<String, Quest> getNameList() {
        return questNameList;
    }

    public static ArrayList<Quest>[] getTierList(){
        return questTierList;
    }

}
