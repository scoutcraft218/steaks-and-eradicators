package snakesPackage;

import java.util.function.Predicate;

import static snakesPackage.ItemGenMethods.generateItem;

@FunctionalInterface
public interface BoundItemGen {
    Item runGenerate();

	static <T> BoundItemGen bind(T pattern, Predicate<Item> filter) {
	    return () -> generateItem(pattern, filter);
	}

	static <T> BoundItemGen bind(T pattern) {
	    return () -> generateItem(pattern);
	}

}

/* BoundItemGen is a helper interface for generating items which says that
	 * if the method "whatever()" (which is bind() in this case) from some variable
	 * "chestBound" is called, it must take the parameters of bind() and forward
	 * it to the runGenerate() method which 100% returns an Item Object.
	 *
	 * An interface is basically an abstract lambda function, so a lambda function
	 * which doesn't do anything but MUST have a method attached to it (interface aspect)
	 * which takes its parameters and gives it to runGenerate()
	 *
	 *
	 * chestBound = bind(chestProb), uses singleItemGen with only a weighted probability
	 * shopBound = bind(weightedShopArray), uses singleItemGen also
	 * startBound = bind(1, item -> (!item.getType().equals("Passive"))), uses customItemGen, T1 with NO passives
	 *
	 */

	/* boundArray holds the ItemGenType, pattern and filter
	 * for how to generate an Item for each item in startArray[player X]
	 *
	 * While generating an Item, call boundArray[i].runGenerate() which calls:
	 * bound1.runGenerate();                // You call the interface method (no args)
			-> bound1.generateItem(pattern, filter);
     			-> bound1.customItemGen(pattern, filter)
          			-> calls customItemGen
          				-> bound1 returns Item result from customItemGen
	 *
	 */

	/* problem:
	BoundItemGen is an object that exists in main, used to
	communicate to the item gen methods.

	A BoundItemGen contains a premade pattern and filter

	issue:
	BoundItemGen has to be defined in main for custom premade
	BoundItemGen patterns, like startBound or chestBound.

	However, BoundItemGen is fundamentally an object, and that
	object doesn't exist in the ItemGenMethods, which take a BoundItemGen
	as its parameter.

	Solution: Turn BoundItemGen into an object, with a
	"pattern" (int array) and "filter" (lambda function)
	instance variable.

	ItemGenMethods imports this class so it knows it physically exists

	main imports this class so that it can actually create BoundItemGens to then
    give to the ItemGenMethods as a parameter

    How:
    BoundItemGen has an interface which defines how it's supposed to look.
    BoundItemGen imports ItemGenMethods, so that whenever a BoundItemGen
    is binded, it binds it with the associated generateItem() which is
    associated to one of the 4 ItemGenMethods.
	 */