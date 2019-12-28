package net.rahka.chess.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor
public class NullFilteredIterator<T> implements Iterator<T> {

    @NonNull
    private T[] elements;
    private int index = 0;

    @Override
    public boolean hasNext() {
        return (index < elements.length && elements[index] != null);
    }

    @Override
    public T next() {
        return elements[index++];
    }

}
