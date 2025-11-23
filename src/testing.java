
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import freemarker.cache.ConditionalTemplateConfigurationFactory;
import freemarker.core.TemplateConfiguration;
import universalThings.uniMethod;

import static snakesPackage.ColorData.*;
import static snakesPackage.ItemGenMethods.*;


import java.io.File;
import java.util.Scanner;
//import java.util.concurrent.Executors; // used for the save thread that activates overtime
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import java.awt.*;
import java.awt.datatransfer.*;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class testing {
    public static void main(String[] args) {

        String a = null;
        System.out.println(a == null);

    }
}

//    public static void resetTierList(){
//
//    }

    /* Used to pre-define the singleGen and customGen as actual variables,
    therefore you can put them as parameters in a method which basically says:
    "which itemGen am I using", and directly place the pattern and filter into
    singleGen/customGen which then runs singleItemGen or customItemGen.

     */

//    public interface ItemGenMethod<T> {
//		Item runGenerate(T pattern, Predicate<Item> filter);
//	}

//    public interface ItemGenMethod<T> {
//		Item runGenerate(T pattern, Predicate<Item> filter);
//
//        default Item runGenerate(int[]pattern, int[] weightedType) {
//            return runGenerate(pattern, typeFilter(weightedType));
//        }
//	}
//
////    public interface StaticGenMethod<T> extends ItemGenMethod {
////
////    }
//
//    public static final ItemGenMethod<Object> singleGen = (pattern, filter) -> {
//        if (pattern instanceof Integer) {
//            return singleItemGen((int)pattern);
//        }
//        else if (pattern instanceof int[]){
//            return singleItemGen((int[])pattern);
//        }
//        else {
//            throw new IllegalArgumentException("ItemGenMethod singleGen, Invalid pattern type: " + pattern.getClass());
//        }
//    };
//
//    public static final ItemGenMethod<Object> customGen = (pattern, filter) -> {
//        if (pattern instanceof Integer) {
//            return customItemGen((int)pattern, filter);
//        }
//        else if (pattern instanceof int[]){
//            return customItemGen((int[])pattern, filter);
//        }
//        else {
//            throw new IllegalArgumentException("ItemGenMethod customGen, Invalid pattern type: " + pattern.getClass());
//        }
//    };
//
//        public static final ItemGenMethod<Object> typeGen = (pattern, filter) -> {
//
//
//        if (pattern instanceof int[]) {
//            return customGen.runGenerate(pattern, filter);
//        }
//        else {
//            throw new IllegalArgumentException("ItemGenMethod typeGen, Invalid pattern type: " + pattern.getClass());
//        }
//    };

    /* the only way would be to:
    when using noDupeItemGen or multiItemGen with manual parameters,
    you have to specify customItemGen and have

    noDupeItemGen needs to decide like while running and not beforehand, meaning that
    it has to take int[] typeProb as an actual parameter

     it's supposed to look like this:
     itemHolder = itemGen.runGenerate(pattern, filter); // generate a random item but use itemGen to do so

     this means that the filter has to be automatically determined when runGenerate is ran
     Predicate<Item> filter = typeFilter(int[] weightedProb, int[] weightedType)

     this means that typeGen needs to be an itemGen with (int[] weightedProb, int[] weightedType),
     and it returns typeItemGen(weightedProb, weightedType)

     the issue is that ItemGenMethod is static, unless i make ItemGenMethod an interface that must have a pattern,
     staticGenMethod inherits from it containing singleItemGen(pattern) and customItemGen(pattern, filter), dynamicItemGen
     inherits it containing typeItemGen(pattern, typeProb)

     the DynamicGenMethod<Object> typeGen = (Object pattern, int[] weightedType) ->
     return customItemGen((int[])pattern, filter), where the filter is determined the moment
     typeGen.runGenerate is called
     */



    	/* Method which has 2 overloaded versions,
	 * takes in a pattern and filter and passes it to
	 * customItemGen or singleItemGen respectively.
	 *
	 * Fundamentally to generate an item, you either create a BoundItemGen
	 * and do bound1.runGenerate(), OR you don't create a BoundItemGen and just run
	 * generateItem(pattern, filter). Because a BoundItemGen requires the same
	 * parameters either way, generateItem is just a generic convenient method to use,
	 * while BoundItemGen is basically a premade object that has both a pattern and filter
	 * instance variable.
	 *
	 * @param T pattern
	 * The probability on how to generate an item.
	 * It's usually either:
	 * int itemTier, guarantees an item probability
	 * int[5] weightedProbability, each index represents a tier to select and then generate.
	 *
	 * @param Predicate<Item> filter
	 * A mini method which takes in an Item and returns something.
	 * Most methods require this to return a boolean, specifically
	 * as a filter for generating items in customItemGen
	 *
	 */

//	public static <T> Item generateItem(T pattern, Predicate<Item> filter) {
//		if (pattern instanceof Integer) { // generate a guaranteed Tier
//			return customItemGen((int)pattern, filter);
//		}
//		else if (pattern instanceof int[]){ // generate based on Probability
//			return customItemGen((int[])pattern, filter);
//		}
//		else {
//			throw new IllegalArgumentException("generateItem customGen, Invalid pattern type: " + pattern.getClass());
//		}
//	}
//
//	public static <T> Item generateItem(T pattern) {
//		if (pattern instanceof Integer) { // generate a guaranteed Tier
//			return singleItemGen((int)pattern);
//		}
//		else if (pattern instanceof int[]){ // generate based on Probability
//            return null;
////			return singleItemGen((int[])pattern);
//		}
//		else {
//			throw new IllegalArgumentException("generateItem singleGen, Invalid pattern type: " + pattern.getClass());
//		}
//	}
//
//    public static <T> Item generateItem(T pattern, T weightedType) {
//        return generateItem(pattern, null, weightedType);
////        if (pattern instanceof int[]){ // generate based on Probability
////
////		}
////		else {
////			throw new IllegalArgumentException("generateItem typeGen, Invalid pattern type: " + pattern.getClass());
////		}
//	}
//
//    public static <T> Item generateItem(T pattern, Predicate<Item> filter, T typeFilter) {
//		if (pattern instanceof Integer) { // generate a guaranteed Tier
//			// get correct filter, return customItemGen(itemTIER + modified filter)
//
//            //
//            /* run customItemGen(pattern, filter.and(typeItemGen(pattern, weightedType)))
//                - issue is that typeItemGen(pattern, weightedType) runs first
//                - you need customItemGen to run typeItemGen inside it
//                answer is customItemGen(pattern, filter, weightedType) -> special version
//             */
//            if (typeFilter instanceof Integer){
//                return null; // (wip)
////                return typeItemGen((int) pattern, filter, (int)typeFilter); // this means it either works or doesn't
//            }
//            else if (typeFilter instanceof int[]){
//                return typeItemGen((int) pattern, filter, (int[])typeFilter);
//            }
//            else {
//                throw new IllegalArgumentException("generateItem typeGen, Invalid typeFilter type: " + pattern.getClass());
//            }
//
//		}
//        else if (pattern instanceof int[]){ // generate based on Probability
//			if (typeFilter instanceof Integer){
//                return typeItemGen((int[]) pattern, filter, (int)typeFilter); // this means it either works or doesn't
//            }
//            else if (typeFilter instanceof int[]){
//                return typeItemGen((int[]) pattern, filter, (int[])typeFilter);
//            }
//            else {
//                throw new IllegalArgumentException("generateItem typeGen, Invalid typeFilter type: " + pattern.getClass());
//            }
//		}
//		else {
//			throw new IllegalArgumentException("generateItem typeGen, Invalid pattern type: " + pattern.getClass());
//		}
