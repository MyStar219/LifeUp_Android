package ru.johnlife.lifetools.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 4/1/2016.
 */
public abstract class BaseAdapter<T extends AbstractData> extends RecyclerView.Adapter implements Iterable<T> {
    public static abstract class ViewHolder<T> extends RecyclerView.ViewHolder {
        private T item = null;
        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract class ItemizedClickListener implements View.OnClickListener {
            private T item;

            public T getItem() {
                return item;
            }

            public void setItem(T item) {
                this.item = item;
            }

        }

        protected void assign(T item) {
            this.item = item;
            hold(item);
        }

        protected abstract void hold(T item);

        public T getItem() {
            return item;
        }
    }


    private List<T> items;
    private int itemLayoutId;

    public BaseAdapter(int itemLayoutId, List<T> items) {
        this.itemLayoutId = itemLayoutId;
        this.items = items;
    }

    public BaseAdapter(int itemLayoutId) {
        this(itemLayoutId, new ArrayList<T>());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = (0 == viewType) ? itemLayoutId : viewType;
        return createViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder<T> holder = (ViewHolder<T>) viewHolder;
        holder.assign(items.get(position));
    }

    protected abstract ViewHolder<T> createViewHolder(final View view);

    @Override
    public int getItemCount() {
        return null == items ? 0 : items.size();
    }

    public int getCount() {
        return getItemCount();
    }

    public T getItem(int position) {
        if (null == items) throw new IllegalStateException("Internal collection is empty");
        if (position < 0 || position >= items.size()) throw new IllegalArgumentException("size is "+items.size()+", requested position is "+position);
        return items.get(position);
    }

    public T remove(int position) {
        if (null == items) throw new IllegalStateException("Internal collection is empty");
        if (position < 0 || position >= items.size()) throw new IllegalArgumentException("size is "+items.size()+", requested position is "+position);
        T item = items.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void add(T item) {
        add(item, items.size());
    }

    public void add(T item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(T item) {
        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void replace(T item) {
        replace(item, items.indexOf(item));
    }

    public void replace(T item, int position) {
        items.set(position, item);
        notifyItemChanged(position);
    }

    protected int indexOf(T item) {
        return items.indexOf(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> collection) {
        for (T item : collection) { //just to trigger proper animation
            add(item);
        }
    }
    @Override
    public Iterator<T> iterator() {
        return null == items ? null : items.iterator();
    }
}
