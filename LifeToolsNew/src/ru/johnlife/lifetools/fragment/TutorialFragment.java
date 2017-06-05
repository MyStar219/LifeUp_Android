package ru.johnlife.lifetools.fragment;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ru.johnlife.lifetools.R;

/**
 * Created by yanyu on 4/25/2016.
 */
public abstract class TutorialFragment extends BaseAbstractFragment {
    private ViewPager pager;
    private TutorialStepDescriptor tutoralSteps[];
    private View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pager.setCurrentItem(pager.getCurrentItem()+1);
        }
    };
    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onTutorialComplete();
        }
    };

    protected abstract void onTutorialComplete();

    @Override
    protected View createView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        tutoralSteps = getTutorialSteps();
        final Button button = (Button) view.findViewById(R.id.button);
        final View skip = view.findViewById(R.id.skip);
        button.setOnClickListener(nextListener);
        skip.setOnClickListener(startListener);
        pager = (ViewPager)view.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return tutoralSteps.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = inflater.inflate(R.layout.item_tutorial, container, false);
                ((ImageView)view.findViewById(R.id.image)).setImageResource(tutoralSteps[position].img);
                ((TextView)view.findViewById(R.id.text)).setText(tutoralSteps[position].str);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                boolean lastPage = position == (tutoralSteps.length - 1);
                button.setText(lastPage ? R.string.start : R.string.next);
                button.setOnClickListener(lastPage ? startListener : nextListener);
                skip.setVisibility(lastPage ? View.GONE : View.VISIBLE);
            }
        });
        return view;
    }

    @Override
    protected boolean isUpAsHomeEnabled() {
        return false;
    }

    protected abstract TutorialStepDescriptor[] getTutorialSteps();

    protected static class TutorialStepDescriptor {
        private int img;
        private int str;

        public TutorialStepDescriptor(int img, int str) {
            this.img = img;
            this.str = str;
        }
    }
}
