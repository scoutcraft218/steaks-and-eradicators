package snakesPackage;

import universalThings.uniMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ItemCSVPrepper {

    /* This function takes a File as a parameter and returns an
    ArrayList of the PREPPED strings of item variables to be
    used later



     */


    public static ArrayList<String[]> readFile(File itemFile) throws FileNotFoundException {

        if (!itemFile.exists()) {
            throw new FileNotFoundException("bob");
        }

        ArrayList<String[]> itemList = new ArrayList<String[]>();

        // Step 1. Declare the File variables
//		File itemFile = new File(ITEM_FileName); // turns ITEM_FileName into an actual File
		Scanner fileScanner = new Scanner(itemFile); // Scanner for the itemFile

		Item itemImport;
		String rawLine;
        String[] csvArray; // csvArray holds each split(";") line inside of fileScanner
		// Import all Items from itemFile into itemDatabase, itemNameList and itemTierList


        System.out.println(fileScanner.nextLine());

        while (fileScanner.hasNextLine()) { // for each line in the txt document
			rawLine = fileScanner.nextLine(); // stores the raw line in string
            csvArray = rawLine.split(";"); // splits the stored line as an array

            if (csvArray[9].isEmpty()) { // correlates to isStocked
                csvArray[9] = "True";
            }
            else {
                csvArray[9] = "False";
            }

            itemList.add(csvArray);
            uniMethod.printArray(csvArray);
		} // while loop

		fileScanner.close();
		// Now: allItems now holds every Item

        return itemList;
    }



}
