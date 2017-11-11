package com.aptoide.uploader.network;

import java.util.List;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class ResponseV7 {

    private Info info;
    private List<Error> errors;

    public ResponseV7(Info info, List<Error> errors) {
        this.info = info;
        this.errors = errors;
    }

    public Error getError() {
        if (errors != null && errors.size() > 0) {
            return errors.get(0);
        } else {
            return null;
        }
    }

    public boolean isOk() {
        return info != null && info.getStatus() == Info.Status.OK;
    }

    public static class Info {

        private Status status;

        public Info(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }


        public enum Status {
            OK, QUEUED, FAIL, Processing
        }
    }

    public static class Error {
        private String code;
        private String description;

        public Error(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
