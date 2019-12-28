package net.rahka.chess.utils;

import java.util.*;

/**
 * An implementation of SortedSet using a hashset and backing linked list for keeping order. Add and remove operations
 * are O(n) time, insert and get operations are constant time.
 */
public class SortedHashSet<E> extends HashSet<E> implements SortedSet<E> {

	private LinkedList<E> sorted;
	private Comparator<E> comparator;

	public SortedHashSet(Comparator<E> comparator) {
		this.comparator = comparator;

		sorted = new LinkedList<>();
	}

	public SortedHashSet(List<E> subList, Comparator<E> comparator) {
		this(comparator);

		addAll(subList);
	}

	@Override
	public boolean add(E e) {
		var notContains = super.add(e);

		if (notContains) {
			var iterator = sorted.iterator();
			for (int i = 0; true; i++) {
				if (!iterator.hasNext()) {
					sorted.add(i, e);
					break;
				}

				var ei = iterator.next();
				if (comparator.compare(e, ei) <= 0) {
					sorted.add(i, e);
					break;
				}
			}
		}

		return notContains;
	}

	@Override
	public boolean remove(Object o) {
		sorted.remove(o);

		return super.remove(o);
	}

	@Override
	public void clear() {
		sorted.clear();

		super.clear();
	}

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return new SortedHashSet<E>(sorted.subList(sorted.indexOf(fromElement), sorted.indexOf(toElement)), comparator);
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return new SortedHashSet<E>(sorted.subList(0, sorted.indexOf(toElement)), comparator);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return new SortedHashSet<E>(sorted.subList(sorted.indexOf(fromElement), sorted.size() - 1), comparator);
	}

	@Override
	public E first() {
		return sorted.getFirst();
	}

	@Override
	public E last() {
		return sorted.getLast();
	}

}
