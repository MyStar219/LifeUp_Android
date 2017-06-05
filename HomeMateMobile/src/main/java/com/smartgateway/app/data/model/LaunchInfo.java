package com.smartgateway.app.data.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class LaunchInfo {

    /**
     * terms_url : http://smartgateway.com/terms
     * privacy_url : http://smartgateway.com/terms
     * about_url : http://smartgateway.com/terms
     * agreement_url : http://smartgateway.com/terms
     */

    private Credentials Credentials;

    private UrlsBean Urls;
    /**
     * id : 1
     * image : /9j/4AAQSkZJRgABAQAAAQABAAD
     * url : http://smartgateway.com.sg
     */

    private List<BannersBean> Banners;

    public UrlsBean getUrls() {
        return Urls;
    }

    public void setUrls(UrlsBean Urls) {
        this.Urls = Urls;
    }

    public List<BannersBean> getBanners() {
        return Banners;
    }

    public void setBanners(List<BannersBean> Banners) {
        this.Banners = Banners;
    }

    public static class UrlsBean {
        private String terms_url;
        private String privacy_url;
        private String about_url;
        private String agreement_url;
        private String findcar_url;
        private String carplate_url;
        private String needhelp_url;

        public static UrlsBean objectFromData(String str) {

            return new Gson().fromJson(str, UrlsBean.class);
        }

        public String getTerms_url() {
            return terms_url;
        }

        public void setTerms_url(String terms_url) {
            this.terms_url = terms_url;
        }

        public String getPrivacy_url() {
            return privacy_url;
        }

        public void setPrivacy_url(String privacy_url) {
            this.privacy_url = privacy_url;
        }

        public String getAbout_url() {
            return about_url;
        }

        public void setAbout_url(String about_url) {
            this.about_url = about_url;
        }

        public String getAgreement_url() {
            return agreement_url;
        }

        public void setAgreement_url(String agreement_url) {
            this.agreement_url = agreement_url;
        }

        public String getFindcar_url() {
            return findcar_url;
        }

        public String getCarplate_url() {
            return carplate_url;
        }

        public String getNeedhelp_url() {
            return needhelp_url;
        }
    }

    public static class BannersBean {
        private int id;
        private String url;

        public static BannersBean objectFromData(String str) {

            return new Gson().fromJson(str, BannersBean.class);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public Credentials getCredentials() {
        return Credentials;
    }
}
