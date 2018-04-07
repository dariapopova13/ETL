package com.university.itis.emotionstestinglibrary;

import android.app.Application;

/**
 * Created by Daria Popova on 06.04.18.
 */
public class ETL {

    private static long testId;
    private String apiKey;
    private boolean deleteUploadedVideos;
    private Application application;
    private static Builder builder;

    private ETL(String apiKey, boolean deleteUploadedVideos, long testId, Application application) {
        this.apiKey = apiKey;
        this.deleteUploadedVideos = deleteUploadedVideos;
        ETL.testId = testId;
        this.application = application;
        initLib();
    }

    public static long getTestId() {
        return testId;
    }

    private void initLib() {
    }


    public static Builder Builder() {
        builder = new Builder();
        return builder;
    }

    public static class Builder {

        private String apiKey;
        private boolean deleteUploadedVideos;
        private long testId;
        private Application application;

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setDeleteUploadedVideos(boolean deleteUploadedVideos) {
            this.deleteUploadedVideos = deleteUploadedVideos;
            return this;
        }

        public Builder setTestId(long testId) {
            this.testId = testId;
            return this;
        }

        public Builder setApplication(Application application) {
            this.application = application;
            return this;
        }

        public ETL build() {
            return new ETL(apiKey, deleteUploadedVideos, testId, application);
        }
    }

}
