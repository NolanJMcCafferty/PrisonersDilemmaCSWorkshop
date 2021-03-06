package tournament;

import java.util.ArrayList;

/**
 * @author ConnorFord
 *
 * Very simple interface for the strategies. The Classes
 * must all implement the one method takeOneTurn(history)
 */

public interface Strategy {

	public String takeOneTurn(ArrayList<Turn<String,String>> history);
	
}
