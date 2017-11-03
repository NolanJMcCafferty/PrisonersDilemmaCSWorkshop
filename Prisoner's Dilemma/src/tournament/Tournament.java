package tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/** Prisoner's Dilemma Tournament
 *
 *hellllloooooo
 * This Tournament class takes the strategies
 * from the PrisonerDilemmaTechniques class and
 * simulates them in various tournament formats.
 *
 * May the best prisoner win!
 *
 * @author Connor Ford
 * @version 10/17
 *
 */

public class Tournament {

	//Number of rounds specifications
	private static final int NUM_ROUNDS_PER_CONTEST = 100;
	private static final int NUM_CONTESTS = 25;

	private PrisonerDilemmaTechniques strategyArr;
	private ArrayList<Strategy> strategies;

	public Tournament() {
		//Load the strategies
		strategyArr = new PrisonerDilemmaTechniques();
		strategies = strategyArr.strategies;
		//Print the strategy names at the top
		System.out.println("Strategies entered in the tournament: ");
		for (Strategy s : strategies) {
			System.out.println(s.toString().substring(11).split("@")[0]);
		}
		System.out.println("");

		// The elimination version of the tournament. At least one player
		// is eliminated after each round.
		runEliminationTournament(strategies, NUM_ROUNDS_PER_CONTEST, NUM_CONTESTS);
	}


	/**
	 * Method runs an elimination tournament knocking out at least
	 * one player per round. The tournament end when all remaining participants
	 * have the same score. Note, this means there can be more than
	 * one winner.
	 *
	 * @param players
	 * @param numTransactionsPerContest
	 * @param numContests
	 * @return void
	 */
	public static void runEliminationTournament(ArrayList<Strategy> players, int numTransactionsPerContest, int numContests) {
		Boolean keepRunningTournament = true;
		while (keepRunningTournament) {
			ArrayList<Player<Strategy,Integer>> scores = runTournament(players, numTransactionsPerContest, numContests);
			// Sorts increasing so lowest score on top
			// [makes removal O(1)]
			Collections.sort(scores, (s1,s2) -> {
				return s1.score.compareTo(s2.score);
			});
			// If all of the players have the same score,
			// then the game is over and they are the winners
			if (allTied(scores)) {
				keepRunningTournament = false;
			} else { // Otherwise, eliminate player(s) with lowest score
				int lowestScore = scores.get(0).score;

				// update player list to keep only players with
				// scores greater than the lowest score
				for (Player<Strategy,Integer> p : scores) {
					if (p.score == lowestScore) {
						players.remove(p.strategy);
					}
				}
			}
		}
		System.out.println("");
		System.out.println("Game Over!");
		System.out.println("Winners: ");
		System.out.println("");
		for (Strategy s : players) {
			//Parse this string to be only the name of the class/strategy
			System.out.println(s.toString().substring(11).split("@")[0]);
		}
	}

	/**
	 * This is the main method that runs the actual
	 * tournament for each round. Pairs all of the strategies
	 * against one another and keeps calculates the percentage
	 * of total points won for each strategy.
	 *
	 * @param players
	 * @param numTransactionsPerContest
	 * @param numContests
	 * @return ArrayList of each strategy and the respective score
	 */
	public static ArrayList<Player<Strategy,Integer>> runTournament(ArrayList<Strategy> players, int numTransactionsPerContest, int numContests) {
		if (players.size() == 1) {
			ArrayList<Player<Strategy,Integer>> ret = new ArrayList<Player<Strategy,Integer>>();
			ret.add(new Player<Strategy,Integer>(players.get(0), 0));
			return ret;
		}
		System.out.println("Running Tournament with " + players.size() + " Players");
		double bestScore = 5.0 * numTransactionsPerContest * numContests * (players.size() - 1);
		ArrayList<Player<Strategy,Integer>> scores = runRoundRobin(players, numTransactionsPerContest, numContests);
		Collections.sort(scores, (s1,s2) -> {
			return -s1.score.compareTo(s2.score);
		});
		for (Player<Strategy,Integer> p : scores) {
			double percent = (p.score / bestScore) * 100.0;
			System.out.println(percent + " percent for " + p.strategy.toString().substring(11).split("@")[0]);
		}
		return scores;
	}

	/**
	 * If all of the players have the same
	 * score return true. Otherwise return false.
	 *
	 * @param scores
	 * @return boolean value on if all the scores are equal
	 */
	public static boolean allTied(ArrayList<Player<Strategy,Integer>> scores) {
		int firstScore = scores.get(0).score;
		for (Player<Strategy,Integer> players : scores) {
			if (players.score != firstScore) {
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @param players
	 * @param numTransactionsPerContest
	 * @param numContests
	 * @return an ArrayList of all of the players/strategies with their respective score counts
	 */
	public static ArrayList<Player<Strategy,Integer>> runRoundRobin(ArrayList<Strategy> players, int numTransactionsPerContest, int numContests) {
		ArrayList<Player<Strategy,Integer>> scores = new ArrayList<Player<Strategy,Integer>> ();
		for (Strategy s : players) {
			scores.add(new Player<Strategy,Integer>(s, 0));
		}
		for (int contestNum = 0; contestNum < numContests; contestNum++) {
			for (int index1 = 0; index1 < players.size(); index1++) {
				for (int index2 = index1 + 1; index2 < players.size(); index2++) {
					ScorePair<Integer,Integer> scorePair = runContest(players.get(index1), players.get(index2), numTransactionsPerContest);
					scores.get(index1).score += scorePair.score1;
					scores.get(index2).score += scorePair.score2;
				}
			}
		}
		return scores;
	}

	/**
	 * Method that runs a contest between two strategies
	 * numTransactionsPerContest times. Keeps track of
	 * and updates the total score for each player.
	 *
	 * @param strategy1
	 * @param strategy2
	 * @param numTransactionsPerContest
	 * @return a score pair from the contest
	 */
	public static ScorePair<Integer,Integer> runContest(Strategy strategy1, Strategy strategy2, int numTransactionsPerContest) {
		ArrayList<Turn<String,String>> history1 = new ArrayList<Turn<String,String>>();
		ArrayList<Turn<String,String>> history2 = new ArrayList<Turn<String,String>>();
		int score1 = 0;
		int score2 = 0;
		for (int i = 0; i < numTransactionsPerContest; i++) {
			String choice1 = strategy1.takeOneTurn(history1);
			String choice2 = strategy2.takeOneTurn(history2);
			int payoff1 = payoff(choice1,choice2);
			int payoff2 = payoff(choice2,choice1);
			score1 += payoff1;
			score2 += payoff2;
			history1.add(new Turn<String,String>(choice1,choice2));
			history2.add(new Turn<String,String>(choice2,choice1));
		}
		return new ScorePair<Integer,Integer>(score1,score2);
	}


	/**
	 * Representation of the game board.
	 * Scores based on the four possible combinations
	 *
	 * @param choice1
	 * @param choice2
	 * @return number of points won
	 */
	public static int payoff(String choice1, String choice2) {
		if (choice1 == "c" && choice2 == "c") {
			return 3;
		}
		if (choice1 == "c" && choice2 == "d") {
			return 0;
		}
		if (choice1 == "d" && choice2 == "c") {
			return 5;
		}
		if (choice1 == "d" && choice2 == "d") {
			return 1;
		}
		else {
			throw new IllegalArgumentException("Your strategy must return a 'c' or a 'd' (note the lower case)");
		}
	}


	public static void main(String s[]) {
		new Tournament();
	}

}
