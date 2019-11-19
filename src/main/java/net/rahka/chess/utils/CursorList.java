package net.rahka.chess.utils;

public class CursorList<T> {

	private Cursor<T> cursor;
	private Node<T> tail;
	private Node<T> head;
	private int size;

	public CursorList() {
		size = 0;

		cursor = new Cursor<>();
	}

	public void push(T t) {
		if (tail == null) {
			tail = new Node<>();
			tail.payload = t;

			cursor.current = new Node<>();
			cursor.current.next = tail;
			tail.previous = cursor.current;
		} else {
			tail.next = new Node<>();
			tail.next.payload = t;
			tail.next.previous = tail;
			tail = tail.next;
		}
		size++;
	}

	public int size() {
		return size;
	}

	public void clear() {
		tail = null;
		size = 0;
		cursor = new Cursor<>();
	}

	public Cursor<T> cursor() {
		return cursor;
	}

	public class Cursor<T> {

		private Node<T> current;

		private int index;

		public int index() {
			return index;
		}

		public void reset() {
			jump(0);
		}

		public void jump(int index) {
			if (index < 0 || index > size - 1) throw new IndexOutOfBoundsException();

			while (this.index != index) {
				if (this.index > index) {
					previous();
				} else {
					next();
				}
			}
		}

		public T current() {
			if (!hasCurrent()) throw new NullPointerException("Current is null");

			return current.payload;
		}

		public T next() {
			if (!hasNext()) throw new NullPointerException("Previous is null");

			index++;
			current = current.next;
			return current.payload;
		}

		public T previous() {
			if (!hasPrevious()) throw new NullPointerException("Previous is null");

			index--;
			current = current.previous;
			return current.payload;
		}

		public boolean hasCurrent() {
			return (current != null);
		}

		public boolean hasNext() {
			return (current != null && current.next != null);
		}

		public boolean hasPrevious() {
			return (current != null && current.previous != null);
		}

	}

	private class Node<T> {

		private T payload;
		private Node<T> next;
		private Node<T> previous;

	}

}
