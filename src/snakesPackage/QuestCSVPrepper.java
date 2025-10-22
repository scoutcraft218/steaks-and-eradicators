package snakesPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class QuestCSVPrepper {


    public static ArrayList<String[]> parseQuests(File questFile) throws FileNotFoundException {
        if (!questFile.exists()){
            throw new FileNotFoundException("bob");
        }

//        // initialize questTierList
//		ArrayList<Quest>[] questTierList = new ArrayList[QUEST_TIER_AMOUNT]; // holds Quests based on QuestTier
//		for (int i = 0; i < 3; i++) { // initiate every ArrayList in itemIDList
//			questTierList[i] = new ArrayList<>();
//		}

        Scanner questScanner = new Scanner(questFile);
        String rawLine;
        String[] csvArray;
        ArrayList<String[]> questList = new ArrayList<>();
        
		questScanner.nextLine(); // skip the headers row

		// Iterate through questFile to create and save the Quests
		while (questScanner.hasNextLine()) {
			rawLine = questScanner.nextLine(); // stores the raw csv line as String
			csvArray = rawLine.split(";"); // splits the csv line as an Array
            questList.add(csvArray);
		} // while loop


        return questList;
		// Now: questArray contains the raw strings of each quest



    }


}
