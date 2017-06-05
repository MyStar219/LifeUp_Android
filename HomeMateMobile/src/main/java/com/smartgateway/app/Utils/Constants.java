package com.smartgateway.app.Utils;

import android.app.Activity;

import com.smartgateway.app.activity.LoginActivity;
import com.smartgateway.app.service.BackgroundService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.ui.picasso.Circle;

public interface Constants {
    ClassConstantsProvider CLASS_CONSTANTS = new ClassConstantsProvider() {

        @Override
        public Class<? extends Activity> getLoginActivityClass() {
            return LoginActivity.class;
        }

        @Override
        public Class<? extends BaseBackgroundService> getBackgroundServiceClass() {
            return BackgroundService.class;
        }
    };

    DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    Circle CIRCLE = new Circle();

    interface State {
        int AVAILABLE = 1;
        int MY_BOOKING = 2;
        int PARTIALLY_BLOCKED = 3;
        int PARTIALLY_BOOKED = 4;
        int FULLY_BOOKED = 5;
        int FULLY_BLOCKED = 6;
        int BOOKED = FULLY_BOOKED;
        int BLOCKED = FULLY_BLOCKED;
    }

    interface BookingState {
        int RESERVED = 100;
        int CONFIRMED = 101;
        int CONSUMED = 102;
        int COMPLETED = 103;
        int CANCELLED = 104;
        int EXPIRED = 105;
    }

    String RESERVED = "reserved";
    String CONFIRMED = "confirmed";
    String CONSUMED = "consumed";
    String COMPLETED = "completed";
    String CANCELLED = "cancelled";
    String EXPIRED = "expired";
    String TITLE = "title";

    interface MaintenanceState {
        int SENT = 200;
        int RECEIVED = 201;
        int PENDING = 202;
        int COMPLETED = 203;
        int RATED = 204;
        int DONE = 205;
    }

    interface MaintenanceStatus {
        String SENT = "sent";
        String RECEIVED = "received";
        String PENDING = "pending";
        String COMPLETED = "completed";
        String RATED = "rated";
        String DONE = "done";
    }

    String EXTRA_FRAGMENT = "fragment.extra";
    String PARAM_PHONE = "phone.param";
    String PARAM_CODE = "code.param";
    String PARAM_MODE = "mode.param";
    String WEB_URL = "web.url";
    String TAG = "SmartGateway";

    // Family Relationship
    String[] FAMILY_ITEMS = new String[]{"Father", "Mother", "Son", "Daughter", "Husband",
        "Wife", "Grandfather", "Grandmother", "Others"};

    // Urls
    String URLS = "urls";
    String PRIVACY_URL = "privacy_url";
    String AGREEMENT_URL = "agreement_url";
    String ABOUT_URL = "about_url";
    String TERMS_URL = "terms_url";

    String FINDCAR_URL = "findcar_url";
    String CARPLATE_URL = "carplate_url";
    String NEEDHELP_URL = "needhelp_url";

    // User data
    String USER_DATA = "user_data";
    String USER_NAME = "user_name";
    String USER_PASSWORD = "user_password";
    String USER_MOBILE = "user_mobile";
    String USER_TOKEN = "user_token";
    String USER_EMAIL = "user_email";
    String USER_VERIFY = "user_verify";
    String USER_HOME = "user_home";
    String USER_PIC_IMAGE = "user_pic_url";
    String USER_PROFILE = "user_profile";
    String USER_CREDENTIALS_JSON = "user_credentials_json";
    String PROVIDER_WEIJU = "Weiju";
    String PROVIDER_HOMEMATE = "HomeMate";
    // User forgot password
    String USER_FORGOT = "user_forgot";
    String FORGOT_USERNAME = "forgot_username";
    String FORGOT_CODE = "forgot_code";
    String PASSWORD_CHANGE_SUCESSFULL = "Successfully updated your password";

    String NOTIFICAION_DATA = "user_data";


    String NOTIFICAION_FAMILY = "family";
    String NOTIFICAION_TRANSFER = "transfer";
    String NOTIFICAION_MAINTENANCE_FEES = "maintenance_fee";
    String NOTIFICAION_HISTORY = "history";
    String NOTIFICAION_BOOKING = "booking";

    String NOTIFICAION_FEEDBACK = "feedback";
    String NOTIFICAION_SYSTEM = "system";
    String NOTIFICAION_MAINTENANCE = "maintenance";
    String NOTIFICAION_CONDO = "condo";

    // Booking information
    String BOOKING_ID = "booking_id";

    // Facility type
    String FACILITY_BBQ = "BBQ Pit";
    String FACILITY_FUNCTION = "Function Room";
    String FACILITY_TENNIS = "Tennis Court";
    String FACILITY_KTV = "KTV Room";
    String FACILITY_ID = "facility id";


    String FROM_WHERE = "fromWhere";
    String FROM_MAINTENANCE = "fromMaintenance";
    String FROM_FEEDBACK = "fromFeedback";

    //Personal center count
    String PERSONAL_COUNT = "personal_count";
    String BOOKINGS = "bookings";
    String MAINTENANCES = "maintenances";
}
