import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 * TODO: Rename to BPlusTree
 */
public class BPlusTree<K extends Comparable<K>, T> {

	public Node<K,T> root;
	public static final int D = 2;

	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public T search(K key) {
		Node<K, T> tempNode = root;	
		while (!tempNode.isLeafNode){	
			IndexNode<K, T> tempIndexNode = (IndexNode<K, T>) tempNode;	
			int i;
			for (i = 0; i < tempIndexNode.keys.size(); i++) {
                if (key.compareTo(tempIndexNode.keys.get(i)) < 0) {
                    break;
                }
            }
			tempNode = tempIndexNode.children.get(i);
		}
		
		LeafNode<K, T> leafNode = (LeafNode<K, T>) tempNode;
		for (int i = 0; i < leafNode.keys.size(); i++)
			if (leafNode.keys.get(i).compareTo(key) == 0) 
				return leafNode.values.get(i);
		
		return null;
	}
	
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) {
		if (root == null){
			LeafNode<K, T> insertedRoot = new LeafNode<K, T>(key, value);
			root = insertedRoot;
			return;
		}else{
			
			List<Node<K, T>> path = new ArrayList<Node<K, T>>();
			Node<K, T> tempNode = root;
			
			while (!tempNode.isLeafNode){
				path.add(tempNode);
				
				IndexNode<K, T> tempIndexNode = (IndexNode<K, T>) tempNode;	
				int i;
				for (i = 0; i < tempIndexNode.keys.size(); i++) {
	                if (key.compareTo(tempIndexNode.keys.get(i)) < 0) {
	                    break;
	                }
	            }
				tempNode = tempIndexNode.children.get(i);
			}
			
							
			Entry<K, Node<K,T>> newchildentry = new AbstractMap.SimpleEntry<K, Node<K,T>>(null, null);				
			LeafNode<K, T> tempLeafNode = (LeafNode<K, T>) tempNode;
		
			if (!(tempLeafNode.keys.size() > 2 * BPlusTree.D - 1)){
				tempLeafNode.insertSorted(key, value);
				newchildentry = null;
			}else{
				newchildentry  = splitLeafNode(tempLeafNode, key, value);
			}
			
			
			while (newchildentry != null && !path.isEmpty()){
				IndexNode<K, T> tempIndexNode = (IndexNode<K, T>) path.remove(path.size() - 1);
			
				if (!(tempIndexNode.keys.size() > 2 * BPlusTree.D - 1)){
					int i;
					for (i = 0; i < tempIndexNode.keys.size(); i++) {
		                if (newchildentry.getKey().compareTo(tempIndexNode.keys.get(i)) < 0) {
		                    break;
		                }
		            }
					tempIndexNode.insertSorted(newchildentry, i);
		
					newchildentry = null;
			    }else{
					newchildentry  = splitIndexNode(tempIndexNode, newchildentry);					
				}
			}
		}
    }
	

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf, K key, T value) {
		boolean splitRoot = leaf == root ? true : false;
		
		leaf.insertSorted(key, value);
		
		List<K> newKeys = new ArrayList<K>();
		List<T> newValues = new ArrayList<T>();
		
		int count = 1;
		while (count <= D + 1){
			newKeys.add(leaf.keys.get(D));
			newValues.add(leaf.values.get(D));
			leaf.keys.remove(D);
			leaf.values.remove(D);
			count++;
		}		
	
		LeafNode<K, T> newLeaf = new LeafNode<K, T>(newKeys, newValues);
		newLeaf.nextLeaf = leaf.nextLeaf;
		if (newLeaf.nextLeaf != null) newLeaf.nextLeaf.previousLeaf = newLeaf;
		leaf.nextLeaf = newLeaf;
		newLeaf.previousLeaf = leaf;
		
		Entry<K, Node<K,T>> newEntry = new AbstractMap.SimpleEntry<K, Node<K,T>>(newLeaf.keys.get(0), newLeaf);
		
		if (splitRoot){
			IndexNode<K, T> newRoot = new IndexNode<K, T>(newEntry.getKey(), leaf, newLeaf);
			root = newRoot;
			newEntry = null;
		}
		
		return newEntry;
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index, Entry<K, Node<K,T>> newchildentry) {
		boolean splitRoot = index == root ? true : false;

		int i;
		for (i = 0; i < index.keys.size(); i++) {
            if (newchildentry.getKey().compareTo(index.keys.get(i)) < 0) {
                break;
            }
        }
		index.insertSorted(newchildentry, i);
		
		K newKey  = index.keys.get(D);
		List<K> newKeys = new ArrayList<K>();
		List<Node<K,T>> newChildren = new ArrayList<Node<K,T>>();
		
		index.keys.remove(D);	
		int count = 1;
		while (count <= D){
			newKeys.add(index.keys.get(D));
			newChildren.add(index.children.get(D + 1));
			index.keys.remove(D);
			index.children.remove(D + 1);
			
			count++;
		}
		newChildren.add(index.children.get(D + 1));
		index.children.remove(D + 1);
		
		IndexNode<K, T> newIndex = new IndexNode<K, T>(newKeys, newChildren);	
		
		Entry<K, Node<K,T>> newEntry = new AbstractMap.SimpleEntry<K, Node<K,T>>(newKey, newIndex);
		
		if (splitRoot){
			IndexNode<K, T> newRoot = new IndexNode<K, T>(newEntry.getKey(), index, newIndex);
			root = newRoot;
			newEntry = null;
		}
		
		return newEntry;	
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {
		if (root.isLeafNode){
			LeafNode<K, T> leafRoot = (LeafNode<K, T>) root;
			int keyIndex = leafRoot.keys.indexOf(key);
			leafRoot.values.remove(keyIndex);
			leafRoot.keys.remove(keyIndex);
		}else{
			List<Node<K, T>> path = new ArrayList<Node<K, T>>();
			List<Integer> indexInParentPath = new ArrayList<Integer>();
			Node<K, T> tempNode = root;
				
			while (!tempNode.isLeafNode){
				path.add(tempNode);
				
				IndexNode<K, T> tempIndexNode = (IndexNode<K, T>) tempNode;	
				int i;
				for (i = 0; i < tempIndexNode.keys.size(); i++) {
	                if (key.compareTo(tempIndexNode.keys.get(i)) < 0) {
	                    break;
	                }
	            }
				indexInParentPath.add(i); // ith in parent's children - ArrayList
				tempNode = tempIndexNode.children.get(i);
			}
			
			LeafNode<K, T> tempLeafNode = (LeafNode<K, T>) tempNode;
			int indexInParent = indexInParentPath.get(indexInParentPath.size() - 1);	
			
			int keyIndex = tempLeafNode.keys.indexOf(key);
			tempLeafNode.values.remove(keyIndex);
			tempLeafNode.keys.remove(keyIndex);
			
			if (!tempLeafNode.isUnderflowed()){//tempLeafNode is not underflowed after delete
				indexInParent = -1;
			}else{//tempLeafNode is underflowed(<d) after delete
				IndexNode<K,T> parent = (IndexNode<K,T>) path.get(path.size() - 1);
				
				if (indexInParent == 0){
					indexInParent = handleLeafNodeUnderflow(1, tempLeafNode, tempLeafNode.nextLeaf, 
							                                parent, indexInParentPath);
				}else{
					indexInParent = handleLeafNodeUnderflow(0, tempLeafNode.previousLeaf, tempLeafNode, 
							                                parent, indexInParentPath);
				}	
			}
			
			while (indexInParent != -1 && !path.isEmpty()){
				IndexNode<K, T> tempIndexNode = (IndexNode<K, T>) path.remove(path.size() - 1);
				indexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				
				if (!tempIndexNode.isUnderflowed()){//tempIndexNode is not underflowed after delete
					indexInParent = -1;
			    }else{//tempIndexNode is underflowed after delete
			    	IndexNode<K,T> parent = (IndexNode<K,T>) path.get(path.size() - 1);
					
					if (indexInParent == 0){
						indexInParent = handleIndexNodeUnderflow(1, tempIndexNode, 
					                    (IndexNode<K,T>) parent.children.get(indexInParent + 1), 
					                    parent, indexInParentPath);
					}else{
						indexInParent = handleIndexNodeUnderflow(0,  
								        (IndexNode<K,T>) parent.children.get(indexInParent - 1), 
								        tempIndexNode, parent, indexInParentPath);
					}					
				}
			}
		}
	}

	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(int sibling, LeafNode<K,T> leftLeaf, LeafNode<K,T> rightLeaf,
			                           IndexNode<K,T> parent, List<Integer> indexInParentPath) {
		if (sibling == 1){//rightleaf is sibling
			if (rightLeaf.keys.size() >= BPlusTree.D + 1){//redistribute
				int count = 1;		
				while (count <= (rightLeaf.keys.size() - BPlusTree.D + 1) / 2){
					leftLeaf.keys.add(rightLeaf.keys.get(0));
					leftLeaf.values.add(rightLeaf.values.get(0));
					rightLeaf.keys.remove(0);
					rightLeaf.values.remove(0);
					count++;
				}
				
				int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				parent.keys.set(leftIndexInParent, rightLeaf.keys.get(0));
				
				return -1;
			}else{//merge
				rightLeaf.previousLeaf = leftLeaf.previousLeaf;
				if (leftLeaf.previousLeaf != null) leftLeaf.previousLeaf.nextLeaf = rightLeaf;
				
				for (int i = leftLeaf.keys.size() - 1; i >= 0; i--){
					rightLeaf.keys.add(0, leftLeaf.keys.get(i));
					rightLeaf.values.add(0, leftLeaf.values.get(i));
				}
				
				int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				parent.children.remove(leftIndexInParent);
				parent.keys.remove(leftIndexInParent);
				
				if (parent.keys.size() == 0){
					root = rightLeaf;
					return -1;
				}
				
				indexInParentPath.remove(indexInParentPath.size() - 1);
				if (indexInParentPath.size() == 0){
					return -1;
				}else{
					return indexInParentPath.get(indexInParentPath.size() - 1);
				}
			}
		}else{//leftleaf is sibling
            if (leftLeaf.keys.size() >= BPlusTree.D + 1){//redistribute
            	int count = 1;
            	while (count <= (leftLeaf.keys.size() - BPlusTree.D + 1)
            			        - (leftLeaf.keys.size() - BPlusTree.D + 1) / 2){
            		int last = leftLeaf.keys.size() - 1;
                	rightLeaf.keys.add(0, leftLeaf.keys.get(last));
    				rightLeaf.values.add(0, leftLeaf.values.get(last));
    				leftLeaf.keys.remove(last);
    				leftLeaf.values.remove(last);
    				count++;
            	}
            	
            	int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				parent.keys.set(rightIndexInParent - 1, rightLeaf.keys.get(0));
				
				return -1;
			}else{//merge
				rightLeaf.previousLeaf = leftLeaf.previousLeaf;
				if (leftLeaf.previousLeaf != null) leftLeaf.previousLeaf.nextLeaf = rightLeaf;
				
				for (int i = leftLeaf.keys.size() - 1; i >= 0; i--){
					rightLeaf.keys.add(0, leftLeaf.keys.get(i));
					rightLeaf.values.add(0, leftLeaf.values.get(i));
				}
				
				int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				parent.children.remove(rightIndexInParent - 1);
				parent.keys.remove(rightIndexInParent - 1);
				
				if (parent.keys.size() == 0){
					root = rightLeaf;
					return -1;
				}
				
				indexInParentPath.remove(indexInParentPath.size() - 1);
				if (indexInParentPath.size() == 0){
					return -1;
				}else{
					return indexInParentPath.get(indexInParentPath.size() - 1);
				}
			
			}
		}
	}

	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(int sibling, IndexNode<K,T> leftIndex, IndexNode<K,T> rightIndex, 
			                            IndexNode<K,T> parent, List<Integer> indexInParentPath) {
		if (sibling == 1){//rightindex is sibling			
			if (rightIndex.keys.size() >= BPlusTree.D + 1){//redistribute
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				
				leftIndex.keys.add(parent.keys.get(leftIndexInParent));
				leftIndex.children.add(rightIndex.children.get(0));
				rightIndex.children.remove(0);
				
				int count = 1;
            	while (count <= (rightIndex.keys.size() - BPlusTree.D + 1) / 2 - 1){
                	leftIndex.keys.add(rightIndex.keys.get(0));
    				leftIndex.children.add(rightIndex.children.get(0));
    				rightIndex.keys.remove(0);
    				rightIndex.children.remove(0);
    				count++;
            	}
            	
				parent.keys.set(leftIndexInParent, rightIndex.keys.get(0));
				rightIndex.keys.remove(0);
				
				return -1;
			}else{//merge
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				
				rightIndex.keys.add(0, parent.keys.get(leftIndexInParent));
				rightIndex.children.add(0, leftIndex.children.get(leftIndex.children.size() - 1));
				for (int i = leftIndex.keys.size() - 1; i >= 0; i--){
					rightIndex.keys.add(0, leftIndex.keys.get(i));
					rightIndex.children.add(0, leftIndex.children.get(i));
				}
							
				parent.children.remove(leftIndexInParent);
				parent.keys.remove(leftIndexInParent);
				
				if (parent.keys.size() == 0){
					root = rightIndex;
					return -1;
				}
				
				indexInParentPath.remove(indexInParentPath.size() - 1);
				if (indexInParentPath.size() == 0){
					return -1;
				}else{
					return indexInParentPath.get(indexInParentPath.size() - 1);
				}
			}
		}else{//leftleaf is sibling
			if (leftIndex.keys.size() >= BPlusTree.D + 1){//redistribute
				int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				
				rightIndex.keys.add(0, parent.keys.get(rightIndexInParent - 1));
				rightIndex.children.add(0, leftIndex.children.get(leftIndex.children.size() - 1));
				leftIndex.children.remove(leftIndex.children.size() - 1);
				
				int count = 1;
            	while (count <= (leftIndex.keys.size() - BPlusTree.D + 1)
            			        - (leftIndex.keys.size() - BPlusTree.D + 1) / 2 - 1){
            		int last = leftIndex.keys.size() - 1;
                	rightIndex.keys.add(0, leftIndex.keys.get(last));
    				rightIndex.children.add(0, leftIndex.children.get(last));
    				leftIndex.keys.remove(last);
    				leftIndex.children.remove(last);
    				count++;
            	}
            	
				parent.keys.set(rightIndexInParent - 1, leftIndex.keys.get(leftIndex.keys.size() - 1));
				leftIndex.keys.remove(leftIndex.keys.size() - 1);
				
				return -1;
			}else{//merge
				int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
				
				rightIndex.keys.add(0, parent.keys.get(rightIndexInParent - 1));
				rightIndex.children.add(0, leftIndex.children.get(leftIndex.children.size() - 1));
				for (int i = leftIndex.keys.size() - 1; i >= 0; i--){
					rightIndex.keys.add(0, leftIndex.keys.get(i));
					rightIndex.children.add(0, leftIndex.children.get(i));
				}
				
				
				parent.children.remove(rightIndexInParent - 1);
				parent.keys.remove(rightIndexInParent - 1);
				
				if (parent.keys.size() == 0){
					root = rightIndex;
					return -1;
				}
				
				indexInParentPath.remove(indexInParentPath.size() - 1);
				if (indexInParentPath.size() == 0){
					return -1;
				}else{
					return indexInParentPath.get(indexInParentPath.size() - 1);
				}
			}
		}
	}

}
