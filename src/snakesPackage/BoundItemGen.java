package snakesPackage;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static snakesPackage.ItemGenMethods.*;

/* turn BoundItemGen into a class with instance variables:
- T pattern
- Predicate<Item> filter
- T weightedType

it then needs the following constructors for each combination which link a lambda function to it
- T pattern, Predicate<Item> filter -> generateItem()
- T pattern -> generateItem()
- T pattern, T weightedType -> generateItem()
- T pattern, Predicate<Item> filter, T weightedType -> generateItem()

it then needs getter/setter methods to change the instance variables
- if an instance variable is changed, check if it's an existing or new change
- if existing, lambda function stays the same
- otherwise, assign a new lambda function to it

a BoundItemGen is used by running boundGenerate() which takes

 */


public class BoundItemGen {
    private final int[] pattern;
    private final Predicate<Item> filter;         // optional filter
    private final int[] weightedType;             // optional weighted type array

    // Functional “lambda” method
    private final Supplier<Item> generator;

//    // Constructor for single pattern with optional filter
//    public BoundItemGen(Object pattern, Predicate<Item> filter) {
//        this.pattern = pattern;
//        this.filter = filter;
//        this.weightedType = null; // no weighted type for this constructor
//
//        // assign lambda based on constructor
//        this.generator = () -> {
//            if (pattern instanceof Integer) return singleItemGen((int)pattern);
//
//            else if (pattern instanceof int[]) return singleItemGen((int[])pattern);
//
//            else throw new IllegalArgumentException("Invalid pattern type");
//        };
//    }


    // “lambda call”
    public Item boundGenerate() {
        return generator.get();
    }

    // Getters for noDupeItemGen or other methods
    public int[] getPattern() { return pattern; }
    public Predicate<Item> getFilter() { return filter; }
    public int[] getWeightedType() { return weightedType; }

    public BoundItemGen(int[] pattern) {
        this.pattern = pattern;
        this.filter = null;
        this.weightedType = null;
        this.generator = () -> singleItemGen(pattern);
    }

    public BoundItemGen(int[] pattern, Predicate<Item> filter) { // customItemGen
	    this.pattern = pattern;
        this.filter = filter;
        this.weightedType = null;

        this.generator = () -> customItemGen(pattern, filter);

//        if (pattern instanceof Integer) { // generate a guaranteed Tier
//			this.generator = () -> customItemGen((int)pattern, filter);
//		}
//		else if (pattern instanceof int[]){ // generate based on Probability
//
//		}
//		else {
//			throw new IllegalArgumentException("generateItem singleGen, Invalid pattern type: " + pattern.getClass());
//		}
	}

    public BoundItemGen(int[] pattern, Predicate<Item> filter, int[] weightedType){
        this.pattern = pattern;
        this.filter = filter;
        this.weightedType = weightedType;

        this.generator = () -> typeItemGen(pattern, filter, weightedType);

    }

    public BoundItemGen(int pattern, Predicate<Item> filter, int[] weightedType){
        this.pattern = patternConverter(pattern);
        this.filter = filter;
        this.weightedType = weightedType;

        this.generator = () -> typeItemGen(pattern, filter, weightedType);
    }

    public BoundItemGen(int[] pattern, Predicate<Item> filter, int weightedType){
        this.pattern = pattern;
        this.filter = filter;
        this.weightedType = typeConverter(weightedType);

        this.generator = () -> typeItemGen(pattern, filter, weightedType);
    }

    public BoundItemGen(int pattern, Predicate<Item> filter, int weightedType){
        this.pattern = patternConverter(pattern);
        this.filter = filter;
        this.weightedType = typeConverter(weightedType);

        this.generator = () -> typeItemGen(pattern, filter, weightedType);
    }



    /* this system doesn't work because calling noDupeItemGen needs to communicate to customItemGen via parameters
    current:
    noDupeItemGen(itemArray)
        -> typeItemGen(pattern, typeFilter)
            -> customItemGen(pattern, typeFilter)[map]
                -> singleItemGen(pattern, map)

    ideal:
    noDupeItemGen(itemArray)[map] update map and try again, ->
        -> typeItemGen(pattern, typeFilter, map) ^fail
            -> customItemGen(pattern, typeFilter, map) ^fail
                -> singleItemGen(pattern, map) ^fail

    issue: methods are called not by raw but by generateItem, and it's impossible to smoothly add a 4th parameter
    solution:
    - make generateItem static/predetermined, parameters are done by BoundItemGen or null
    - multiItemGen(itemArray, pattern, null, null )[map] v update map, try again
        -> noDupeItemGen(itemArray, pattern, null, null, map)
            -> typeItemGen(pattern, null, null, map)
                -> customItemGen(pattern, null, boundItemGen, map) v update map(new pattern/type) , try again
                    -> singleItemGen(pattern, map) ^ fail

    the map is: (if bad success/fail, check if you can update, do but otherwise report up)
    tier_type_map=
        [0]{0,1,2,3,4,5,6,7}
        [1]{0,1,2,3,4,5,6,7}
        [2]{0,1,2,3,4,5,6,7}
        [3]{0,1,2,3,4,5,6,7}
        [4]{0,1,2,3,4,5,6,7}
    # this means ONLY update the map if the lower method failed
    weightedProb controls [4]{0,1,2,3,4,5,6,7}, fail = all [1-4]{0,1,2,3,4,5,6,7} doesn't work
    -> typeItemGen controls [4]{0,1,2,3,4,5,6,7}, fail = [4]{0,1,2,3,4,5,6,7} doesn't work
        -> customItemGen controls [4]{0}, fail = [4]{0} doesn't work
            -> singleItemGen controls [4], fail = [4] doesn't work

    2 types of maps:
    - tier_type_map, if copy_itemTierList failed, modify the tier_type_map index corresponding to that and try again
    - copy_itemTierList, local loot table used for generation

    is int itemTier or int[] weightedProb the main?
    - int uses 1 section of copy_itemTierList -> int[] weightedProb for itemTier, use section of copy_itemTierList[itemTier]
        -> if int fails, report to int[], int[] resets copy_itemTierList and updates tier_type_map

    follows this structure
    -> multiItemGen(itemArray, weightedProb, filter, typeFilter)[map] v update map, try again
        -> multiItemGen(itemArray, itemTier, filter, typeFilter, map )[map] -> corresponds to pick an item tier, test all possible types
            -> noDupeItemGen(itemArray, itemTier, filter, typeFilter, map)
                -> typeItemGen(itemTier, filter, typeFilter, map) if fail,
                    -> customItemGen(itemTier, null, map) if fail, ^ pick a new type, if no type available, report up
                        -> singleItemGen(itemTier, map) ^ fail, no more copy_itemTierList[itemTier]


    don't need to change the map, singleItemGen fails, this means customItemGen parameters are wrong
    - therefore, the passed down map needs to be local (for copy_itemTierList)

    if success, check update map/try again or true success
    else if fail, update map/try again
    else if map->fail, return fail, higher scope repeats this process

    if pattern = int != int[]
    -> multiItemGen(itemArray, int itemTier, null, null) -> multiItemGen(itemArray, int[] weightedProb, null, null)



     */

}