package ru.johnlife.lifetools.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.SparseArray;

public class SparseList<E> extends SparseArray<E> implements Iterable<E> {
	
	private List<E> list;

	public SparseList() {
		super();
	}

	public SparseList(int initialCapacity) {
		super(initialCapacity);
	}
	
	public List<E> asList() {
		int size = size();
		if (list == null) {
			list = new ArrayList<E>(size);
		} else {
			list.clear();
		}
		for (int i=0; i<size; i++) {
			list.add(valueAt(i));
		}
		return list;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public E next() {
				return valueAt(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
