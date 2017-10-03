/*
 * Lucas Cauthen - Duke Fu
 * CSE 373 - Project 1
 * DoubleLinkedList: Implementation of IList that's backing data structure is a Doublely Linked List
 */

package datastructures.concrete;

import datastructures.interfaces.IList;
import misc.exceptions.EmptyContainerException;
import misc.exceptions.NotYetImplementedException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Note: For more info on the expected behavior of your methods, see the source
 * code for IList.
 */
public class DoubleLinkedList<T> implements IList<T> {
	// You may not rename these fields or change their types.
	// We will be inspecting these in our private tests.
	// You also may not add any additional fields.
	private Node<T> front;
	private Node<T> back;
	private int size;

	public DoubleLinkedList() {
		this.front = null;
		this.back = null;
		this.size = 0;
	}

	//Add a new node to the end of the list
	@Override
	public void add(T item) {
		if (front == null) { // Add when empty
			front = back = new Node<T>(item);
		} else if (front == back) { // Add with one item in list
			back = new Node<T>(front, item, null);
			front.next = back;
		} else { // Add with two or more items in list
			Node<T> oldBack = back;
			back = new Node<T>(back, item, null);
			oldBack.next = back;
		}
		size++;
	}

	//Remove the last item in the list
	@Override
	public T remove() {
		if (back != null) {
			T item = back.data;
			if (size == 1) { //Remove item from size 1 list
				front = null;
				back = null;
			} else if (size == 2) { //Remove item from size 2 list
				back = front;
				front.next = null;
			} else { //remove item from size 3 or more list
				back = back.prev;
				back.next = null;
			}
			size--;
			return item;
		}
		throw new EmptyContainerException();
	}

	//Returns the item at the given index
	//Throws IndexOutOfBoundsException if the given index is not in the list
	@Override
	public T get(int index) {
		return this.getNode(index).data;
	}

	//Sets the value of the item at a given index to a new value
	//Throws IndexOutOfBoundsException if the given index is not in the list
	@Override
	public void set(int index, T item) {
		Node<T> cur = this.getNode(index);
		if (index == 0) { // Set item in the front
			front = new Node<T>(null, item, front.next);
			if (size == 1) {
				back = front;
			} else {
				front.next.prev = front;
			}
		} else if (index == size - 1) { // Set item in the back
			Node<T> prev = cur.prev;
			back = new Node<T>(cur.prev, item, null);
			prev.next = back;
		} else { // Set item in the middle
			Node<T> newNode = new Node<T>(cur.prev, item, cur.next);
			cur.prev.next = newNode;
			cur.next.prev = newNode;
		}
	}

	//Inserts an item into the list at the given index, subsequent items in the list have their indexes shifted by one
	//Throws IndexOutOfBoundsException if the given index is not between 0 and the size of the list
	@Override
	public void insert(int index, T item) {
		if (index == size) { // Insert at end
			this.add(item);
		} else if (index == 0) { // Insert at front
			Node<T> newNode = new Node<T>(null, item, front);
			if (size == 1) {
				front.prev = newNode;
				front = newNode;
			} else {
				front = newNode;
				front.next.prev = newNode;
			}
			size++;
		} else { // Insert in middle
			Node<T> cur = this.getNode(index);
			Node<T> newNode = new Node<T>(cur.prev, item, cur);
			cur.prev.next = newNode;
			cur.prev = newNode;
			size++;
		}
	}

	//Removes the item at the given index
	//Throws IndexOutOfBoundsException if the given index is not in the list
	@Override
	public T delete(int index) {
		if (size > 0) {
			if (index == (size - 1)) { //If the list only has one item
				return this.remove();
			} else if (index == 0) { //If the list has more than one item, but we are still deleting from the beginning
				front.next.prev = null;
				T dataToReturn = front.data;
				front = front.next;
				size--;
				return dataToReturn;				
			} else { //Deleting from the middle
				Node<T> node = this.getNode(index);
				node.prev.next = node.next;
				node.next.prev = node.prev;
				size--;
				return node.data;
			}
		}
		throw new IndexOutOfBoundsException();
	}

	//Returns the index of a given item in the list or -1 if the item is not in the list
	@Override
	public int indexOf(T item) {
		int index = 0;
		Node<T> cur = front;
		while (cur != null) {
			if ((cur.data != null && cur.data.equals(item)) || cur.data == item) {
				return index;
			}
			cur = cur.next;
			index++;
		}
		return -1;
	}

	//Return the size of the list
	@Override
	public int size() {
		return size;
	}

	//Return true if a given item is in the list and false otherwise
	@Override
	public boolean contains(T other) {
		return this.indexOf(other) != -1;
	}

	@Override
	public Iterator<T> iterator() {
		// Note: we have provided a part of the implementation of
		// an iterator for you. You should complete the methods stubs
		// in the DoubleLinkedListIterator inner class at the bottom
		// of this file. You do not need to change this method.
		return new DoubleLinkedListIterator<>(this.front);
	}

	//Helper method that gets a node at a given index
	//Throws IndexOutOfBoundsException if the given index is not in the list
	private Node<T> getNode(int index) {
		if (index >= 0 && index < size) {
			if (index < size / 2) { // If we are near the first half of elements start from the front
				int i = 0;
				Node<T> cur = front;
				while (i < index) {
					cur = cur.next;
					i++;
				}
				return cur;
			} else { // If we are near the last half of elements, start from the back
				int i = this.size - 1;
				Node<T> cur = back;
				while (i > index) {
					cur = cur.prev;
					i--;
				}
				return cur;
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	private static class Node<E> {
		// You may not change the fields in this node or add any new fields.
		public final E data;
		public Node<E> prev;
		public Node<E> next;

		public Node(Node<E> prev, E data, Node<E> next) {
			this.data = data;
			this.prev = prev;
			this.next = next;
		}

		public Node(E data) {
			this(null, data, null);
		}

		// Feel free to add additional constructors or methods to this class.
	}

	private static class DoubleLinkedListIterator<T> implements Iterator<T> {
		// You should not need to change this field, or add any new fields.
		private Node<T> current;

		public DoubleLinkedListIterator(Node<T> current) {
			// You do not need to make any changes to this constructor.
			this.current = current;
		}

		/**
		 * Returns 'true' if the iterator still has elements to look at; returns 'false'
		 * otherwise.
		 */
		public boolean hasNext() {
			return current != null;
		}

		/**
		 * Returns the next item in the iteration and internally updates the iterator to
		 * advance one element forward.
		 *
		 * @throws NoSuchElementException
		 *             if we have reached the end of the iteration and there are no more
		 *             elements to look at.
		 */
		public T next() {
			if (this.hasNext()) {
				T data = current.data;
				current = current.next;
				return data;
			} else {
				throw new NoSuchElementException();
			}
		}
	}
}
