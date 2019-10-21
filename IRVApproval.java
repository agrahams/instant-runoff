import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
public class IRVApproval {
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
			//run IRV election and get rankings
			String ranking = roundRanking(permutations, votesPerPermutation, totalRunners);

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
				rankingCompProg(permutations, votesPerPermutation, totalRunners);
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
			System.out.println("10 = IRV vs. Approval Ranking comparison");

		}

		/*System.out.println("The Winner is: "+winner);
		//round(originals,originalNumbers,totalRunners);
		s.next();
		System.out.println(Second);
		System.out.println(totalRunners);
		System.out.println(totalPicks);
		System.out.println(originals.size());

		int possible = totalRunners;
		for(int i = 1;i<totalPicks;i++){
			possible*=(totalRunners+1);
		}
		System.out.println(possible);
		//sort(originals, originalNumbers,winner, totalRunners);
		 */
		
		System.out.println();


	}

	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns a character of the winner of the election
	//runs 1 round of IRV voting for the given ballots
	static char round(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;

		//for(int i = 0;i<tops.size();i++){
		//	System.out.println(votes.get(i) + " " +tops.get(i));
		//}

		//while there is no winner. ADAM NOTE: need to better understand how "winner" works.
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

			/*if(max/((double)total)*100 >50){
				System.out.println("The Winner is: " + (char)(maxPos+'A'));
				return (char)(maxPos+'A');
			} */


			//gets the second place finisher and sets the global variable equal to that variable
			char minChar = (char)(minPos + 'A');
			if(getSecond){
				//System.out.println(minChar);
				Second = minChar;
			}

			//System.out.println("Lowest votes: " + minChar);
			//for(int i = 0;i<votesPer.length;i++){
			//	System.out.println((char)(i+65) + " = " + votesPer[i]);
			//}
			/* for(int i = 0;i<tops.size();i++){
				char current = tops.get(i).charAt(0);

				if(current == minChar){
					if(tops.get(i).length() > 1){
						tops.set(i, tops.get(i).substring(1));
					}else{
						tops.remove(i);
					}
				}
			} */

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
					if(compactStrings.get(i).length()==2){
						compactStrings2.add(compactStrings.get(i));
						compactVotes2.add(compactVotes.get(i));
					}
				}	
				for(int i = 0;i<compactStrings.size();i++){
					if(compactStrings.get(i).length()==3){
						for(int m = i;m<compactStrings.size();m++){
							if(compactStrings.get(m).length()==3 && !(compactStrings.get(m).charAt(0)==compactStrings.get(m).charAt(1) || compactStrings.get(m).charAt(0)==compactStrings.get(m).charAt(2) || compactStrings.get(m).charAt(1)==compactStrings.get(m).charAt(2))){  //check that length is 3 and no same entries
								int temp = compactVotes.get(m);
								for(int k=m-1;k>-1;k--){ //search all entries less than m to see if they are the "same"
									if(compactStrings.get(k).length()==3 && (compactStrings.get(k).charAt(0)==compactStrings.get(k).charAt(1) || compactStrings.get(k).charAt(0)==compactStrings.get(k).charAt(2) || (compactStrings.get(k).charAt(2)==compactStrings.get(k).charAt(1))) && (compactStrings.get(m).substring(0,2).equals(compactStrings.get(k).substring(0,2)) || compactStrings.get(k).equals(compactStrings.get(m).substring(0,1)+compactStrings.get(m).substring(0,2)))){
										temp = temp + compactVotes.get(k);
									}
								}
								for(int k=m+1;k<compactStrings.size();k++){
									if(compactStrings.get(k).length()==3 && (compactStrings.get(k).charAt(0)==compactStrings.get(k).charAt(1) || compactStrings.get(k).charAt(0)==compactStrings.get(k).charAt(2) || (compactStrings.get(k).charAt(2)==compactStrings.get(k).charAt(1))) && (compactStrings.get(m).substring(0,2).equals(compactStrings.get(k).substring(0,2)) || compactStrings.get(k).equals(compactStrings.get(m).substring(0,1)+compactStrings.get(m).substring(0,2)))){  /* Used to sort of work as: compactStrings.get(m).substring(0,2).equals(compactStrings.get(k).substring(0,2))){*/
										temp = temp + compactVotes.get(k);
									}
								}
								compactStrings2.add(compactStrings.get(m));
								compactVotes2.add(temp);
								break; //this is better, still not perfect
							} break;//This did not work.  It might add okay (need to double check), but it eliminates the AA and AAA options without adding them.  It also does NOT add the AAE to AED as it should.
						}
					}
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
		/*		//Now need to combine all three-vote options into one.  That is, make ABA and ABB also count for ABC
				ArrayList<Integer> compactVotesMost2 = new ArrayList<Integer>();
				ArrayList<String> compactStringsMost2 = new ArrayList<String>();

				for(int i = 0;i<compactStringsMost.size();i++){
					
					if(compactStringsMost.get(i).length()==1){
						compactStringsMost2.add(compactStringsMost.get(i));
						compactVotesMost2.add(compactVotesMost.get(i));
					}else if(compactStrings2.get(i).length()==2){
						
					}else if(compactStrings2.get(i).length()==3){
						int temp=0;
						for(int k = i+1;k<compactStrings2.size();k++){
							if(compactStrings2.get(i).equals(compactStrings2.get(k).substring(0,2))){
								compactStringsMost.add(compactStrings2.get(k));
								compactVotesMost.add(compactVotes2.get(k)+compactVotes2.get(i));
							}
						}
						
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
				}*/
			}
		}

		System.out.println("The IRV Winner is: " + tops.get(0).charAt(0));

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

	//Start new RAnking program here
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns an array ranking candidates in the election
	//runs an IRV election for the given ballots and gives a ranking of the candidates
	static String roundRanking(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		
		String rankingArray[] = new String[picks];
		for (int i = 0; i < rankingArray.length; i++) {
		    rankingArray[i] = "";
		}
		int d = picks-1;
		//while there is no winner. ADAM NOTE: need to better understand how "winner" works.
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
				//System.out.println(minChar);
				Second = minChar;
			}
			//Add person who just got eliminated to ranking array
			/*  Do something like this:  add eliminated person to the n-1 position in an array which we will call rankingArray--need to figure out where to set it up.  Once eliminated person is added to ranking array, need to do -1 to some counter so that next eliminated person gets put into n-2 position, etc.
			*/
			rankingArray[d] = String.valueOf(minChar);
			d--;

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
		String irvranking = "";
		for(int i = 0; i < picks;i++){
			irvranking = irvranking + rankingArray[i];
		}
		String irvranking2 =  tops.get(0).charAt(0) + irvranking;
		//System.out.println("The Ranking, from winner to first eliminated, is: " + irvranking2);
		//String IRVranking = rankingArray[picks-1] + rankingArray[0];
		
		return irvranking2;
	}
	//END NEW RANKING PROGRAM

	//APPROVAL VOTE RANKING
	//Pre: take in arraylist of rankings, arraylist of votes, and number of candidates
	//Post: outputs ranking of approval vote
	static String roundApproval(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		
		String approvalArray[] = new String[picks];
		for (int i = 0; i < approvalArray.length; i++) {
		    approvalArray[i] = "";
		}
		int d = picks-1;
		char approvalRankingArray[] = new char[picks]; //declare approval ranking array
		//for (int i = 0; i < approvalRankingArray.length; i++) {
		//    approvalRankingArray[i] = "";
		//}
		//while there is no winner. ADAM NOTE: need to better understand how "winner" works.
		
			int votesPer[] = new int[picks];

			for(int i = 0;i<picks;i++){
				votesPer[i] = 0;
			}

			//get the number of votes per candidate and put them into the array votesPer (votes for A in votesPer[0] etc)
			for(int count = 0;count< tops.size();count++){
				String temp = tops.get(count);
				int person0 = (int)((char)(temp.charAt(0))- 'A');
				votesPer[person0]+=votes.get(count);
				if(temp.length() > 1 && temp.charAt(1) != temp.charAt(0)){ 
					int person1 = (int)((char)(temp.charAt(1))- 'A');
				votesPer[person1]+=votes.get(count);
				}
				if(temp.length()>2 && temp.charAt(2) != temp.charAt(0) && temp.charAt(2) != temp.charAt(1)){
					int person2 = (int)((char)(temp.charAt(2))- 'A');
					votesPer[person2]+=votes.get(count);
				}
				if(temp.length()>3 && temp.charAt(3) != temp.charAt(0) && temp.charAt(3) != temp.charAt(1) && temp.charAt(3) != temp.charAt(2)){
					int person3 = (int)((char)(temp.charAt(3))- 'A');
					votesPer[person3]+=votes.get(count);
				}
				if(temp.length()>4 && temp.charAt(4) != temp.charAt(0) && temp.charAt(4) != temp.charAt(1) && temp.charAt(4) != temp.charAt(2) && temp.charAt(4) != temp.charAt(3)){
					int person4 = (int)((char)(temp.charAt(4))- 'A');
					votesPer[person4]+=votes.get(count);
				}
			}
			
			/*
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
			*/

			//at this point, votesPer has the number of approval votes for each candidate.  Now I need to create a new array with them in the correct ranking.  First, create a temporary array with same Approval vote info
			int votesPerTemp[] = new int[picks];
			for(int i = 0;i<picks;i++){
				votesPerTemp[i] = votesPer[i];
			}

			//now figure out which candidate has highest number of votes
			for(int i = 0; i < picks; i++){
				int max2 = 0;
				int maxPos2 = 0;
				for(int j = 0; j < votesPerTemp.length; j++){
					if(votesPerTemp[j] > max2){
						max2 = votesPerTemp[j];
						maxPos2 = j;
					}
				}
				char maxChar2 = (char)(maxPos2 + 'A'); //cast highest approval to character
				approvalRankingArray[i] = maxChar2; //put highest approval in ranking
				//System.out.println("maxChar is" + maxChar2);
				votesPerTemp[maxPos2] = -1; //make their votes zero to find next highest
			}
			

			//now make a string of the ranking
		
		String approvalRanking = "";
		for(int i = 0; i < approvalRankingArray.length; i++){
			approvalRanking = approvalRanking + approvalRankingArray[i];
		}
		//System.out.println("The Approval vote ranking, from highest to lowest, is: " + approvalRanking);
		return approvalRanking;
	}
	
	//RANKING COMPARISON PROGRAM
	//Pre: takes in arraylist rankings and votes, also number of picks
	//Post: Compares rankings of IRV and Approval Voting, states if there is any discrepancy (major or minor), and Prints out the ranking of the candidates if there is any discrepancy.
	static void rankingCompProg(ArrayList<String> a, ArrayList<Integer> b, int number){
		String approvalRank = roundApproval(a, b, number);
		String irvRank = roundRanking(a, b, number);
		char aR1 = approvalRank.charAt(0);
		char iR1 = irvRank.charAt(0);
		if(approvalRank.equals(irvRank)){
			System.out.println("The rankings are identical");
		}else if(aR1 != iR1){
			System.out.println("The winner of the election is different!  The rankings are");
			System.out.println("Approval ranking:" + approvalRank);
			System.out.println("IRV ranking:" + irvRank);
		}else{
			System.out.println("The rankings are different!  The rankings are");
			System.out.println("Approval ranking:" + " " +approvalRank);
			System.out.println("IRV ranking:     " + " " +irvRank);
		}
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
	//static void topThreeGapSort(ArrayList<String> compactStringsMost, ArrayList<Integer> compactVotesMost, char winner, int picks){
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
	System.out.println("Min position is "+minPos+", with votes of "+min+" \n");
	System.out.println("Max position is "+maxPos+", with votes of "+max+" \n");
	//what if it is equal?
	/*Now have data that minPos=3rd place candidate, min=number of votes for that candidate=votesPer[minPos], ditto for maxPos */ 
	//Then need to find number for non winning, non losing candidate.  Do

	int winnerPos = (int)((char)(winner - 'A'));

	int otherPos = 0; 
	while (otherPos==minPos || otherPos==winnerPos) { //maybe should not use == here? =failed
		otherPos++;
	}
	System.out.println("Other position is "+otherPos+" \n");
	System.out.println("Winner position is "+winnerPos+" \n");
	/*At this point, winnerPos is the number for winner, otherPos is number for nonwinnerNonloser, minPos=number for loser  */
	//Now find gap between third place and second

	int n = 0;
	n=votesPer[otherPos]-votesPer[minPos];
	System.out.println("Gap is "+n+" \n");
	if(n<0){
		System.out.println("Error. Gap between second and third is calculating wrong.\n");
	}/* output error if n<=0 */


	/*(Note: let winnerPos be winner (numerical), minPos be loser, otherPos be other candidate, could be in first or second place, hard to say)*/

	//convert winnerPos etc back to letters, call them R=other, W=winner
	//THIS WAY DID NOT COMPILE:
	//char R=String.valueOf((char)(otherPos + 'A'));
	//char W=String.valueOf((char)(winnerPos + 'A')); //this should be same as winner
	//char L=String.valueOf((char)(minPos + 'A'));
	char R = (char)(otherPos + 'A');
	char W = (char)(winnerPos + 'A'); //this should be same as winner
	char L = (char)(minPos + 'A');
	System.out.println("Other is "+R+", Winner is "+W+", Loser is "+L+" \n");
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
	/*	When I had this code: topThreeVotes[rwlPos] = topThreeVotes.get(rwlPos)-(n+1); it would not compile.  Then I switched it to the "set" thing, and now it compiles.  Have no idea if it does what I want, though. */
	//Could also change the arraylist to an array, then it would work.
		topThreeVotes.set(rwlPos,(topThreeVotes.get(rwlPos)-(n+1)));
		topThreeVotes.set(wrlPos,(topThreeVotes.get(wrlPos)+(n+1)));
		//topThreeVotes[wrlPos] = topThreeVotes.get(wrlPos)+(n+1); how it used to be
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
	static void indepIrrelAlt(ArrayList<String> topThreePerms, ArrayList<Integer> topThreeVotes, char winner, int picks){	//need to fix the inputs.  Need full Preferences, not just top three, and votes, may not need picks?

	}

} //ends IRVadamBetter