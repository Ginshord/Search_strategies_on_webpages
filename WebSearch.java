import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.*;
/* He Jin: I use /.../as my COMMENTS  */
public class WebSearch
{
	static LinkedList<SearchNode> OPEN; // Feel free to choose your own data structures for searching,
	static HashSet<String> CLOSED;      // and be sure to read documentation about them.
	static LinkedList<SearchNode> OPENED;
	static Queue<SearchNode> queue ;
	static int nodesVisited = 0;
	static final boolean DEBUGGING = false; // When set, report what's happening.
	// WARNING: lots of info is printed.

	static int beamWidth = 2; // If searchStrategy = "beam",

	static final String START_NODE     = "page1.html";

	// A web page is a goal node if it includes
	// the following string.
	static final String GOAL_PATTERN   = "QUERY1 QUERY2 QUERY3 QUERY4";

	public static void main(String args[])
	{
		if (args.length != 2)
		{
			System.out.println("You must provide the directoryName and searchStrategyName.  Please try again.");
		}
		else
		{
			String directoryName = args[0]; // Read the search strategy to use.
			String searchStrategyName = args[1]; // Read the search strategy to use.

			if (searchStrategyName.equalsIgnoreCase("breadth") ||
					searchStrategyName.equalsIgnoreCase("depth")   ||
					searchStrategyName.equalsIgnoreCase("best")    ||
					searchStrategyName.equalsIgnoreCase("beam"))
			{
				performSearch(START_NODE, directoryName, searchStrategyName);
			}
			else
			{
				System.out.println("The valid search strategies are:");
				System.out.println("  BREADTH DEPTH BEST BEAM");
			}
		}

		Utilities.waitHere("Press ENTER to exit.");
	}

	static void performSearch(String startNode, String directoryName, String searchStrategy)
	{


		OPEN   = new LinkedList<SearchNode>();
		OPENED   = new LinkedList<SearchNode>();
		CLOSED = new HashSet<String>();
		queue = new PriorityQueue<SearchNode>();

		OPEN.add(new SearchNode(startNode));
		OPENED.add(new SearchNode(startNode));
		queue.add(new SearchNode(startNode));

		if (searchStrategy.equalsIgnoreCase("best")) {
			while (queue.peek() != null)
			{
					//System.out.println("-----------");
					SearchNode currentNode = queue.poll(); /*if it's the best-first search, then pop out the node with highest hValue*/
					String currentURL = currentNode.getNodeName();
					//System.out.println("select :"+currentURL);
					/* because greedy best-first only expand the node with the highest heuristic value,
					thus remove all other links, and then find next hyperlink with the highest hValue in the content
					redo that until we find the goal state */
				//queue.clear();
					nodesVisited++;

					// Go and fetch the contents of this file.
					String contents = Utilities.getFileContents(directoryName
							+ File.separator
							+ currentURL);

					if (isaGoalNode(contents)) /*contents.indexOf(GOAL_PATTERN)>=0  :match the the goal*/
					{
						currentNode.reportSolutionPath(); /* implement this method */
						break;
					}

					// Remember this node was visited.
					CLOSED.add(currentURL);
					addNewChildrenToOPEN(currentNode, contents, searchStrategy, directoryName);

					// Provide a status report.
					if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
							+ " |OPEN| = " + OPEN.size());
			}
		}else if (searchStrategy.equalsIgnoreCase("beam")) {
			while (queue.peek() != null)
			{
					//System.out.println("-----------");
					SearchNode[] keep10node = new SearchNode[10];
					if (queue.size()>10) {
						 for (int i = 0; i < 10; i++ ) keep10node[i] = queue.poll();  /*if it's the beam10 search, then pop out 10 nodes with highest hValue*/
					}else{ //  size < 10
							for (int i = 0; i < queue.size() ; i++ ) keep10node[i] = queue.poll();
					}
					queue.clear();
					String currentURL = null;
					String contents = null;
					for (int i = 0; i < 10 ; i++ ) {
							if (keep10node[i] != null) {
								currentURL = keep10node[i].getNodeName();
								//System.out.println("go to child nodes :"+currentURL);
								contents = Utilities.getFileContents(directoryName
										+ File.separator
										+ currentURL);
								nodesVisited++;
								CLOSED.add(currentURL);
								addNewChildrenToOPEN(keep10node[i], contents, searchStrategy, directoryName);
							}
					}

					// addNewChildrenToOPEN(currentNode, contents, searchStrategy, directoryName);

					// Provide a status report.
					if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
							+ " |OPEN| = " + OPEN.size());
			}
		}else{  /*bfs , dfs*/
			while (!OPEN.isEmpty()){
				SearchNode currentNode = pop(OPEN); /*if it's the best-first search, then pop out the node with highest hValue*/
				String currentURL = currentNode.getNodeName();
				nodesVisited++;

				// Go and fetch the contents of this file.
				String contents = Utilities.getFileContents(directoryName
						+ File.separator
						+ currentURL);

				if (isaGoalNode(contents)) /*contents.indexOf(GOAL_PATTERN)>=0  :match the the goal*/
				{
					currentNode.reportSolutionPath(); /* implement this method */
					break;
				}

				// Remember this node was visited.
				CLOSED.add(currentURL);

				addNewChildrenToOPEN(currentNode, contents, searchStrategy, directoryName);

				// Provide a status report.
				if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
						+ " |OPEN| = " + OPEN.size());
			}
		}
		if (!searchStrategy.equalsIgnoreCase("beam")) {
			System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
					" " + directoryName + File.separator + startNode +
					", using: " + searchStrategy + " search.");
		}
	}

	// This method reads the page's contents and
	// collects the 'children' nodes (ie, the hyperlinks on this page).
	// The parent node is also passed in so that 'backpointers' can be
	// created (in order to later extract solution paths).
	static void addNewChildrenToOPEN(SearchNode parent, String contents, String searchStrategy, String directoryName)
	{
		// StringTokenizer's are a nice class built into Java.
		// Be sure to read about them in some Java documentation.
		// They are useful when one wants to break up a string into words (tokens).
		StringTokenizer st = new StringTokenizer(contents);

		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (token.equalsIgnoreCase("<A"))  /* <A HERF = page#.html > ...</A>  */
			{
				String hyperlink; // The name of the child node.

				if (DEBUGGING) System.out.println("Encountered a HYPERLINK");

				token = st.nextToken();
				if (!token.equalsIgnoreCase("HREF"))
				{
					System.out.println("Expecting 'HREF' and got: " + token);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase("="))
				{
					System.out.println("Expecting '=' and got: " + token);
				}

				// Now we should be at the name of file being linked to.
				hyperlink = st.nextToken();
				if (!hyperlink.startsWith("page"))
				{
					System.out.println("Expecting 'page#.html' and got: " + hyperlink);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase(">"))
				{
					System.out.println("Expecting '>' and got: " + token);
				}

				if (DEBUGGING) System.out.println(" - found a link to " + hyperlink);

				//////////////////////////////////////////////////////////////////////
				// Have collected a child node; now have to decide what to do with it.
				//////////////////////////////////////////////////////////////////////

				if (alreadyInOpen(hyperlink))
				{ // If already in OPEN, we'll ignore this hyperlink
					// (Be sure to read the "Technical Note" below.)
					if (DEBUGGING) System.out.println(" - this node is in the OPEN list.");
				}
				else if (CLOSED.contains(hyperlink))
				{ // If already in CLOSED, we'll also ignore this hyperlink.
					if (DEBUGGING) System.out.println(" - this node is in the CLOSED list.");
				}
				else
				{ // Collect the hypertext if this is a previously unvisited node.
					// (This is only needed for HEURISTIC SEARCH, but collect in
					// all cases for simplicity.)
					String hypertext = ""; // The text associated with this hyperlink.

					do
					{
						token = st.nextToken();
						if (!token.equalsIgnoreCase("</A>")) hypertext += " " + token;
					}
					while (!token.equalsIgnoreCase("</A>"));
					if (DEBUGGING) System.out.println("   with hypertext: " + hypertext);

					//////////////////////////////////////////////////////////////////////
					// At this point, you have a new child (hyperlink) and you have to
					// insert it into OPEN according to the search strategy being used.
					// Your heuristic function for best-first search should accept as
					// arguments both "hypertext" (ie, the text associated with this
					// hyperlink) and "contents" (ie, the full text of the current page).
					//////////////////////////////////////////////////////////////////////
					if (searchStrategy.equalsIgnoreCase("breadth")) { /*FIFO*/
							SearchNode child = new SearchNode(hyperlink);
							child.parentIs(parent);
							OPEN.add(child); /* queue: add / poll */
							OPENED.add(child);

					}else if (searchStrategy.equalsIgnoreCase("depth")) {/*LIFO*/
							SearchNode child = new SearchNode(hyperlink);
							child.parentIs(parent);
							OPEN.push(child); /* stack : push / pop */
							OPENED.add(child);

					}else if (searchStrategy.equalsIgnoreCase("best")) {
					/*heuristic function definition: h(n) = 0.2x + 0.3y + 0.5z
							where x is the total # of QUERY words on that hyperlink page,
		           y is the total # of QUERY words in the hypertext associated with that hyperlink,
		           z is the similarity results from the goal sequence of words “QUERY1 QUERY2 QUERY3 QUERY4”
		           Now for z value, Define similarity by quantifying a notion of distance:... in report
							*/
							/* for x value */
							int x = 0 , y = 0, z = 0;
							Double hValue = 0.00;
							x = getXvalue(hyperlink,directoryName);
							y = getYvalue(hypertext);
							z = getZvalue(hyperlink,directoryName);
							//System.out.println(hyperlink);
							//System.out.println("x:"+x+"y:"+y+"z:"+z);
							hValue = 0.1 * x + 0.4 * y + 0.5 * z;
							//System.out.println("heuristic value:"+hValue);
							SearchNode child = new SearchNode(hyperlink);
							child.parentIs(parent);
							child.setHvalue(hValue);
							queue.add(child); /* PriorityQueue : add() / remove() or poll() */
							OPENED.add(child);
					}else{ /*beam*/
						int x = 0 , y = 0, z = 0;
						Double hValue = 0.00;
						x = getXvalue(hyperlink,directoryName);
						y = getYvalue(hypertext);
						z = getZvalue(hyperlink,directoryName);
						//System.out.println(hyperlink);
						//System.out.println("x:"+x+"y:"+y+"z:"+z);
						hValue = 0.1 * x + 0.4 * y + 0.5 * z;
						//System.out.println("heuristic value:"+hValue);
						SearchNode child = new SearchNode(hyperlink);
						child.parentIs(parent);
						child.setHvalue(hValue);
						queue.add(child); /* PriorityQueue : add() / remove() or poll() */
						OPENED.add(child);

						if (z==1000) /*contents.indexOf(GOAL_PATTERN)>=0  :match the the goal*/
						{
							// Report the solution path found
							// (You might also wish to write a method that
							// counts the solution-path's length, and then print that
							// number here.)
							child.reportSolutionPath(); /* implement this method */
							System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
									" " + directoryName + File.separator + "page1.html" +
									", using: " + searchStrategy + " search.");
							System.exit(0);
						}
					}
				}
			}
		}
	}
static int getXvalue(String hyperlink,String directoryName ){  /*count how many special words on that hyperlink page*/
		int ret = 0;
		String contents = Utilities.getFileContents(directoryName
			+ File.separator
			+ hyperlink);
		StringTokenizer st = new StringTokenizer(contents);

		while (st.hasMoreTokens()){
				String token = st.nextToken();
				if (token.equalsIgnoreCase("QUERY1")|token.equalsIgnoreCase("QUERY2")|token.equalsIgnoreCase("QUERY3")|token.equalsIgnoreCase("QUERY4")) {
						ret++;
				}
		}
		return ret;
}
static int getYvalue(String hypertext){ /*count how many special words in the hypertext associated with that hyperlink*/
		int ret = 0;
		StringTokenizer st = new StringTokenizer(hypertext);
		while (st.hasMoreTokens()){
			String token = st.nextToken();
			if (token.equalsIgnoreCase("QUERY1")|token.equalsIgnoreCase("QUERY2")|token.equalsIgnoreCase("QUERY3")|token.equalsIgnoreCase("QUERY4")) {
					ret++;
			}
		}
		return ret;
}
static int getZvalue(String hyperlink,String directoryName ){
	int ret = 0, i = 0, count = 0;
	String contents = Utilities.getFileContents(directoryName
		+ File.separator
		+ hyperlink);
	StringTokenizer st = new StringTokenizer(contents);
	StringTokenizer st1 = new StringTokenizer(contents);
	String token = null;
	while (st.hasMoreTokens()){
		st = new StringTokenizer(contents);
		st1 = new StringTokenizer(contents);
		 for (i = 0; i <= count; i++ ) {
			  if(st.hasMoreTokens()){
				 token = st.nextToken();
		 		 st1.nextToken();
			 }
		 }
		if (token.equals("QUERY1") ) {
			 if (st.hasMoreTokens()) token = st.nextToken();
			 if (token.equals("QUERY2")) {
				  if (st.hasMoreTokens()) token = st.nextToken();
					if (token.equals("QUERY3")) {
							if (st.hasMoreTokens()) token = st.nextToken();
							if (token.equals("QUERY4")) return 1000;
							else ret= Math.max(3,ret);   //q1 q2 q3
					}else ret= Math.max(2,ret); //q1 q2
			 }else ret= Math.max(1,ret); //q1
		}
		else if (token.equals("QUERY2")) {
			if (st.hasMoreTokens()) token = st.nextToken();
			if (token.equals("QUERY3")) {
					if (st.hasMoreTokens()) token = st.nextToken();
					if (token.equals("QUERY4")) ret= Math.max(3,ret); //q2 q3 q4
					else ret= Math.max(2,ret);   //q2 q3
		  }else ret= Math.max(1,ret); //q2
		}
		else if (token.equals("QUERY3")) {
			if (st.hasMoreTokens()) token = st.nextToken();
			if (token.equals("QUERY4")) ret= Math.max(2,ret); //q3 q4
			else ret= Math.max(1,ret);   //q3
		}
		else if(token.equals("QUERY4")) ret= Math.max(1,ret);
		else ret= Math.max(0,ret); // without any QUERY
		count++;
	}
	return ret;
}

	// A GOAL is a page that contains the goalPattern set above.
	static boolean isaGoalNode(String contents)
	{
		return (contents != null && contents.indexOf(GOAL_PATTERN) >= 0);
	}

	// Is this hyperlink already in the OPEN list?
	// This isn't a very efficient way to do a lookup,
	// but its fast enough for this homework.
	// Also, this for-loop structure can be
	// be adapted for use when inserting nodes into OPEN
	// according to their heuristic score.
	static boolean alreadyInOpen(String hyperlink)
	{
		int length = OPEN.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = OPEN.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}

		return false;  // Not in OPEN.
	}

	// You can use this to remove the first element from OPEN.
	static SearchNode pop(LinkedList<SearchNode> list)//FIFO
	{
		SearchNode result = list.removeFirst();/*whats difference between removefirst() and pop()*/
		return result;
	}
}

/////////////////////////////////////////////////////////////////////////////////

// You'll need to design a Search node data structure.

// Note that the above code assumes there is a method called getHvalue()           ok
// that returns (as a double) the heuristic value associated with a search node,
// a method called getNodeName() that returns (as a String)        								 ok
// the name of the file (eg, "page7.html") associated with this node, and
// a (void) method called reportSolutionPath() that prints the path                ok
// from the start node to the current node represented by the SearchNode instance.
class SearchNode implements Comparable<SearchNode>
{
	final String nodeName;
  SearchNode parent;
	Double hValue;
	public SearchNode(String name) {
		nodeName = name;
	}

	public void reportSolutionPath() {
		WebSearch web = new WebSearch();

		System.out.print("Path:  " + nodeName);
		while(parent != null){
				System.out.print( " <- "+ parent.getNodeName() );
				for (int i = 0; i < web.OPENED.size() ; i++ ) {
						if(parent.getNodeName().equalsIgnoreCase(web.OPENED.get(i).getNodeName())){
							parent = web.OPENED.get(i).getParent();
							break;
						}
				}
		}
	}

	public int compareTo(SearchNode n) {
		if(hValue>n.hValue){
				return -1;
		}else if(hValue<n.hValue){
				return 1;
		}else{
		return 0;
		}
	}

	public String getNodeName() {
		return nodeName;
	}

	public void parentIs(SearchNode name){
		 parent = name;
	}

	public SearchNode getParent(){
		return parent;
	}

	public Double getHvalue(){
		return hValue;
	}
	public void setHvalue(Double v){
		 hValue = v;
	}

}

/////////////////////////////////////////////////////////////////////////////////

// Some 'helper' functions follow.  You needn't understand their internal details.
// Feel free to move this to a separate Java file if you wish.
class Utilities
{
	// In J++, the console window can close up before you read it,
	// so this method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.println("");
		System.out.println(msg);
		try { System.in.read(); } catch(Exception e) {} // Ignore any errors while reading.
	}

	// This method will read the contents of a file, returning it
	// as a string.  (Don't worry if you don't understand how it works.)
	public static synchronized String getFileContents(String fileName)
	{
		File file = new File(fileName);
		String results = null;

		try
		{
			int length = (int)file.length(), bytesRead;
			byte byteArray[] = new byte[length];

			ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
			FileInputStream       inputStream = new FileInputStream(file);
			bytesRead = inputStream.read(byteArray);
			bytesBuffer.write(byteArray, 0, bytesRead);
			inputStream.close();

			results = bytesBuffer.toString();
		}
		catch(IOException e)
		{
			System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
		}

		return results;
	}
}
