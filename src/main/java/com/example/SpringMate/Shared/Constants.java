package com.example.SpringMate.Shared;

public final class Constants {

    private Constants() {
    }

    /**
     * Request correlation keys
     */
    public static final class RequestCorrelation {
        public static final String REQUEST_ID_HEADER = "X-Request-Id";
        public static final String MDC_KEY = "requestId";

        private RequestCorrelation() {
        }
    }

    /**
     * Cache namespace constants for Redis caching
     */
    public static final class CacheNamespace {
        public static final String CATEGORY = "categories";
        public static final String CONDITION = "conditions";
        public static final String COUNTRY = "countries";
        public static final String STATE = "states";
        public static final String CITY = "cities";

        private CacheNamespace() {
        }
    }

    /**
     * Default category names for seeding
     */
    public static final String[] CATEGORIES = {
            "cars", "bikes", "mobile_phones", "electronic", "furniture", "property", "others"
    };

    /**
     * Email subject headers
     */
    public static final class EmailHeaders {
        public static final String LOGIN = "OTP Verification";
        public static final String CONTACT_SELLER = "New message on MarketMate";

        private EmailHeaders() {
        }
    }

    /**
     * Image-related constants
     */
    public static final class Images {
        public static final class Listing {
            public static final int MAX_LIMIT = 6;

            private Listing() {
            }
        }

        private Images() {
        }
    }

    /**
     * Application context identifiers
     */
    public static final class AppContext {
        public static final String ADMIN = "ADMIN";
        public static final String PUBLIC = "PUBLIC";

        private AppContext() {
        }
    }

    public static final String DEFAULT_CATEGORY = "others";

    /**
     * Standard error messages
     */
    public static final class Messages {
        public static final class Error {
            public static final String SOMETHING_WENT_WRONG = "Something went wrong!";
            public static final String BAD_REQUEST = "Bad Request!";
            public static final String FORBIDDEN = "Forbidden";
            public static final String UNAUTHORIZED = "Unauthorized";
            public static final String NOT_FOUND = "Requested resource not found";

            private Error() {
            }
        }

        private Messages() {
        }
    }
}
