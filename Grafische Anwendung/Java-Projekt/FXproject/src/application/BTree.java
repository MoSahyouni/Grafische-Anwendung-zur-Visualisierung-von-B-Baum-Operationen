package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.sun.prism.paint.Color;

class BTreeNode {
    List<Integer> keys;
    List<BTreeNode> children;
    boolean isLeaf;
    
    BTreeNode(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }
    BTreeNode(BTreeNode node)
    {
    	this.keys = new ArrayList<Integer>(node.keys);
        this.children = new ArrayList<BTreeNode>(node.children);
        this.isLeaf = node.isLeaf;
    }
    public int getChildIndex(BTreeNode node, int key) {
        // Find the index of the child node where the key might be present
        int idx = 0;
        while (idx < node.keys.size() && key > node.keys.get(idx)) {
            idx++;
        }
        return idx;
    }
    public int getKeyIndex(BTreeNode root, int key) {
    	if (root == null) {
            return -1; // If the root is null, key not found
        }

        // Search for the key in the current node
        int index = root.keys.indexOf(key);
        if (index != -1) {
            return index; // Key found in the current node
        }

        // If key not found in the current node, search in the child nodes
        int i = 0;
        while (i < root.keys.size() && key > root.keys.get(i)) {
            i++;
        }
        if (i < root.children.size()) {
            // Recursively search in the appropriate child node
            return getKeyIndex(root.children.get(i), key);
        } else {
            return -1; // Key not found in the tree
        }
    }
    
    public static BTreeNode search(BTreeNode node, int key) {
        // If the current node is null, return null
        if (node == null) {
            return null;
        }

        // Search for the key in the current node's keys
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) {
            i++;
        }

        // If the key is found at this node, return the node
        if (i < node.keys.size() && key == node.keys.get(i)) {
            return node;
        }

        // If the current node is a leaf, key is not present
        if (node.isLeaf) {
            return null;
        }

        // Recursively search in the appropriate child node
        return search(node.children.get(i), key);
    }
    
    public List<Integer> getChildrenKeys(BTreeNode node, int key) {
    	List<Integer> childrenKeys = new ArrayList<>();
        BTreeNode targetNode = BTreeNode.search(node, key);

        // If the key is found in the tree
        if (targetNode != null) {
            int childIdx = getKeyIndex(targetNode, key) + 1;

            // Add keys from the left child
            if (childIdx > 0) {
                BTreeNode leftChild = targetNode.children.get(childIdx - 1);
                for (Integer leftKey : leftChild.keys) {
                    childrenKeys.add(leftKey);
                }
            }

            // Add keys from the right child
            if (childIdx < targetNode.children.size()) {
                BTreeNode rightChild = targetNode.children.get(childIdx);
                for (Integer rightKey : rightChild.keys) {
                    childrenKeys.add(rightKey);
                }
            }
        }

        return childrenKeys;
    }
    
    public List<Integer> getAllChildrenKeys() {
    	List<Integer> allKeys = new ArrayList<>();
        collectKeys(this, allKeys);
        return allKeys;
    }
    
    private void collectKeys(BTreeNode node, List<Integer> keys) {
        if (node != null) {
            for (Integer key : node.keys) {
                keys.add(key);
            }
            for (BTreeNode child : node.children) {
                collectKeys(child, keys); // Recursively collect keys from children
            }
        }
    }

    private void findKeyIndexes(BTreeNode parent, int ChildIdx, int key1, int key2, List<Integer> indexes) {
        if (parent != null) {
            for (int i = 0; i < parent.keys.size(); i++) {
                int currentKey = parent.keys.get(i);
                if (currentKey == key1 || currentKey == key2) {
                    indexes.add(ChildIdx); // Add the index if the key matches
                }
            }
            // Recursively check children
            for (int i = 0; i < parent.children.size(); i++)  {
            	BTreeNode child = parent.children.get(i);
                findKeyIndexes(child, ChildIdx, key1, key2, indexes);
            }
        }
    }

    public List<Integer> getChildIndexesContainingKeys(BTreeNode parent, int key1, int key2) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < parent.children.size(); i++)  {
        	BTreeNode child = parent.children.get(i);
            findKeyIndexes(child, i, key1, key2, indexes);
        }
        return indexes;
    }
    
	public static boolean keysInSameNode(BTreeNode root, int val1, int val2) {
		BTreeNode node =BTreeNode.search(root, val2);
		if(node == null) return false;
		return node.keys.contains(val1);
	}
	
	public BTreeNode findLowestCommonAncestor(BTreeNode root, int key1, int key2) {
	    // Search for the nodes containing the keys
	    BTreeNode node1 = search(root, key1);
	    BTreeNode node2 = search(root, key2);
	    // If either of the keys is not present in the tree, return null
	    if (node1 == null || node2 == null) {
	        return null;
	    }

	    // Find the paths from the root to each key
	    List<BTreeNode> path1 = findPathToKey(root, key1);
	    List<BTreeNode> path2 = findPathToKey(root, key2);

	    // Find the last common node in the paths
	    BTreeNode lca = null;
	    int index = 0;
	    while (index < Math.min(path1.size(), path2.size()) && path1.get(index) == path2.get(index)) {
	        lca = path1.get(index);
	        index++;
	    }
	    return lca;
	}

	// Helper method to find the path from the root to a key
	private List<BTreeNode> findPathToKey(BTreeNode node, int key) {
	    List<BTreeNode> path = new ArrayList<>();
	    while (node != null) {
	        path.add(node);
	        int index = node.keys.indexOf(key);
	        if (index != -1) {
	            break; // Found the key in this node
	        }
	        int i = 0;
	        while (i < node.keys.size() && key > node.keys.get(i)) {
	            i++;
	        }
	        // Move down to the appropriate child
	        node = node.children.get(i);
	    }
	    return path;
	}
}

public class BTree {
    public BTreeNode root;
    private int t; // Minimum degree
    public static boolean merged =true;
    public static int toRemove ;
    public BTree(int t) {
        this.root = null;
        this.t = t;
    }

    public void insert(int key) {
        if (root == null) {
            root = new BTreeNode(true);
            root.keys.add(key);
        } else {
        	List<Integer> oldrootKeys= new ArrayList<>();
        	for(Integer x :root.keys)
        	{
        		oldrootKeys.add(x);
        	}
        	int oldrootChildrensize = root.children.size();
            if (root.keys.size() == (2 * t - 1)) {
                BTreeNode s = new BTreeNode(false);
                s.children.add(0, root);
                splitChild(s, 0);
                root = s;
                
                if(oldrootChildrensize ==0)DrawBTree.AnimationSplitRoot(oldrootKeys, oldrootChildrensize, root, key);
                else DrawBTree.AnimationSplitRootMoveChildren(oldrootKeys, oldrootChildrensize, root, key);
            }
            insertNonFull(root, key);
        }
    }
    
    private void splitChild(BTreeNode x, int i) {
        BTreeNode y = x.children.get(i);
        BTreeNode z = new BTreeNode(y.isLeaf);

        // Calculate the index for splitting
        int mid = t - 1;

        // Copy the keys from y to z
        for (int j = t; j < y.keys.size(); j++) {
            z.keys.add(y.keys.get(j));
        }
        y.keys.subList(t, y.keys.size()).clear();

        // If y is not a leaf, copy the children from y to z
        if (!y.isLeaf) {
            for (int j = t; j < y.children.size(); j++) {
                z.children.add(y.children.get(j));
            }
            y.children.subList(t, y.children.size()).clear(); // Entferne die zweite Hälfte der Kinder aus y
        }

        // Insert the middle key into x
        x.keys.add(i, y.keys.remove(mid));

        // Link z into x's children
        x.children.add(i + 1, z);
        
    }


    private void insertNonFull(BTreeNode x, int key) {
        int i = 0;
        while (i < x.keys.size() && key > x.keys.get(i)) {
            i++;
        }
        if (x.isLeaf) {
            x.keys.add(i, key);
            DrawBTree.animationInserttoNode(x.keys, key);
         } else {
            BTreeNode child = x.children.get(i);
            int index = i; if(index!=0) index--;
            List<Integer> oldChildKeys = new ArrayList<>(x.keys);
            List<Integer> splitedChildKeys = new ArrayList<>(child.keys);
            List<BTreeNode> oldChildren = new ArrayList<>();
            for(BTreeNode ch : x.children)
            	oldChildren.add(new BTreeNode(ch));
            if (child.keys.size() == 2 * t - 1) {
                splitChild(x, i);
                if (key > x.keys.get(i)) {
                    i++;
                }
                DrawBTree.AnimationSplitChild(oldChildKeys, oldChildren, splitedChildKeys, x, key,index);
            }
            DrawBTree.animationToChild( x.keys.get(index), child, key );
            insertNonFull(x.children.get(i), key);
        }
    }

    public boolean search(int key) {
        return search(root, key);
    }

    public boolean search(BTreeNode node, int key) {
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) {
            i++;
        }
        if (i < node.keys.size() && key == node.keys.get(i)) {
            return true;
        }
        if (node.isLeaf) {
            return false;
        }
        return search(node.children.get(i), key);
    }
    
    public void printTree() {
        printTree(root, "");
    }
    
    private void printTree(BTreeNode node, String prefix) {
        if (node == null)
            return;

        System.out.print(prefix);
        for (int i = 0; i < node.keys.size(); i++) {
            System.out.print(node.keys.get(i) + " - ");
        }
        System.out.println();

        if (!node.isLeaf) {
            for (int i = 0; i < node.children.size(); i++) {
                String childPrefix = i == node.children.size() - 1 ? prefix + "└── " : prefix + "├── ";
                printTree(node.children.get(i), childPrefix);
            }
        }

    }
    
    public void delete(int key) {
        if (root == null) {
            // The tree is empty, nothing to delete
            return;
        }
        toRemove = key;
        // Call a recursive helper method to perform deletion
        deleteKey(root, key,false);

        // If the root node is empty after deletion, and it has a child, make the child the new root
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
        
    }
    
    private void deleteKey(BTreeNode node, int key, boolean found) { 
        merged &=false;
    	// Find the index of the key in the node
        int index = node.keys.indexOf(key);
        // Case 1: The key is present in the current node
        if (index != -1) {
            // Case 1a: The node is a leaf node and has a degree > t - 1
            if (node.isLeaf && node.keys.size() > t - 1) {
            	// Remove the key from the node
                if(!found&&node.keys.get(index)==toRemove)DrawBTree.animationRemoveKey(node.keys.get(index));
                node.keys.remove(index);   
                
            } else {
            	// Case 1b: The node has a degree <= t - 1
            	// Find the Successor Or Predecessor key and replace the key to be deleted with it
            	int successorOrPredecessor = decidePredecessorOrSuccessor(node, key);
            	int ChildIdx = index;
            	if(successorOrPredecessor > key) ChildIdx++;
            	BTreeNode child = node.children.get(ChildIdx);
            	boolean skip = false;
            	if(node.children.get(index).keys.size() <= t - 1 && node.children.get(index+1).keys.size() <= t - 1)
            	{
            		merged =true;
            		borrowOrMerge(node, index, node.children.get(index));
            		successorOrPredecessor = findPredecessor(node.children.get(index), key);
            		int childIdx = getChildIndex(node, key);
            		//clear the Transitions made from decidePredecessorOrSuccessor 
            		DrawBTree.successorAnimations.clear();
            		deleteKey(node.children.get(childIdx), key,false);
            		skip = true;
            	}
            	else if (node.children.get(ChildIdx).keys.size() <= t - 1) {
            		merged =true;
            		borrowOrMerge(node, index, child);
            		if(successorOrPredecessor > key&& merged) ChildIdx --;
            		deleteKey(node.children.get(ChildIdx), key,true);
                }else if(!skip){
                	node.keys.set(index, successorOrPredecessor);
                	deleteKey(node.children.get(ChildIdx), successorOrPredecessor,true);
                	DrawBTree.switchWithSuccessorOrPredecessor(successorOrPredecessor, key);
                }
                if(merged && key == toRemove)DrawBTree.animationRemoveKey( key);   
            }
        } else {
            // Case 2: The key is not present in the current node
            // Determine the child node where the key might be present
            int childIdx = getChildIndex(node, key);
            if(!found)
            {
            	if(childIdx >0)DrawBTree.animationToChild(node.keys.get(childIdx-1), node.children.get(childIdx ), key);
                else DrawBTree.animationToChild(node.keys.get(childIdx), node.children.get(childIdx ), key);
            }
            // If the child exists and has fewer than t keys, borrow a key from its sibling or merge
            if (childIdx < node.children.size()) {
            	BTreeNode child = node.children.get(childIdx);
                if (child.keys.size() == t - 1) {
                    // Perform borrow or merge operations
                	merged =true;
                	childIdx = borrowOrMerge(node, childIdx, child);
                }
            }
            // Recursively delete the key from the appropriate child node
            deleteKey(node.children.get(childIdx ), key,found);
        }
    }
    
    private int borrowOrMerge(BTreeNode parent, int childIdx, BTreeNode child) {
    	int newIdx = childIdx;
        if (childIdx < parent.children.size() - 1|| (childIdx < parent.children.size() -1)) {
            // Try borrowing a key from right sibling
            BTreeNode rightChild = parent.children.get(childIdx + 1);
            BTreeNode leftChild = child;
            
            if (rightChild.keys.size() > t - 1) {
            	merged &=false;
            	// Borrow a key from right sibling
            	int borrowedKey = rightChild.keys.remove(0);
                DrawBTree.AnimationBorrowKey( leftChild, borrowedKey,parent,childIdx);
                leftChild.keys.add(parent.keys.get(childIdx));
                parent.keys.set(childIdx, borrowedKey);
                // Adjust children if not leaf node
                if (!leftChild.isLeaf) {
                    BTreeNode borrowedChild = rightChild.children.remove(0);
                    leftChild.children.add(borrowedChild);
                    DrawBTree.AnimationMoveChild(parent, borrowedChild, borrowedKey);
                }
                return newIdx;
            }
        }// Try borrowing a key from left sibling
    	 else if (childIdx > 0 &&  parent.children.get(childIdx - 1).keys.size() > t - 1) {
        	BTreeNode leftChild = parent.children.get(childIdx - 1);
            BTreeNode rightChild = child;
            merged&=false;
            // Borrow a key from left sibling
            int borrowedKey = leftChild.keys.remove(leftChild.keys.size() - 1);
            DrawBTree.AnimationBorrowKey( rightChild, borrowedKey,parent,childIdx);
            rightChild.keys.add(0, parent.keys.get(childIdx - 1));
            parent.keys.set(childIdx - 1, borrowedKey);

            // Adjust children if not leaf node
            if (!rightChild.isLeaf) {
            	BTreeNode borrowedChild = leftChild.children.remove(leftChild.children.size() - 1);
                rightChild.children.add(0, borrowedChild);
                DrawBTree.AnimationMoveChild(parent, borrowedChild, borrowedKey);
            }
            return newIdx;
        }

        // If borrowing is not possible, merge child with its sibling
        if (newIdx == parent.children.size()-1 ||
        (childIdx > 0 && parent.children.size()!=4 && parent.children.get(newIdx).keys.get(0) < parent.keys.get(newIdx-1))) {
            // Merge with left sibling
        	BTreeNode leftSibling = parent.children.get(childIdx - 1);
            BTreeNode rightSibling = child;
            DrawBTree.AnimationMergeChildren(parent, childIdx-1, parent.keys.get(childIdx-1));
            merged&=true;
            // Merge child with left sibling
            leftSibling.keys.add(parent.keys.remove(childIdx - 1));
            leftSibling.keys.addAll(rightSibling.keys);
            
            if (!rightSibling.isLeaf) {
                leftSibling.children.addAll(rightSibling.children);
            }
            parent.children.remove(childIdx);
            newIdx = childIdx - 1;
        } else {
            // Merge with right sibling
        	BTreeNode leftSibling = child;
            BTreeNode rightSibling = parent.children.get(childIdx + 1);
            DrawBTree.AnimationMergeChildren(parent, childIdx, parent.keys.get(childIdx));
            merged&=true;
            // Merge child with right sibling
            leftSibling.keys.add(parent.keys.remove(childIdx));
            leftSibling.keys.addAll(rightSibling.keys);
            if (!rightSibling.isLeaf) {
                leftSibling.children.addAll(rightSibling.children);
            }
            parent.children.remove(childIdx + 1);
        }
        return newIdx;
    }
    
    private int findSuccessor(BTreeNode node, int key) 
    {
    	// Find the successor of a key in the subtree rooted at the given node
        // If the node is a leaf, find the successor in its keys
        if (node.isLeaf) {
            int successor = node.keys.get(0);
            for (int k : node.keys) {
                if (k > key && k < successor) {
                	successor = k;
                }
            }
            return successor;
        }
        int index = 0;
        while (index < node.keys.size() && node.keys.get(index) <= key) {
            index++;
        }
        // Recursively find the successor in the appropriate child node
        DrawBTree.animationToSuccessorOrPredecessor(node.keys.get((index>0)? index-1 :index), node.children.get(index), key);
        return findSuccessor(node.children.get(index), key);
    }

    private int findPredecessor(BTreeNode node, int key) 
    {	// Find the predecessor of a key in the subtree rooted at the given node
        // If the node is a leaf, find the predecessor in its keys
        if (node.isLeaf) {
            int predecessor = node.keys.get(0);
            for (int k : node.keys) {
            	if (k < key && k > predecessor) {
                	predecessor = k;
                }
            }
            return predecessor;
        }

        // Find the index of the key in the node's keys
        int index = 0;
        while (index < node.keys.size() && node.keys.get(index) < key) {
            index++;
        }

        // Recursively find the predecessor in the appropriate child node
        DrawBTree.animationToSuccessorOrPredecessor(node.keys.get((index>0)? index-1 :index), node.children.get(index), key);
        return findPredecessor(node.children.get(index), key);
    }
    
    public int getChildIndex(BTreeNode node, int key) {
        // Find the index of the child node where the key might be present
        int idx = 0;
        while (idx < node.keys.size() && key > node.keys.get(idx)) {
            idx++;
        }
        return idx;
    }
    
    private int decidePredecessorOrSuccessor(BTreeNode node, int key) {
    	// If the node is a leaf, return the successor
    	if (node.isLeaf) {
            return findSuccessor(node, key);
        }
        // Find the index of the child where the key might be present
        int childIdx = getChildIndex(node, key);
        // check if a Predecessor is available 
        if(node.children.get(childIdx).keys.size()>= t) 
        {
        	DrawBTree.animationToSuccessorOrPredecessor(node.keys.get(childIdx), node.children.get(childIdx), key);
        	return findPredecessor(node.children.get(childIdx), key);
        }
        // check if a Successor is available 
        if(node.children.get(childIdx).keys.size()<= t-1 && node.children.get(childIdx+1).keys.size() >=t) 
        {
        	DrawBTree.animationToSuccessorOrPredecessor(node.keys.get(childIdx), node.children.get(childIdx+1), key);
        	return findSuccessor(node.children.get(childIdx+1), key);
        }
        //Predecessor and Successor not available
        if(node.children.get(childIdx).keys.size()<= t-1 && node.children.get(childIdx+1).keys.size() <=t-1) 
        {
        	
        	return key;
        }
        return key;
    }
    
    public static boolean areKeysInSameLevel(BTreeNode root, int key1, int key2) {
        int level1 = findLevelForKey(root, key1);
        int level2 = findLevelForKey(root, key2);
        
        // If both keys are found and are at the same level, return true
        return level1 != -1 && level2 != -1 && level1 == level2;
    }

    private static int findLevelForKey(BTreeNode node, int key) {
        // Base case: If node is null, return -1
        if (node == null) {
            return -1;
        }
        
        // Check if the current node contains the key
        if (node.keys.contains(key)) {
            return 0;
        }
        
        // Recursively search in the subtrees
        for (BTreeNode child : node.children) {
            int level = findLevelForKey(child, key);
            if (level != -1) {
                return level + 1;
            }
        }
        
        // Key not found in this subtree
        return -1;
    }
    public static List<Integer> getKeysAtSameLevel(BTreeNode root, int key) {
        List<Integer> keysAtSameLevel = new ArrayList<>();
        if (root == null) {
            return keysAtSameLevel;
        }
        
        Queue<BTreeNode> queue = new LinkedList<>();
        queue.offer(root);
        
        boolean foundKey = false;
        while (!queue.isEmpty() && !foundKey) {
        	if (keysAtSameLevel.contains(key)) {
                foundKey = true; // Stop when the key is found
                break;
            }
            int size = queue.size();
            keysAtSameLevel.clear(); // Clear the list at the beginning of each level
            
            for (int i = 0; i < size; i++) {
                BTreeNode node = queue.poll();
                for (int k : node.keys) {
                    keysAtSameLevel.add(k); // Add keys at the current level
                }
                if (node.children != null) {
                    queue.addAll(node.children); // Add children to the queue for the next level
                }
                
            }
        }
        
        return keysAtSameLevel;
    }
    
    public static Map<Integer, Integer> mostrightAndmostLeft(BTreeNode root)
    {
    	Map<Integer, Integer> most = new HashMap<>();
    	int childrensize = root.children.size();
    	List<BTreeNode> children =new ArrayList<>(root.children);
    	int rl =0;
    	while(childrensize>0)
    	{
    		int mostright = children.get(childrensize-1).keys.get(children.get(childrensize-1).keys.size()-1); 
    		children= new ArrayList<>(children.get(childrensize-1).children.size());
    		rl+=1;
    		childrensize = children.size();
    		if(children.size() ==0)
    		 most.put(mostright, rl);
    	}
    	childrensize = root.children.size();
    	children =new ArrayList<>(root.children);
    	int ll =0;
    	while(childrensize>0)
    	{
    		int mostleft = children.get(0).keys.get(0); 
    		children= new ArrayList<>(children.get(0).children.size());
    		ll+=1;
    		childrensize = children.size();
    		if(children.size() ==0)
       		 most.put(mostleft, ll);
    	}
   	 return most;
    }
}
