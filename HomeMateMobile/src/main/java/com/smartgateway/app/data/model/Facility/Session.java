package com.smartgateway.app.data.model.Facility;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 29/6/16.
 */
public class Session {

    /**
     * date : 2016-07-17
     * name : Multi-purpose Room
     * fid : 3
     * time : [{"status":"bookable","start":"11:00 AM","end":"12:00 PM","peak":false,"session_id":278449},{"status":"bookable","start":"12:00 PM","end":"01:00 PM","peak":false,"session_id":278450},{"status":"bookable","start":"01:00 PM","end":"02:00 PM","peak":false,"session_id":278451},{"status":"bookable","start":"02:00 PM","end":"03:00 PM","peak":false,"session_id":278452},{"status":"bookable","start":"03:00 PM","end":"04:00 PM","peak":false,"session_id":278453},{"status":"bookable","start":"04:00 PM","end":"05:00 PM","peak":false,"session_id":278454},{"status":"bookable","start":"05:00 PM","end":"06:00 PM","peak":false,"session_id":278455},{"status":"bookable","start":"06:00 PM","end":"07:00 PM","peak":false,"session_id":278456},{"status":"bookable","start":"07:00 PM","end":"08:00 PM","peak":false,"session_id":278457},{"status":"bookable","start":"08:00 PM","end":"09:00 PM","peak":false,"session_id":278458},{"status":"bookable","start":"09:00 PM","end":"10:00 PM","peak":false,"session_id":278459},{"status":"bookable","start":"10:00 PM","end":"11:00 PM","peak":false,"session_id":278460}]
     */

    private FacilityBean Facility;

    public static Session objectFromData(String str) {

        return new Gson().fromJson(str, Session.class);
    }

    public FacilityBean getFacility() {
        return Facility;
    }

    public void setFacility(FacilityBean Facility) {
        this.Facility = Facility;
    }

    public static class FacilityBean {
        private String date;
        private String name;
        private String fid;
        /**
         * status : bookable
         * start : 11:00 AM
         * end : 12:00 PM
         * peak : false
         * session_id : 278449
         */

        private List<TimeBean> time;

        public static FacilityBean objectFromData(String str) {

            return new Gson().fromJson(str, FacilityBean.class);
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFid() {
            return fid;
        }

        public void setFid(String fid) {
            this.fid = fid;
        }

        public List<TimeBean> getTime() {
            return time;
        }

        public void setTime(List<TimeBean> time) {
            this.time = time;
        }

        public static class TimeBean {
            private String status;
            private String start;
            private String end;
            private boolean peak;
            private int session_id;

            public static TimeBean objectFromData(String str) {

                return new Gson().fromJson(str, TimeBean.class);
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getStart() {
                return start;
            }

            public void setStart(String start) {
                this.start = start;
            }

            public String getEnd() {
                return end;
            }

            public void setEnd(String end) {
                this.end = end;
            }

            public boolean isPeak() {
                return peak;
            }

            public void setPeak(boolean peak) {
                this.peak = peak;
            }

            public int getSession_id() {
                return session_id;
            }

            public void setSession_id(int session_id) {
                this.session_id = session_id;
            }
        }
    }
}
