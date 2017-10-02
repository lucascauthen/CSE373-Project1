package datastructures.concrete.dictionaries;

import datastructures.interfaces.IDictionary;
import misc.exceptions.NoSuchKeyException;
import misc.exceptions.NotYetImplementedException;

/**
 * See IDictionary for more details on what this class should do
 */
public class ArrayDictionary<K, V> implements IDictionary<K, V> {
    // You may not change or rename this field: we will be inspecting
    // it using our private tests.
    private Pair<K, V>[] pairs;

    // You're encouraged to add extra fields (and helper methods) though!
    private int size;
    private int arraySize;

    public ArrayDictionary() {
        size = 0;
        arraySize = 10;
        pairs = makeArrayOfPairs(arraySize);
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain Pair<K, V> objects.
     *
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private Pair<K, V>[] makeArrayOfPairs(int arraySize) {
        // It turns out that creating arrays of generic objects in Java
        // is complicated due to something known as 'type erasure'.
        //
        // We've given you this helper method to help simplify this part of
        // your assignment. Use this helper method as appropriate when
        // implementing the rest of this class.
        //
        // You are not required to understand how this method works, what
        // type erasure is, or how arrays and generics interact. Do not
        // modify this method in any way.
        return (Pair<K, V>[]) (new Pair[arraySize]);

    }

    @Override
    public V get(K key) {
        throw new NotYetImplementedException();
    }

    @Override
    public void put(K key, V value) {
        if(size < arraySize) {
        		pairs[size] = new Pair<>(key, value);
        		size++;
        } else { //Need to resize and copy everything over to the new array
        		arraySize *= 2;
        		Pair<K, V>[] newArray = makeArrayOfPairs(arraySize);
        		for(int i = 0; i < this.size; i++) {
        			newArray[i] = pairs[i];
        		}
        		pairs = newArray;
        		this.put(key, value); //
        }
    }

    @Override
    public V remove(K key) {
        
    }

    @Override
    public boolean containsKey(K key) {
        throw new NotYetImplementedException();
    }

    @Override
    public int size() {
        throw new NotYetImplementedException();
    }
    
    /*
     * Returns the index of a given key
     * Returns -1 if there is no pair with the given key
     */
    private int indexOf(K key) {
    		for(int i = 0; i < size; i++) {
    			if(pairs[i].key.equals(key)) {
    				return -1;
    			}
    		}
    		return -1;
    }

    private static class Pair<K, V> {
        public K key;
        public V value;

        // You may add constructors and methods to this class as necessary.
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }
    }
}
