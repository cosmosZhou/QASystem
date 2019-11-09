package com.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import com.util.Utility.Couplet;
import com.util.Utility.IndexedPriorityQueue;
import com.util.Utility.PriorityQueue;

public class MaximumSatisfaction {

	public static Comparator<TreeNode> comparator = new Comparator<TreeNode>() {
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			return Integer.compare(o1.totalSatisfaction, o2.totalSatisfaction);
		}
	};

	public static class Status {
		HashSet<TreeNode> center = new HashSet<TreeNode>();
		HashSet<TreeNode> coverage = new HashSet<TreeNode>();
		IndexedPriorityQueue<TreeNode> priorityQueue = new IndexedPriorityQueue<TreeNode>(comparator);

		void construct(TreeNode[] nodes) throws Exception {
			for (TreeNode node : nodes) {
				node.evaluateAdditiveSatisfaction(coverage);
				// System.out.println("TotalSatisfaction = " +
				// node.totalSatisfaction);
				priorityQueue.add(node.id, node);
//				System.out.println("x[" + node.id + "] = " + node.totalSatisfaction);
				System.out.println("pq.set(" + node.id + ", " + node.totalSatisfaction + "d);");
			}

			int maximum = -1;

			TreeNode node = priorityQueue.peek();
			if (node == null) {

			}
			System.out.println("TotalSatisfaction = " + node.totalSatisfaction);
			if (maximum < 0) {
				maximum = node.totalSatisfaction;
				priorityQueue.poll();
				HashSet<TreeNode> neighbors = node.neighbors();

				printNeighbors(neighbors);
				this.center.add(node);
				this.coverage.addAll(neighbors);
				node.updateAdditiveSatisfaction(priorityQueue);
			} else if (node.totalSatisfaction == maximum) {
				priorityQueue.poll();

				HashSet<TreeNode> neighbors = node.neighbors();
				printNeighbors(neighbors);
			}

			for (int id : priorityQueue.pq) {
				System.out.print(id + "\t");
			}
			System.out.println();
		}
	}

	ArrayList<HashSet<TreeNode>> setList = new ArrayList<HashSet<TreeNode>>();

	public static class TreeNode {
		ArrayList<TreeNode> child = new ArrayList<TreeNode>();
		TreeNode parent;

		TreeNode(int id, int satisfaction) {
			this.id = id;
			this.satisfaction = satisfaction;
		}

		/**
		 * as stated, each room have at least one door and at most 3 doors(of
		 * course Alice can go to every room in this house).
		 * 
		 * @param child
		 * @throws Exception
		 */
		void addChild(TreeNode child) throws Exception {
			if (this.child.size() >= 3) {
				throw new Exception("more than 3 edges occurred!");
			}
			this.child.add(child);
			child.parent = this;
		}

		int id;
		int satisfaction;
		int totalSatisfaction;

		HashSet<TreeNode> neighbors() {
			HashSet<TreeNode> set = new HashSet<TreeNode>();
			set.add(this);
			if (parent != null) {
				set.add(parent);
			}

			for (TreeNode ch : child) {
				set.add(ch);
			}
			return set;
		}

		void addSatisfaction(HashSet<TreeNode> set, TreeNode node) {
			if (!set.contains(node)) {
				totalSatisfaction += node.satisfaction;
				// set.add(node);
			}
		}

		void evaluateAdditiveSatisfaction(HashSet<TreeNode> set) {
			totalSatisfaction = 0;
			addSatisfaction(set, this);

			if (this.parent != null) {
				addSatisfaction(set, parent);
			}

			for (TreeNode ch : child) {
				addSatisfaction(set, ch);
			}
		}

		void addToPriorityQueue(IndexedPriorityQueue<TreeNode> priorityQueue) throws Exception {
			priorityQueue.add(id, this);
		}

		void updateAdditiveSatisfaction(IndexedPriorityQueue<TreeNode> priorityQueue) throws Exception {
			totalSatisfaction = 0;
			addToPriorityQueue(priorityQueue);
			if (this.parent != null) {
				parent.totalSatisfaction -= this.satisfaction;
				parent.addToPriorityQueue(priorityQueue);
			}

			for (TreeNode ch : child) {
				ch.totalSatisfaction -= this.satisfaction;
				ch.addToPriorityQueue(priorityQueue);
			}
		}

		@Override
		public String toString() {
			try {
				Utility.LNodeShadow LNodeShadow = buildShadowTree();
				LNodeShadow.max_width = 10;
				return LNodeShadow.toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public Utility.LNodeShadow buildShadowTree() throws Exception {
			// recursive inorder scan used to build the shadow tree
			// create the new shadow tree;
			String str = "S[" + this.id + "]=" + satisfaction;
			Utility.LNodeShadow newNode = new Utility.LNodeShadow(str);

			switch (child.size()) {
			case 0:
				break;
			case 1:
				newNode.x = new Utility.LNodeShadow[1];
				newNode.x[0] = child.get(0).buildShadowTree();
				break;
			case 2:
				newNode.x = new Utility.LNodeShadow[1];
				newNode.x[0] = child.get(0).buildShadowTree();
				newNode.y = new Utility.LNodeShadow[1];
				newNode.y[0] = child.get(1).buildShadowTree();
				break;
			case 3:
				newNode.x = new Utility.LNodeShadow[1];
				newNode.x[0] = child.get(0).buildShadowTree();
				newNode.y = new Utility.LNodeShadow[2];
				newNode.y[0] = child.get(1).buildShadowTree();
				newNode.y[1] = child.get(2).buildShadowTree();
				break;
			default:
				throw new Exception("edges more than 3");
			}
			return newNode;
		}

		static TreeNode construct_root(int N, int S[], Couplet.Integer[] connection) throws Exception {
			TreeNode[] nodes = TreeNode.construct(N, S, connection);
			TreeNode root = null;
			for (int i = 0; i < S.length; ++i) {
				if (nodes[i].parent == null) {
					root = nodes[i];
					break;
				}
			}
			return root;
		}

		static TreeNode[] construct(int N, int S[], Couplet.Integer[] connection) throws Exception {
			TreeNode[] nodes = new TreeNode[S.length];
			for (int i = 0; i < S.length; ++i) {
				nodes[i] = new TreeNode(i + 1, S[i]);
			}

			for (Couplet.Integer pair : connection) {
				if (nodes[pair.x].parent != null) {
					nodes[pair.x].addChild(nodes[pair.y]);
				} else if (nodes[pair.y].parent != null) {
					nodes[pair.y].addChild(nodes[pair.x]);
				} else if (nodes[pair.x].child.size() < nodes[pair.y].child.size()) {
					nodes[pair.y].addChild(nodes[pair.x]);
				} else {
					nodes[pair.x].addChild(nodes[pair.y]);
				}
			}
			return nodes;

			// the following code might be redundant, for the root
			// of the tree is unique.
			// TreeNode root = null;
			// for (int i = 0; i < S.length; ++i) {
			// if (nodes[i].parent == null) {
			// if (root == null) {
			// root = nodes[i];
			// } else {
			// TreeNode child = nodes[i];
			// TreeNode parent = child.child.remove(child.child.size() - 1);
			// parent.addChild(child);
			// }
			// }
			// }
		}
	}

	/**
	 * 
	 * @param N
	 *            a very big house with N rooms and N-1 doors
	 * @param M
	 *            M wireless routers to be put into the room.
	 * @param S[]
	 *            the satisfaction point S[i] Alice could have if room i have
	 *            Wifi, predetermined.
	 * @param connection
	 *            which is an array of integer couplet consisting of two
	 *            integers x, y, which represents a door is between room x and
	 *            y.
	 * @return the maximum point of satisfaction.
	 * @throws Exception
	 */
	// output the maximum point of satisfaction.
	static int maximumPointOfSatisfaction(int N, int M, int S[], Couplet.Integer[] connection) throws Exception {
		TreeNode[] nodes = TreeNode.construct(N, S, connection);
		TreeNode root = null;
		for (int i = 0; i < S.length; ++i) {
			if (nodes[i].parent == null) {
				root = nodes[i];
				break;
			}
		}
		System.out.println(root);
		Status status = new Status();
		status.construct(nodes);
		return 0;
	}

	static void printNeighbors(HashSet<TreeNode> neighbors) {
		for (TreeNode node : neighbors) {
			System.out.print(node.satisfaction + "\t");
		}
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		int N = 5;
		int M = 2;
		int S[] = { 1, 2, 3, 4, 5, 10, 20 };
		int connectionArray[][] = { { 2, 1 }, { 3, 2 }, { 4, 2 }, { 5, 3 }, { 5, 6 }, { 5, 7 }, };

		Couplet.Integer[] connection = new Couplet.Integer[connectionArray.length];
		for (int i = 0; i < connectionArray.length; ++i) {
			connection[i] = new Couplet.Integer(connectionArray[i][0] - 1, connectionArray[i][1] - 1);
		}

		int maximumPointOfSatisfactionResult = maximumPointOfSatisfaction(N, M, S, connection);
		System.out.println(maximumPointOfSatisfactionResult);
	}
}
