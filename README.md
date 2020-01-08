# Instant-runoff
Programs analyzing real-world IRV elections

These are programs I have written, or were built off of student work, to analyze real-world Instant Runoff Elections.  More precisely, to investigate the frequency of voting anomalies in IRV elections. 

There are three programs.  The "Approval" program does calculations with Approval voting, the "adamBetter" program calculates many things for 3-candidate elections: monotonicity, Condorcet, No-show, and IAS/PI anomalies, as well as checking to see if the Borda count gives the same winner as the IRV method.  The "top4" program checks for anomalies at the level of 4 candidates.

Below I give an overview of how the major parts of certain programs work:
_______________________________________________________________

Overview of how monotonicity anomaly program works:

We will explain how the program decides which ballots to change, how many of those ballots, and when. We will start with how the program works at the three-candidate level. In this exposition, assume that the three candidates are A, B, and C, with those respective orders in terms of when they were eliminated in the voting process. So A wins the IRV election in a head-to-head with B in the last round, C drops out in the round of three candidates. The program begins by looking at the gap between the number of first-place votes between B and C, let that gap be n > 0. The goal of the program is to find n+1 ballots where B is in first place, change those ballots such that A moves above B, and then rerun the IRV election to see if C now wins the election. 

There are three such possible ballots at the three-candidate level: BAC, BCA, and just B. BAC ballots are checked first because changing BAC to ABC does not change the relative position of A and C in any ballots, thus increasing the likelihood that the swapped ballots would cause a monotonicity anomaly. Specifically, it checks first to see if there are n+1 or greater ballots of type BAC. If so, n+1 of those ballots are changed to ABC, and the program reruns the election to see if C wins. If so, the program reports a monotonicity anomaly, and if not it reports no monotonicity anomaly. In either case, the program ends at that point. If there are k < n+1 ballots of type BAC, all k ballots are changed to ABC, and the program then looks to see if there are n+1−k or greater ballots of type B. The process described above is then repeated: if n+1−k ballots are present, they are switched to ABC and we rerun the election. If p < n+1−k ballots are present, all are switched to ABC and the program looks at ballots of type BCA. Ballots of this type are the last to be checked because changing BCA to ABC changes the relative positions of A and C, making an anomaly less likely. If there are n+1−k−p or greater ballots of type BCA, then n+1−k−p are swapped to ABC and we rerun the election to see if C wins. If there are less than n+1−k−p ballots of type BCA, the program reports no monotonicity anomaly. 

At the four-candidate level, the program becomes more complicated, though the structure of it is the same. Suppose the candidates are A, B, C and D, with those respective orders; A is the winner of the IRV election, D drops out in the round of four candidates, and C drops out in the round of three. There are now four possible ways the program can change the drop-out order to create a monotonicity anomaly: C can drop below D, and B or D could be the eventual winner, or B can drop below D, and C or D could be the eventual winner. The program will proceed as described above, swapping ballots in a certain order to reach a given gap, but which ballots and in what order will change depending on the situation. In all situations, once the swapped ballots exceed the gap then the election is rerun to see if the winner changes and the program ends. If the swapped ballots never exceed the gap then no monotonicity anomaly is reported. The ordering for the ballot swaps are as follows: 
For the option where C drops below D and D is the eventual winner, the order in which the ballots are changed is 

CA > CAB > CAD > C > CBA > CDA > CB > CD > CBD > CDB, 

meaning that CA ballots are checked first, then CAB, etc, through to CDB. In each situation, the ballot is changed to raise A to the top of the ballot. So CA changes to AC, CDB changes to ACD, and so on. The ordering was chosen to impact the relative ordering of A and D as much as possible, since the goal is to have D be able to beat A in a pairwise contest. With similar reasoning, in the case where C drops below D and B is the eventual winner, the order in which the ballots are changed is 

CA > CAB > CAD > C > CDA > CBA > CD > CB > CDB > CBD 

The ordering for when B drops below D and D is the eventual winner is 

BA > BAC > BAD > B > BCA > BDA > BC > BD > BCD > BDC 

and for when B drops below D and C is the eventual winner is 

BA > BAC > BAD > B > BDA > BCA > BD > BC > BDC > BCD. 

____________________________________________________________________________

Overview of how other programs work:

No-show paradox: The program to find no-show anomalies is similar to the program for monotonicity anomalies. In particular, the No-show program looks for anomalies existing at each particular level of the election. The main difference is that instead of swapping ballots, for the no-show anomalies voters were simply removed from the ballot and then the election was re-run. We will describe in detail how the program runs at the three-candidate level, then generalize to illustrate how it would work at higher levels. 

Suppose the three candidates are A, B, and C, with those respective orders. The program finds the gap in first-place votes (n) between candidates B and C, then looks for ballots that could be removed so that B would be eliminated in the round of three candidates instead of C. In order to have a no-show anomaly, though, those removed voters must prefer C to A, and we must have C beat A in a head-to-head round (after those ballots have been removed). The only such ballots are of type BCA. So if the number of BCA votes exceeds n, the program removes n + 1 of those ballots and then reruns the election. If C wins there is a no-show anomaly at the three-candidate level, otherwise there is no anomaly. 

At the four-candidate level, the program proceeds as described above, but in a particular order similar to the method for the monotonicity program. Once again there are three options: (1) C can drop below D, and D could be the eventual winner, (2) B can drop below D, and D could be the eventual winner, or (3) B can drop below D, and C could be the eventual winner. The (respective) orders in which ballots are dropped to search for no-show anomalies are as follows: 

(1)	 CBD > CD > CDB > CDA (No other ballots have C in first place and clearly indicate a preference for D above A) 

(2)	BCD > BD > BDC > BDA 

(3)	BDC > BC > BCD > BCA 

Condorcet criterion: Suppose an election had n candidates. Each candidate was matched in a head-to-head contest with every other candidate. To match two candidates the program would simply eliminate the other n − 2 candidates from the race, then total first-place votes for the two remaining candidates. If a candidate wins a head-to-head matchup, that candidate receives one point. Points are tallied, and the program checks to see if any candidate received n − 1 points. If so, that candidate is declared the Condorcet winner and is compared to the IRV winner of the election. If the two are different, the election demonstrates a Condorcet anomaly. If there is no Condorcet winner or the Condorcet and IRV winner are the same, then the program reports no Condorcet anomaly. Every election analysed had a Condorcet winner.

Independence of the Alternative Set/Path Independence: The program would eliminate, from all the ballots, one candidate who was not the IRV winner, and then rerun the election. If the new winner was different from the IRV winner, the program would report an IAS/PI anomaly. If not, the program would reset all of the voting data, eliminate a different losing candidate, and repeat the procedure until either an anomaly was found or all losing candidates had been checked. Note that our program was only looking for anomalies related to a change in the winner of the election, as opposed to the ordering of the candidates (which would likely have changed in some of the elections). 

