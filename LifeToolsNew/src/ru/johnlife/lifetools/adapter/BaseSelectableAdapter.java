package ru.johnlife.lifetools.adapter;

import android.view.View;

import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/23/2016.
 */
public abstract class BaseSelectableAdapter<T extends AbstractData> extends BaseAdapter<T> {
    private T selected;

    public abstract class ViewHolderSelectable extends BaseAdapter.ViewHolder<T> {
        public ViewHolderSelectable(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    T oldSelected = selected;
                    selected = getItem();
                    if (oldSelected != null) {
                        notifyItemChanged(indexOf(oldSelected));
                    }
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        protected boolean isSelected() {
            return getItem() == selected;
        }
    }


    public BaseSelectableAdapter(int itemLayoutId) {
        super(itemLayoutId);
    }

    public BaseSelectableAdapter(int itemLayoutId, List<T> items) {
        super(itemLayoutId, items);
    }

    protected boolean isSelected(T item) {
        return selected == item;
    }

}
