import snakesPackage.Item;
import snakesPackage.ItemCSVMaker;
import snakesPackage.ItemCSVPrepper;
import snakesPackage.ItemGenMethods;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class testing {
    public static void main(String[] args) throws FileNotFoundException {
        final String ITEM_FileName = "Snakes items list_testing.csv";
        File itemFile = new File(ITEM_FileName);

        ArrayList<String[]> itemList = ItemCSVPrepper.readFile(itemFile); // parse csv_reader in itemList
        ItemCSVMaker.createDatabase(itemList); // parse itemNameList and itemTierList
        HashMap<String, Item> itemNameList = ItemCSVMaker.getNameList();
        ArrayList<Item>[] itemTierList = ItemCSVMaker.getTierList();
        ItemGenMethods.activateItemGenMethods(itemList);
        ItemGenMethods.setComplexity(3);

        itemNameList.get("blunderbuss").fullPrint();

    }
}
