
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * Quora Coding Challenge, Ontology
 * 
 * Quora has many questions on different topics, and a common product use-case for
 * our @mention selectors and search service is to look-up questions under a certain
 * topic as quickly as possible.
 * 
 * For this problem, imagine a simplified version of Quora where each question has only
 * one topic associated with it. In turn, the topics form a simplified ontology where
 * each topic has a list of children, and all topics are descendants of a single root
 * topic.
 * 
 * Design a system that allows for FAST searches of questions under topics. There are N
 * topics, M questions, and K queries, given in this order. Each query has a desired topic
 * as well as a desired string prefix. For each query, return the number of questions that
 * fall under the queried topic and begin with the desired string. When considering topics,
 * we want to include all descendants of the queried topic as well as the queried topic itself.
 * In other words, each query searches over the subtree of the topic.
 * 
 * The topic ontology is given in the form of a flattened tree of topic names, where each topic
 * may optionally have children. If a topic has children, they are listed after it within parantheses,
 * and those topics may have children of their own, etc. See the sample for the exact input format.
 * The tree is guaranteed to have a single root topic.
 * 
 * For ease of parsing, each topic name will be composed of English alphabetical characters,
 * and each question and query text will be composed of English alphabetical characters, spaces,
 * and question marks. Each question and query text will be well behaved: there will be no
 * consecutive spaces or leading/trailing spaces. All queries, however, are case sensitive.
 * 
 * @author George Ding
 * @url https://www.quora.com/challenges
 * @date Wednesday, October 19, 2016
 */
public class Ontology {

	/**
	 * Class to represent a node in a QuestionTrie. It holds
	 * a count for how many questions have the currently accumulated
	 * prefix and a Map for all its children mapping the next character
	 * in a question to the corresponding QuestionTrieNode.
	 * 
	 * @author George Ding
	 */
	static class QuestionTrieNode {

		int questionCount;
		Map<Character, QuestionTrieNode> children;

		public QuestionTrieNode() {
			this.questionCount = 0;
			this.children = new HashMap<>();
		}
	}

	/**
	 * Class for a QuestionTrie. A QuestionTrie is a prefix tree
	 * for all of the questions in a given topic and its children topics. 
	 * It exclusively keeps count of how many questions are seen with 
	 * each prefix in the tree.
	 * 
	 * @author George Ding
	 */
	static class QuestionTrie {

		// The root of this QuestionTrie
		QuestionTrieNode root;

		public QuestionTrie() {
			this.root = new QuestionTrieNode();
		}
		
		// Function to add a question into the QuestionTrie, updating the count for each prefix.
		void addQuestion(String question) {
			QuestionTrieNode currNode = root;
			int currIndex = 0;
			while (currIndex < question.length()) {
				currNode.questionCount++;
				char currChar = question.charAt(currIndex);
				if (!currNode.children.containsKey(currChar)) {
					currNode.children.put(currChar, new QuestionTrieNode());
				}
				currNode = currNode.children.get(currChar);
				currIndex++;
			}
			currNode.questionCount++;
		}

		// Function to get the number of times we see a question prefix in this QuestionTrie.
		int getQuestionPrefixCount(String questionPrefix) {
			QuestionTrieNode currNode = root;
			int currIndex = 0;
			while (currIndex < questionPrefix.length()) {
				char currChar = questionPrefix.charAt(currIndex);
				if (!currNode.children.containsKey(currChar)) {
					return 0;
				}
				currNode = currNode.children.get(currChar);
				currIndex++;
			}
			return currNode.questionCount;
		}
	}

	/**
	 * Class to represent a node in a topic ontology tree.
	 * Holds the topic it represents, a literal representation of all the
	 * questions for the topic in a stack, the questions in the topic
	 * represented by a QuestionTrie, the depth of the topic in terms of how
	 * far it is from the root topic, and list of children nodes.
	 * 
	 * @author George Ding
	 */
	static class TopicTreeNode {

		String topic;
		Stack<String> questionsLiteral;
		QuestionTrie questions;
		List<TopicTreeNode> children;
		int depth;

		// Constructor to build a TopicTreeNode with topic and depth.
		public TopicTreeNode(String topic, int depth) 
		{
			this.topic = topic;
			this.questionsLiteral = new Stack<>();
			this.questions = null;
			this.children = new ArrayList<>();
			this.depth = depth;
		}

		// Function to record question for topic by adding it to
		// the questionsLiteral Stack.
		void addQuestion(String question) {
			this.questionsLiteral.push(question);
		}
	}
	
	/**
	 * Iterative function to take a flattened string representation of a topic
	 * tree and builds the topic ontology tree. Returns the tree as a map from
	 * topic to its node in the tree. The map will be the only access point to the
	 * tree so that the structure of the tree is hidden in memory and access to
	 * any nodes will be a constant O(1) operation. 
	 * 
	 * @param flattenedTree: Flattened String representation of the topic tree
	 * @param topicTreeMap: Tree map mapping topic to the corresponding topic node
	 * in the ontology tree.
	 */
	static void buildTopicOntologyTreeHelper(
			String[] flattenedTree,
			Map<String, TopicTreeNode> topicTreeMap) {
		
		Stack<TopicTreeNode> parentStack = new Stack<>();
		
		// Initialize root node in tree. This is the node above the root topic.
		parentStack.push(new TopicTreeNode("", 0));
		for (int i = 0; i < flattenedTree.length; i++) {
			if (flattenedTree[i].equals("(")) {
				continue;
			}
			if (flattenedTree[i].equals(")")) {
				parentStack.pop();
				continue;
			}
			
			// Set its depth to the size of the stack, representing how many parent topics
			// are above it.
			TopicTreeNode currNode = new TopicTreeNode(flattenedTree[i], parentStack.size());
			parentStack.peek().children.add(currNode);
			topicTreeMap.put(currNode.topic, currNode);
			if (i < flattenedTree.length-1 && flattenedTree[i+1].equals("(")) {
				parentStack.push(currNode);
			}
		}
	}

	/**
	 * Wrapper function to build and return the tree map representation of
	 * a topic ontology represented by a flattened tree. We are only interested in
	 * having a reference to our tree map and not a pointer to the root of the tree
	 * because it allows us to access nodes in constant time, O(1), while abstracting
	 * out the structure of the topic ontology tree to be hidden in memory.
	 * 
	 * @param flattenedTree: String representation of the topic ontology to be built.
	 * @return The tree map of the topic ontology tree
	 */
	static Map<String, TopicTreeNode> buildTopicOntology(String flattenedTree) {

		Map<String, TopicTreeNode> topicTreeMap = new HashMap<>();
		String[] flattenedTreeArr = flattenedTree.split(" ");
		buildTopicOntologyTreeHelper(flattenedTreeArr, topicTreeMap);
		return topicTreeMap;
	}

	/**
	 * Given the tree map of a topic ontology, add a question to a given
	 * topic.
	 * 
	 * @param topicOntologyTreeMap: The topic ontology
	 * @param topicQuestion: "Topic: Question"
	 */
	static void addQuestionToTopic(
			Map<String, TopicTreeNode> topicOntologyTreeMap,
			String topicQuestion)
	{
		int indexOfColon = topicQuestion.indexOf(':');
		String topic = topicQuestion.substring(0,indexOfColon);
		String question = topicQuestion.substring(indexOfColon+2);
		topicOntologyTreeMap.get(topic).addQuestion(question);
	}
	
	static void mergeTrieHelper(QuestionTrieNode parentNode, QuestionTrieNode childNode) {
		
		parentNode.questionCount += childNode.questionCount;
		for (Character childsChild : childNode.children.keySet()) {
			if (parentNode.children.keySet().contains(childsChild)) {
				mergeTrieHelper(parentNode.children.get(childsChild), childNode.children.get(childsChild));
			} else {
				parentNode.children.put(childsChild, childNode.children.get(childsChild));
			}
		}
	}
	
	static void mergeTrie(QuestionTrie parentTrie, QuestionTrie childTrie) {
		mergeTrieHelper(parentTrie.root, childTrie.root);
	}

	/**
	 * Breadth First Search helper function for processQuery that searches
	 * through every child topic of a given topic subtree to find the
	 * aggregate count for the number of times a certain question prefix
	 * is seen.
	 * 
	 * @param topicOntologyTreeMap
	 * @param topic
	 * @param questionPrefix
	 * @return
	 */
	static int bfsQuestionPrefix(
			Map<String, TopicTreeNode> topicOntologyTreeMap,
			String topic, 
			String questionPrefix)
	{
		
		TopicTreeNode topicSubTreeRoot = topicOntologyTreeMap.get(topic);
		
		if (topicSubTreeRoot.questions != null) {
			return topicSubTreeRoot.questions.getQuestionPrefixCount(questionPrefix);
		}
		
		Queue<TopicTreeNode> bfsFrontier = new LinkedList<>();
		bfsFrontier.add(topicSubTreeRoot);
		QuestionTrie currTrie = new QuestionTrie();
		while (!bfsFrontier.isEmpty()) {
			TopicTreeNode currChildTopic = bfsFrontier.remove();
			if (currChildTopic.questions != null) {
				mergeTrie(currTrie, currChildTopic.questions);
				currChildTopic.questions = null;
				continue;
			}
			while(!currChildTopic.questionsLiteral.isEmpty()) {
				currTrie.addQuestion(currChildTopic.questionsLiteral.pop());
			}
			bfsFrontier.addAll(currChildTopic.children);
		}
		topicSubTreeRoot.questions = currTrie;
		return currTrie.getQuestionPrefixCount(questionPrefix);
	}

	/**
	 * Given the tree map of a topic ontology, compute a query so as to
	 * how many questions in a given topic start with a certain question prefix.
	 * 
	 * @param topicOntologyTreeMap: The topic ontology
	 * @param query: "Topic Question Prefix"
	 * @return The number of questions in the topic that start with a given question prefix
	 */
	static int processQuery(
			Map<String, TopicTreeNode> topicOntologyTreeMap,
			String query)
	{
		int indexOfSpace = query.indexOf(' ');
		String topic = query.substring(0,indexOfSpace);
		String questionPrefix = query.substring(indexOfSpace+1);
		return bfsQuestionPrefix(topicOntologyTreeMap, topic, questionPrefix);
	}
	
	static void orderQuery(
			List<String>[] sorted, 
			Map<String, Queue<Integer>> ordered, 
			String query, 
			Map<String, TopicTreeNode> topicOntology,
			int order)
	{
		int ordering = topicOntology.get(query.substring(0,query.indexOf(' '))).depth;
		if (sorted[ordering] == null) {
			sorted[ordering] = new ArrayList<>();
		}
		sorted[ordering].add(query);
		if (!ordered.containsKey(query)) {
			ordered.put(query, new LinkedList<Integer>());
		}
		ordered.get(query).add(order);
	}
	
	static void doOntology(
			List<String>[] sorted, 
			Map<String, Queue<Integer>> ordered,
			Map<String, TopicTreeNode> topicOntology, 
			int[] results)
	{
		for (int i = sorted.length-1; i>=0; i--) {
			if (sorted[i] != null) {
				for (String query : sorted[i]) {
					results[ordered.get(query).remove()] = processQuery(topicOntology, query);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter writer = new PrintWriter(System.out);
        int N = Integer.parseInt(br.readLine());
        String flattenedTree = br.readLine();
        Map<String, TopicTreeNode> topicOntology = buildTopicOntology(flattenedTree);
        int M = Integer.parseInt(br.readLine());
        for (int i = 0; i < M; i++) {
        	addQuestionToTopic(topicOntology, br.readLine());
		}
        int K = Integer.parseInt(br.readLine());
        List<String>[] sortedQueries = (ArrayList<String> []) new ArrayList[N];
        Map<String, Queue<Integer>> orderedQueries = new HashMap<>();
        for (int j = 0; j < K; j++) {
        	orderQuery(sortedQueries, orderedQueries, br.readLine(), topicOntology, j);
		}
        int[] results = new int[K];
        doOntology(sortedQueries, orderedQueries, topicOntology, results);
        for (int res : results) {
        	writer.println(res);
        }
        writer.flush();
        writer.close();
	}
}
