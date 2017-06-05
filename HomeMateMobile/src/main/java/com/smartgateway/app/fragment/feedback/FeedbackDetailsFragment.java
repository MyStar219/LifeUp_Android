package com.smartgateway.app.fragment.feedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hedgehog.ratingbar.RatingBar;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.FeedbackData;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.data.model.FeedBack.FeedbackDetail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.state.FeedbackStateHelper;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.tools.Base64Bitmap;
import ru.johnlife.lifetools.util.BitmapUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FeedbackDetailsFragment extends BaseAbstractFragment {
    private static FeedbackStateHelper states;
    private FeedbackData feedback;

    private ImageView img;
    private TextView type;
    private TextView description;
    private TextView item;
    private TextView apartment;
    private TextView state;
    private Button btnFeedback;

    private String token;

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_feedback);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_details, container, false);
        Context context = view.getContext();
        if (null == states) {
            states = new FeedbackStateHelper(context);
        }

        initView(view);
        getFeedbackDetail();

        return view;
    }

    private void initView(View view){
        img = (ImageView) view.findViewById(R.id.imgFeedback);
        item = (TextView) view.findViewById(R.id.item);
        apartment = (TextView) view.findViewById(R.id.apartment);
        description = (TextView) view.findViewById(R.id.feedbackDescription);
        state = (TextView) view.findViewById(R.id.state);
        type = (TextView) view.findViewById(R.id.feedbackCategory);
        btnFeedback = (Button) view.findViewById(R.id.btnFeedback);

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitFeedbackFragment fragment = new SubmitFeedbackFragment();
                fragment.setId(feedback.getId());
                BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                activity.changeFragment(fragment, true);
            }
        });

        item.setText(feedback.getItem());
        apartment.setText(feedback.getWhere());

        StateHelper.StateData stateData = states.get(feedback.getState());
        state.setBackgroundColor(stateData.getColor());
        state.setText(stateData.getName());
        state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0 ,0 ,0);
        if (stateData.getName().equals("Completed")) {
            btnFeedback.setVisibility(View.VISIBLE);
        } else {
            btnFeedback.setVisibility(View.GONE);
        }
    }

    public void setItem(FeedbackData item) {
        this.feedback = item;
    }

    private void getFeedbackDetail(){
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        token = preferences.getString(Constants.USER_TOKEN, "");
        final DialogUtil dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();
        RetrofitManager.getFeedbackApiService()
                .detail(token, feedback.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FeedbackDetail>() {
                    @Override
                    public void call(FeedbackDetail feedbackDetail) {
                        dialogUtil.dismissProgressDialog();

                        List<String> imageUrl = feedbackDetail.getFeedback().getImageUrl();
                        if (imageUrl != null && imageUrl.size() > 0) {
                            Log.d("MaintainceDetail", "loading from url " + imageUrl.get(0));
                            Picasso.with(getActivity()).load(imageUrl.get(0)).into(img);
                        }
                        type.setText(feedbackDetail.getFeedback().getCategory());
                        description.setText(feedbackDetail.getFeedback().getDescription());
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                    }
                });
    }

}
