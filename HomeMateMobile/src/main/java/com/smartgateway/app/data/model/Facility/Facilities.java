package com.smartgateway.app.data.model.Facility;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 29/6/16.
 */
public class Facilities {

    /**
     * dates : [{"state":"available","bookdate":"2016-07-08","id":1315},{"state":"available","bookdate":"2016-07-09","id":1338},{"state":"available","bookdate":"2016-07-10","id":1339},{"state":"available","bookdate":"2016-07-11","id":1354},{"state":"available","bookdate":"2016-07-12","id":1367},{"state":"available","bookdate":"2016-07-13","id":1380},{"state":"available","bookdate":"2016-07-14","id":1393},{"state":"available","bookdate":"2016-07-15","id":1406},{"state":"available","bookdate":"2016-07-16","id":1419},{"state":"available","bookdate":"2016-07-17","id":1432},{"state":"available","bookdate":"2016-07-18","id":1445},{"state":"available","bookdate":"2016-07-19","id":1458},{"state":"available","bookdate":"2016-07-20","id":1471},{"state":"available","bookdate":"2016-07-21","id":1484},{"state":"available","bookdate":"2016-07-22","id":1497},{"state":"available","bookdate":"2016-07-23","id":1510},{"state":"available","bookdate":"2016-07-24","id":1533},{"state":"available","bookdate":"2016-07-25","id":1534},{"state":"available","bookdate":"2016-07-26","id":1549},{"state":"available","bookdate":"2016-07-27","id":1577},{"state":"available","bookdate":"2016-07-28","id":1578},{"state":"available","bookdate":"2016-07-29","id":1593},{"state":"available","bookdate":"2016-07-30","id":1606},{"state":"available","bookdate":"2016-07-31","id":1619},{"state":"available","bookdate":"2016-08-01","id":1632},{"state":"available","bookdate":"2016-08-02","id":1645},{"state":"available","bookdate":"2016-08-03","id":1658},{"state":"available","bookdate":"2016-08-04","id":1671},{"state":"available","bookdate":"2016-08-05","id":1686}]
     * image_url : https://api.lifeup.com.sg/image/facility/3/2.jpg
     * name : Multi-purpose Room
     * fid : 3
     */

    private List<FacilityBean> Facility;

    public static Facilities objectFromData(String str) {

        return new Gson().fromJson(str, Facilities.class);
    }

    public List<FacilityBean> getFacility() {
        return Facility;
    }

    public void setFacility(List<FacilityBean> Facility) {
        this.Facility = Facility;
    }

    public static class FacilityBean {
        private String image_url;
        private String name;
        private int fid;
        /**
         * state : available
         * bookdate : 2016-07-08
         * id : 1315
         */

        private List<DatesBean> dates;

        public static FacilityBean objectFromData(String str) {

            return new Gson().fromJson(str, FacilityBean.class);
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFid() {
            return fid;
        }

        public void setFid(int fid) {
            this.fid = fid;
        }

        public List<DatesBean> getDates() {
            return dates;
        }

        public void setDates(List<DatesBean> dates) {
            this.dates = dates;
        }

        public static class DatesBean {
            private String state;
            private String bookdate;
            private int id;

            public static DatesBean objectFromData(String str) {

                return new Gson().fromJson(str, DatesBean.class);
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getBookdate() {
                return bookdate;
            }

            public void setBookdate(String bookdate) {
                this.bookdate = bookdate;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }
        }
    }
}
