package ru.johnlife.lifetools.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;

public abstract class BaseListFragment<T extends AbstractData> extends BaseAbstractFragment {

    private Context context;

    private RecyclerView list;
    private ImageView dash_imgv;
    private BaseAdapter<T> adapter;
    private LinearLayoutManager linearLayoutManager;

    protected abstract BaseAdapter<T> instantiateAdapter(Context context);

    protected int getLayoutId() {

        return R.layout.newfragmentlist;
    }

    public BaseAdapter<T> getAdapter() {
        return adapter;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        ViewGroup view = (ViewGroup) inflater.inflate(getLayoutId(), container, true);
        list = (RecyclerView) view.findViewById(getListId());
       /// dash_imgv = (ImageView) view.findViewById(getDashboardId());

        if (null != list) {
            list.setHasFixedSize(true);
            list.setFocusable(false);
            linearLayoutManager = new LinearLayoutManager(list.getContext());
            list.setLayoutManager(getListLayoutManager());
            adapter = instantiateAdapter(context);
            list.setAdapter(adapter);
        }


	/*	dash_imgv.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {


				Toast.makeText(getActivity(),"clicked",Toast.LENGTH_LONG).show();

				///changeFragment(new DasboardFragment(), true);


				*//*BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
				DasboardFragment fragment = new DasboardFragment();
				fragment.setFacility(getItem());
				activity.changeFragment(fragment, true);*//*

			}
		});*/
        return view;
    }

    protected int getListId() {
        return R.id.list;
    }

    @NonNull
    protected LinearLayoutManager getListLayoutManager() {
        return linearLayoutManager;
    }

    public RecyclerView getList() {
        return list;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
