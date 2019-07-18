package com.pcauto.bktree;

import java.util.*;

public class BKTree<T> extends AbstractSet<T> {

    private final DistanceFunction<T> distanceFunction;
    private Node<T> rootNode;
    private int length;
    private int modCount; // Modification count for fail-fast iterator.

    /**
     * @param distanceFunction A function that computes the distance between two objects of type T.
     */
    public BKTree(DistanceFunction<T> distanceFunction) {
        if (distanceFunction == null)
            throw new NullPointerException("distanceFunction cannot be null.");

        this.distanceFunction = distanceFunction;
        length = 0;
        modCount = 0;
    }

    /**
     * Search for items in the tree that are within a maximum distance from t.
     * @param t The item to find approximate matches of from the tree.
     * @param radius The maximum distance from t.
     * @return An immutable list of objects, sorted by distance, that approximately match t.
     */
    public List<SearchResult<T>> search(T t, int radius) {
        if (t == null)
            return Collections.emptyList();
        ArrayList<SearchResult<T>> searchResults = new ArrayList<>();
        ArrayDeque<Node<T>> nextNodes = new ArrayDeque<>();
        if (rootNode != null)
            nextNodes.add(rootNode);

        while(!nextNodes.isEmpty()) {
            Node<T> nextNode = nextNodes.poll();
            int distance = distanceFunction.distance(nextNode.item, t);
            if (distance <= radius)
                searchResults.add(new SearchResult<>(distance, nextNode.item));
            int lowBound = Math.max(0, distance - radius), highBound = distance + radius;
            for (Integer i = lowBound; i <= highBound; i++) {
                if (nextNode.children.containsKey(i))
                    nextNodes.add(nextNode.children.get(i));
            }
        }

        searchResults.trimToSize();
        Collections.sort(searchResults);
        return Collections.unmodifiableList(searchResults);
    }

    /**
     * Add an item to the tree.
     * @param t A non-null, searchable object to add to the tree.
     * @return If t did not already exist in the tree.
     */
    @Override
    public boolean add(T t) {
        if (t == null)
            throw new NullPointerException();

        if (rootNode == null) {
            rootNode = new Node<>(t);
            length = 1;
            modCount++; // Modified tree by adding root.
            return true;
        }

        Node<T> parentNode = rootNode;
        Integer distance;
        while ((distance = distanceFunction.distance(parentNode.item, t)) != 0
                || !t.equals(parentNode.item)) {
            Node<T> childNode = parentNode.children.get(distance);
            if (childNode == null) {
                parentNode.children.put(distance, new Node<>(t));
                length++;
                modCount++; // Modified tree by adding a child.
                return true;
            }
            parentNode = childNode;
        }

        return false;
    }

    /**
     * Check if the given object is in the tree.
     * @param o An object that is potentially in the tree.
     * @return If the tree contains object o.
     */
    @Override
    public boolean contains(Object o) {
        if (o == null || rootNode == null)
            return false;
        try {
            @SuppressWarnings("unchecked")
            List<SearchResult<T>> searchList = search((T) o, 0); // Find objects exactly matching o.
            // Search all results of distance 0 to find equal object.
            for (SearchResult<T> result : searchList) {
                if (result.getItem().equals(o))
                    return true;
            }
            return false;
        } catch (ClassCastException e) { // If o is not an instance of T, return false.
            return false;
        }
    }

    /**
     * Remove an object from the tree.
     * @param o An object that is potentially in the tree.
     * @return If an item was removed.
     */
    @Override
    public boolean remove(Object o) {
        if (o == null || rootNode == null)
            return false;
        if (rootNode.item.equals(o)) {
            length--;
            rootNode = replaceNode(rootNode);
            return true;
        }
        try {
            @SuppressWarnings("unchecked")
            T t = (T)o;

            // Repeat until no matching child is found.
            for (Node<T> parentNode = rootNode, childNode; parentNode != null; parentNode = childNode) {
                int distance = distanceFunction.distance(parentNode.item, t);
                childNode = parentNode.children.get(distance);

                if (childNode != null && childNode.item.equals(t)) { // If a matching child is found, remove the child.
                    length--;
                    childNode = replaceNode(parentNode.children.remove(distance));
                    if (childNode != null) // If there are descendants, add its replacement to the parent.
                        parentNode.children.put(distance, childNode);
                    return true;
                }
            }

            return false; // No items to remove.
        } catch (ClassCastException e) { // If o is not an instance of T, return false.
            return false;
        }
    }

    /**
     * Replace a node with its best matching child, and make the other children the new node's descendants.
     * @param oldNode The original node being replaced.
     * @return The replacement node, or null if oldNode has no children.
     */
    private Node<T> replaceNode(Node<T> oldNode) {
        modCount++; // Will modify tree when node is replaced.
        Iterator<Node<T>> entries = oldNode.children.values().iterator();
        if (!entries.hasNext()) // if the old node has no children, replace with null.
            return null;
        Node<T> newNode = entries.next(); // Otherwise, replace with the closest matching child.
        while(entries.hasNext()) { // Make other children descendants of new node.
            Node<T> valNode = entries.next();

            for (Node<T> parentNode = newNode, childNode; ; parentNode = childNode) {
                Integer distance = distanceFunction.distance(parentNode.item, valNode.item);
                childNode = parentNode.children.get(distance);
                if (childNode == null) { // If the child node isn't taken for distance...
                    parentNode.children.put(distance, valNode); // Make valNode the new child node of parentNode.
                    break;
                }
            } // Continue until valNode finds new parent.
        }
        return newNode;
    }

    /**
     * Iterate though the tree in breadth first order.
     * @return An iterator for the tree.
     */
    @Override
    public Iterator<T> iterator() {
        if (length == 0)
            return Collections.emptyIterator();
        final ArrayDeque<Node<T>> nextNodes = new ArrayDeque<>();
        nextNodes.add(rootNode);
        return new Iterator<T>() {

            private Node<T> lastNode;
            private int itModCount = modCount;

            @Override
            public boolean hasNext() {
                return !nextNodes.isEmpty();
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                ensureNotModified(); // Fail if modified outside remove.
                lastNode = nextNodes.poll();
                nextNodes.addAll(lastNode.children.values());
                return lastNode.item;
            }

            @Override
            public void remove() {
                if (lastNode == null)
                    throw new IllegalStateException(); // Cannot remove what hasn't been visited.
                ensureNotModified();
                if (!lastNode.children.isEmpty()) {
                    Node<T> replacementNode = nextNodes.removeLast();
                    for (int i = lastNode.children.size(); i > 1; i--) // Remove all but first child.
                        replacementNode = nextNodes.removeLast();
                    nextNodes.addFirst(replacementNode); // Replace parent in deque with first child.
                }
                BKTree.this.remove(lastNode.item);
                lastNode = null;
                itModCount = modCount;
            }

            // Fail if the tree has been modified outside iterator.
            private void ensureNotModified() {
                if (itModCount != modCount)
                    throw new ConcurrentModificationException();
            }
        };
    }

    /**
     * @return The number of items in the tree.
     */
    @Override
    public int size() {
        return length;
    }

    /**
     * A result of the search method in BKTree, containing the item found and its distance from the query.
     * @param <T> The type the BKTree it came from represents.
     */
    public static class SearchResult<T> implements Comparable<SearchResult<T>> {
        private final int distance;
        private final T item;

        public SearchResult(int distance, T item) {
            this.distance = distance;
            this.item = item;
        }

        /**
         * @return The distance from the object queried.
         */
        public int getDistance() {
            return distance;
        }

        /**
         * @return The item found from the query.
         */
        public T getItem() {
            return item;
        }

        /**
         * Comparison method to sort by distance with.
         * @see Comparable
         */
        @Override
        public int compareTo(SearchResult<T> o) {
            if (o == null) // null first.
                return 1;
            return Integer.compare(distance, o.distance);
        }
    }

    // A tree node with multiple children ordered by distance.
    private static class Node<T> {
        public final T item;
        public final Map<Integer, Node<T>> children;

        public Node(T item) {
            this.item = item;
            this.children = new TreeMap<>();
        }
    }
}
