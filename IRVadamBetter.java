import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;

//This program finds Monotonicity, No-show, Condorcet and IAS/PI anomalies in 3-candidate elections, as well as checks to see if the Borda count winner matches the IRV winner

//The Monotonicity program (topThreeGapSort) starts about line 1250.  Before that are preliminary calculations and other Monotonicity programs that did not work as well.


public class IRVadamBetter {
	static boolean monotonicity = false;
	static char Second = '3';
	static boolean getSecond;
	static int whichCheck;
	public static void main(String[] args) {

		System.out.println();
		
		if(args.length == 1){
			whichCheck = Integer.parseInt(args[0]);

			Scanner s = new Scanner(System.in);

			//read in the values from the user
			//total number of runners
			int totalRunners = -1;
			while(totalRunners<=0){
				totalRunners = s.nextInt();
				if(totalRunners<=0){
					System.out.println("Invalid number of runners.");
				}
			}

			//runners allowed per ballot
			int totalPicks = -1;
			while(totalPicks> totalRunners || totalPicks<=0){
				totalPicks= s.nextInt();
				if(totalPicks> totalRunners || totalPicks<=0){
					System.out.println("Invalid number of picks.");
				}
			}


			ArrayList<Integer> votesPerPermutation= new ArrayList<Integer>();
			ArrayList<String> permutations= new ArrayList<String>();

			s.nextLine();

			int votes = s.nextInt();

			//read in each combination of voting ballots and how many ballots hold that specific combination
			while(votes >= 0){
				votesPerPermutation.add(votes);
				String temp = s.next();

				if(temp.length()>totalPicks){
					temp = temp.substring(0, totalPicks);
				}

				permutations.add(temp);
				votes = s.nextInt();
			}

			//arrayLists to hold the original values of the input
			ArrayList<String> originals=  new ArrayList<String>(permutations);
			ArrayList<Integer> originalNumbers=  new ArrayList<Integer>(votesPerPermutation);

			//run 1 round and return the winner of the round, then reset all of the data back to the original data
			char winner = round(permutations, votesPerPermutation, totalRunners);
			
			
			
			permutations.clear();
			permutations.addAll(originals);
			votesPerPermutation.clear();
			votesPerPermutation.addAll(originalNumbers);

			//call which monotonicity check you wish to run
			//sort can lead to advanced or total Monotonicity check, efficientSort leads to total Monotonicity check

			if (whichCheck == 1){
				tweak(permutations, votesPerPermutation, totalRunners);
			}else if(whichCheck == 2 || whichCheck == 3 || whichCheck == 7 || whichCheck == 8){
				sort(originals, originalNumbers,winner, totalRunners);
			}else if (whichCheck == 4){
				monotonicityEfficientSort(permutations, votesPerPermutation, winner, totalRunners, Second);
			}else if (whichCheck == 5){
				smartMonotonicity(permutations, votesPerPermutation, totalRunners, originals, originalNumbers, winner);
			}else if (whichCheck == 6){
				smartMonotonicityUniversal(permutations, votesPerPermutation, totalRunners, originals, originalNumbers, winner);
			}else if (whichCheck == 9){
				topThreeGapSort(permutations, votesPerPermutation, winner, totalRunners);
			}else if (whichCheck == 10){
				indepIrrelAlt(permutations, votesPerPermutation, winner, totalRunners);  
			}else if (whichCheck == 11){
				condorcetWinnerCheck(permutations, votesPerPermutation, winner, totalRunners);  
			}else if (whichCheck == 12){
				bordaWinnerCheck(permutations, votesPerPermutation, winner, totalRunners);
			}else if (whichCheck == 13){
				topThreeNoShowCheck(permutations, votesPerPermutation, winner, totalRunners);
			}

		}else{
			System.out.println("Error: Which check to run was not specified.");
			System.out.println("Example input from command line:\n");
			System.out.println("java IRV x <input.txt\n");
			System.out.println("Where x is:");
			System.out.println("0 = just run IRV");
			System.out.println("1 = Simple Monotonicity Check");
			System.out.println("2 = Advanced Monotonicity Check");
			System.out.println("3 = Total Monotonicity Check");
			System.out.println("4 = Total Monotonicity Check With Efficiency Sort");
			System.out.println("5 = Smart Monotonicity Check with check between 2nd and 3rd place");
			System.out.println("6 = Smart Monotonicity Check with check between all adjacent pairs");
			System.out.println("7 = Print Number of Runs from Advanced Check");
			System.out.println("8 = Print Number of Runs from Total Check");
			System.out.println("9 = Top Three Gap Check");
			System.out.println("10 = Independence of Irrelevant Alternatives Check");
			System.out.println("11 = Condorcet Winner Check");
			System.out.println("12 = Borda Count Winner Check");
			System.out.println("13 = Top Three No-show Check");
		}
		System.out.println();


	}

	//Round method:
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns a character of the winner of the election
	//runs 1 round of IRV voting for the given ballots
	static char round(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		char[] rankingArray = new char[picks];  //rankingArray[picks-1-c] = minChar;
		//for(int i = 0;i<tops.size();i++){
		//	System.out.println(votes.get(i) + " " +tops.get(i));
		//}

		//while there is no winner. 
		while (!winner(tops)){
			int votesPer[] = new int[picks];

			for(int i = 0;i<picks;i++){
				votesPer[i] = 0;
			}

			//get the number of votes per candidate and put them into the array votesPer (votes for A in votesPer[0] etc)
			for(int count = 0;count< tops.size();count++){
				String temp = tops.get(count);
				int person = (int)((char)(temp.charAt(0))- 'A');
				votesPer[person]+=votes.get(count);	
			}

			int min = 2147483647;
			int minPos = 0;

			int max = -1;
			int maxPos = 0;

			/*int total = 0;
			for(int i = 0;i<picks;i++){
				total+=votesPer[i];
			} */

			//run through the array and get which number candidate has the least number of votes
			for(int i = 0; i<picks;i++){
				if(votesPer[i]<min && votesPer[i] != 0){
					min = votesPer[i];
					minPos = i;
				}
				if(votesPer[i]>max){
					max = votesPer[i];
					maxPos = i;
				}
			}

			


			//gets the second place finisher and sets the global variable equal to that variable
			char minChar = (char)(minPos + 'A');
			if(getSecond){
				//System.out.println(minChar);
				Second = minChar;
			}

			
			rankingArray[picks-1-c] = minChar;  //lists the loser in the ranking array

			//remove the person who has the least number of votes
			tops = removePerson(tops, minChar);

			//go through and remove the elements from the array that have no valid candidates left on the ballot after removing the last place person
			int j = 0;
			while(j <tops.size()){
				if(tops.get(j).equals("")){
					tops.remove(j);
					votes.remove(j);
				}else{
					j++;
				}
			}


			c++;

			//once there are 3 runners left, save the top 3 finishers to a text file with their combinations, then compact those combinations and save those to another text file
			if(c == picks-3){
				getSecond = true;
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopThree.txt"));

					for(int i = 0;i<tops.size();i++){
						out.write("" + votes.get(i) + " " + tops.get(i));
						out.newLine();
					}

					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ArrayList<Integer> compactVotes = new ArrayList<Integer>();
				ArrayList<String> compactStrings = new ArrayList<String>();

				for(int i = 0;i<tops.size();i++){
					if(compactStrings.contains(tops.get(i))){
						compactVotes.set(compactStrings.indexOf(tops.get(i)), compactVotes.get(compactStrings.indexOf(tops.get(i)))+votes.get(i));
					}else{
						compactStrings.add(tops.get(i));
						compactVotes.add(votes.get(i));
					}
				}

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopThreeCompact.txt"));

					for(int i = 0;i<compactVotes.size();i++){
						out.write("" + compactVotes.get(i) + " " + compactStrings.get(i));
						out.newLine();
					}

					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Now we have adam attempt at making it more compact:  First we put in all one vote and two vote options, then compact the three-vote options
				ArrayList<Integer> compactVotes2 = new ArrayList<Integer>();
				ArrayList<String> compactStrings2 = new ArrayList<String>();

				for(int i = 0;i<compactStrings.size();i++){
					if(compactStrings.get(i).length()==1){
						int temp1 = compactVotes.get(i);
						for(int p=i-1;p>-1;p--){
							if((compactStrings.get(p).length()==2 && compactStrings.get(p).equals(compactStrings.get(i)+compactStrings.get(i))) || (compactStrings.get(p).length()==3 && compactStrings.get(p).equals(compactStrings.get(i)+compactStrings.get(i)+compactStrings.get(i)))){
								temp1 = temp1 + compactVotes.get(p);
							}
						}
						for(int p=i+1;p<compactStrings.size();p++){
							if((compactStrings.get(p).length()==2 && compactStrings.get(p).equals(compactStrings.get(i)+compactStrings.get(i))) || (compactStrings.get(p).length()==3 && compactStrings.get(p).equals(compactStrings.get(i)+compactStrings.get(i)+compactStrings.get(i)))){
								temp1 = temp1 + compactVotes.get(p);
							}
						}
						compactStrings2.add(compactStrings.get(i));
						compactVotes2.add(temp1);
					}
				}
				

				for(int i = 0;i<compactStrings.size();i++){
					if(compactStrings.get(i).length()==2 && !(compactStrings.get(i).charAt(0)==compactStrings.get(i).charAt(1))) {
						int temp = compactVotes.get(i);
						for(int m = i+1;m<compactStrings.size();m++){
							if(compactStrings.get(m).length()==3 && (compactStrings.get(m).equals(compactStrings.get(i)+compactStrings.get(i).substring(0,1)) || compactStrings.get(m).equals(compactStrings.get(i)+compactStrings.get(i).substring(1,2)) || compactStrings.get(m).equals(compactStrings.get(i).substring(0,1)+compactStrings.get(i)))) {  // for all indices greater than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes.get(m);
							}
						}

						for(int m = i-1;m>-1;m--){
							if(compactStrings.get(m).length()==3 && (compactStrings.get(m).equals(compactStrings.get(i)+compactStrings.get(i).substring(0,1)) || compactStrings.get(m).equals(compactStrings.get(i)+compactStrings.get(i).substring(1,2)) || compactStrings.get(m).equals(compactStrings.get(i).substring(0,1)+compactStrings.get(i)))) {  // for all indices less than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes.get(m);
							}
						}

								compactStrings2.add(compactStrings.get(i));
								compactVotes2.add(temp);
								//break; 
					} //break;
				}

				for(int i = 0;i<compactStrings.size();i++){
					if(compactStrings.get(i).length()==3 && !(compactStrings.get(i).charAt(0)==compactStrings.get(i).charAt(1) || compactStrings.get(i).charAt(0)==compactStrings.get(i).charAt(2) || compactStrings.get(i).charAt(1)==compactStrings.get(i).charAt(2))){  // if permutation has length 3 and no matching entries
						compactStrings2.add(compactStrings.get(i));
						compactVotes2.add(compactVotes.get(i));
					} //adds all non-repeating three-letter permutations to new arrays
				}

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopThreeCompactOrder.txt"));

					for(int i = 0;i<compactVotes2.size();i++){
						out.write("" + compactVotes2.get(i) + " " + compactStrings2.get(i));
						out.newLine();
					}

					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	//Now need to do one more step--Once I find an entry in the array that has length two, need to add it to the corresponding length-three entry in the array.  If the array entry has one or three entries, then add it to the next arraylist.
				ArrayList<Integer> compactVotesMost = new ArrayList<Integer>();
				ArrayList<String> compactStringsMost = new ArrayList<String>();

				for(int i = 0;i<compactStrings2.size();i++){
					
					if(compactStrings2.get(i).length()==1){
						compactStringsMost.add(compactStrings2.get(i));
						compactVotesMost.add(compactVotes2.get(i));
					}else if(compactStrings2.get(i).length()==2){
						for(int k = i+1;k<compactStrings2.size();k++){
							if(compactStrings2.get(i).equals(compactStrings2.get(k).substring(0,2))) {
								compactStringsMost.add(compactStrings2.get(k));
								compactVotesMost.add(compactVotes2.get(k)+compactVotes2.get(i));
							}
						}	
					}else if(compactStrings2.get(i).length()==3){
						
					}
				}
				

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopThreeMostCompact.txt"));
					out.write("3");
					out.newLine();
					out.write("3");
					out.newLine();
					for(int i = 0;i<compactVotesMost.size();i++){
						out.write("" + compactVotesMost.get(i) + " " + compactStringsMost.get(i));
						out.newLine();
					}
					out.write("-1");
					out.newLine();
					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
		}

		

		System.out.println("The Ranking is: ");  
		System.out.println("1 " + tops.get(0).charAt(0)); 
			for(int i = 1;i<picks;i++){
						System.out.println(i+1 + " " + rankingArray[i] + " ");
						
					}
			//tops.get(0).charAt(0));

		return tops.get(0).charAt(0);
	}

	//Round No Ranking no top three method:
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns a character of the winner of the election
	//runs 1 round of IRV voting for the given ballots
	static char roundNoRankNoTop(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		

		while (!winner(tops)){
			int votesPer[] = new int[picks];

			for(int i = 0;i<picks;i++){
				votesPer[i] = 0;
			}

			//get the number of votes per candidate and put them into the array votesPer (votes for A in votesPer[0] etc)
			for(int count = 0;count< tops.size();count++){
				String temp = tops.get(count);
				int person = (int)((char)(temp.charAt(0))- 'A');
				votesPer[person]+=votes.get(count);	
			}

			int min = 2147483647;
			int minPos = 0;

			int max = -1;
			int maxPos = 0;


			//run through the array and get which number candidate has the least number of votes
			for(int i = 0; i<picks;i++){
				if(votesPer[i]<min && votesPer[i] != 0){
					min = votesPer[i];
					minPos = i;
				}
				if(votesPer[i]>max){
					max = votesPer[i];
					maxPos = i;
				}
			}

			//gets the second place finisher and sets the global variable equal to that variable
			char minChar = (char)(minPos + 'A');
			if(getSecond){
				Second = minChar;
			}

			//rankingArray[picks-1-c] = minChar;  //lists the loser in the ranking array
			
			//remove the person who has the least number of votes
			tops = removePerson(tops, minChar);

			//go through and remove the elements from the array that have no valid candidates left on the ballot after removing the last place person
			int j = 0;
			while(j <tops.size()){
				if(tops.get(j).equals("")){
					tops.remove(j);
					votes.remove(j);
				}else{
					j++;
				}
			}


			c++;

		}

		//System.out.println("The Winner is: " + tops.get(0).charAt(0));


		return tops.get(0).charAt(0);
	}

	static boolean winner(ArrayList<String> strings){
		for(int i = 0;i<strings.size();i++){
			if(strings.get(0).charAt(0) != strings.get(i).charAt(0))
				return false;
		}
		return true;
	}

	static ArrayList<String> removePerson(ArrayList<String> a, char loser){
		String person = ""+loser;

		for(int i = 0;i< a.size();i++){
			a.set(i, a.get(i).replaceAll(person, ""));
		}

		return a;
	}

	//ADVANCED MONOTONICITY CHECK
	//Pre: takes in the list of combinations and the list of votes per combination, how many combinations deep you are down in your check (recursive call number)
	//the winner, the number of runners, and how many rows of combinations you need to check in this run
	//Post: returns the arrayList of the current combinations 
	//called in sort() in a loop to change how many "num"s deep you need to check for issues in
	public static ArrayList<String> monotonicity(ArrayList<String> a, ArrayList<Integer> b, int number, char winner, int totalRunners, int num){
		ArrayList<String> originals = new ArrayList<String>(a);
		ArrayList<Integer> originalInts = new ArrayList<Integer>(b);
		String winnerString = ""+winner;

		if(endMonotonicity(a,winner) == false ||monotonicity == true){
			//System.out.println("Enter loop");
			if( number>=num || !(a.get(number).contains(winnerString))){
				//System.out.println("First if");
				//System.out.println(a.get(number));
				return a;
			}else{
				//System.out.println("second if");
				a = monotonicity(a, b, number+1, winner, totalRunners, num);
				if(monotonicity == true)
					return a;
				String temp = a.get(number);
				//System.out.println(a.get(number) + " SwapTest++++++++++++++++++++ " + number);
				while(a.get(number).charAt(0) != winner){
					//System.out.println("SwapSUCCESS--------------------------" + temp);
					temp = swap(temp, temp.indexOf(winnerString), temp.indexOf(winnerString)-1);
					//System.out.println("SwapSUCCESS--------------------------" + temp);
					a.set(number, temp);
					int i = 0;
					/*for(String st: a){
						System.out.println(b.get(i)+" "+st);
						i++;
					}*/
					originals.clear();
					originals.addAll(a);
					char w = round(a, b, totalRunners);
					a.clear();
					a.addAll(originals);
					b.clear();
					b.addAll(originalInts);
					if(winner != w){
						System.out.println("Monotonicity issue!\nOriginal Winner: " + winner + "\nNew Winner: " + w);
						monotonicity = true;
						return a;
					}
				}
				return a;
			}

		}

		return a;
	}

	static void sort(ArrayList<String> a, ArrayList<Integer> b, char w, int t){
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> temp2 = new ArrayList<String>();
		ArrayList<Integer> temp3 = new ArrayList<Integer>();
		ArrayList<Integer> temp4 = new ArrayList<Integer>();
		String winner = "" +w;

		for(int i = 0;i<a.size();i++){
			if(a.get(i).contains(winner)){
				temp.add(a.get(i));
				temp3.add(b.get(i));
			}else{
				temp2.add(a.get(i));
				temp4.add(b.get(i));
			}
		}

		temp.addAll(temp2);
		temp3.addAll(temp4);
		a = temp;
		b = temp3;

		ArrayList<String> originals = new ArrayList<String>(a);
		ArrayList<Integer> originalInts = new ArrayList<Integer>(b);

		for(int i = 0;i<a.size();i++){
			for(int j =0;j<i;j++){
				if(a.get(j).charAt(0) == w){
					String thing = a.get(j);
					int thing2 = b.get(j);
					a.set(j,a.get(i));
					a.set(i, thing);
					b.set(j,b.get(i));
					b.set(i, thing2);
				}
			}
		}

		int number = 0;
		for(int i = 0;i<a.size() && a.get(i).charAt(0) != w;i++){
			number++;
		}
		
		int totes = 0;
		for(int i = 0;i<a.size() && a.get(i).contains(winner);i++){
			totes++;
		}

		if(whichCheck == 2){
			for(int i = number;i>0;i--){
				System.out.println("NUMBER OF TIME __+_+_+_+_+_+_+_" +i);
				monotonicity(a,b,0,w, t, i);
				a.clear();
				a.addAll(originals);
				b.clear();
				b.addAll(originalInts);
			}
		}else if (whichCheck == 7){

			long c = 0;
			double average = 0;
			String adds = ""+c;
			for(int j = 0;j<totes;j++){
				for(int i = 0 ;i<j;i++){
					//if(a.get(i).indexOf(winner)+1 >0){
					c = c+(a.get(i).indexOf(winner)+1);
					adds+="+"+(a.get(i).indexOf(winner)+1);
					if(j == totes-1){
					average = average+(a.get(i).indexOf(winner)+1);
					}
					//}
				}
			}
			System.out.println("Number of Runs: "+c);
			
			average = average+(a.get(totes-1).indexOf(winner)+1);
			average/=totes;
			System.out.println("Average Winning Position in Ballot: " + average);
			System.out.println("Approximation: " + (totes*totes*(average-1)));

		}else if (whichCheck == 8){

			double c = 1;
			double average = 0;
			for(int i = 0 ;i<totes;i++){
				//if(a.get(i).indexOf(winner)+1 >0){
				c = c*(a.get(i).indexOf(winner)+1);
				//System.out.print("*"+(a.get(i).indexOf(winner)+1));
				average = average+(a.get(i).indexOf(winner)+1);
				//}
			}

			System.out.println("Number of Runs: "+c);
			
			average/=totes;
			System.out.println("Average Winning Position in Ballot: " + average);
			System.out.println("Approximation: " + Math.pow(average, (double)totes));
		}else if (whichCheck == 3){

			moreMonotonicity(a,b,0,w,t,number,a,b); 
		}
	}


	//Pre: takes in a permutation, and 2 letters to be swapped
	//Post: returns the string with the 2 letters swapped
	//swaps the 2 specified letters in the string
	static String swap(String s, int a, int b){
		char temp = s.charAt(a);
		char[] arr = s.toCharArray();
		arr[a]=arr[b];
		arr[b] = temp;
		String sw = new String(arr);
		return sw;
	}


	//Pre: takes in the list of permutations and the eventual winner
	//Post: returns a boolean to see of checking of swaps should continue past this point
	//if all permutations that contain the winner have the winner in first place, return true, else, return false
	static boolean endMonotonicity(ArrayList<String> a, char winner){
		boolean end = true;
		String stringWinner = ""+winner;
		int number = 0;

		for(int i = 0;i<a.size() && a.get(i).contains(stringWinner);i++){
			number++;
		}

		for(int i = 0;i<number;i++){
			if(a.get(i).charAt(0) != winner){
				end = false;
			}
		}
		return end;
	}


	//TOTAL MONOTONICITY CHECK
	//Pre: takes in the list of combinations and the list of votes per combination, how many combinations deep you are down in your check (recursive call number)
	//the winner, the number of runners, how many rows of combinations you need to check in this run, and the original combinations and votes per combination
	//Post: n/a
	//total monotoniciy check, checks all possible combinations of swaps for issues
	static void moreMonotonicity(ArrayList<String> a, ArrayList<Integer> b, int number, char winner, int totalRunners, int num, ArrayList<String> orig, ArrayList<Integer> origInts){
		ArrayList<String> originals = new ArrayList<String>(orig);
		ArrayList<Integer> originalInts = new ArrayList<Integer>(origInts);

		String winnerString = ""+winner;
		String temp = a.get(number);

		//System.out.println(number+ " " +num);
		if(number>=num || monotonicity == true){
			//System.out.println("First if");
			return;
		}else{
			//System.out.println("Second if " + a.get(number).indexOf(winnerString));
			while(a.get(number).indexOf(winnerString) >0){

				moreMonotonicity(a,b,number+1,winner,totalRunners,num, originals, originalInts);
				int i = 0;
				//for(String st: a){
				//System.out.println(b.get(i)+" "+st);
				//i++;
				//} 
				ArrayList<String> beforeRound = new ArrayList<String>(a);
				ArrayList<Integer> beforeRoundInts = new ArrayList<Integer>(b);

				if(monotonicity)
					return;
				char win = round(a,b,totalRunners);
				if(win != winner){
					System.out.println("Monotonicity Issue!\n");
					i = 0;
					System.out.println("Original Set:");
					for(String st: originals){
						System.out.println(originalInts.get(i)+" "+st);
						i++;
					} 
					i = 0;
					System.out.println("\nNew Set:");
					for(String st: beforeRound){
						System.out.println(beforeRoundInts.get(i)+" "+st);
						i++;
					} 
					System.out.println();
					monotonicity = true;
					return;
				}

				a.clear();
				a.addAll(beforeRound);
				b.clear();
				b.addAll(beforeRoundInts);

				//System.out.println(a.get(number)+" "+number);

				//System.out.println("Swap-----"+ temp);
				if(temp.indexOf(winnerString) >=1)
					temp = swap(temp, temp.indexOf(winnerString), temp.indexOf(winnerString)-1);
				//System.out.println("Swap-----"+ temp);
				a.set(number, temp);

				if(a.get(number).charAt(0) == winner){
					beforeRound.clear();
					beforeRound.addAll(a);
					beforeRoundInts.clear();
					beforeRoundInts.addAll(b);

					moreMonotonicity(a,b,number+1,winner,totalRunners,num, originals, originalInts);
					if(monotonicity)
						return;

					char win2 = round(a,b,totalRunners);
					if(win2 != winner){
						System.out.println("Monotonicity Issue!\n");
						i = 0;
						System.out.println("Original Set:");
						for(String st: originals){
							System.out.println(originalInts.get(i)+" "+st);
							i++;
						} 
						i = 0;
						System.out.println("\nNew Set:");
						for(String st: beforeRound){
							System.out.println(beforeRoundInts.get(i)+" "+st);
							i++;
						} 
						System.out.println();
						monotonicity = true;
						return;
					}
					a.clear();
					a.addAll(beforeRound);
					b.clear();
					b.addAll(beforeRoundInts);

					//System.out.println("SECOND IF+++++++++++++++++" + a.get(number)+ " " + originals.get(number));
					a.set(number, originals.get(number));
					b.set(number, originalInts.get(number));
					break;
				}
			}
			return;
		}
	}


	//Pre: takes in the original permutations and the votes corresponding to them, the eventual winner
	//the total number of runners, and the eventual second place finisher
	//Post: void
	//sorts the arrayLists to make checks where swap could make third place slingshot to win the election before other swaps
	static void monotonicityEfficientSort(ArrayList<String> a, ArrayList<Integer> b, char w, int t, char second){
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> temp2 = new ArrayList<String>();
		ArrayList<Integer> temp3 = new ArrayList<Integer>();
		ArrayList<Integer> temp4 = new ArrayList<Integer>();
		String winner = "" +w;
		String sec = ""+second;

		for(int i = 0;i<a.size();i++){
			if(a.get(i).contains(winner)){
				temp.add(a.get(i));
				temp3.add(b.get(i));
			}else{
				temp2.add(a.get(i));
				temp4.add(b.get(i));
			}
		}

		for(int i = 0;i<temp.size();i++){
			for(int j =0;j<i;j++){
				if(temp.get(i).contains(sec)){
					String thing = temp.get(j);
					int thing2 = temp3.get(j);
					temp.set(j,temp.get(i));
					temp.set(i, thing);
					temp3.set(j,temp3.get(i));
					temp3.set(i, thing2);
				}
			}
		}



		int number = 0;
		for(int i = 0;i<temp.size() && temp.get(i).contains(sec);i++){
			number++;
		}

		for(int i = 0;i<number;i++){
			for(int j =0;j<i;j++){
				if(secondAheadOfFirst(temp.get(i),w,second)){
					String thing = temp.get(j);
					int thing2 = temp3.get(j);
					temp.set(j,temp.get(i));
					temp.set(i, thing);
					temp3.set(j,temp3.get(i));
					temp3.set(i, thing2);
				}
			}
		}

		number = 0;
		for(int i = 0;i<temp.size() && secondAheadOfFirst(temp.get(i),w,second);i++){
			number++;
		}

		for(int i = 0;i<number;i++){
			for(int j =0;j<i;j++){
				if(!(temp.get(i).indexOf(second) == 0 && temp.get(i).indexOf(w) == 1)){
					String thing = temp.get(j);
					int thing2 = temp3.get(j);
					temp.set(j,temp.get(i));
					temp.set(i, thing);
					temp3.set(j,temp3.get(i));
					temp3.set(i, thing2);
				}
			}
		}

		temp.addAll(temp2);
		temp3.addAll(temp4);
		a = temp;
		b = temp3; 

		//for(int i = 0;i<a.size();i++){
		//	System.out.println(b.get(i) + " " + a.get(i));
		//}

		//System.out.println(number + " " + a.size()+Second);
		moreMonotonicity(a,b,0,w,t,number,a,b);
	}


	//Pre: takes in a permutation of votes
	//Post: returns a boolean to see if the eventual winner is behind the eventual second place finisher
	//if first is AHEAD of second in the permutation, return false, if not, return true
	static boolean secondAheadOfFirst(String combo, char first, char second){
		if(combo.indexOf(first)<combo.indexOf(second) && combo.indexOf(second)>=0)
			return false;

		return true;
	}


	//SMART MONOTONICITY CHECK
	//Pre: takes in the starting permutations and the number of votes corresponding to each permutation, the number of people running
	//the original permutations and votes of them, and the original winner
	//Post: void
	//performs the monotonicity check for checking distances between 2nd and 3rd place and makes switches based on ballots
	//that have more votes than each of those distances
	static void smartMonotonicity(ArrayList<String> tops, ArrayList<Integer> votes, int runners, ArrayList<String> orig, ArrayList<Integer> origInts, char winner){
		int votesPer[] = new int[runners];

		for(int i = 0;i<runners;i++){
			votesPer[i] = 0;
		}

		//get the number of votes per candidate and put them into the array votesPer (votes for A in votesPer[0] etc)
		for(int count = 0;count< tops.size();count++){
			String temp = tops.get(count);
			int person = (int)((char)(temp.charAt(0))- 'A');
			votesPer[person]+=votes.get(count);	
		}

		int top3Votes[] = new int[3];

		top3Votes[0] = 0;
		top3Votes[1] = -1;
		top3Votes[2] = -2;

		char top3People[] = new char[3];

		top3People[0] = '1';
		top3People[1] = '1';
		top3People[2] = '1';

		//get the top 3 vote getters and how many votes they each have
		for(int i = 0;i<votesPer.length;i++){
			if(votesPer[i] >top3Votes[0]){
				top3Votes[2] = top3Votes[1];
				top3People[2] = top3People[1];

				top3Votes[1] = top3Votes[0];
				top3People[1] = top3People[0];

				top3Votes[0] = votesPer[i];
				top3People[0] = (char)(i+'A');
			}else if(votesPer[i] >top3Votes[1]){
				top3Votes[2] = top3Votes[1];
				top3People[2] = top3People[1];

				top3Votes[1] = votesPer[i];
				top3People[1] = (char)(i+'A');
			}else if (votesPer[i] >top3Votes[2]){
				top3Votes[2] = votesPer[i];
				top3People[2] = (char)(i+'A');
			}
		}

		for(int i = 0;i<top3Votes.length;i++){
			System.out.println("" + top3People[i] + " " + top3Votes[i]);
		}

		//get the number of votes between the second and third place candidates  ADAM NOTE: This is not a good enough 
		//distance between the second and third place candidates--need to actually run the IRV election until we get 
		//top three candidates, then do it from there.  Also, don't want the distance between the 2nd and 3rd ranked 
		//candidates, want the distance between the nonwinner (i.e. loser in the final round) and the third-place 
		//candidate (i.e. candidate who first drops out when there are only three left)  
		int votesBetween2And3 = top3Votes[1] - top3Votes[2];

		System.out.println("Distance between 2 and 3: " + votesBetween2And3);

		ArrayList<String> combosWhere2HasMajority = new ArrayList<String>();
		ArrayList<Integer> combosWhere2HasMajorityVotes = new ArrayList<Integer>();
		ArrayList<Integer> combosWhere2HasMajorityPlaceHolders = new ArrayList<Integer>();

		//get the combinations where the second place finisher has first place votes  ADAM NOTE: I think there may be
		// a flaw in this code where it has  && votes.get(i) > (votesBetween2And3)  because we might need to swap some
		// even if they are less than the distance between 2 and 3, since we just need the total to be > distance, not each.
		for(int i = 0;i<tops.size();i++){
			if(tops.get(i).charAt(0) == top3People[1] && votes.get(i) > (votesBetween2And3)){
				combosWhere2HasMajority.add(tops.get(i));
				combosWhere2HasMajorityVotes.add(votes.get(i));
				combosWhere2HasMajorityPlaceHolders.add(i);
			}
		}

		System.out.println("Number of ballots viable for swapping: " + combosWhere2HasMajority.size());

		//run this for each combination
		for(int i = 0;i<combosWhere2HasMajorityVotes.size();i++){
			//if there are more votes in a combination than n+1
			if(combosWhere2HasMajorityVotes.get(i) > (votesBetween2And3+1)){

				//take the votes off of the original combo
				votes.set(combosWhere2HasMajorityPlaceHolders.get(i), votes.get(combosWhere2HasMajorityPlaceHolders.get(i))-(votesBetween2And3+1));

				String temp = combosWhere2HasMajority.get(i);

				//change the string to give the first place winner the newly switched votes
				String gone = ""+winner;
				temp = temp.replaceAll(gone, "");
				String temp2 = ""+winner + temp;

				tops.add(temp2);
				votes.add(votesBetween2And3+1);

				char secondWinner = round(tops, votes, runners);

				if(secondWinner != winner){
					System.out.println("MONOTONICITY ISSUE FOUND: " + winner + " " + secondWinner);
					System.out.println("Replace " + (votesBetween2And3+1) + " " + combosWhere2HasMajority.get(i) + " with " + temp2);
					return;
				}

				tops.clear();
				tops.addAll(orig);
				votes.clear();
				votes.addAll(origInts);
			}
		}

		System.out.println("Done");
	}


	//SMART MONOTONICITY UNIVERSAL CHECK
	//Pre: takes in the starting permutations and the number of votes corresponding to each permutation, the number of people running
	//the original permutations and votes of them, and the original winner
	//Post: void
	//performs the monotonicity check for checking distances between adjacent pairs (1-2, 2-3, 3-4, ...) and makes switches based on ballots
	//that have more votes than each of those distances
	static void smartMonotonicityUniversal(ArrayList<String> tops, ArrayList<Integer> votes, int runners, ArrayList<String> orig, ArrayList<Integer> origInts, char winner){
		int votesPer[] = new int[runners];

		for(int i = 0;i<runners;i++){
			votesPer[i] = 0;
		}

		//get the number of votes per candidate and put them into the array votesPer (votes for A in votesPer[0] etc)
		for(int count = 0;count< tops.size();count++){
			String temp = tops.get(count);
			int person = (int)((char)(temp.charAt(0))- 'A');
			votesPer[person]+=votes.get(count);	
		}

		int sortedVotesCurrent[] = new int[runners];
		char sortedPeopleCurrent[] = new char[runners];

		//initialize the sorted lists with the current order of votesPer
		for(int i = 0;i<sortedVotesCurrent.length;i++){
			sortedVotesCurrent[i] = votesPer[i];
			sortedPeopleCurrent[i] = (char)(i+65);
		}

		//sort the arrays by top vote getters
		// :)
		for(int i = 0;i<sortedVotesCurrent.length;i++){
			for(int j = 0;j<sortedVotesCurrent.length-1;j++){
				if(sortedVotesCurrent[j]<sortedVotesCurrent[j+1]){
					int temp = sortedVotesCurrent[j];
					sortedVotesCurrent[j] = sortedVotesCurrent[j+1];
					sortedVotesCurrent[j+1] = temp;

					char swap = sortedPeopleCurrent[j];
					sortedPeopleCurrent[j] = sortedPeopleCurrent[j+1];
					sortedPeopleCurrent[j+1] = swap;
				}
			}
		}

		for(int j = 0;j<sortedVotesCurrent.length;j++){
			System.out.println(""+sortedPeopleCurrent[j]+ " " + sortedVotesCurrent[j]);
		} 

		for(int distanceBetweenChecks = 0;distanceBetweenChecks<runners-2;distanceBetweenChecks++){

			//get the number of votes between the second and third candidates being looked at
			int votesBetween2And3 = sortedVotesCurrent[1+distanceBetweenChecks] - sortedVotesCurrent[2+distanceBetweenChecks];

			System.out.println("Distance between "+(2+distanceBetweenChecks)+ " and "+(3+distanceBetweenChecks)+": "+votesBetween2And3);

			ArrayList<String> combosWhere2HasMajority = new ArrayList<String>();
			ArrayList<Integer> combosWhere2HasMajorityVotes = new ArrayList<Integer>();
			ArrayList<Integer> combosWhere2HasMajorityPlaceHolders = new ArrayList<Integer>();

			//get the combinations where the second place finisher has first place votes 
			for(int i = 0;i<tops.size();i++){
				if(tops.get(i).charAt(0) == sortedPeopleCurrent[1+distanceBetweenChecks] && votes.get(i) > (votesBetween2And3)){
					combosWhere2HasMajority.add(tops.get(i));
					combosWhere2HasMajorityVotes.add(votes.get(i));
					combosWhere2HasMajorityPlaceHolders.add(i);
				}
			}

			System.out.println("Number of ballots viable for swapping: " + combosWhere2HasMajority.size());

			//run this for each combination
			for(int i = 0;i<combosWhere2HasMajorityVotes.size();i++){
				//if there are more votes in a combination than n+1
				if(combosWhere2HasMajorityVotes.get(i) > (votesBetween2And3+1)){

					//take the votes off of the original combo
					votes.set(combosWhere2HasMajorityPlaceHolders.get(i), votes.get(combosWhere2HasMajorityPlaceHolders.get(i))-(votesBetween2And3+1));

					String temp = combosWhere2HasMajority.get(i);

					//change the string to give the first place winner the newly switched votes
					String gone = ""+winner;
					temp = temp.replaceAll(gone, "");
					String temp2 = ""+winner + temp;

					tops.add(temp2);
					votes.add(votesBetween2And3+1);

					char secondWinner = round(tops, votes, runners);

					if(secondWinner != winner){
						System.out.println("MONOTONICITY ISSUE FOUND: " + winner + " " + secondWinner);
						System.out.println("Replace " + (votesBetween2And3+1) + " " + combosWhere2HasMajority.get(i) + " with " + temp2);
						return;
					}

					tops.clear();
					tops.addAll(orig);
					votes.clear();
					votes.addAll(origInts);
				}
			}
		}

		System.out.println("Done");
	}
	
	
	//SIMPLE MONOTONICITY CHECK
	//Pre: takes in the list of permutations and the votes corresponding to those permutations and the total number of runners
	//Post: void
	//simple check to see if any 1 swap could cause a monotonicity issue
	static void tweak(ArrayList<String> Opermutations, ArrayList<Integer> OvotesPerPermutation, int OtotalRunners) {
		ArrayList<String> permutations = new ArrayList<String>(Opermutations);
		ArrayList<Integer> votesPerPermutation = new ArrayList<Integer>(OvotesPerPermutation);
		ArrayList<Integer> changedVotes= new ArrayList<Integer>();
		ArrayList<String> savedPermutations= new ArrayList<String>();
		ArrayList<Integer> indexOfChangedPermutations= new ArrayList<Integer>();
		ArrayList<Character> winners= new ArrayList<Character>();
		int totalRunners = OtotalRunners;
		System.out.println(" Before round "+permutations.size()+"  "+ votesPerPermutation.size());
		
		char Owinner = round(permutations, votesPerPermutation, totalRunners);
		System.out.println(" After round "+permutations.size()+"  "+ votesPerPermutation.size());
		// Go through every permutation once 
		for (int index=0;index !=Opermutations.size(); index++){
			
			String ballot = Opermutations.get(index);
			
			
			if(ballot.indexOf(Owinner) != 0 && ballot.indexOf(Owinner) != -1){
				System.out.println("Using Ballot: "+ballot);
				char[] ballotChar	= ballot.toCharArray();
				for(int i=ballot.indexOf(Owinner);i!= 0 && i!= -1;i--){
					
					permutations.clear();
					permutations.addAll(Opermutations);
					votesPerPermutation.clear();
					votesPerPermutation.addAll(OvotesPerPermutation);
					
					char temp=ballotChar[i-1];
					ballotChar[i-1] = ballotChar[i];
					ballotChar[i]=temp;
					String newBallot = new String(ballotChar);
					permutations.set(index, newBallot);
					System.out.println("Changed Ballot from : "+ballot+" To: "+newBallot+" "+permutations.size());
					System.out.println(" Before round "+permutations.size()+"  "+ votesPerPermutation.size());
					char winner = round(permutations,votesPerPermutation, totalRunners);
					System.out.println(" After round "+permutations.size()+"  "+ votesPerPermutation.size());
					if (winner != Owinner){
						System.out.println("\nNEW WINNER\n\n");
						newBallot = new String(ballotChar);			
						savedPermutations.add(newBallot);
						changedVotes.add(votesPerPermutation.get(index));
						winners.add(winner);
					} else {
						System.out.println("Same winner as before.");
					}
					
				}
				
				
			} else{
				System.out.println("Ballot "+ballot+ " has "+Owinner+ " in first or not at all");
			}
		}
		
		/*for(int i = 0; i <winners.size(); i++){
			System.out.println("The winner could switch to "+winners.get(i)+" if "+changedVotes.get(i)
					+" people changed their vote from "+Opermutations.get(i)+" to "+savedPermutations.get(i) );
		} */
		if(winners.size()==0){
			System.out.println("No monotonoicity issues found");
		}
	}
	
	
	static int runCandidates(int oppA,int oppB, int totalRunners,ArrayList<String> permutations, ArrayList<Integer> votes ) {
		char opponentA = (char) oppA;
		char opponentB = (char) oppB;
		int scoreA =0; int scoreB=0;
		System.out.print(opponentA+" vs "+opponentB +" winner is: ");
		
		for (int index=0;index !=permutations.size(); index++){
			String p = permutations.get(index);
			
			if (p.indexOf(opponentA) != -1 && ((p.indexOf(opponentB) == -1) || (p.indexOf(opponentA)  < p.indexOf(opponentB)))){
				
				scoreA = scoreA +votes.get(index);
			} else if (p.indexOf(opponentB) != -1 && ((p.indexOf(opponentA) == -1) || (p.indexOf(opponentB)  < p.indexOf(opponentA)))){
				scoreB = scoreB + votes.get(index);
				
			}
		}
		
		if( scoreA > scoreB ){
			System.out.println(opponentA);
			return oppA;
		} else if ( scoreB > scoreA){
			System.out.println(opponentB);
			return oppB;
		} else {
			System.out.println("Anomaly, both opponents scored the same, giving it to "+opponentA);
			return oppA;
		}
	
	}
	
	static void election(ArrayList<String> tops, ArrayList<Integer> votes, int runners, int picks){
		
		for(int i = 0;i<tops.size();i++){
			tops.set(i, editString(tops.get(i), runners));
		}
		
		int votesTotal[] = new int[runners];
		
		for(int i = 0;i<tops.size();i++){
			String temp = tops.get(i);
			for(int j = 0;j<temp.length();j++){
				votesTotal[temp.charAt(j)-65] += (picks-j)*votes.get(i);
			}
		}
		
		int max = -1;
		int place = 0;
		
		for(int i = 0;i<votesTotal.length;i++){
			if(votesTotal[i]>max){
				max = votesTotal[i];
				place = i;
			}
		}
		
		System.out.println("Candidate " + (char)(place+65) + " wins with " + max + " points.");
		
		for(int i = 0;i<votesTotal.length;i++){
			System.out.println((char)(i+65) + ": " + votesTotal[i] + " points.");
		}
	}

	static String editString(String a, int runners){
		int counts[] = new int[runners];
		
		for(int i = 0;i<a.length();i++){
			counts[a.charAt(i)-65]++;
		}
		
		
		for(int i=0;i<counts.length;i++){
			if(counts[i]>1){
				char search = (char)(65+i);
				String temp = "" + search;
				a = a.replaceAll(temp, "~");
				a = a.replaceFirst("~", temp);
				a = a.replaceAll("~", "");
			}
		}
		
		return a;
		
	}

	/* TOP THREE MONOTONICITY CHECK.  
	Pre: takes in voting data, winner, and number of picks
	Post: Outputs whether or not there is a violation, and how it happens.

	Finds gap between second and third place candidates, and switches votes in order to close the gap to make second-place drop out before third, then checks to see if that causes a monotonicity violation.
	*/
	
	static void topThreeGapSort(ArrayList<String> topThreePerms, ArrayList<Integer> topThreeVotes, char winner, int picks){	
		int votesPer[] = new int[picks];
		if(topThreePerms.size()!=9){System.out.println("Potential error, less than 9 items in array\n");
		}
		for(int i = 0;i<picks;i++){
			votesPer[i] = 0; 
		}
		for(int count = 0;count< topThreePerms.size();count++){
			String temp = topThreePerms.get(count); //declares permutation as temp
			int person = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			votesPer[person]+=topThreeVotes.get(count);	//adds up votes per person
			//at this point, votesPer[0] is the number of votes for candidate A, etc.
		}
		System.out.println("System check.\n Total votes are"+votesPer[0]+" for A, "+votesPer[1]+" for B, and " + votesPer[2] + " for C.\n");

		//declare ints for min, max
		int min = 2147483647;
		int minPos = 0;

		int max = -1;
		int maxPos = 0;


	for(int i = 0; i<picks;i++){
		if(votesPer[i]<min && votesPer[i] != 0){
			min = votesPer[i];
			minPos = i;
		}
		if(votesPer[i]>max){
			max = votesPer[i];
			maxPos = i;
		}
	}
	//System.out.println("Min position is "+minPos+", with votes of "+min+" \n");
	//System.out.println("Max position is "+maxPos+", with votes of "+max+" \n");
	//what if it is equal?
	/*Now have data that minPos=3rd place candidate, min=number of votes for that candidate=votesPer[minPos], ditto for maxPos */ 
	//Then need to find number for non winning, non losing candidate.  Do

	int winnerPos = (int)((char)(winner - 'A'));

	int otherPos = 0; 
	while (otherPos==minPos || otherPos==winnerPos) { 
		otherPos++;
	}
	//System.out.println("Other position is "+otherPos+" \n");
	//System.out.println("Winner position is "+winnerPos+" \n");
	/*At this point, winnerPos is the number for winner, otherPos is number for nonwinnerNonloser, minPos=number for loser  */
	//Now find gap between third place and second

	int n = 0;
	n=votesPer[otherPos]-votesPer[minPos];
	//System.out.println("Gap is "+n+" \n");
	if(n<0){
		System.out.println("Error. Gap between second and third is calculating wrong.\n");
	}/* output error if n<=0 */


	/*(Note: let winnerPos be winner (numerical), minPos be loser, otherPos be other candidate, could be in first or second place, hard to say)*/

	//convert winnerPos etc back to letters, call them R=other, W=winner

	char R = (char)(otherPos + 'A');
	char W = (char)(winnerPos + 'A'); //this should be same as winner
	char L = (char)(minPos + 'A');
	//System.out.println("Other is "+R+", Winner is "+W+", Loser is "+L+" \n");
	if(W==winner){
	} else{
		System.out.println("Error in calculation of winning candidate\n");
	}
	

	//make strings to match perm data:
	String rwl= "" + R + W + L;
	System.out.println("rwl is "+rwl+" \n");
	int rwlPos=-1;
	for(int j = 0;j<topThreePerms.size();j++){
		if (topThreePerms.get(j).equals(rwl)){
			//if (topThreePerms.get(j).equals(R+W+L)){
   			rwlPos = j;
   			System.out.println("rwlpos is "+rwlPos +"\n");
			//break;
		} 
	}//now have index for permutation RWL
	String wrl= "" + W + R + L;
	int wrlPos=-1;
	for(int k=0; k<topThreePerms.size();k++){  //need to find index k for WRL
		if(topThreePerms.get(k).equals(wrl)){
			 wrlPos = k;  //Note that k=wrlPos
			 System.out.println("wrlpos is "+wrlPos +"\n");
			 break;
		}
	}
	int rPos=-1;
	String r= "" +  R ;
	int rwlNum=0;
	int totalR=0;
	int rNum=0;
	int rlwPos=-1;
	String rlw= "" + R + L + W;
	int totalRLW=0;
	if(topThreeVotes.get(rwlPos)>=(n+1)){
	
		topThreeVotes.set(rwlPos,(topThreeVotes.get(rwlPos)-(n+1)));
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+(n+1)));
		
	 	char N = round(topThreePerms, topThreeVotes, picks);
	 	if(N!=W){                       //returns anomaly and some data
	 		System.out.println("Monotonicity Anomaly!\n");
	 		System.out.println("Swapped "+ n +" + 1 votes from Second-place to Winner, and now " + L + " is the winner");
	 		//System.exit(0);
	 	} else {
	 		System.out.println("No monotonicity anomaly with one swap rwl to wrl.\n"); 
	 		//System.exit(0);
	 	}
	}                            //now if  topThreeVotes[rwlPos]<(n+1), change all RWL to WRL
	else if(topThreeVotes.get(rwlPos)<(n+1)){  
		rwlNum=topThreeVotes.get(rwlPos); 
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+topThreeVotes.get(rwlPos)));
		topThreeVotes.set(rwlPos,0);
		for(int i=0;i<topThreePerms.size();i++){   //Find which index is R
			if(topThreePerms.get(i).equals(r)){
				rPos = i;
				break;
			}			
		}
	
 	//if #R<n+1-#RWL, swap all R votes to become WRL:  Note m=rPos
	if(topThreeVotes.get(rPos)>=(n+1-rwlNum)){
		topThreeVotes.set(rPos,(topThreeVotes.get(rPos)-(n+1-rwlNum)));
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+(n+1-rwlNum)));
		char N = round(topThreePerms, topThreeVotes, picks);
		if(N!=W){  
 			totalR=n+1-rwlNum;            //returns anomaly and some data
 			System.out.println("Monotonicity Anomaly!\n");
 			System.out.println("Swapped "+ rwlNum +" votes from Second-place(RWL) to Winner as well as " + totalR + " votes from Second-place(R) and now " + L + " is the winner\n");
 			//System.exit(0);
		} else {
			System.out.println("No monotonicity anomaly with two swaps, wrl and r to rwl.\n");
			//System.exit(0);
		}
	} 	//add all R votes to WRL
	else if(topThreeVotes.get(rPos)<(n+1-rwlNum)){
		rNum=topThreeVotes.get(rPos);
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+topThreeVotes.get(rPos)));
		topThreeVotes.set(rPos,0);
		for(int i=0;i<topThreePerms.size();i++){   //Find which index is RLW
			if(topThreePerms.get(i).equals(rlw)){
				rlwPos = i;
				break;
			}
		}
	 //Compare #RLW to n+1-#RWL-#R.  If bigger, swap n+1-#RWL-#R RWLs to WRL. 
	if(topThreeVotes.get(rlwPos)>=(n+1-rwlNum-rNum)){
		topThreeVotes.set(rlwPos,(topThreeVotes.get(rlwPos)-(n+1-rwlNum-rNum)));
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+(n+1-rwlNum-rNum)));
		char N = round(topThreePerms, topThreeVotes, picks);  //rerun election
		if(N!=W){   
			totalRLW=n+1-rwlNum-rNum;             //returns anomaly and some data
	 		System.out.println("Monotonicity Anomaly!\n");
	 		System.out.println("Swapped "+ rwlNum +" votes from Second-place(RWL) to Winner as well as " + totalR +" votes from Second-place(R) as well as " + totalRLW +" votes from Second-place(RLW) and now " + L + " is the winner\n");
	 		//System.exit(0);
		} else {
			System.out.println("No monotonicity anomaly with all three swaps.\n");
			//System.exit(0);
		}
	}
	else{ //if #RLW<(n+1-#RWL-#R), return no monotonicity anomaly.
		System.out.println("No monotonicity anomaly.  Not enough Other votes to make up gap and rerun election.\n");
	} //ends else statement
	} // ends else if(topThreeVotes.get(rPos)<(n+1-rwlNum)){
	} //ends else if(topThreeVotes.get(rwlPos)<(n+1)){  
	} //ends Top three Gap sort

	/* INDEPENDENCE OF IRRELEVANT ALTERNATIVES CHECK.  
	Pre: takes in voting data, IRV winner, and number of picks
	Post: Outputs whether or not there is a violation, and how it happens.

	Details: Removes one losing candidate from all ballots and reruns election, then checks to see if winner in the comparison profile is different from original.  If so, report IIA violation.  Then go on to next losing candidate and do the same
	*/
	static void indepIrrelAlt(ArrayList<String> perms, ArrayList<Integer> votes, char origWinner, int picks){	
		int origWinnerInt = (int)((char)(origWinner - 'A'));
		int picksMinusOne = picks-1;
		for(int i=0;i<picks;i++){
			if(i==origWinnerInt){//System.out.println(i + " = original winner");
			}else{
				ArrayList<String> tempPerms=  new ArrayList<String>(perms);
				ArrayList<Integer> tempVotes=  new ArrayList<Integer>(votes);
				//String tempPerms[] = new String[perms.size()];
				char tempChar = (char)(i + 'A');
				String tempString = "" + tempChar;
				tempPerms = removePerson(tempPerms, tempChar);
				int j = 0;
				while(j <tempPerms.size()){
					if(tempPerms.get(j).equals("")){
						tempPerms.remove(j);
						tempVotes.remove(j);
					}else{
						j++;
					}
				}
				tempPerms.add(tempString);
				tempVotes.add(1);
				
				char newWinner = roundNoRankNoTop(tempPerms, tempVotes, picks);
				//System.out.println(" The new winner is " + newWinner + " and the original winner was " + origWinner);
				if(newWinner==origWinner){System.out.println("When candidate " + tempChar + " is removed, nothing happens. ");
				}else{
					System.out.println("IIA anomaly! When candidate " + tempChar + " is removed, the new IRV winner is " + newWinner + " instead of " + origWinner );
				}

				tempPerms.clear();
				tempPerms.addAll(perms);
				tempVotes.clear();
				tempVotes.addAll(votes);
			}

		}

	} //ends indepIrrelAlt program  

	/* Condorcet Winner CHECK.  
	Pre: takes in voting data, IRV winner, and number of picks
	Post: Outputs whether or not there is a Condorcet winner, and if that person is the same as the IRV winner.

	Details: Removes all but two candidates from all ballots and compares head-to-head.  head to head winner gets a point, then see if the top score is #runners-1, if so compare to IRV winner to see if Condorcet winner loses to the IRV winner.  If so reports Condorcet violation, otherwise reports that there is no Condorcet winner.
	*/
	static void condorcetWinnerCheck(ArrayList<String> perms, ArrayList<Integer> votes, char origWinner, int picks){
		int condArray[] = new int[picks];
		for(int q = 0;q<picks;q++){
			condArray[q] = 0; 
		}
		//System.out.println("Starting Cond Program");
		for(int i=0;i<picks;i++){
			//System.out.println("Made i for");
			for(int j=i+1;j<picks;j++){
				//System.out.println("Made j for");
				ArrayList<String> tempPerms=  new ArrayList<String>(perms);
				ArrayList<Integer> tempVotes=  new ArrayList<Integer>(votes);
				for(int c=0;c<picks;c++){ //remove non-head to headers
					if((c!=i) && (c != j)){
						char tempChar = (char)(c + 'A');
						tempPerms = removePerson(tempPerms, tempChar);
						int k = 0;
						while(k <tempPerms.size()){
							if(tempPerms.get(k).equals("")){
								tempPerms.remove(k);
								tempVotes.remove(k);
							}else{
								k++;
							}
						}
					}	
				}
				int condVotes[] = new int[picks];
				for(int m = 0;m<picks;m++){
					condVotes[m] = 0; 
				}
				for(int count = 0;count< tempPerms.size();count++){
					String temp = tempPerms.get(count); //declares permutation as temp
					int person = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
					condVotes[person]+=tempVotes.get(count);	//adds up votes per person
					//at this point, condVotes[0] is the number of votes for candidate A, etc, all of which are zero except for the two head to headers.
				}

				int min = 2147483647;
				int minPos = 0;

				int max = -1;
				int maxPos = 0;

				for(int n = 0; n<picks;n++){
					if(condVotes[n]<min && condVotes[n] != 0){
						min = condVotes[n];
						minPos = n;
					}
					if(condVotes[n]>max){
						max = condVotes[n];
						maxPos = n;
					}
				}	
				System.out.println("Candidate " + maxPos + " beats Candidate " + minPos + " in a head to head.");
				condArray[maxPos]+=1; //adds one point to Condorcet array for the head-to-head winner
			}
		} //at the end of this, the condArray has the points for all the head-to-head matches
		int min = 2147483647;
		int minPos = 0;

		int max = -1;
		int condWin = 0;

		for(int n = 0; n<picks;n++){
			if(condArray[n]<min && condArray[n] != 0){
				min = condArray[n];
				minPos = n;
			}
			if(condArray[n]>max){
				max = condArray[n];
				condWin = n;
			}
		}
		char condWinner = (char)(condWin + 'A');
		if(condArray[condWin]==(picks-1)){
			System.out.println("Condorcet Winner is Candidate " + condWinner +".");
			if(origWinner!=condWinner){
				System.out.println("Condorcet Anomaly!  Condorcet winner is Candidate " + condWinner +" and the IRV winner is Candidate " + origWinner +".");
			}else{
				System.out.println("No Condorcet Anomaly.");
			}
		}else{
			System.out.println("No Condorcet Winner. The scores are:");
			for(int i=0;i<picks;i++){
				char tempCondChar = (char)(i + 'A');
				System.out.println(tempCondChar + " " + condArray[i]);
			}
		}
	} //ends Condorcet Winner check



	static void bordaWinnerCheck(ArrayList<String> perms, ArrayList<Integer> votes, char origWinner, int picks){
		ArrayList<String> tempPerms=  new ArrayList<String>(perms);
		int bordaPoints[] = new int[picks];
		for(int m = 0;m<picks;m++){
					bordaPoints[m] = 0; 
		}

		//replace bad vote permutations so as to not overcount borda points.
		for(int i=0; i<tempPerms.size(); i++){ //replace AA with A
			if(tempPerms.get(i).length()==2 && (tempPerms.get(i).charAt(0)==tempPerms.get(i).charAt(1))){
				tempPerms.set(i,tempPerms.get(i).substring(0,1)); //replace AAA with A
			}else if(tempPerms.get(i).length()==3 && (tempPerms.get(i).charAt(0)==tempPerms.get(i).charAt(1)) && (tempPerms.get(i).charAt(0)==tempPerms.get(i).charAt(2))){
				tempPerms.set(i,tempPerms.get(i).substring(0,1)); //replace AAB with AB
			}else if(tempPerms.get(i).length()==3 && (tempPerms.get(i).charAt(0)==tempPerms.get(i).charAt(1)) && (tempPerms.get(i).charAt(0)!=tempPerms.get(i).charAt(2))){
				tempPerms.set(i,tempPerms.get(i).substring(1)); //replace ABA with AB
			}else if(tempPerms.get(i).length()==3 && (tempPerms.get(i).charAt(0)==tempPerms.get(i).charAt(2)) && (tempPerms.get(i).charAt(0)!=tempPerms.get(i).charAt(1))){
				tempPerms.set(i,tempPerms.get(i).substring(0,2)); //replace ABB with AB
			}else if(tempPerms.get(i).length()==3 && (tempPerms.get(i).charAt(1)==tempPerms.get(i).charAt(2)) && (tempPerms.get(i).charAt(0)!=tempPerms.get(i).charAt(1))){
				tempPerms.set(i,tempPerms.get(i).substring(0,2));
			}
		}
			//now we add up points for each candidate, storing the total points in bordaPoints array.
		for(int j=0; j<tempPerms.size(); j++){
			if(tempPerms.get(j).length()==3){
				String temp = tempPerms.get(j); //declares permutation as temp
				int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
				bordaPoints[person0]+=(votes.get(j)*3);//adds up votes per person times 3

				int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
				bordaPoints[person1]+=(votes.get(j)*2);//adds up votes per person times 2

				int person2 = (int)((char)(temp.charAt(2))- 'A'); //gets int for which person is third in temp
				bordaPoints[person2]+=(votes.get(j));//adds up votes per person times 1
				//at this point, bordaPoints[0] is the number of points for candidate A, etc, many of which are still zero.
			}else if(tempPerms.get(j).length()==2){
				String temp = tempPerms.get(j); //declares permutation as temp
				int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
				bordaPoints[person0]+=(votes.get(j)*3);//adds up votes per person times 3

				int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
				bordaPoints[person1]+=(votes.get(j)*2);//adds up votes per person times 2
			}else if(tempPerms.get(j).length()==1){
				String temp = tempPerms.get(j); //declares permutation as temp
				int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
				bordaPoints[person0]+=(votes.get(j)*3);//adds up votes per person times 3
			}
			// for larger Burlington data:
			// if(tempPerms.get(j).length()>4){
			// 	String temp = tempPerms.get(j); //declares permutation as temp
			// 	int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			// 	bordaPoints[person0]+=(votes.get(j)*5);//adds up votes per person times 3

			// 	int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
			// 	bordaPoints[person1]+=(votes.get(j)*4);//adds up votes per person times 2

			// 	int person2 = (int)((char)(temp.charAt(2))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person2]+=(votes.get(j)*3);//adds up votes per person times 1
			// 	//at this point, bordaPoints[0] is the number of points for candidate A, etc, many of which are still zero.
			// 	int person3 = (int)((char)(temp.charAt(3))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person3]+=(votes.get(j)*2);
			// 	int person4 = (int)((char)(temp.charAt(4))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person4]+=(votes.get(j));
			// }
			// if(tempPerms.get(j).length()==4){
			// 	String temp = tempPerms.get(j); //declares permutation as temp
			// 	int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			// 	bordaPoints[person0]+=(votes.get(j)*5);//adds up votes per person times 3

			// 	int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
			// 	bordaPoints[person1]+=(votes.get(j)*4);//adds up votes per person times 2

			// 	int person2 = (int)((char)(temp.charAt(2))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person2]+=(votes.get(j)*3);//adds up votes per person times 1
			// 	//at this point, bordaPoints[0] is the number of points for candidate A, etc, many of which are still zero.
			// 	int person3 = (int)((char)(temp.charAt(3))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person3]+=(votes.get(j)*2);
			// }
			// if(tempPerms.get(j).length()==3){
			// 	String temp = tempPerms.get(j); //declares permutation as temp
			// 	int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			// 	bordaPoints[person0]+=(votes.get(j)*5);//adds up votes per person times 3

			// 	int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
			// 	bordaPoints[person1]+=(votes.get(j)*4);//adds up votes per person times 2

			// 	int person2 = (int)((char)(temp.charAt(2))- 'A'); //gets int for which person is third in temp
			// 	bordaPoints[person2]+=(votes.get(j)*3);//adds up votes per person times 1
			// 	//at this point, bordaPoints[0] is the number of points for candidate A, etc, many of which are still zero.
			// }else if(tempPerms.get(j).length()==2){
			// 	String temp = tempPerms.get(j); //declares permutation as temp
			// 	int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			// 	bordaPoints[person0]+=(votes.get(j)*5);//adds up votes per person times 3

			// 	int person1 = (int)((char)(temp.charAt(1))- 'A'); //gets int for which person is second in temp
			// 	bordaPoints[person1]+=(votes.get(j)*4);//adds up votes per person times 2
			// }else if(tempPerms.get(j).length()==1){
			// 	String temp = tempPerms.get(j); //declares permutation as temp
			// 	int person0 = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			// 	bordaPoints[person0]+=(votes.get(j)*5);//adds up votes per person times 3
			// }

		}	
		
		//figure out which candidate has the most Borda points
		int min = 2147483647;
		int minPos = 0;

		int max = -1;
		int bordaWin = 0;

		for(int n = 0; n<picks;n++){
			if(bordaPoints[n]<min && bordaPoints[n] != 0){
				min = bordaPoints[n];
				minPos = n;
			}
			if(bordaPoints[n]>max){
				max = bordaPoints[n];
				bordaWin = n;
			}
		}
		char bordaWinner = (char)(bordaWin + 'A');

		//check to see if borda winner and IRV winner match
		if(origWinner!=bordaWinner){
				System.out.println("Borda count anomaly!  Borda winner is Candidate " + bordaWinner +" and the IRV winner is Candidate " + origWinner +".");
			}else{
				System.out.println("No Borda count anomaly.");
			}
	}	//end bordaWinner check


		/* TOP THREE No-show CHECK.  
	Pre: takes in voting data, winner, and number of picks
	Post: Outputs whether or not there is a violation, and how it happens.

	Finds gap between second and third place candidates, and checks to see if RWL votes are enough to close the gap to make second-place drop out before third.  If so, drops that number of votes and then checks to see if that causes a new winner.
	*/
	
	static void topThreeNoShowCheck(ArrayList<String> topThreePerms, ArrayList<Integer> topThreeVotes, char winner, int picks){	
		int votesPer[] = new int[picks];
		if(topThreePerms.size()!=9){System.out.println("Potential error, less than 9 items in array\n");
		}
		for(int i = 0;i<picks;i++){
			votesPer[i] = 0; 
		}
		for(int count = 0;count< topThreePerms.size();count++){
			String temp = topThreePerms.get(count); //declares permutation as temp
			int person = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			votesPer[person]+=topThreeVotes.get(count);	//adds up votes per person
			//at this point, votesPer[0] is the number of votes for candidate A, etc.
		}
		//System.out.println("System check.\n Total votes are"+votesPer[0]+" for A, "+votesPer[1]+" for B, and " + votesPer[2] + " for C.\n");

		//declare ints for min, max
		int min = 2147483647;
		int minPos = 0;

		int max = -1;
		int maxPos = 0;


	for(int i = 0; i<picks;i++){
		if(votesPer[i]<min && votesPer[i] != 0){
			min = votesPer[i];
			minPos = i;
		}
		if(votesPer[i]>max){
			max = votesPer[i];
			maxPos = i;
		}
	}
	//System.out.println("Min position is "+minPos+", with votes of "+min+" \n");
	//System.out.println("Max position is "+maxPos+", with votes of "+max+" \n");
	//what if it is equal?
	/*Now have data that minPos=3rd place candidate, min=number of votes for that candidate=votesPer[minPos], ditto for maxPos */ 
	//Then need to find number for non winning, non losing candidate.  Do

	int winnerPos = (int)((char)(winner - 'A'));

	int otherPos = 0; 
	while (otherPos==minPos || otherPos==winnerPos) { 
		otherPos++;
	}
	//System.out.println("Other position is "+otherPos+" \n");
	//System.out.println("Winner position is "+winnerPos+" \n");
	/*At this point, winnerPos is the number for winner, otherPos is number for nonwinnerNonloser, minPos=number for loser  */
	//Now find gap between third place and second

	int n = 0;
	n=votesPer[otherPos]-votesPer[minPos];
	//System.out.println("Gap is "+n+" \n");
	if(n<0){
		System.out.println("Error. Gap between second and third is calculating wrong.\n");
	}/* output error if n<=0 */


	/*(Note: let winnerPos be winner (numerical), minPos be loser, otherPos be other candidate, could be in first or second place, hard to say)*/

	//convert winnerPos etc back to letters, call them R=other, W=winner

	char R = (char)(otherPos + 'A');
	char W = (char)(winnerPos + 'A'); //this should be same as winner
	char L = (char)(minPos + 'A');
	//System.out.println("Other is "+R+", Winner is "+W+", Loser is "+L+" \n");
	if(W==winner){
	} else{
		System.out.println("Error in calculation of winning candidate\n");
	}
	

	//make strings to match perm data:
	String rlw= "" + R + L + W;
	//System.out.println("rwl is "+rwl+" \n");
	int rlwPos=-1;
	for(int j = 0;j<topThreePerms.size();j++){
		if (topThreePerms.get(j).equals(rlw)){
   			rlwPos = j;
   			//System.out.println("rlwpos is "+rlwPos +"\n");
		} 
	}//now have index for permutation RLW
	
	if(topThreeVotes.get(rlwPos)>=(n+1)){
	
		topThreeVotes.set(rlwPos,(topThreeVotes.get(rlwPos)-(n+1)));
		
	 	char N = round(topThreePerms, topThreeVotes, picks);
	 	if(N!=W){                       //returns anomaly and some data
	 		System.out.println("No-Show Anomaly!\n");
	 		System.out.println("Removed "+ n +" + 1 votes from RLW column, and now " + L + " is the winner");
	 		//System.exit(0);
	 	} else {
	 		System.out.println("No No-show anomaly for three-person.\n"); 
	 		//System.exit(0);
	 	}
	}                            
	
	else{ //if #RLW<(n+1-#RWL-#R), return no monotonicity anomaly.
		System.out.println("No No-show anomaly.  Not enough RLW votes to make up gap and rerun election.\n");
	} //ends else statement
	} //ends No-show check



} 
//ends IRVadamBetter
