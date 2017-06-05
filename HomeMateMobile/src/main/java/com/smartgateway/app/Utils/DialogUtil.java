package com.smartgateway.app.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.smartgateway.app.R;
import com.smartgateway.app.fragment.SgWalletFragment;
import com.smartgateway.app.fragment.apartment.AddApartmentFragment;
import com.smartgateway.app.fragment.apartment.ApartmentListFragment;
import com.smartgateway.app.fragment.booking.BookingListFragment;
import com.smartgateway.app.fragment.facility.DepositFragment;
import com.smartgateway.app.fragment.facility.FacilityListFragment;
import com.smartgateway.app.fragment.family.FamilyListFragment;
import com.smartgateway.app.fragment.family.InviteFragment;
import com.smartgateway.app.fragment.feedback.AddFeedbackFragment;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.fragment.feedback.SubmitFeedbackFragment;
import com.smartgateway.app.fragment.maintenance.AddMaintenanceFragment;
import com.smartgateway.app.fragment.maintenance.MaintenanceListFragment;
import com.smartgateway.app.fragment.maintenance.SubmitMaintenanceFragment;
import com.smartgateway.app.fragment.user.ChangePassFragment;
import com.smartgateway.app.fragment.user.ForgotPassFragment;
import com.smartgateway.app.fragment.user.SignupFragment;
import com.smartgateway.app.fragment.user.VerifyFragment;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseActivity;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

import static com.smartgateway.app.service.NetworkService.SmartGatewayOkhttpInterceptor.INSUFFICIENT_FUNDS_ERROR_CODE;
import static com.smartgateway.app.service.NetworkService.SmartGatewayOkhttpInterceptor.INVALID_TOKEN_ERROR_CODE;

/**
 * Created by Terry on 1/7/16.
 *
 */
public class DialogUtil {

    private Context context;
    private KProgressHUD hud;


    public DialogUtil(Context context) {
        this.context = context;
    }

    public void showDissmissDialog(int errorMessage, int title){
        if (!isActivityAlive(context)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(errorMessage)
                .setTitle(title);
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDissmissDialog(String errorMessage, int title) {
        if (!isActivityAlive(context)) {
            return;
        }

        showErrorAlert(this.context, errorMessage, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public static boolean isActivityAlive(Context context) {
        if (context instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) context;
            if (activity.isFinishing() || activity.isFinished()) {
                Log.d("DialogUtil", "activity is destroyed, we don't perform any action on destroyed context");
                return false;
            }
        }
        return true;
    }

    public void showProgressDialog() {
        if (!isActivityAlive(context)) {
            return;
        }

        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f)
                .show();
    }

    public void dismissProgressDialog() {
        if (!isActivityAlive(context)) {
            hud = null;
            return;
        }

        if (hud != null) {
            hud.dismiss();
            hud = null;
        }
    }

    static public AlertDialog showErrorAlert(Context context, String strDetail, final View.OnClickListener listener) {
        if (!isActivityAlive(context)) {
            return null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.ios_alert, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        ((TextView)view.findViewById(R.id.txtMsg)).setText(strDetail);
        view.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return dialog;
    }

    static public void showDetailErrorAlert(final Context context, String detail, int responseCode) {
        View.OnClickListener okClickListener = null;
        final List<Class<? extends BaseAbstractFragment>> list = new ArrayList<>();

        if (responseCode >= 400 &&
                responseCode <= 600 &&
                responseCode != INSUFFICIENT_FUNDS_ERROR_CODE &&
                responseCode != INVALID_TOKEN_ERROR_CODE) {
            list.add(FacilityListFragment.class);
            list.add(FeedbackListFragment.class);
            list.add(AddFeedbackFragment.class);
            list.add(SgWalletFragment.class);
            list.add(FamilyListFragment.class);
            list.add(ApartmentListFragment.class);
            list.add(BookingListFragment.class);
            list.add(MaintenanceListFragment.class);
            list.add(AddMaintenanceFragment.class);
        }
        else if (responseCode >= 200 &&
                    responseCode < 300) {
            list.add(ChangePassFragment.class);
        }

        final List<Class<? extends BaseAbstractFragment>> customSuccessDetailFragments = new ArrayList<>();
        customSuccessDetailFragments.add(SignupFragment.class);
        customSuccessDetailFragments.add(ForgotPassFragment.class);
        customSuccessDetailFragments.add(AddApartmentFragment.class);
        customSuccessDetailFragments.add(VerifyFragment.class);
        customSuccessDetailFragments.add(AddMaintenanceFragment.class);
        customSuccessDetailFragments.add(SubmitFeedbackFragment.class);
        customSuccessDetailFragments.add(SubmitMaintenanceFragment.class);
        customSuccessDetailFragments.add(DepositFragment.class);
        customSuccessDetailFragments.add(InviteFragment.class);

        if (responseCode == 200 && context instanceof BaseMainActivity) {
            BaseMainActivity activity = ((BaseMainActivity)context);
            BaseAbstractFragment currentFragment = activity.getCurrentFragment();
            if (currentFragment != null &&
                customSuccessDetailFragments.contains(currentFragment.getClass())) {
                // we use custom dialogs for 200 code and just return here
                Log.d("DialogUtil", "custom 200 code details handler");
                return;
            }
        }

        if (detail.length() == 0) { //If there is no Alert to send, we just take action here and we return
            returnToPreviousScreen(context, list);
            return;
        }

        okClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPreviousScreen(context, list);
            }
        };

        showErrorAlert(context, detail, okClickListener);
    }

    static private void returnToPreviousScreen(Context context, List<Class<? extends BaseAbstractFragment>> list) {
        if (context instanceof BaseMainActivity) {
            BaseMainActivity activity = ((BaseMainActivity)context);
            if (list.contains(activity.getCurrentFragment().getClass())) {
                Log.d("DialogUtil", "custom back button while error");
                activity.onBackPressed();
            }
        }
    }

    static public void showErrorAlert(Context context,
                                      String strDetail,
                                      String strOK,
                                      String strCancel,
                                      final View.OnClickListener okClickListener,
                                      final View.OnClickListener cancelClickListener) {
        if (!isActivityAlive(context)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.ios_alert2, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        ((TextView)view.findViewById(R.id.txtMsg)).setText(strDetail);
        Button btnOK = (Button)view.findViewById(R.id.btnOK);
        btnOK.setText(strOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (okClickListener != null) {
                    okClickListener.onClick(v);
                }
            }
        });
        Button btnCancel = (Button)view.findViewById(R.id.btnCancel);
        btnCancel.setText(strCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (cancelClickListener != null) {
                    cancelClickListener.onClick(v);
                }
            }
        });
    }

    static public void showErrorAlertV(Context context, String strDetail, String strOK, String strCancel, final View.OnClickListener listener1, final View.OnClickListener listener2) {
        if (!isActivityAlive(context)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.ios_alert3, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();

        ((TextView)view.findViewById(R.id.txtMsg)).setText(strDetail);

        Button btnOK = (Button)view.findViewById(R.id.btnOK);
        btnOK.setText(strOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener1 != null) {
                    listener1.onClick(v);
                }
            }
        });
        Button btnCancel = (Button)view.findViewById(R.id.btnCancel);
        btnCancel.setText(strCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener2 != null) {
                    listener2.onClick(v);
                }
            }
        });
    }
}
