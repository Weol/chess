package net.rahka.chess.utils;

public class StateCursorList {

	private Cursor cursor;
	private Node tail;
	private Node head;
	private int size;

	public StateCursorList() {
		size = 0;

		cursor = new Cursor();
	}

	public void push(long[] t) {
		if (tail == null) {
			tail = new Node();
			tail.payload = t;

			cursor.current = tail;
			head = tail;
		} else {
			tail.next = new Node();
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
		cursor = new Cursor();
	}

	public Cursor cursor() {
		return cursor;
	}

	public class Cursor {

		private Node current;

		private int index;

		public int index() {
			return index;
		}

		public void reset() {
			index = 0;
			current = head;
		}

		public long[] current() {
			if (!hasCurrent()) throw new NullPointerException("Current is null");

			return current.payload;
		}

		public long[] next() {
			if (!hasNext()) throw new NullPointerException("Previous is null");

			index++;
			current = current.next;
			return current.payload;
		}

		public long[] previous() {
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

	private static class Node {

		private long[] payload;
		private Node next;
		private Node previous;

	}

}
