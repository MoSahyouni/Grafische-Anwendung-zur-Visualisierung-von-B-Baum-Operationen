package application;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.layout.Pane;

public class BSNode {
	int value;
	BSNode leftChild;
	BSNode rightChild;
	
	BSNode(int value)
	{
		this.value = value;
		rightChild = null;
		leftChild = null;
	}

	public BSNode() {
		
	}
	
    public boolean isChild(int targetValue) {
        if (value == targetValue) {
            return true;
        }
        if (leftChild != null && leftChild.isChild(targetValue)) {
            return true;
        }
        if (rightChild != null && rightChild.isChild(targetValue)) {
            return true;
        }
        return false;
    }
    
    public BSNode findParent(BSNode root) {
    	
        if (root == null || root == this) {
            return null; // The root itself has no parent or the node is the root
        }

        // Check if the node is the left or right child of the current root
        if (root.leftChild == this || root.rightChild == this) {
            return root; // Return the root as it's the parent of the given node
        }

        // Recursively search in the left and right subtrees
        BSNode parent = findParent(root.leftChild);
        if (parent == null) {
            parent = findParent(root.rightChild);
        }

        return parent;
    }	
}
class BinSTree
{	
	BSNode root;
	BinSTree(int wert)
	{
		root= new BSNode(wert);
	}
	
	public BinSTree() {
		root = null;
	}

	public void add( int value) throws InterruptedException {
		root = addNode(root, null, value);
	}
	private BSNode addNode(BSNode current, BSNode parent, int value) throws InterruptedException 
	{
		if(current == null) {
			if(parent!=null)
				DrawBinSearchTree.animationAddNewNode(parent.value, value);
			return new BSNode(value);
			}
		if (value < current.value) {
	        current.leftChild = addNode( current.leftChild, current, value);
	        if(current.leftChild != null)
	            DrawBinSearchTree.animationToChild(current.value, current.leftChild.value , value);
	        
	    } else if (value > current.value) {
	        current.rightChild = addNode( current.rightChild, current, value);
	        if(current.rightChild != null) 
	        	DrawBinSearchTree.animationToChild(current.value, current.rightChild.value , value);
	        	
	    } else {
	    	sharedMethodes.KeyalreadyInTree(value);
	        return current;
	    } 

	    return current;
	}
	
	
	public static void print(BSNode root) 
	{
		System.out.println(root.value);
		if(root.rightChild != null) print(root.rightChild);
		if(root.leftChild != null) print(root.leftChild);
	}
	private boolean containsNodeRecursive(BSNode current, int value) {
	    if (current == null) {
	        return false;
	    } 
	    if (value == current.value) {
	        return true;
	    } 
	    return value < current.value
	      ? containsNodeRecursive(current.leftChild, value)
	      : containsNodeRecursive(current.rightChild, value);
	}
	public boolean containsNode(int value) {
	    return containsNodeRecursive(root, value);
	}
	
	private BSNode deleteRecursive(BSNode current, int value, BSNode parent, boolean move) throws InterruptedException { 
		if (current == null) {
	        return current; // Value not found in the tree
	    }

	    if (value == current.value) {
	        // Node to delete found
	        if (current.leftChild != null && current.rightChild != null) {
	            // Node has two children, find the successor
	            BSNode successor = findSuccessor(current);
	            int currentval = current.value;
	            DrawBinSearchTree.move(currentval, successor.value);
	            current.rightChild = deleteRecursive(current.rightChild, successor.value, current, false);
	            current.value = successor.value;
	            return current;
	        } else if (current.leftChild != null && current.rightChild == null) {
	            // Node has only a left child, move the child up
	        	int currentval = current.value;
	            current = current.leftChild;
	            if(move)
	            	DrawBinSearchTree.move(currentval, current.value);
	            return current;
	        } else if (current.rightChild != null && current.leftChild == null) {
	            // Node has only a right child, move the child up
	        	int currentval = current.value;
	            current = current.rightChild;
	            if(move)
	            	DrawBinSearchTree.move(currentval, current.value);
	            return current;
	        } else {
	            // Node is a leaf node, delete it
	            if (parent.leftChild == current) {
	            	if(move)
	            		DrawBinSearchTree.move(current.value);
	                return null;
	            } else {
	            	if(move)
	            		DrawBinSearchTree.move(current.value);
	                return null;
	            }
	        }
	    } else if (value < current.value) {
	        // Recur to the left subtree
	    	if(move)DrawBinSearchTree.animationToChild(current.value, current.leftChild.value , value);
	        current.leftChild = deleteRecursive(current.leftChild, value, current,move);
	        return current;
	    } else {
	        // Recur to the right subtree
	    	if(move)DrawBinSearchTree.animationToChild(current.value, current.rightChild.value , value);
	    	current.rightChild = deleteRecursive(current.rightChild, value, current, move);
	        return current;
	    }
	}
	
	public BSNode delete(int value)  {
		    try {
				return root = deleteRecursive(root, value, new BSNode(), true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    return this.root;
		}
	
	private BSNode findSuccessor(BSNode current) {
	    // Find the leftmost node in the right subtree
		    BSNode successor = current.rightChild;
		    DrawBinSearchTree.animationTosuccessor(current.value, current.rightChild.value);
		    while (successor.leftChild != null) {
		    	DrawBinSearchTree.animationTosuccessor(successor.value, successor.leftChild.value);
		        successor = successor.leftChild;
		    }
		    return successor;
	}
	
	public int treeNodeCount(BSNode node)
	{
		if(node.leftChild!= null)
			return 1+ treeNodeCount(node.leftChild); 
		else {
			if(node.rightChild != null)
				return 1+ treeNodeCount(node.rightChild);
			else
				return 1;}
	}
	public static List<Integer> getChildrenValues(BSNode node) {
        List<Integer> childrenValues = new ArrayList<>();

        if (node.leftChild != null) {
            childrenValues.add(node.leftChild.value);
        } else {
            childrenValues.add(null); // Indicate no left child
        }

        if (node.rightChild != null) {
            childrenValues.add(node.rightChild.value);
        } else {
            childrenValues.add(null); // Indicate no right child
        }

        return childrenValues;
    }
	
	public void printTree(BSNode node) {
        if (node != null) {
            // print left Subtree
            printTree(node.leftChild);
            System.out.print(node.value + " ");
            // right Subtree
            printTree(node.rightChild);
        }
    }
    
    public void printTree() {
    	// start the printTree(root) methode
        printTree(root);
        System.out.println(); 
    }
	
    public static BSNode findLowestCommonAncestor(BSNode root, BSNode node1, BSNode node2) {
        if (root == null || node1 == null || node2 == null) {
            return null; // Invalid input
        }

        // Check if one of the nodes is the root
        if (root == node1 || root == node2) {
            return root;
        }

        // Check if node1 and node2 are in separate subtrees
        boolean isNode1InLeft = isNodeInSubtree(root.leftChild, node1);
        boolean isNode2InLeft = isNodeInSubtree(root.leftChild, node2);

        // If node1 and node2 are in separate subtrees, root is the lowest common ancestor
        if (isNode1InLeft != isNode2InLeft) {
            return root;
        }

        // Recursively search for the lowest common ancestor in the appropriate subtree
        BSNode subtreeRoot = isNode1InLeft ? root.leftChild : root.rightChild;
        return findLowestCommonAncestor(subtreeRoot, node1, node2);
    }

    private static boolean isNodeInSubtree(BSNode root, BSNode target) {
        if (root == null) {
            return false;
        }
        if (root == target) {
            return true;
        }
        return isNodeInSubtree(root.leftChild, target) || isNodeInSubtree(root.rightChild, target);
    }
    
    public static boolean search(BSNode node, int value) {
        // Base case: If the node is null, the value is not found
        if (node == null) {
            return false;
        }

        // If the value is found at the current node, return true
        if (node.value == value) {
            return true;
        }

        // Recursively search in the left subtree if the value is less than the current node's value
        if (value < node.value) {
            return search(node.leftChild, value);
        }
        // Recursively search in the right subtree if the value is greater than the current node's value
        else {
            return search(node.rightChild, value);
        }
    }
	
}