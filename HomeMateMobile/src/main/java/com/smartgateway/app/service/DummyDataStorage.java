package com.smartgateway.app.service;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.R;
import com.smartgateway.app.data.AnnouncementData;
import com.smartgateway.app.data.ApartmentData;
import com.smartgateway.app.data.BookingData;
import com.smartgateway.app.data.BookingHourData;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.smartgateway.app.data.FamilyData;
import com.smartgateway.app.data.FeedbackData;
import com.smartgateway.app.data.MaintenanceData;
import com.smartgateway.app.data.PaymentData;
import com.smartgateway.app.data.SelectedHoursData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by yanyu on 5/20/2016.
 */
public class DummyDataStorage {
//    private static Random r = new Random();
//    private static long getDummyDate() {
//        return System.currentTimeMillis() + r.nextInt(5184000)*1000;
//    }

//    static List<FacilityData> dummyFacilities = Arrays.asList(new FacilityData[]{
//            new FacilityData(R.drawable.dummy_badminton, "Badminton", Arrays.asList(new FacilityVariantData[]{
//                    new FacilityVariantData("Court 1"),
//                    new FacilityVariantData("Court 2"),
//                    new FacilityVariantData("Court 3"),
//            })),
//            new FacilityData(R.drawable.dummy_bbq, "BBQ", Arrays.asList(new FacilityVariantData[]{
//                    new FacilityVariantData("Pit No. 10"),
//                    new FacilityVariantData("Pit No. 20"),
//            })),
//            new FacilityData(R.drawable.dummy_conference, "Multi-purpose-room", Arrays.asList(new FacilityVariantData[]{
//                    new FacilityVariantData("Room"),
//            })),
//    });
//
//    static List<ApartmentData> dummyApartments = Arrays.asList(new ApartmentData[]{
//            new ApartmentData("D'Hillside Loft", "4", "5A", "", false),
//            new ApartmentData("Seahill", "14", "7C", ""),
//    });

    static List<PaymentData> dummyPayments = Arrays.asList(new PaymentData[]{
            new PaymentData(R.drawable.payment_cash, "Cash"),
            new PaymentData(R.drawable.payment_cheque, "Cheque"),
            new PaymentData(R.drawable.payment_online, "Online payment"),
    });

//    static List<List<AnnouncementData>> dummyAnnouncements = new ArrayList<List<AnnouncementData>>() {{
//        add(Arrays.asList(new AnnouncementData[]{
//                new AnnouncementData("Fun-Fair Date", "Seahill condominium will be having its annual fun fair event for its residents from 13 March to 20 March", getDummyDate()),
//                new AnnouncementData("Firedrill", "D'Hillside Loft Condominium will be having bi-annual firedrill event for its residents on 22 Feb 2016", getDummyDate()),
//        }));
//        add(Arrays.asList(new AnnouncementData[]{
//                new AnnouncementData("Maintenance", "We will be having maintenance scheduled from 12pm to 4pm on 31 Feb 2116. We regret to inform that we all will die.", getDummyDate()),
//                new AnnouncementData("Maintenance", "We will be having maintenance scheduled from 8am to 4pm on 29 Aug 2016. We regret to inform that there will be no power.", getDummyDate()),
//        }));
//    }};

//    static List<FamilyData> dummyFamily = Arrays.asList(new FamilyData[]{
//            new FamilyData("Kim Xi Ting", "Wife"),
//            new FamilyData("Halsey Khoo", "Daughter"),
//            new FamilyData("Jeslie Khoo", "Daughter", true),
//    });

//    static List<MaintenanceData> dummyMaintenances = Arrays.asList(new MaintenanceData[]{
//        new MaintenanceData(
//                getDummyDate(),
//                "Tap Leaking",
//                "D'Hillside Loft Condo\nBlock 4 unit 4",
//                Constants.MaintenanceState.COMPLETED,
//                "My kitchen sink tap has been leaking for 2 days.\nI tried to tighten the grip, but it has been helpful.\nI have no idea what is the main problem that is causing the leaking. It will be very much appreciated if you can attend to it asap.",
//                "Kitchen Facilities"
//        ),
//        new MaintenanceData(
//                getDummyDate(),
//                "Light Fuse Blown",
//                "D'Hillside Loft Condo\nBlock 4 unit 4",
//                Constants.MaintenanceState.IN_PROGRESS,
//                "Room Facilities"
//        ),
//    });

//    static List<FeedbackData> dummyFeedback = Arrays.asList(new FeedbackData[]{
//            new FeedbackData(
//                    getDummyDate(),
//                    "Tap Leaking",
//                    "D'Hillside Loft Condo\nBlock 4 unit 4",
//                    Constants.MaintenanceState.COMPLETED,
//                    "My kitchen sink tap has been leaking for 2 days.\nI tried to tighten the grip, but it has been helpful.\nI have no idea what is the main problem that is causing the leaking. It will be very much appreciated if you can attend to it asap.",
//                    "Kitchen Facilities"
//            ),
//            new FeedbackData(
//                    getDummyDate(),
//                    "Light Fuse Blown",
//                    "D'Hillside Loft Condo\nBlock 4 unit 4",
//                    Constants.MaintenanceState.FIXED,
//                    "Room Facilities"
//            ),
//    });

//    static List<BookingData> dummyBookings = new ArrayList<BookingData>(){{
//        SelectedHoursData s = new SelectedHoursData();
//        s.add(new BookingHourData(19,1));
//        add(
//                new BookingData(
//                        dummyFacilities.get(0),
//                        dummyFacilities.get(0).getVariant("Court 1"),
//                        getDummyDate(),
//                        s,
//                        Constants.BookingState.CONFIRMED
//                )
//        );
//        FacilityData bbq = dummyFacilities.get(1);
//        s = new SelectedHoursData();
//        for (int i=9; i<15; i++) {
//            s.add(new BookingHourData(i,1));
//        }
//        add(
//                new BookingData(
//                        bbq,
//                        bbq.getVariant("Pit No. 20"),
//                        getDummyDate(),
//                        s,
//                        Constants.BookingState.WAITING
//                )
//        );
//        s = new SelectedHoursData();
//        for (int i=9; i<14; i++) {
//            s.add(new BookingHourData(i,1));
//        }
//        add(
//                new BookingData(
//                        bbq,
//                        bbq.getVariant("Pit No. 10"),
//                        getDummyDate(),
//                        s,
//                        Constants.BookingState.REFUNDED
//                )
//        );
//        s = new SelectedHoursData();
//        for (int i=9; i<15; i++) {
//            s.add(new BookingHourData(i,1));
//        }
//        add(
//                new BookingData(
//                        bbq,
//                        bbq.getVariant("Pit No. 20"),
//                        getDummyDate(),
//                        s,
//                        Constants.BookingState.REFUNDING
//                )
//        );
//
//    }};


}
