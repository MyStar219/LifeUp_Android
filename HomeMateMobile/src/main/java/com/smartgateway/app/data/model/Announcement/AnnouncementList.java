package com.smartgateway.app.data.model.Announcement;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class AnnouncementList {

    private AnnouncementBean Announcement;

    public static AnnouncementList objectFromData(String str) {

        return new Gson().fromJson(str, AnnouncementList.class);
    }

    public AnnouncementBean getAnnouncement() {
        return Announcement;
    }

    public void setAnnouncement(AnnouncementBean Announcement) {
        this.Announcement = Announcement;
    }

    public static class AnnouncementBean {
        private List<SystemBean> condo;
        /**
         * content : This is the first message sent from Smart Gateway.
         * status : notread
         * create_date : 2016-04-29 09:50:43
         * id : 1
         * subject : System annoncement 1
         */

        private List<SystemBean> system;

        public static AnnouncementBean objectFromData(String str) {

            return new Gson().fromJson(str, AnnouncementBean.class);
        }

        public List<SystemBean> getCondo() {
            return condo;
        }

        public void setCondo(List<SystemBean> condo) {
            this.condo = condo;
        }

        public List<SystemBean> getSystem() {
            return system;
        }

        public void setSystem(List<SystemBean> system) {
            this.system = system;
        }

        public static class SystemBean {
            private String content;
            private String status;
            private String create_date;
            private int id;
            private String subject;

            public static SystemBean objectFromData(String str) {

                return new Gson().fromJson(str, SystemBean.class);
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getCreate_date() {
                return create_date;
            }

            public void setCreate_date(String create_date) {
                this.create_date = create_date;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getSubject() {
                return subject;
            }

            public void setSubject(String subject) {
                this.subject = subject;
            }
        }
    }
}
