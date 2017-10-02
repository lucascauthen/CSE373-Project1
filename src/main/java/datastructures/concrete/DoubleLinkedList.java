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

	@Override
	public void add(T item) {
		if (front == null) { // Add when empty
			front = back = new Node<T>(item);
		} else if (front == back) { // Add with one item
			back = new Node<T>(front, item, null);
			front.next = back;
		} else { // Add with two or more items
			Node<T> oldBack = back;
			back = new Node<T>(back, item, null);
			oldBack.next = back;
		}
		size++;
	}

	@Override
	public T remove() {
		if (back != null) {
			T item = back.data;
			if (back == front) {
				front = null;
				back = null;
			} else if (size == 2) {
				back = front;
				front.next = null;
			} else {
				back = back.prev;
				back.next = null;
			}
			size--;
			return item;
		}
		throw new EmptyContainerException();
	}

	@Override
	public T get(int index) {
		return this.getNode(index).data;
	}

	@Override
	public void set(int index, T item) {
		Node<T> cur = this.getNode(index);
		if (index == 0) { // Set the front
			front = new Node<T>(null, item, front.next);
			if (size == 1) {
				back = front;
			} else {
				front.next.prev = front;
			}
		} else if (index == size - 1) { // Set the back
			Node<T> prev = cur.prev;
			back = new Node<T>(cur.prev, item, null);
			prev.next = back;
		} else { // Set the middle
			Node<T> newNode = new Node<T>(cur.prev, item, cur.next);
			cur.prev.next = newNode;
			cur.next.prev = newNode;
		}
	}

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
			} else {
				Node<T> node = this.getNode(index);
				node.prev.next = node.next;
				node.next.prev = node.prev;
				size--;
				return node.data;
			}
		}
		throw new IndexOutOfBoundsException();
	}

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

	@Override
	public int size() {
		return size;
	}

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
