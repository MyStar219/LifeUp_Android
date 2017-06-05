package com.smartgateway.app.fragment.facility;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.Constants;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.fragment.PagerFragment;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yanyu on 5/14/2016.
 */
public class FacilityBookingFragment extends PagerFragment {

    private String id;

    private FacilityData facility;
    private ImageView backdrop;

    private CompositeSubscription subscription = new CompositeSubscription();

    public void setFacility(FacilityData facility) {
        this.facility = facility;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = String.valueOf(facility.getId()); //Get facility id
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected TabDescriptor[] getTabDescriptors() {
        List<FacilityVariantData> variants = new ArrayList<>();
        variants.addAll(facility.getVariants());
        TabDescriptor[] tabs = new TabDescriptor[variants.size()];
        int i = 0;
        for (FacilityVariantData variant : variants) {
            final String name = variant.getName();
            final FacilityVariantData variantClassParam = variant;
            tabs[i++] = new TabDescriptor(name, new FragmentFactory() {
                @Override
                public BaseAbstractFragment createFragment() {
                    return new CalendarFragment().setFacility(facility).setVariant(variantClassParam);
                }
            });
        }
        return tabs;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        AppBarLayout toolbar = createToolbarFrom(R.layout.toolbar_facility);
        TabLayout tabLayout = (TabLayout) toolbar.findViewById(R.id.tabs);

        initFacilityTabName(toolbar, tabLayout);
        setTabLayout(tabLayout);

        backdrop = (ImageView) toolbar.findViewById(R.id.main_backdrop);
        loadImageForFirstTab();
        return toolbar;
    }

    private void initFacilityTabName(AppBarLayout toolbar, TabLayout tabLayout) {
        // we show other layout instead tabs in case one variant
        if (facility.getVariants().size() == 1) {
            tabLayout.setVisibility(View.GONE);
            TextView name = (TextView) toolbar.findViewById(R.id.facility_name);
            name.setText(facility.getVariants().get(0).getName());
        } else {
            View nameBoard = toolbar.findViewById(R.id.facility_name_board);
            nameBoard.setVisibility(View.GONE);
        }
    }

    private void loadImageForFirstTab() {
        if (facility == null ||
            facility.getVariants() == null ||
            facility.getVariants().size() == 0) {
            return;
        }

        String imageUrl = facility.getVariants().get(0).getImage_url();
        loadPreviewImage(imageUrl);
    }

    private void loadPreviewImage(String imageUrl) {
        Picasso.with(getActivity())
                .load(imageUrl)
                .into(backdrop);
    }

    @Override
    protected String getTitle(Resources res) {
        return facility.getName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    @Override
    public void onTabSelected(int index) {
        String imageUrl = facility.getVariants().get(index).getImage_url();

        Log.d("SMART", "image to show is " + imageUrl);
        loadPreviewImage(imageUrl);
    }
}
