package snakesPackage;

public class DistanceBoardCalculator {
	public static void main(String[] args){
		/* method which takes the firstTile and secondTile number and calculates the distance between them
		 * it also assumes that each row on the grid does a squiggly like a snake and not each row start on the beginning
		 * 
		 * issue, wrap around is screwed (12 is good, 13 is messed up)
		 */
		
		int firstTile = 55;
		int secondTile = 85;
		
		int rowSize = 12;
		int columnSize = 12;
		int interval = 12;
		
		int maximumRow;
		int intervalCheck;
		
		// magic machine which returns 1 if divisible by interval: System.out.println(Math.abs((changes % 12) - 12)/12);
		
		// basically tells whenever it's on another row, ACCOUNTING FOR 12, 24, etc being on the same row
		int firstChange = ((firstTile / interval) - Math.abs((firstTile %
                interval) - interval)/interval)%2;
		int secondChange = ((secondTile / interval) - Math.abs((secondTile % interval) - interval)/interval)%2; // subtract by 1 ONLY if it's divisible by 12
		
		// 13 = 24, 1 = 12
		// 14 = 23, 2 = 11
		// 15 = 22, 3 = 10
		
		/* 13 = 1
		 * 25 = 2
		 * 37 = 3
		 * 
		 */
		
		
		System.out.println("firstChange is: " + firstChange);
		System.out.println("secondChange is: " + secondChange);
		
		System.out.println("firstTile is: " + firstTile);
		System.out.println("secondTile is: " + secondTile);
		
		// first part
		int firstX;
		int firstY;
		
		maximumRow = ((firstTile / (interval + Math.abs((firstTile % interval) - interval)/interval%2)) + 1) * interval;
		intervalCheck = Math.abs((firstTile % interval) - interval)/interval;
		
		if (firstChange == 1) { // reversed row
			firstX = Math.abs(firstTile - maximumRow - 1); // (firstTile / interval)
		}
		else {
			firstX = firstTile - (maximumRow - interval); // maximumRow but minus 1 interval (24 - 25) instead of (36 - 25)
		}
		
		firstY = (firstTile / (interval + intervalCheck)) + 1;
		
		System.out.println("normal first: (" + firstX + "," + firstY + ")"); // regular coordinates
		
		// second part
		int secondX;
		int secondY;
		
		maximumRow = ((secondTile / (interval + Math.abs((secondTile % interval) - interval)/interval%2)) + 1) * interval;
		intervalCheck = Math.abs((secondTile % interval) - interval)/interval;
		
		if (secondChange == 1) { // reversed row
			secondX = Math.abs(secondTile - maximumRow - 1); // (firstTile / interval)
		}
		else {
			secondX = secondTile - (maximumRow - interval); // maximumRow but minus 1 interval (24 - 25) instead of (36 - 25)
		}
		
		secondY = (secondTile / (interval + intervalCheck)) + 1;
		
		System.out.println("normal second: (" + secondX + "," + secondY + ")"); // regular coordinates
		
		// final printing
		double xDifferenceSquare = Math.pow((firstX - secondX),2);
		double yDifferenceSquare = Math.pow((firstY - secondY),2);
		
		
		double distanceBetween = Math.sqrt(xDifferenceSquare + yDifferenceSquare);
		
		System.out.println(xDifferenceSquare);
		System.out.println(yDifferenceSquare);
		
		System.out.println(distanceBetween);
		
		
		
//		int firstTile = 1;
//		int secondTile = 13;
//		
//		int rowSize = 12;
//		int columnSize = 12;
//		int interval = 12;
//		
//		/* magic machine which returns 1 if divisible by interval: System.out.println(Math.abs((changes % 12) - 12)/12);
//		
//		*/
//		
//		// basically tells whenever it's on another row, ACCOUNTING FOR 12, 24, etc being on the same row
//		int firstRow = ((firstTile / interval)  - Math.abs((firstTile % interval) - interval)/interval)%2;
//		int secondRow = ((secondTile / interval) - Math.abs((secondTile % interval) - interval)/interval)%2; // subtract by 1 ONLY if it's divisible by 12
//		
//		// 13 = 24, 1 = 12
//		// 14 = 23, 2 = 11
//		// 15 = 22, 3 = 10
//		
//		System.out.println(firstRow);
//		System.out.println(secondRow);
//		
//		// first part
//		int firstX;
//		int firstY;
//		
//		firstX = firstTile - interval * (firstTile / (interval + 1)); // firstTile minus, subtract from interval but dependent on row
//		firstY = firstTile / (interval + 1); // divided by the interval, plus 1 since tile 12 is y = 0
//		System.out.println("normal first: (" + firstX + "," + firstY + ")");
//		
//		if (firstRow == 1) { // based on Y, inverse X
//			firstX = Math.abs(interval + 1 - firstX); // find the difference
//		}
//		
//		// second part
//		int secondX;
//		int secondY;
//		
//		secondX = secondTile - interval * (secondTile / (interval + 1));
//		secondY = secondTile / (interval + 1);
//		System.out.println("normal second: (" + secondX + "," + secondY + ")");
//		
//		if (secondRow == 1) { // normal
//			secondX = Math.abs(interval + 1 - secondX); // find the difference
//		}
//		
//		
//		
//		
//		double xDifferenceSquare = Math.pow((firstX - secondX),2);
//		double yDifferenceSquare = Math.pow((firstY - secondY),2);
//		
//		
//		double distanceBetween = Math.sqrt(xDifferenceSquare + yDifferenceSquare);
//		
//		
//		System.out.println("final first: (" + firstX + "," + firstY + ")");
//		System.out.println("final second: (" + secondX + "," + secondY + ")");
//		
//		System.out.println(xDifferenceSquare);
//		System.out.println(yDifferenceSquare);
//		
//		System.out.println(distanceBetween);
		
		
		
	}
}
