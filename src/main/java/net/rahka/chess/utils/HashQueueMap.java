package net.rahka.chess.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class HashQueueMap<K, V> {

	private HashMap<K, Queue<V>> map = new HashMap<>();

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public V poll(K key) {
		var queue = map.get(key);
		V value = null;
		if (queue != null) {
			if (!queue.isEmpty()) {
				value = queue.poll();

				if (queue.isEmpty()) {
					map.remove(key);
				}
			}
		}
		return value;
	}

	public V peek(K key) {
		var queue = map.get(key);
		if (queue != null) {
			if (!queue.isEmpty()) {
				return queue.peek();
			}
		}
		return null;
	}

	public V push(K key, V value) {
		var queue = map.get(key);
		if (queue != null) {
			queue.add(value);
		} else {
			queue = new LinkedList<>();
			queue.add(value);
			map.put(key, queue);
		}
		return null;
	}

	public void clear() {
		map = new HashMap<>();
	}

	public Iterator<V> headIterator() {
		var entries = map.entrySet().iterator();

		return new Iterator<>() {

			private Queue<V> queue;

			@Override
			public boolean hasNext() {
				return entries.hasNext();
			}

			@Override
			public V next() {
				queue = entries.next().getValue();
				return queue.peek();
			}

			@Override
			public void remove() {
				queue.poll();

				if (queue.isEmpty()) {
					entries.remove();
				}
			}

		};
	}

}
