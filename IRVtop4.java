import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
public class IRVtop4 {
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

			//run 1 round and return an array of the order in which candidates dropped out 
			permutations.clear();
			permutations.addAll(originals);
			votesPerPermutation.clear();
			votesPerPermutation.addAll(originalNumbers);
			char[] orderArray = ranking(permutations, votesPerPermutation, totalRunners);
			permutations.clear();
			permutations.addAll(originals);
			votesPerPermutation.clear();
			votesPerPermutation.addAll(originalNumbers);
			ArrayList<Integer> top4Votes = compactingVotes(permutations, votesPerPermutation, totalRunners);
			permutations.clear();
			permutations.addAll(originals);
			votesPerPermutation.clear();
			votesPerPermutation.addAll(originalNumbers);
			ArrayList<String> top4Perms = compactingPerms(permutations, votesPerPermutation, totalRunners);
			
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
			}/*else if (whichCheck == 10){
				indepIrrelAlt(//permutations, votesPerPermutation, winner, totalRunners);  NEED TO FIGURE OUT WHAT THE INPUTS ARE
			}*/else if (whichCheck == 11){
				topFourGapSort(top4Perms, top4Votes, winner, orderArray, totalRunners);
			}else if (whichCheck == 12){
				topFourNoShow(top4Perms, top4Votes, winner, orderArray, totalRunners);
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
			System.out.println("11 = Top Four Gap Check");
			System.out.println("12 = Top Four No Show");
		}

		
		System.out.println();


	}

/*	//ROUND method
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns a character of the winner of the election.  
	//runs 1 round of IRV voting for the given ballots
	static char round(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		char[] rankingArray = new char[picks]; 

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
			System.out.println("votesPer are");
			for(int i = 0; i<picks;i++){
				System.out.println(votesPer[i]+ " ");
			}	

			//gets the second place finisher and sets the global variable equal to that variable
			char minChar = (char)(minPos + 'A');
			if(getSecond){
				//System.out.println(minChar);
				Second = minChar;
			}

			//insert loser into ranking array
			rankingArray[picks-1-c] = minChar;  

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

			System.out.println("Top " + c + " are");
			for(int i = 0; i<tops.size();i++){
				System.out.println(tops.get(i)+ " " + votes.get(i) + " ");
			}	

			c++;
		
			 
		}

		System.out.println("The Winner is: " + tops.get(0).charAt(0));

		System.out.println("The Ranking is: ");  
			for(int i = 0;i<picks;i++){
						System.out.println(i+1 + " " + rankingArray[i] + " ");
						
					}

		return tops.get(0).charAt(0);
	}*/

//Round with text file
	//ROUND with text file method
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns a character of the winner of the election.  Also spits out top 4 text file
	//runs 1 round of IRV voting for the given ballots
	static char round(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		//tops = new ArrayList<String>(tops);
		//votes = new ArrayList<Integer>(votes);

		int c = 0;
		char[] rankingArray = new char[picks]; 

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
			/*System.out.println("votesPer are");
			for(int i = 0; i<picks;i++){
				System.out.println(votesPer[i]+ " ");
			}	*/

			//gets the second place finisher and sets the global variable equal to that variable
			char minChar = (char)(minPos + 'A');
			if(getSecond){
				//System.out.println(minChar);
				Second = minChar;
			}

			//insert loser into ranking array
			rankingArray[picks-1-c] = minChar;  

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

			/*System.out.println("Top " + c + " are");
			for(int i = 0; i<tops.size();i++){
				System.out.println(tops.get(i)+ " " + votes.get(i) + " ");
			}	*/

			c++;

			//once there are 4 runners left, save the top 4 finishers to a text file with their combinations, then compact those combinations and save those to another text file
			if(c == picks-4){
				getSecond = true;
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopFour.txt"));

					for(int i = 0;i<tops.size();i++){
						out.write("" + votes.get(i) + " " + tops.get(i));
						out.newLine();
					}

					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//Finds all repeat permutations and adds them together
				ArrayList<Integer> compactVotes4 = new ArrayList<Integer>();
				ArrayList<String> compactStrings4 = new ArrayList<String>();

				for(int i = 0;i<tops.size();i++){
					if(compactStrings4.contains(tops.get(i))){
						compactVotes4.set(compactStrings4.indexOf(tops.get(i)), compactVotes4.get(compactStrings4.indexOf(tops.get(i)))+votes.get(i)); //if a permutation is already found, add it to current number of votes
					}else{
						compactStrings4.add(tops.get(i));
						compactVotes4.add(votes.get(i));
					}//if permutation not already there, add it to the arrays
				}

				//Now write out the result to a text file called TopFourCompact
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopFourCompact.txt"));

					for(int i = 0;i<compactVotes4.size();i++){
						out.write("" + compactVotes4.get(i) + " " + compactStrings4.get(i));
						out.newLine();
					}

					out.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// for(int i = 0; i<compactVotes4.size();i++){
				// 	System.out.println("" + compactVotes4.get(i) + " " + compactStrings4.get(i));
				// }


				//Now we have adam attempt at making it more compact:  First we put in all one vote and two vote options, then compact the three-vote options
				ArrayList<Integer> compactVotes24 = new ArrayList<Integer>();
				ArrayList<String> compactStrings24 = new ArrayList<String>();

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==1){  //if permutation has length 1
						int temp1 = compactVotes4.get(i);  //temporary number of votes
						for(int p=i-1;p>-1;p--){ //this should consolidate all A, AA, and AAA votes into one spot as temp1, first for values less than i, then for greater than i.  Not sure if it actually works, though? (May 2017)
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						for(int p=i+1;p<compactStrings4.size();p++){
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						compactStrings24.add(compactStrings4.get(i)); //add votes and permutation to new arrays
						compactVotes24.add(temp1);
					}
				}
				/*for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==2){
						compactStrings24.add(compactStrings4.get(i));
						compactVotes24.add(compactVotes4.get(i));
					} //adds all two-letter permutations to new arrays
				}	*/
				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==2 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1))) {
						int temp = compactVotes4.get(i);
						for(int m = i+1;m<compactStrings4.size();m++){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices greater than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

						for(int m = i-1;m>-1;m--){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices less than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

								compactStrings24.add(compactStrings4.get(i));
								compactVotes24.add(temp);
								//break; 
					} //break;
				}

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==3 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1) || compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(2) || compactStrings4.get(i).charAt(1)==compactStrings4.get(i).charAt(2))){  // if permutation has length 3 and no matching entries
						compactStrings24.add(compactStrings4.get(i));
						compactVotes24.add(compactVotes4.get(i));
					} //adds all non-repeating three-letter permutations to new arrays
				}	

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("TopFourMostCompact.txt"));
					out.write("" + picks);
					out.newLine();
					out.write("3");
					out.newLine();
					for(int i = 0;i<compactVotes24.size();i++){
						out.write("" + compactVotes24.get(i) + " " + compactStrings24.get(i));
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

		System.out.println("The Winner is: " + tops.get(0).charAt(0));

		/*System.out.println("The Ranking is: ");  
			for(int i = 0;i<picks;i++){
						System.out.println(i+1 + " " + rankingArray[i] + " ");
						
					} */

		return tops.get(0).charAt(0);
	}






	//TOP 4 Votes method
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns an array with the top 4 vote totals in correct order
	//runs 1 round of IRV voting for the given ballots
	static ArrayList<Integer> compactingVotes(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		ArrayList<Integer> totalCompactVotes = new ArrayList<Integer>();
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

			//once there are 4 runners left compact those combinations 
			if(c == picks-4){
				getSecond = true;

				//Finds all repeat permutations and adds them together
				ArrayList<Integer> compactVotes4 = new ArrayList<Integer>();
				ArrayList<String> compactStrings4 = new ArrayList<String>();

				for(int i = 0;i<tops.size();i++){
					if(compactStrings4.contains(tops.get(i))){
						compactVotes4.set(compactStrings4.indexOf(tops.get(i)), compactVotes4.get(compactStrings4.indexOf(tops.get(i)))+votes.get(i)); //if a permutation is already found, add it to current number of votes
					}else{
						compactStrings4.add(tops.get(i));
						compactVotes4.add(votes.get(i));
					}//if permutation not already there, add it to the arrays
				}

				//Now we have adam attempt at making it more compact:  First we put in all one vote and two vote options, then compact the three-vote options
				ArrayList<Integer> compactVotes24 = new ArrayList<Integer>();
				ArrayList<String> compactStrings24 = new ArrayList<String>();

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==1){  //if permutation has length 1
						int temp1 = compactVotes4.get(i);  //temporary number of votes
						for(int p=i-1;p>-1;p--){ //this should consolidate all A, AA, and AAA votes into one spot as temp1, first for values less than i, then for greater than i.  Not sure if it actually works, though? (May 2017)
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						for(int p=i+1;p<compactStrings4.size();p++){
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						compactStrings24.add(compactStrings4.get(i)); //add votes and permutation to new arrays
						compactVotes24.add(temp1);
					}
				}
				
				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==2 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1))) {
						int temp = compactVotes4.get(i);
						for(int m = i+1;m<compactStrings4.size();m++){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices greater than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

						for(int m = i-1;m>-1;m--){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices less than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

								compactStrings24.add(compactStrings4.get(i));
								compactVotes24.add(temp);
								//break; 
					} //break;
				}

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==3 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1) || compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(2) || compactStrings4.get(i).charAt(1)==compactStrings4.get(i).charAt(2))){  // if permutation has length 3 and no matching entries
						compactStrings24.add(compactStrings4.get(i));
						compactVotes24.add(compactVotes4.get(i));
					} //adds all non-repeating three-letter permutations to new arrays

				}	
				//Copy cV24 to tCV
				for(int i=0;i<compactVotes24.size();i++){
					totalCompactVotes.add(compactVotes24.get(i));
				}
				
			}
		}
		/*System.out.println("Total compact votes are");
		for(int i = 0;i<totalCompactVotes.length;i++){
			System.out.println(""+ totalCompactVotes.get(i) + " ");
		}*/
		return totalCompactVotes;
	}

//TOP 4 Perms method
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: returns an array with the top 4 permutations in correct order
	//runs 1 round of IRV voting for the given ballots
	static ArrayList<String> compactingPerms(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		ArrayList<String> totalCompactStrings = new ArrayList<String>();
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

			//once there are 4 runners left, save the top 4 finishers to a text file with their combinations, then compact those combinations and save those to another text file
			if(c == picks-4){
				getSecond = true;

				//Finds all repeat permutations and adds them together
				ArrayList<Integer> compactVotes4 = new ArrayList<Integer>();
				ArrayList<String> compactStrings4 = new ArrayList<String>();

				for(int i = 0;i<tops.size();i++){
					if(compactStrings4.contains(tops.get(i))){
						compactVotes4.set(compactStrings4.indexOf(tops.get(i)), compactVotes4.get(compactStrings4.indexOf(tops.get(i)))+votes.get(i)); //if a permutation is already found, add it to current number of votes
					}else{
						compactStrings4.add(tops.get(i));
						compactVotes4.add(votes.get(i));
					}//if permutation not already there, add it to the arrays
				}

				//Now we have adam attempt at making it more compact:  First we put in all one vote and two vote options, then compact the three-vote options
				ArrayList<Integer> compactVotes24 = new ArrayList<Integer>();
				ArrayList<String> compactStrings24 = new ArrayList<String>();

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==1){  //if permutation has length 1
						int temp1 = compactVotes4.get(i);  //temporary number of votes
						for(int p=i-1;p>-1;p--){ //this should consolidate all A, AA, and AAA votes into one spot as temp1, first for values less than i, then for greater than i.  Not sure if it actually works, though? (May 2017)
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						for(int p=i+1;p<compactStrings4.size();p++){
							if((compactStrings4.get(p).length()==2 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i))) || (compactStrings4.get(p).length()==3 && compactStrings4.get(p).equals(compactStrings4.get(i)+compactStrings4.get(i)+compactStrings4.get(i)))){
								temp1 = temp1 + compactVotes4.get(p);
							}
						}
						compactStrings24.add(compactStrings4.get(i)); //add votes and permutation to new arrays
						compactVotes24.add(temp1);
					}
				}
				
				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==2 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1))) {
						int temp = compactVotes4.get(i);
						for(int m = i+1;m<compactStrings4.size();m++){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices greater than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

						for(int m = i-1;m>-1;m--){
							if(compactStrings4.get(m).length()==3 && (compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(0,1)) || compactStrings4.get(m).equals(compactStrings4.get(i)+compactStrings4.get(i).substring(1,2)) || compactStrings4.get(m).equals(compactStrings4.get(i).substring(0,1)+compactStrings4.get(i)))) {  // for all indices less than i, check that length is 3 and match the two-permutation.  So if perm is AB, this would catch ABA, ABB, and AAB
								temp = temp + compactVotes4.get(m);
							}
						}

								compactStrings24.add(compactStrings4.get(i));
								compactVotes24.add(temp);
								//break; 
					} //break;
				}

				for(int i = 0;i<compactStrings4.size();i++){
					if(compactStrings4.get(i).length()==3 && !(compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(1) || compactStrings4.get(i).charAt(0)==compactStrings4.get(i).charAt(2) || compactStrings4.get(i).charAt(1)==compactStrings4.get(i).charAt(2))){  // if permutation has length 3 and no matching entries
						compactStrings24.add(compactStrings4.get(i));
						compactVotes24.add(compactVotes4.get(i));
					} //adds all non-repeating three-letter permutations to new arrays
				}	
				//Copy cS24 to tCS
				for(int i=0;i<compactStrings24.size();i++){
					totalCompactStrings.add(compactStrings24.get(i));
				}
			}
		}
		return totalCompactStrings;
	}



	//Ranking program: Does the same thing as round method, but outputs an array with ranking of candidates instead of just the winner
	//Pre: takes in the voting combinations, the number of votes per combination, and the number of candidates in the election
	//Post: an array with ranking of candidates 
	//runs 1 round of IRV voting for the given ballots

static char[] ranking(ArrayList<String> tops, ArrayList<Integer> votes, int picks){
		int c = 0;
		char[] rankingArray = new char[picks]; 

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

			//insert loser into ranking array
			rankingArray[picks-1-c] = minChar;  

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
		rankingArray[0] = tops.get(0).charAt(0);
						
		return rankingArray;
	}

//End ranking program	




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

	//ADVANCED MONOTONICITY CHECK (David and Nick)
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


	//TOTAL MONOTONICITY CHECK (David and Nick)
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


	//SMART MONOTONICITY CHECK (David and Nick)
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


	//SMART MONOTONICITY UNIVERSAL CHECK (David and Nick)
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
	
	
	//SIMPLE MONOTONICITY CHECK  (David and Nick)
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
		

	/* TOP THREE MONOTONICITY CHECK.  (Adam)
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


	//General if check method:  If the check vote is greater than the gap, use this
	//takes in check position and swapper position, and gap between candidates (Note: the gap between candidates is n, and the program looks for n+1 votes, so the gap should be entered as n, the actual gap,  runs check for the gap.
	//NOTE: needs to be adjusted to have correct names for topFourVotes, perms, n3 (gap) and picks 

	//methods that will help the TopFour program run more nicely.  Unclear if it will actually change the underlying TopFour, etc file or if it will just do whatever internally.  Maybe need to return an arraylist?

	static void ifGreatern3(ArrayList<String> perms, ArrayList<Integer> votes, int picks, int gap, char winner, int checkPos, int swapPos){
		//move votes from checker to swapper
		votes.set(checkPos,(votes.get(checkPos)-(gap+1)));
		votes.set(swapPos,(votes.get(swapPos)+(gap+1)));
		
		//find new winner
	 	char N = round(perms, votes, picks);
	 	if(N!=winner){        //if the new winner is not the original winner, returns anomaly and some data
	 		System.out.println("Monotonicity Anomaly!\n");
	 		System.out.println("Swapped "+ gap +" + 1 votes from third-place to Winner, and now " + N + " is the winner");
	 		//System.exit(0);
	 	} else {
	 		System.out.println("perms: " + perms);
	 		System.out.println("No monotonicity anomaly with swaps up to " //+ perms.get(checkPos) + " to " + perms.get(swapPos)
	 			); 
	 		//System.exit(0);
	 	}
	}

	static void elseIfLessn3(ArrayList<Integer> votes, int gap, int checkPos, int swapPos){
		//else if not enough check votes: change all check votes to swap votes
		votes.set(swapPos,(votes.get(swapPos)+votes.get(checkPos)));
		gap = gap-votes.get(checkPos); //reset the gap now that 3rd place lost some votes
		votes.set(checkPos,0);
	}
	

/* TOP FOUR MONOTONICITY CHECK.  (Adam)
	Pre: takes in top four voting data, winner, ranking array, and number of picks
	Post: Outputs whether or not there is a violation, and how it happens.

	Finds gap between third and fourth place candidates, and switches votes in order to close the gap to make third-place drop out before fourth, then checks to see if that causes a monotonicity violation.  Repeats the process but forces 2nd-place to drop before fourth.
	*/
	//static void topThreeGapSort(ArrayList<String> compactStringsMost, ArrayList<Integer> compactVotesMost, char winner, int picks){
	static void topFourGapSort(ArrayList<String> topFourPerms, ArrayList<Integer> topFourVotes, char winner, char[] rankArray, int picks){	
		
		int votesPer[] = new int[picks];
		if(topFourPerms.size() > 40){System.out.println("Potential error, more than 40 items in array\n");
		}
		for(int i = 0;i<picks;i++){
			votesPer[i] = 0; 
		}
		for(int count = 0;count< topFourPerms.size();count++){
			String temp = topFourPerms.get(count); //declares permutation as temp
			int person = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			votesPer[person]+=topFourVotes.get(count);	//adds up votes per person
			//at this point, votesPer[0] is the number of votes for candidate A, etc.
		}
		System.out.println("System check.\n order array is");
		for(int i = 0;i<picks;i++){
			System.out.println(""+rankArray[i]); 
		}

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
		/*Now have data that minPos=4th place candidate, min=number of votes for that candidate=votesPer[minPos], ditto for maxPos */ 
		//Then need to find numbers for 2nd, 3rd place candidates.  Do

		int winnerPos = (int)((char)(winner - 'A'));
		if(winnerPos != (int)((char)(rankArray[0] - 'A'))){
			System.out.println("Potential error, winner does not match up\n");
		}
		if(minPos != (int)((char)(rankArray[3] - 'A'))){
			System.out.println("Potential error, loser does not match up\n");
		}
		int r2Pos = (int)((char)(rankArray[1] - 'A')); //number for second place
		int r3Pos = (int)((char)(rankArray[2] - 'A')); //number for third place
		
		System.out.println("2nd position is "+r2Pos+" \n"); 
		System.out.println("3rd position is "+r3Pos+" \n");
		System.out.println("Winner position is "+winnerPos+" \n");
		/*At this point, winnerPos is the number for winner, minPos=number for loser  */
		//Now find gap between fourth place and third

		int n3 = 0;
		n3 = votesPer[r3Pos]-votesPer[minPos];
		System.out.println("Gap between 3rd and last is "+n3+" \n");
		if(n3<0){
			System.out.println("Error. Gap between third and fourth is calculating wrong.\n");
		}/* output error if n<=0 */

		int n2 = 0;
		n2 = votesPer[r2Pos]-votesPer[minPos];
		System.out.println("Gap between 2nd and last is "+n2+" \n");
		if(n2<0){
			System.out.println("Error. Gap between second and fourth is calculating wrong.\n");
		}/* output error if n<=0 */



		/*(Note: winnerPos is eventual winner (numerical), minPos is 4th place and has lowest votes, 2ndPos and 3rdPos are other candidates, could be in any place, hard to say)*/

		//convert winnerPos etc back to letters, call them R=other, W=winner

		char R2 = (char)(r2Pos + 'A');
		char R3 = (char)(r3Pos + 'A');  //Letter for 3rd place
		char W = (char)(winnerPos + 'A'); //this should be same as winner
		char L = (char)(minPos + 'A');
		System.out.println("Third is "+R3+", Second is "+R2+ ", Winner is "+W+", Loser is "+L+" \n");
		if(W==winner){
		} else{
			System.out.println("Error in calculation of winning candidate\n");
		}
		//copy original numbers since top4 votes gets changed
		ArrayList<String> top4originalPerms=  new ArrayList<String>(topFourPerms);
		ArrayList<Integer> top4originalNumbers=  new ArrayList<Integer>(topFourVotes);

		//This part of the code is for checking all swaps that move the 3rd-place candidate below the 4th place candidate.

		//Define permutations and find their position in the TopFourPerms array.
		//make strings to match perm data:  First find array index of R3W 
		String r3w= "" + R3 + W;
		//System.out.println("r3w is "+r3w+" \n");
		int r3wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3w)){
	   			r3wPos = j;
	   			//System.out.println("r3wpos is "+r3wPos +"\n");
				//break;
			} 
		}
		String wr3= "" + W + R3 ;  //index of WR3
		int wr3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr3)){
	   			wr3Pos = j;
	   			//System.out.println("wr3pos is "+wr3Pos +"\n");
				//break;
			} 
		}
		String r3wr2= "" + R3 + W + R2; // define and find array index of R3WR2
		int r3wr2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3wr2)){
	   		r3wr2Pos = j;
	   		//System.out.println("r3wr2 position is "+r3wr2Pos +"\n");
			} 
		}
		String wr3r2= "" + W + R3 + R2; // define and find array index of R3WR2
		int wr3r2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr3r2)){
				wr3r2Pos = j;
			//break;
			} 
		}
		String r3wl= "" + R3 + W + L; // define and find array index of R3WL
		int r3wlPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3wl)){
					r3wlPos = j;
				//break;
			} 
		}
		String wr3l= ""  + W + R3+ L; // define and find array index of wR3L
		int wr3lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr3l)){
					wr3lPos = j;
				//break;
			} 
		}
		String r3= "" + R3; // define and find array index of R3
		int r3Posi=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3)){
					r3Posi = j;
			} 
		}
		String r3r2w= "" + R3 + R2 + W ; // define and find array index of r3r2w
		int r3r2wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2w)){
   				r3r2wPos = j;
			} 
		}
		String r3lw= "" + R3 + L + W ; // define and find array index of r3lw
		int r3lwPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3lw)){
   				r3lwPos = j;
			} 
		}
		String r3r2= "" + R3 + R2 ; // define and find array index of r3r2
		int r3r2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2)){
					r3r2Pos = j;
			} 
		}
		String r3l= "" + R3 + L ; // define and find array index of r3l
		int r3lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3l)){
					r3lPos = j;
			} 
		}
		String r3r2l= "" + R3 +R2 + L ; // define and find array index of r3r2l
		int r3r2lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2l)){
					r3r2lPos = j;
			} 
		}
		String r3lr2= "" + R3 + L + R2 ; // define and find array index of r3lr2
		int r3lr2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3lr2)){
					r3lr2Pos = j;
			} 
		}
		
		//check for swapping r3W
		if(topFourVotes.get(r3wPos)>=(n3+1)){ //if happen to have more r3W votes than the gap
			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wPos, wr3Pos);

			//  topFourVotes.set(r3wPos,(topFourVotes.get(r3wPos)-(n3+1)));
			// topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+(n3+1)));
			
		 // 	char N = round(topFourPerms, topFourVotes, picks);
		 // 	if(N!=W){        //if the new winner is not the original winner, returns anomaly and some data
		 // 		System.out.println("Monotonicity Anomaly!\n");
		 // 		System.out.println("Swapped "+ n3 +" + 1 votes from third-place to Winner, and now " + N + " is the winner");
		 // 		//System.exit(0);
		 // 	} else {
		 // 		System.out.println("No monotonicity anomaly with one swap r3w to wr3.\n"); 
		 // 		//System.exit(0); 
		 // 	}
		} else if(topFourVotes.get(r3wPos)<(n3+1)) {   //now if  topFourVotes[r3wPos]<(n+1), change all R3W to WR3
			//r3wNum=topFourVotes.get(r3wPos); 
			topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+topFourVotes.get(r3wPos)));
			n3 = n3-topFourVotes.get(r3wPos); //reset the gap now that 3rd place lost some votes
			topFourVotes.set(r3wPos,0);
		

		//Now look at swapping r3wr2
		//if #R3WR2 > new gap, swap enough R3Wr2 votes to become WR3r2:  Note m=rPos
		if(topFourVotes.get(r3wr2Pos)>=(n3+1)){

			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wr2Pos, wr3r2Pos);

			/*topFourVotes.set(r3wr2Pos,(topFourVotes.get(r3wr2Pos)-(n3+1)));
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
				//totalR=n3+1-r3wNum;            //returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w to Winner as well as votes R3Wr2 and now " + N + " is the winner\n");
				//System.exit(0);
			} else {
			System.out.println("No monotonicity anomaly with two swaps, wrl and r to r3w.\n");
			//System.exit(0);
			}*/
		} 	//if #r3wr2 is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3wr2Pos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3wr2Pos)));
		n3 = n3-topFourVotes.get(r3wr2Pos);
		topFourVotes.set(r3wr2Pos,0);


		//Now look at swapping r3wl
		//if #R3WL > new gap, swap enough R3Wr2 votes to become WR3r2 and rerun election to compare
		if(topFourVotes.get(r3wlPos)>=(n3+1)){

			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wlPos, wr3lPos);
			/*topFourVotes.set(r3wlPos,(topFourVotes.get(r3wlPos)-(n3+1)));
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2 and r3wl and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with three swaps: r3w, R3Wr2 and r3wl.\n");
				//System.exit(0);
			}*/
		} 	//if #r3wl is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3wlPos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3wlPos)));
		n3 = n3-topFourVotes.get(r3wlPos);
		topFourVotes.set(r3wlPos,0);


		//Now look at swapping r3
 		//if #r3 > new gap, swap enough R3 votes to become WR3 and rerun election to compare
		if(topFourVotes.get(r3Posi)>=(n3+1)){

			//ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3Posi, wr3Pos);

			topFourVotes.set(r3Posi,(topFourVotes.get(r3Posi)-(n3+1)));
			topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, and r3 and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with four swaps: r3w, R3Wr2, r3wl and r3.\n");
				//System.exit(0);
			}
		} 	//if #r3 is not enough to fill gap, add to wr3, reset gap, and zero out r3 votes
		else if(topFourVotes.get(r3Posi)<(n3+1)){
		topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+topFourVotes.get(r3Posi)));
		n3 = n3-topFourVotes.get(r3Posi);
		topFourVotes.set(r3Posi,0);


		//Now look at swapping r3r2w
 		//if #r3r2w > new gap, swap enough R3Wr2 votes to become WR3r2 and rerun election to compare
		if(topFourVotes.get(r3r2wPos)>=(n3+1)){
		topFourVotes.set(r3r2wPos,(topFourVotes.get(r3r2wPos)-(n3+1)));
		topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
		char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3 and r3r2w and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3 and r3r2w.\n");
				//System.exit(0);
			}
		} 	//if #r3r2w is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3r2wPos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3r2wPos)));
		n3 = n3-topFourVotes.get(r3r2wPos);
		topFourVotes.set(r3r2wPos,0);


		//Now look at swapping r3lw
 		//if #r3lw > new gap, swap enough R3LW votes to become WR3L and rerun election to compare
		if(topFourVotes.get(r3lwPos)>=(n3+1)){
			topFourVotes.set(r3lwPos,(topFourVotes.get(r3lwPos)-(n3+1)));
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
 				//returns anomaly and some data
 				System.out.println("Monotonicity Anomaly!\n");
 				System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w and r3lw and now " + N + " is the winner\n");
 				//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3, r3r2w and r3lw.\n");
				//System.exit(0);
			}
		} 	//if #r3lw is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3lwPos)<(n3+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3lwPos)));
			n3 = n3-topFourVotes.get(r3lwPos);
			topFourVotes.set(r3lwPos,0);


		//Now look at swapping r3r2
 		//if #r3r2 > new gap, swap enough r3r2 votes to become WR3r2 and rerun election to compare
		if(topFourVotes.get(r3r2Pos)>=(n3+1)){
			topFourVotes.set(r3r2Pos,(topFourVotes.get(r3r2Pos)-(n3+1)));
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw and r3r2 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw and r3r2.\n");
				//System.exit(0);
			}
		} 	//if #r3r2 is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3r2Pos)<(n3+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3r2Pos)));
			n3 = n3-topFourVotes.get(r3r2Pos);
			topFourVotes.set(r3r2Pos,0);


		//Now look at swapping r3L
 		//if #r3l > new gap, swap enough r3l votes to become Wr3l and rerun election to compare
		if(topFourVotes.get(r3lPos)>=(n3+1)){
			topFourVotes.set(r3lPos,(topFourVotes.get(r3lPos)-(n3+1)));
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w and r3l and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2 and r3l.\n");
				//System.exit(0);
			}
		} 	//if #r3l is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3lPos)<(n3+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3lPos)));
			n3 = n3-topFourVotes.get(r3lPos);
			topFourVotes.set(r3lPos,0);


		//Now look at swapping r3r2L
 		//if #r3r2l > new gap, swap enough r3r2l votes to become Wr3r2 and rerun election to compare
		if(topFourVotes.get(r3r2lPos)>=(n3+1)){
			topFourVotes.set(r3r2lPos,(topFourVotes.get(r3r2lPos)-(n3+1)));
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w, r3l and r3r2l and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 9 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2, r3l and r3r2l.\n");
			}
		} 	//if #r3r2l is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3r2lPos)<(n3+1)){
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3r2lPos)));
			n3 = n3-topFourVotes.get(r3r2lPos);
			topFourVotes.set(r3r2lPos,0);


		//Now look at swapping r3Lr2
 		//if #r3lr2 > new gap, swap enough r3lr2 votes to become Wr3l and rerun election to compare
			if(topFourVotes.get(r3lr2Pos)>=(n3+1)){
				topFourVotes.set(r3lr2Pos,(topFourVotes.get(r3lr2Pos)-(n3+1)));
				topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N!=W){  
 					//returns anomaly and some data
 					System.out.println("Monotonicity Anomaly!\n");
 					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w, r3l, r3r2l and r3lr2 and now " + N + " is the winner\n");
 					//System.exit(0);
				} else {
					System.out.println("No monotonicity anomaly with 10 swaps of leading r3.\n");
				}
			} 	
			//if #r3lr2 is not enough to fill gap, return no monotonicity anomaly.
			else{ 
				System.out.println("No monotonicity anomaly.  Not enough Third-place votes to make up gap and rerun election.\n");
			} 
			} 
		} 
		} 
		} 
		} 
		}
		}
	} // ends 2nd else if
	} //ends 1st else if

	topFourVotes.clear();
	topFourVotes.addAll(top4originalNumbers);
	topFourPerms.clear();
	topFourPerms.addAll(top4originalPerms);

	//This code is checking third-place under fourth, then having second-place win




		n3 = votesPer[r3Pos]-votesPer[minPos];
//check for swapping r3W
		if(topFourVotes.get(r3wPos)>=(n3+1)){ //if happen to have more r3W votes than the gap
			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wPos, wr3Pos);

			
		} else if(topFourVotes.get(r3wPos)<(n3+1)) {   //now if  topFourVotes[r3wPos]<(n+1), change all R3W to WR3
			//r3wNum=topFourVotes.get(r3wPos); 
			topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+topFourVotes.get(r3wPos)));
			n3 = n3-topFourVotes.get(r3wPos); //reset the gap now that 3rd place lost some votes
			topFourVotes.set(r3wPos,0);
		

		//Now look at swapping r3wr2
		//if #R3WR2 > new gap, swap enough R3Wr2 votes to become WR3r2:  Note m=rPos
		if(topFourVotes.get(r3wr2Pos)>=(n3+1)){

			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wr2Pos, wr3r2Pos);

		} 	//if #r3wr2 is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3wr2Pos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3wr2Pos)));
		n3 = n3-topFourVotes.get(r3wr2Pos);
		topFourVotes.set(r3wr2Pos,0);


		//Now look at swapping r3wl
		//if #R3WL > new gap, swap enough R3Wr2 votes to become WR3r2 and rerun election to compare
		if(topFourVotes.get(r3wlPos)>=(n3+1)){

			ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3wlPos, wr3lPos);
			
		} 	//if #r3wl is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3wlPos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3wlPos)));
		n3 = n3-topFourVotes.get(r3wlPos);
		topFourVotes.set(r3wlPos,0);


		//Now look at swapping r3
 		//if #r3 > new gap, swap enough R3 votes to become WR3 and rerun election to compare
		if(topFourVotes.get(r3Posi)>=(n3+1)){

			//ifGreatern3(topFourPerms, topFourVotes, picks, n3, W, r3Posi, wr3Pos);

			topFourVotes.set(r3Posi,(topFourVotes.get(r3Posi)-(n3+1)));
			topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, and r3 and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with four swaps: r3w, R3Wr2, r3wl and r3.\n");
				//System.exit(0);
			}
		} 	//if #r3 is not enough to fill gap, add to wr3, reset gap, and zero out r3 votes
		else if(topFourVotes.get(r3Posi)<(n3+1)){
		topFourVotes.set(wr3Pos,(topFourVotes.get(wr3Pos)+topFourVotes.get(r3Posi)));
		n3 = n3-topFourVotes.get(r3Posi);
		topFourVotes.set(r3Posi,0);


		//Now look at swapping r3lw
 		//if #r3lw > new gap, swap enough R3LW votes to become WR3L and rerun election to compare
		if(topFourVotes.get(r3lwPos)>=(n3+1)){
		topFourVotes.set(r3lwPos,(topFourVotes.get(r3lwPos)-(n3+1)));
		topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
		char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3 and ???r3r2w and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3 and ???r3r2w.\n");
				//System.exit(0);
			}
		} 	//if #r3lw is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3lwPos)<(n3+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3lwPos)));
		n3 = n3-topFourVotes.get(r3lwPos);
		topFourVotes.set(r3lwPos,0);


		//Now look at swapping r3r2w
 		//if #r3r2w > new gap, swap enough R3r2W votes to become WR3r2 and rerun election to compare
		if(topFourVotes.get(r3r2wPos)>=(n3+1)){
			topFourVotes.set(r3r2wPos,(topFourVotes.get(r3r2wPos)-(n3+1)));
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
 				//returns anomaly and some data
 				System.out.println("Monotonicity Anomaly!\n");
 				System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w and r3r2w and now " + N + " is the winner\n");
 				//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3, r3r2w and r3r2w.\n");
				//System.exit(0);
			}
		} 	//if #r3lw is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3r2wPos)<(n3+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3r2wPos)));
			n3 = n3-topFourVotes.get(r3r2wPos);
			topFourVotes.set(r3r2wPos,0);


		//Now look at swapping r3l
 		//if #r3l > new gap, swap enough r3l votes to become WR3l and rerun election to compare
		if(topFourVotes.get(r3lPos)>=(n3+1)){
			topFourVotes.set(r3lPos,(topFourVotes.get(r3lPos)-(n3+1)));
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw and r3l and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw and r3l.\n");
				//System.exit(0);
			}
		} 	//if #r3l is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3lPos)<(n3+1)){
			//rNum=topFourVotes.get(r3wlPos);
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+topFourVotes.get(r3lPos)));
			n3 = n3-topFourVotes.get(r3lPos);
			topFourVotes.set(r3lPos,0);


		//Now look at swapping r3r2
 		//if #r3r2 > new gap, swap enough r3r2 votes to become Wr3r2 and rerun election to compare
		if(topFourVotes.get(r3r2Pos)>=(n3+1)){
			topFourVotes.set(r3r2Pos,(topFourVotes.get(r3r2Pos)-(n3+1)));
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w and r3r2 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2 and r3l.\n");
				//System.exit(0);
			}
		} 	//if #r3r2 is not enough to fill gap, add to wr3r2 and reset gap
		else if(topFourVotes.get(r3r2Pos)<(n3+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3r2Pos)));
			n3 = n3-topFourVotes.get(r3r2Pos);
			topFourVotes.set(r3r2Pos,0);


		//Now look at swapping r3lr2
 		//if #r3lr2 > new gap, swap enough r3lr2 votes to become Wr3l and rerun election to compare
		if(topFourVotes.get(r3lr2Pos)>=(n3+1)){
			topFourVotes.set(r3lr2Pos,(topFourVotes.get(r3lr2Pos)-(n3+1)));
			topFourVotes.set(wr3lPos,(topFourVotes.get(wr3lPos)+(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w, r3l and r3lr2 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 9 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2, r3l and r3lr2.\n");
			}
		} 	//if #r3lr2 is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r3lr2Pos)<(n3+1)){
			topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+topFourVotes.get(r3lr2Pos)));
			n3 = n3-topFourVotes.get(r3lr2Pos);
			topFourVotes.set(r3lr2Pos,0);


		//Now look at swapping r3r2l
 		//if #r3r2l > new gap, swap enough r3r2l votes to become Wr3r2 and rerun election to compare
			if(topFourVotes.get(r3r2lPos)>=(n3+1)){
				topFourVotes.set(r3r2lPos,(topFourVotes.get(r3r2lPos)-(n3+1)));
				topFourVotes.set(wr3r2Pos,(topFourVotes.get(wr3r2Pos)+(n3+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N!=W){  
 					//returns anomaly and some data
 					System.out.println("Monotonicity Anomaly!\n");
 					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r3lw, r3r2w, r3l, r3r2l and r3r2l and now " + N + " is the winner\n");
 					//System.exit(0);
				} else {
					System.out.println("No monotonicity anomaly with 10 swaps of leading r3.\n");
				}
			} 	
			//if #r3r2l is not enough to fill gap, return no monotonicity anomaly.
			else{ 
				System.out.println("No monotonicity anomaly.  Not enough Third-place votes to make up gap and rerun election.\n");
			} 
			} 
		} 
		} 
		} 
		} 
		}
		}
	} // ends 2nd else if
	} //ends 1st else if

	topFourVotes.clear();
	topFourVotes.addAll(top4originalNumbers);
	topFourPerms.clear();
	topFourPerms.addAll(top4originalPerms);		




	//This part of the code is for checking all swaps that move the 2nd-place candidate below the 4th place candidate.  First part checks to see if it can make 3rd place win.

		//Define permutations and find their position in the TopFourPerms array.
		//make strings to match perm data:  First find array index of R2W 
		String r2w= "" + R2 + W;
		//System.out.println("r2w is "+r2w+" \n");
		int r2wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2w)){
	   			r2wPos = j;
	   			//System.out.println("r2wpos is "+r2wPos +"\n");
			} 
		}
		String wr2= "" + W + R2 ;  //index of WR2
		int wr2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr2)){
	   			wr2Pos = j;
	   			//System.out.println("wr2pos is "+wr2Pos +"\n");
				//break;
			} 
		}
		String r2wr3= "" + R2 + W + R3; // define and find array index of R2WR3
		int r2wr3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2wr3)){
	   		r2wr3Pos = j;
	   		//System.out.println("r2wr3 position is "+r2wr3Pos +"\n");
			} 
		}
		String wr2r3= "" + W + R2 + R3; // define and find array index of R2WR3
		int wr2r3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr2r3)){
				wr2r3Pos = j;
			//break;
			} 
		}
		String r2wl= "" + R2 + W + L; // define and find array index of R2WL
		int r2wlPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2wl)){
					r2wlPos = j;
				//break;
			} 
		}
		String wr2l= ""  + W + R2+ L; // define and find array index of wR2L
		int wr2lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(wr2l)){
					wr2lPos = j;
				//break;
			} 
		}
		String r2= "" + R2; // define and find array index of R2
		int r2Posi=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2)){
					r2Posi = j;
			} 
		}
		String r2r3w= "" + R2 + R3 + W ; // define and find array index of r2r3w
		int r2r3wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3w)){
					r2r3wPos = j;
			} 
		}
		String r2lw= "" + R2 + L + W ; // define and find array index of r2lw
		int r2lwPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2lw)){
					r2lwPos = j;
			} 
		}
		String r2r3= "" + R2 + R3 ; // define and find array index of r2r3
		int r2r3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3)){
					r2r3Pos = j;
			} 
		}
		String r2l= "" + R2 + L ; // define and find array index of r2l
		int r2lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2l)){
					r2lPos = j;
			} 
		}
		String r2r3l= "" + R2 +R3 + L ; // define and find array index of r2r3l
		int r2r3lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3l)){
					r2r3lPos = j;
			} 
		}
		String r2lr3= "" + R2 + L + R3 ; // define and find array index of r2lr3
		int r2lr3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2lr3)){
					r2lr3Pos = j;
			} 
		}



		//check for swapping r2W
		if(topFourVotes.get(r2wPos)>=(n2+1)){ //if happen to have more r2W votes than the gap
		
			topFourVotes.set(r2wPos,(topFourVotes.get(r2wPos)-(n2+1)));
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+(n2+1)));
			
		 	char N = round(topFourPerms, topFourVotes, picks);
		 	if(N!=W){        //if the new winner is not the original winner, returns anomaly and some data
		 		System.out.println("Monotonicity Anomaly!\n");
		 		System.out.println("Swapped "+ n2 +" + 1 votes from 2nd-place to Winner, and now " + N + " is the winner");
		 		//System.exit(0);
		 	} else {
		 		System.out.println("No monotonicity anomaly with one swap r2w to wr2.\n"); 
		 		//System.exit(0);
		 	}
		} else if(topFourVotes.get(r2wPos)<(n2+1)) {   //now if  topFourVotes[r2wPos]<(n+1), change all r2W to Wr2
			//r2wNum=topFourVotes.get(r2wPos); 
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+topFourVotes.get(r2wPos)));
			n2 = n2-topFourVotes.get(r2wPos); //reset the gap now that 2nd place lost some votes
			topFourVotes.set(r2wPos,0);
		

		//Now look at swapping r2wr3
		//if #r2wr3 > new gap, swap enough r2wr3 votes to become wr2r3:  Note m=rPos
		if(topFourVotes.get(r2wr3Pos)>=(n2+1)){
			topFourVotes.set(r2wr3Pos,(topFourVotes.get(r2wr3Pos)-(n2+1)));
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
				//totalR=n2+1-r3wNum;            //returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w to Winner as well as votes r2wr3 and now " + N + " is the winner\n");
				//System.exit(0);
			} else {
			System.out.println("No monotonicity anomaly with two swaps, wrl and r to r3w.\n");
			//System.exit(0);
			}
		} 	//if #r2wr3 is not enough to fill gap, add to wr2r3 and reset gap
		else if(topFourVotes.get(r2wr3Pos)<(n2+1)){
		//rNum=topFourVotes.get(r2wr3Pos);
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2wr3Pos)));
		n2 = n2-topFourVotes.get(r2wr3Pos);
		topFourVotes.set(r2wr3Pos,0);


		//Now look at swapping r2wl
		//if #r2wl > new gap, swap enough r2wl votes to become lWR3 and rerun election to compare
		if(topFourVotes.get(r2wlPos)>=(n2+1)){
		topFourVotes.set(r2wlPos,(topFourVotes.get(r2wlPos)-(n2+1)));
		topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
		char N = round(topFourPerms, topFourVotes, picks);
		if(N!=W){  
				//returns anomaly and some data
				System.out.println("Monotonicity Anomaly!\n");
				System.out.println("Swapped votes from r3w, R3Wr2 and r2wl and now " + N + " is the winner\n");
				//System.exit(0);
		} else {
			System.out.println("No monotonicity anomaly with three swaps: r3w, R3Wr2 and r2wl.\n");
			//System.exit(0);
		}
		} 	//if #r2wl is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2wlPos)<(n2+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2wlPos)));
		n2 = n2-topFourVotes.get(r2wlPos);
		topFourVotes.set(r2wlPos,0);


		//Now look at swapping r2
			//if #r2 > new gap, swap enough r2 votes to become Wr2 and rerun election to compare
		if(topFourVotes.get(r2Posi)>=(n2+1)){
			topFourVotes.set(r2Posi,(topFourVotes.get(r2Posi)-(n2+1)));
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r2w, r2Wr3, r2wl, and r2 and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with four swaps: r2w, r2Wr3, r2wl and r2.\n");
				//System.exit(0);
			}
		} 	//if #r2 is not enough to fill gap, add to wr2, reset gap, and zero out r2 votes
		else if(topFourVotes.get(r2Posi)<(n2+1)){
		topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+topFourVotes.get(r2Posi)));
		n2 = n2-topFourVotes.get(r2Posi);
		topFourVotes.set(r2Posi,0);


		//Now look at swapping r2lw
			//if #r2lw > new gap, swap enough r2lw votes to become wr2l and rerun election to compare
		if(topFourVotes.get(r2lwPos)>=(n2+1)){
			topFourVotes.set(r2lwPos,(topFourVotes.get(r2lwPos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w and r2lw and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3, r3r2w and r2lw.\n");
				//System.exit(0);
			}
		} 	//if #r2lw is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2lwPos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2lwPos)));
			n2 = n2-topFourVotes.get(r2lwPos);
			topFourVotes.set(r2lwPos,0);


			//Now look at swapping r2r3w
			//if #r2r3w > new gap, swap enough R3Wr2 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2r3wPos)>=(n2+1)){
		topFourVotes.set(r2r3wPos,(topFourVotes.get(r2r3wPos)-(n2+1)));
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
		char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3 and r2r3w and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3 and r2r3w.\n");
				//System.exit(0);
			}
		} 	//if #r2r3w is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r2r3wPos)<(n2+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2r3wPos)));
		n2 = n2-topFourVotes.get(r2r3wPos);
		topFourVotes.set(r2r3wPos,0);


			//Now look at swapping r2l
			//if #r2l > new gap, swap enough r2l votes to become Wr2l and rerun election to compare
		if(topFourVotes.get(r2lPos)>=(n2+1)){
			topFourVotes.set(r2lPos,(topFourVotes.get(r2lPos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r2lw, r3r2w and r2l and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r2lw, r3r2 and r2l.\n");
				//System.exit(0);
			}
		} 	//if #r2l is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2lPos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2lPos)));
			n2 = n2-topFourVotes.get(r2lPos);
			topFourVotes.set(r2lPos,0);


		//Now look at swapping r2r3
			//if #r2r3 > new gap, swap enough r2r3 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2r3Pos)>=(n2+1)){
			topFourVotes.set(r2r3Pos,(topFourVotes.get(r2r3Pos)-(n2+1)));
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r2r3w, r3lw and r2r3 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r2r3w, r3lw and r2r3.\n");
				//System.exit(0);
			}
		} 	//if #r2r3 is not enough to fill gap, add to wr2r3 and reset gap
		else if(topFourVotes.get(r2r3Pos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2r3Pos)));
			n2 = n2-topFourVotes.get(r2r3Pos);
			topFourVotes.set(r2r3Pos,0);


		//Now look at swapping r2lr3
			//if #r2lr3 > new gap, swap enough r2lr3 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2lr3Pos)>=(n2+1)){
			topFourVotes.set(r2lr3Pos,(topFourVotes.get(r2lr3Pos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r2r3w, r3lw, r2r3w, r3l and r2lr3 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 9 swaps: r3w, R3Wr2, r3wl, r3, r2r3w, r3lw, r2r3, r3l and r2lr3.\n");
			}
		} 	//if #r2lr3 is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2lr3Pos)<(n2+1)){
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2lr3Pos)));
			n2 = n2-topFourVotes.get(r2lr3Pos);
			topFourVotes.set(r2lr3Pos,0);


		//Now look at swapping r2r3l
			//if #r2r3l > new gap, swap enough r2r3l votes to become Wr3l and rerun election to compare
			if(topFourVotes.get(r2r3lPos)>=(n2+1)){
				topFourVotes.set(r2r3lPos,(topFourVotes.get(r2r3lPos)-(n2+1)));
				topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N!=W){  
						//returns anomaly and some data
						System.out.println("Monotonicity Anomaly!\n");
						System.out.println("Swapped votes from r2w, R2Wr3, r2wl, r2, r2r3w, r2lw, r2r3w, r2l, r2r3l and r2lr3 and now " + N + " is the winner\n");
						//System.exit(0);
				} else {
					System.out.println("No monotonicity anomaly with 10 swaps of leading r2.\n");
				}
			} 	
			//if #r2r3l is not enough to fill gap, return no monotonicity anomaly.
			else{ 
				System.out.println("No monotonicity anomaly.  Not enough 2nd-place votes to make up gap and rerun election.\n");
			} 
			} 
		} 
		} 
		} 
		} 
		}
		}
	} // ends 2nd else if
	} //ends 1st else if

	topFourVotes.clear();
	topFourVotes.addAll(top4originalNumbers);



//THIS IS WHERE THE PROGRAM CHECKS TO SEE IF CAN SWAP 2ND below last and get last to beat winner in head to head

		n2 = votesPer[r2Pos]-votesPer[minPos];

		//check for swapping r2W
		if(topFourVotes.get(r2wPos)>=(n2+1)){ //if happen to have more r2W votes than the gap
		
			topFourVotes.set(r2wPos,(topFourVotes.get(r2wPos)-(n2+1)));
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+(n2+1)));
			
		 	char N = round(topFourPerms, topFourVotes, picks);
		 	if(N!=W){        //if the new winner is not the original winner, returns anomaly and some data
		 		System.out.println("Monotonicity Anomaly!\n");
		 		System.out.println("Swapped "+ n2 +" + 1 votes from 2nd-place to Winner, and now " + N + " is the winner");
		 		//System.exit(0);
		 	} else {
		 		System.out.println("No monotonicity anomaly with one swap r2w to wr2.\n"); 
		 		//System.exit(0);
		 	}
		} else if(topFourVotes.get(r2wPos)<(n2+1)) {   //now if  topFourVotes[r2wPos]<(n+1), change all r2W to Wr2
			//r2wNum=topFourVotes.get(r2wPos); 
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+topFourVotes.get(r2wPos)));
			n2 = n2-topFourVotes.get(r2wPos); //reset the gap now that 2nd place lost some votes
			topFourVotes.set(r2wPos,0);
		

		//Now look at swapping r2wr3
		//if #r2wr3 > new gap, swap enough r2wr3 votes to become wr2r3:  Note m=rPos
		if(topFourVotes.get(r2wr3Pos)>=(n2+1)){
			topFourVotes.set(r2wr3Pos,(topFourVotes.get(r2wr3Pos)-(n2+1)));
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
				//totalR=n2+1-r3wNum;            //returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w to Winner as well as votes r2wr3 and now " + N + " is the winner\n");
				//System.exit(0);
			} else {
			System.out.println("No monotonicity anomaly with two swaps, wrl and r to r3w.\n");
			//System.exit(0);
			}
		} 	//if #r2wr3 is not enough to fill gap, add to wr2r3 and reset gap
		else if(topFourVotes.get(r2wr3Pos)<(n2+1)){
		//rNum=topFourVotes.get(r2wr3Pos);
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2wr3Pos)));
		n2 = n2-topFourVotes.get(r2wr3Pos);
		topFourVotes.set(r2wr3Pos,0);


		//Now look at swapping r2wl
		//if #r2wl > new gap, swap enough r2wl votes to become lWR3 and rerun election to compare
		if(topFourVotes.get(r2wlPos)>=(n2+1)){
		topFourVotes.set(r2wlPos,(topFourVotes.get(r2wlPos)-(n2+1)));
		topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
		char N = round(topFourPerms, topFourVotes, picks);
		if(N!=W){  
				//returns anomaly and some data
				System.out.println("Monotonicity Anomaly!\n");
				System.out.println("Swapped votes from r3w, R3Wr2 and r2wl and now " + N + " is the winner\n");
				//System.exit(0);
		} else {
			System.out.println("No monotonicity anomaly with three swaps: r3w, R3Wr2 and r2wl.\n");
			//System.exit(0);
		}
		} 	//if #r2wl is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2wlPos)<(n2+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2wlPos)));
		n2 = n2-topFourVotes.get(r2wlPos);
		topFourVotes.set(r2wlPos,0);


		//Now look at swapping r2
			//if #r2 > new gap, swap enough r2 votes to become Wr2 and rerun election to compare
		if(topFourVotes.get(r2Posi)>=(n2+1)){
			topFourVotes.set(r2Posi,(topFourVotes.get(r2Posi)-(n2+1)));
			topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r2w, r2Wr3, r2wl, and r2 and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with four swaps: r2w, r2Wr3, r2wl and r2.\n");
				//System.exit(0);
			}
		} 	//if #r2 is not enough to fill gap, add to wr2, reset gap, and zero out r2 votes
		else if(topFourVotes.get(r2Posi)<(n2+1)){
		topFourVotes.set(wr2Pos,(topFourVotes.get(wr2Pos)+topFourVotes.get(r2Posi)));
		n2 = n2-topFourVotes.get(r2Posi);
		topFourVotes.set(r2Posi,0);


			//Now look at swapping r2r3w
			//if #r2r3w > new gap, swap enough R3Wr2 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2r3wPos)>=(n2+1)){
		topFourVotes.set(r2r3wPos,(topFourVotes.get(r2r3wPos)-(n2+1)));
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
		char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
	 			//returns anomaly and some data
	 			System.out.println("Monotonicity Anomaly!\n");
	 			System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3 and r2r3w and now " + N + " is the winner\n");
	 			//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3 and r2r3w.\n");
				//System.exit(0);
			}
		} 	//if #r2r3w is not enough to fill gap, add to wr3l and reset gap
		else if(topFourVotes.get(r2r3wPos)<(n2+1)){
		//rNum=topFourVotes.get(r3wr2Pos);
		topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2r3wPos)));
		n2 = n2-topFourVotes.get(r2r3wPos);
		topFourVotes.set(r2r3wPos,0);


			//Now look at swapping r2lw
			//if #r2lw > new gap, swap enough r2lw votes to become wr2l and rerun election to compare
		if(topFourVotes.get(r2lwPos)>=(n2+1)){
			topFourVotes.set(r2lwPos,(topFourVotes.get(r2lwPos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w and r2lw and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 5 swaps: r3w, R3Wr2, r3wl, r3, r3r2w and r2lw.\n");
				//System.exit(0);
			}
		} 	//if #r2lw is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2lwPos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2lwPos)));
			n2 = n2-topFourVotes.get(r2lwPos);
			topFourVotes.set(r2lwPos,0);

		
		//Now look at swapping r2r3
			//if #r2r3 > new gap, swap enough r2r3 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2r3Pos)>=(n2+1)){
			topFourVotes.set(r2r3Pos,(topFourVotes.get(r2r3Pos)-(n2+1)));
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r2r3w, r3lw and r2r3 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r2r3w, r3lw and r2r3.\n");
				//System.exit(0);
			}
		} 	//if #r2r3 is not enough to fill gap, add to wr2r3 and reset gap
		else if(topFourVotes.get(r2r3Pos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2r3Pos)));
			n2 = n2-topFourVotes.get(r2r3Pos);
			topFourVotes.set(r2r3Pos,0);


		//Now look at swapping r2l
			//if #r2l > new gap, swap enough r2l votes to become Wr2l and rerun election to compare
		if(topFourVotes.get(r2lPos)>=(n2+1)){
			topFourVotes.set(r2lPos,(topFourVotes.get(r2lPos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r3r2w, r2lw, r3r2w and r2l and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 7 swaps: r3w, R3Wr2, r3wl, r3, r3r2w, r2lw, r3r2 and r2l.\n");
				//System.exit(0);
			}
		} 	//if #r2l is not enough to fill gap, add to wr2l and reset gap
		else if(topFourVotes.get(r2lPos)<(n2+1)){
			//rNum=topFourVotes.get(r3wr2Pos);
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+topFourVotes.get(r2lPos)));
			n2 = n2-topFourVotes.get(r2lPos);
			topFourVotes.set(r2lPos,0);

		
		
		//Now look at swapping r2r3l
			//if #r2r3l > new gap, swap enough r2r3l votes to become Wr3l and rerun election to compare
		if(topFourVotes.get(r2r3lPos)>=(n2+1)){
			topFourVotes.set(r2r3lPos,(topFourVotes.get(r2r3lPos)-(n2+1)));
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r2w, R2Wr3, r2wl, r2, r2r3w, r2lw, r2r3w, r2l, r2r3l and r2lr3 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 10 swaps of leading r2.\n");
			}
		} 	//if #r2r3L is not enough to fill gap, add to wr2R3 and reset gap
		else if(topFourVotes.get(r2r3lPos)<(n2+1)){
			topFourVotes.set(wr2r3Pos,(topFourVotes.get(wr2r3Pos)+topFourVotes.get(r2r3lPos)));
			n2 = n2-topFourVotes.get(r2r3lPos);
			topFourVotes.set(r2r3lPos,0);


		//Now look at swapping r2lr3
			//if #r2lr3 > new gap, swap enough r2lr3 votes to become Wr2r3 and rerun election to compare
		if(topFourVotes.get(r2lr3Pos)>=(n2+1)){
			topFourVotes.set(r2lr3Pos,(topFourVotes.get(r2lr3Pos)-(n2+1)));
			topFourVotes.set(wr2lPos,(topFourVotes.get(wr2lPos)+(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N!=W){  
					//returns anomaly and some data
					System.out.println("Monotonicity Anomaly!\n");
					System.out.println("Swapped votes from r3w, R3Wr2, r3wl, r3, r2r3w, r3lw, r2r3w, r3l and r2lr3 and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No monotonicity anomaly with 9 swaps: r3w, R3Wr2, r3wl, r3, r2r3w, r3lw, r2r3, r3l and r2lr3.\n");
			}
		} 	//if #r2Lr3 is not enough to fill gap, return no monotonicity anomaly.
			else{ 
				System.out.println("No monotonicity anomaly.  Not enough 2nd-place votes to make up gap and rerun election.\n");
			} 
			} 
		} 
		} 
		} 
		} 
		}
		}
	} // ends 2nd else if
	} //ends 1st else if








	} //ends Top Four Gap sort


/* TOP FOUR No Show CHECK.  (Adam)
	Pre: takes in top four voting data, winner, ranking array, and number of picks
	Post: Outputs whether or not there is a No Show violation, and how it happens.

	Finds gap between third and fourth place candidates, and drops votes in order to close the gap to make third-place drop out before fourth, then checks to see if that causes a monotonicity violation.  Repeats the process but forces 2nd-place to drop before fourth.
	*/
	
	static void topFourNoShow(ArrayList<String> topFourPerms, ArrayList<Integer> topFourVotes, char winner, char[] rankArray, int picks){
	//first 200 lines of code are basically same as top 4 gap sort.  need to check if I can include all permutations at first or need to dole them out.
	//Then do R3 under L, and both R2 under L options (R3 wins, R2 wins)
		int votesPer[] = new int[picks];
		if(topFourPerms.size() > 40){System.out.println("Potential error, more than 40 items in array\n");
		}
		for(int i = 0;i<picks;i++){
			votesPer[i] = 0; 
		}
		for(int count = 0;count< topFourPerms.size();count++){
			String temp = topFourPerms.get(count); //declares permutation as temp
			int person = (int)((char)(temp.charAt(0))- 'A'); //gets int for which person is first in temp
			votesPer[person]+=topFourVotes.get(count);	//adds up votes per person
			//at this point, votesPer[0] is the number of votes for candidate A, etc.
		}
		// System.out.println("System check.\n order array is");
		// for(int i = 0;i<picks;i++){
		// 	System.out.println(""+rankArray[i]); 
		// }
		
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
		/*Now have data that minPos=4th place candidate, min=number of votes for that candidate=votesPer[minPos], ditto for maxPos */ 
		//Then need to find numbers for 2nd, 3rd place candidates.  Do

		int winnerPos = (int)((char)(winner - 'A'));
		if(winnerPos != (int)((char)(rankArray[0] - 'A'))){
			System.out.println("Potential error, winner does not match up\n");
		}
		if(minPos != (int)((char)(rankArray[3] - 'A'))){
			System.out.println("Potential error, loser does not match up\n");
		}
		int r2Pos = (int)((char)(rankArray[1] - 'A')); //number for second place
		int r3Pos = (int)((char)(rankArray[2] - 'A')); //number for third place
		
		//System.out.println("2nd position is "+r2Pos+" \n"); 
		//System.out.println("3rd position is "+r3Pos+" \n");
		//System.out.println("Winner position is "+winnerPos+" \n");
		/*At this point, winnerPos is the number for winner, minPos=number for loser  */
		//Now find gap between fourth place and third

		int n3 = 0;
		n3 = votesPer[r3Pos]-votesPer[minPos];
		//System.out.println("Gap between 3rd and last is "+n3+" \n");
		if(n3<0){
			System.out.println("Error. Gap between third and fourth is calculating wrong.\n");
		}/* output error if n<=0 */

		int n2 = 0;
		n2 = votesPer[r2Pos]-votesPer[minPos];
		//System.out.println("Gap between 2nd and last is "+n2+" \n");
		if(n3<0){
			System.out.println("Error. Gap between second and fourth is calculating wrong.\n");
		}/* output error if n<=0 */



		/*(Note: winnerPos is eventual winner (numerical), minPos is 4th place and has lowest votes, 2ndPos and 3rdPos are other candidates, could be in any place, hard to say)*/

		//convert winnerPos etc back to letters, call them R=other, W=winner

		char R2 = (char)(r2Pos + 'A');
		char R3 = (char)(r3Pos + 'A');  //Letter for 3rd place
		char W = (char)(winnerPos + 'A'); //this should be same as winner
		char L = (char)(minPos + 'A');
		System.out.println("Third is "+R3+", Second is "+R2+ ", Winner is "+W+", Loser is "+L+" \n");
		if(W==winner){
		} else{
			System.out.println("Error in calculation of winning candidate\n");
		}
		//copy original numbers since top4 votes gets changed
		ArrayList<String> top4originalPerms=  new ArrayList<String>(topFourPerms);
		ArrayList<Integer> top4originalNumbers=  new ArrayList<Integer>(topFourVotes);

		//This part of the code is for checking all swaps that move the 3rd-place candidate below the 4-th place candidate.

		//Define permutations and find their position in the TopFourPerms array.
		//make strings to match perm data:  First find array index of R3LW 
		
		String r3lw= "" + R3 + L + W ; // define and find array index of r3lw (1st)
		int r3lwPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3lw)){
   				r3lwPos = j;
			} 
		}
		String r2r3= "" + R2 + R3 ; // define and find array index of r3r2
		int r2r3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3)){
					r2r3Pos = j;
			} 
		}
		String r3l= "" + R3 + L ; // define and find array index of r3l (1st)
		int r3lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3l)){
					r3lPos = j;
			} 
		}
		String r3r2l= "" + R3 +R2 + L ; // define and find array index of r3r2l (1st)
		int r3r2lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2l)){
					r3r2lPos = j;
			} 
		}
		String r3r2= "" + R3 +R2  ; // define and find array index of r3r2 (1st)
		int r3r2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2)){
					r3r2Pos = j;
			} 
		}
		String r3r2w= "" + R3 +R2 + W ; // define and find array index of r3r2w (1st)
		int r3r2wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3r2w)){
					r3r2wPos = j;
			} 
		}
		String r3lr2= "" + R3 + L + R2 ; // define and find array index of r3lr2 (1st)
		int r3lr2Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r3lr2)){
					r3lr2Pos = j;
			} 
		}
		String r2l= "" + R2 + L ; // define and find array index of r2l
		int r2lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2l)){
					r2lPos = j;
			} 
		}
		String r2r3l= "" + R2 +R3 + L ; // define and find array index of r2r3l
		int r2r3lPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3l)){
					r2r3lPos = j;
			} 
		}
		String r2lr3= "" + R2 + L + R3 ; // define and find array index of r2lr3
		int r2lr3Pos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2lr3)){
					r2lr3Pos = j;
			} 
		}
		String r2r3w= "" + R2 + R3 + W ; // define and find array index of r2r3w
		int r2r3wPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2r3w)){
					r2r3wPos = j;
			} 
		}
		String r2lw= "" + R2 + L + W ; // define and find array index of r2lw
		int r2lwPos=-1;
		for(int j = 0;j<topFourPerms.size();j++){
			if (topFourPerms.get(j).equals(r2lw)){
					r2lwPos = j;
			} 
		}

		//Now check to see if making 3rd place leave before L can make a No Show anomaly with L winning
		if(topFourVotes.get(r3r2lPos)>=(n3+1)){
			topFourVotes.set(r3r2lPos,(topFourVotes.get(r3r2lPos)-(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N==L){  
					//returns anomaly and some data
					System.out.println("No Show Anomaly?\n");
					System.out.println("Removed " + n3 +" plus one r3r2l votes and now " + N + " is the winner.  Need " + L + " to be the winner.\n");
					//System.exit(0);
			} else {
				System.out.println("No no-show anomaly with removal of 1 leading r3.\n");
			}
		} 	//if #r3r2l is not enough to fill gap, remove and reset gap
		else if(topFourVotes.get(r3r2lPos)<(n3+1)){
			n3 = n3-topFourVotes.get(r3r2lPos);
			topFourVotes.set(r3r2lPos,0);
		//Now check r3l
			if(topFourVotes.get(r3lPos)>=(n3+1)){
				topFourVotes.set(r3lPos,(topFourVotes.get(r3lPos)-(n3+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N==L){  
						//returns anomaly and some data
						System.out.println("No Show Anomaly?\n");
						System.out.println("Removed " + n3 +" plus one r3r2l and r3l votes and now " + L + " is the winner.  Need " + L + " to be the winner.\n");
						//System.exit(0);
				} else {
					System.out.println("No no-show anomaly with removal of 2 leading r3.\n");
				}
			} 	//if #r3l is not enough to fill gap, remove and reset gap
			else if(topFourVotes.get(r3lPos)<(n3+1)){
				n3 = n3-topFourVotes.get(r3lPos);
				topFourVotes.set(r3lPos,0);
			//Now check r3lr2
				if(topFourVotes.get(r3lr2Pos)>=(n3+1)){
					topFourVotes.set(r3lr2Pos,(topFourVotes.get(r3lr2Pos)-(n3+1)));
					char N = round(topFourPerms, topFourVotes, picks);
					if(N==L){  
							//returns anomaly and some data
							System.out.println("No Show Anomaly?\n");
							System.out.println("Removed " + n3 +" plus one r3r2l, r3l and r3Lr2 votes and now " + N + " is the winner.  Need " + L + " to be the winner.\n");
							//System.exit(0);
					} else {
						System.out.println("No no-show anomaly with removal of 3 leading r3.\n");
					}
				} 	//if #r3lr2 is not enough to fill gap, remove and reset gap
				else if(topFourVotes.get(r3lr2Pos)<(n3+1)){
					n3 = n3-topFourVotes.get(r3lr2Pos);
					topFourVotes.set(r3lr2Pos,0);
				//Now check r3lw
					if(topFourVotes.get(r3lwPos)>=(n3+1)){
						topFourVotes.set(r3lwPos,(topFourVotes.get(r3lwPos)-(n3+1)));
						char N = round(topFourPerms, topFourVotes, picks);
						if(N==L){  
								//returns anomaly and some data
								System.out.println("Potential No Show Anomaly!\n");
								System.out.println("Removed " + n3 +" plus one r3r2l, r3l, r3lr2 and r3Lw votes and now " + N + " is the winner.  Need " + L + " to be the winner.\n");
								//System.exit(0);
						} else {
							System.out.println("No no-show anomaly with removal of 4 leading r3.\n");
						}
					} 	//if #r3lr2 is not enough to fill gap, remove and reset gap
					else if(topFourVotes.get(r3lwPos)<(n3+1)){
						System.out.println("No no-show anomaly with removal of 4 leading r3, because not enough votes to make up the gap.\n");

					} //ends r3lw else if
				} //ends r3lr2 else if
			} //ends r3l else if
		} //ends r3r2l else if

		topFourVotes.clear();
		topFourVotes.addAll(top4originalNumbers);
		topFourPerms.clear();
		topFourPerms.addAll(top4originalPerms);
		n3 = votesPer[r3Pos]-votesPer[minPos];
		

		//Now check to see if making 3rd place leave before L can make a No Show anomaly with R2 winning
		if(topFourVotes.get(r3lr2Pos)>=(n3+1)){
			topFourVotes.set(r3lr2Pos,(topFourVotes.get(r3lr2Pos)-(n3+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N==R2){  
					//returns anomaly and some data
					System.out.println("No Show Anomaly?\n");
					System.out.println("Removed " + n3 +" plus one r3lr2 votes and now " + N + " is the winner.  Need " + R2 + " to be the winner.\n");
					//System.exit(0);
			} else {
				System.out.println("No no-show anomaly with removal of 1 leading r3.\n");
			}
		} 	//if #r3lr2 is not enough to fill gap, remove and reset gap
		else if(topFourVotes.get(r3lr2Pos)<(n3+1)){
			n3 = n3-topFourVotes.get(r3lr2Pos);
			topFourVotes.set(r3lr2Pos,0);
		//Now check r3r2
			if(topFourVotes.get(r3r2Pos)>=(n3+1)){
				topFourVotes.set(r3r2Pos,(topFourVotes.get(r3r2Pos)-(n3+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N==R2){  
						//returns anomaly and some data
						System.out.println("No Show Anomaly?\n");
						System.out.println("Removed " + n3 +" plus one r3lr2 and r3r2 votes and now " + N + " is the winner.  Need " + R2 + " to be the winner.\n");
						//System.exit(0);
				} else {
					System.out.println("No no-show anomaly with removal of 2 leading r3.\n");
				}
			} 	//if #r3r2 is not enough to fill gap, remove and reset gap
			else if(topFourVotes.get(r3r2Pos)<(n3+1)){
				n3 = n3-topFourVotes.get(r3r2Pos);
				topFourVotes.set(r3r2Pos,0);
			//Now check r3r2l
				if(topFourVotes.get(r3r2lPos)>=(n3+1)){
					topFourVotes.set(r3r2lPos,(topFourVotes.get(r3r2lPos)-(n3+1)));
					char N = round(topFourPerms, topFourVotes, picks);
					if(N==R2){  
							//returns anomaly and some data
							System.out.println("No Show Anomaly?\n");
							System.out.println("Removed " + n3 +" plus one r3lr2, r3r2 and r3r2L votes and now " + N + " is the winner.  Need " + R2 + " to be the winner.\n");
							//System.exit(0);
					} else {
						System.out.println("No no-show anomaly with removal of 3 leading r3.\n");
					}
				} 	//if #r3r2l is not enough to fill gap, remove and reset gap
				else if(topFourVotes.get(r3r2lPos)<(n3+1)){
					n3 = n3-topFourVotes.get(r3r2lPos);
					topFourVotes.set(r3r2lPos,0);
				//Now check r3r2w
					if(topFourVotes.get(r3r2wPos)>=(n3+1)){
						topFourVotes.set(r3r2wPos,(topFourVotes.get(r3r2wPos)-(n3+1)));
						char N = round(topFourPerms, topFourVotes, picks);
						if(N==R2){  
								//returns anomaly and some data
								System.out.println("Potential No Show Anomaly!\n");
								System.out.println("Removed " + n3 +" plus one r3lr2, r3r2, r3r2l and r3r2w votes and now " + N + " is the winner.  Need " + R2 + " to be the winner.\n");
								//System.exit(0);
						} else {
							System.out.println("No no-show anomaly with removal of 4 leading r3.\n");
						}
					} 	//if #r3r2w is not enough to fill gap, game over
					else if(topFourVotes.get(r3r2wPos)<(n3+1)){
						System.out.println("No no-show anomaly with removal of 4 leading r3, because not enough votes to make up the gap.\n");

					} //ends r3r2w else if
				} //ends r3r2l else if
			} //ends r3r2l else if
		} //ends r3r2w else if

		topFourVotes.clear();
		topFourVotes.addAll(top4originalNumbers);

		//Now check to see if making 2nd place leave before L, and get R3 to win can make a No Show anomaly

		if(topFourVotes.get(r2lr3Pos)>=(n2+1)){
			topFourVotes.set(r2lr3Pos,(topFourVotes.get(r2lr3Pos)-(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N==R3){  
					//returns anomaly and some data
					System.out.println("No Show Anomaly!\n");
					System.out.println("Removed " + n2 +" plus one r2lr3 votes and now " + N + " is the winner.  Need " + R3 + " to be the winner.\n");
					//System.exit(0);
			} else {
				System.out.println("No no-show anomaly with removal of 1 leading r2.\n");
			}
		} 	//if #r2lr3 is not enough to fill gap, remove and reset gap
		else if(topFourVotes.get(r2lr3Pos)<(n2+1)){
			n2 = n2-topFourVotes.get(r2lr3Pos);
			topFourVotes.set(r2lr3Pos,0);
		//Now check r2r3
			if(topFourVotes.get(r2r3Pos)>=(n2+1)){
				topFourVotes.set(r2r3Pos,(topFourVotes.get(r2r3Pos)-(n2+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N==R3){  
						//returns anomaly and some data
						System.out.println("No Show Anomaly!\n");
						System.out.println("Removed " + n2 +" plus one r2lr3 and r2r3 votes and now " + N + " is the winner.  Need " + R3 + " to be the winner.\n");
						//System.exit(0);
				} else {
					System.out.println("No no-show anomaly with removal of 2 leading r2.\n");
				}
			} 	//if #r2r3 is not enough to fill gap, remove and reset gap
			else if(topFourVotes.get(r2r3Pos)<(n2+1)){
				n2 = n2-topFourVotes.get(r2r3Pos);
				topFourVotes.set(r2r3Pos,0);

			//Now check r2r3l
				if(topFourVotes.get(r2r3lPos)>=(n2+1)){
					topFourVotes.set(r2r3lPos,(topFourVotes.get(r2r3lPos)-(n2+1)));
					char N = round(topFourPerms, topFourVotes, picks);
					if(N==R3){  
							//returns anomaly and some data
							System.out.println("No Show Anomaly!\n");
							System.out.println("Removed " + n2 +" plus one r2lr3, r2r3 and r2r3l votes and now " + N + " is the winner.  Need " + R3 + " to be the winner.\n");
							//System.exit(0);
					} else {
						System.out.println("No no-show anomaly with removal of 3 leading r2.\n");
					}
				} 	//if #r2r3l is not enough to fill gap, remove and reset gap
				else if(topFourVotes.get(r2r3lPos)<(n2+1)){
					n2 = n2-topFourVotes.get(r2r3lPos);
					topFourVotes.set(r2r3lPos,0);

				//Now check r2r3w
					if(topFourVotes.get(r2r3wPos)>=(n2+1)){
						topFourVotes.set(r2r3wPos,(topFourVotes.get(r2r3wPos)-(n2+1)));
						char N = round(topFourPerms, topFourVotes, picks);
						if(N==R3){  
								//returns anomaly and some data
								System.out.println("No Show Anomaly!\n");
								System.out.println("Removed " + n2 +" plus one r2lr3, r2r3, r2r3l and r2r3w votes and now " + N + " is the winner\n");
								//System.exit(0);
						} else {
							System.out.println("No no-show anomaly with removal of 4 leading r2.\n");
						}
					} 	//if #r2r3w is not enough to fill gap, remove and reset gap
					else if(topFourVotes.get(r2r3wPos)<(n2+1)){
						System.out.println("No no-show anomaly with removal of 4 leading r2, because not enough votes to make up the gap.\n");

					} //ends r2r3w else if
				} //ends r2r3l else if
			} //ends r2r3 else if
		} //ends r2r3w else if

		topFourVotes.clear();
		topFourVotes.addAll(top4originalNumbers);
		n2 = votesPer[r2Pos]-votesPer[minPos];

		//Now check to see if making 2nd place leave before L, and get L to win can make a No Show anomaly
		if(topFourVotes.get(r2r3lPos)>=(n2+1)){
			topFourVotes.set(r2r3lPos,(topFourVotes.get(r2r3lPos)-(n2+1)));
			char N = round(topFourPerms, topFourVotes, picks);
			if(N==L){  
					//returns anomaly and some data
					System.out.println("No Show Anomaly!\n");
					System.out.println("Removed " + n2 +" plus one r2r3l votes and now " + N + " is the winner\n");
					//System.exit(0);
			} else {
				System.out.println("No no-show anomaly with removal of 1 leading r2.\n");
			}
		} 	//if #r2lr3 is not enough to fill gap, remove and reset gap
		else if(topFourVotes.get(r2r3lPos)<(n2+1)){
			n2 = n2-topFourVotes.get(r2r3lPos);
			topFourVotes.set(r2r3lPos,0);
		//Now check r2l
			if(topFourVotes.get(r2lPos)>=(n2+1)){
				topFourVotes.set(r2lPos,(topFourVotes.get(r2lPos)-(n2+1)));
				char N = round(topFourPerms, topFourVotes, picks);
				if(N==L){  
						//returns anomaly and some data
						System.out.println("No Show Anomaly!\n");
						System.out.println("Removed " + n2 +" plus one r2r3l and r2l votes and now " + N + " is the winner\n");
						//System.exit(0);
				} else {
					System.out.println("No no-show anomaly with removal of 2 leading r2.\n");
				}
			} 	//if #r2l is not enough to fill gap, remove and reset gap
			else if(topFourVotes.get(r2lPos)<(n2+1)){
				n2 = n2-topFourVotes.get(r2lPos);
				topFourVotes.set(r2lPos,0);

			//Now check r2lr3
				if(topFourVotes.get(r2lr3Pos)>=(n2+1)){
					topFourVotes.set(r2lr3Pos,(topFourVotes.get(r2lr3Pos)-(n2+1)));
					char N = round(topFourPerms, topFourVotes, picks);
					if(N==L){  
							//returns anomaly and some data
							System.out.println("No Show Anomaly!\n");
							System.out.println("Removed " + n2 +" plus one r2r3l, r2l and r2lr3 votes and now " + N + " is the winner\n");
							//System.exit(0);
					} else {
						System.out.println("No no-show anomaly with removal of 3 leading r2.\n");
					}
				} 	//if #r2lr3 is not enough to fill gap, remove and reset gap
				else if(topFourVotes.get(r2lr3Pos)<(n2+1)){
					n2 = n2-topFourVotes.get(r2lr3Pos);
					topFourVotes.set(r2lr3Pos,0);

				//Now check r2lw
					if(topFourVotes.get(r2lwPos)>=(n2+1)){
						topFourVotes.set(r2lwPos,(topFourVotes.get(r2lwPos)-(n2+1)));
						char N = round(topFourPerms, topFourVotes, picks);
						if(N==L){  
								//returns anomaly and some data
								System.out.println("No Show Anomaly!\n");
								System.out.println("Removed " + n2 +" plus one r2r3l, r2l, r2lr3 and r2lw votes and now " + N + " is the winner\n");
								//System.exit(0);
						} else {
							System.out.println("No no-show anomaly with removal of 4 leading r2.\n");
						}
					} 	//if #r2lw is not enough to fill gap, remove and reset gap
					else if(topFourVotes.get(r2lwPos)<(n2+1)){
						System.out.println("No no-show anomaly with removal of 4 leading r2, because not enough votes to make up the gap.\n");

					} //ends r2lw else if
				} //ends r2lr3 else if
			} //ends r2l else if
		} //ends r2r3l else if




	}//ends top4NoShow

} //ends IRVtop4