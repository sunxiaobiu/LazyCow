package tinker.sample.android.util;

public class Cookie {
    private static String Cookie;
    //private static final com.core.Cookie singleton = new Cookie( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private Cookie() {
    }

    /* Other methods protected by singleton */
    public static String getCookie() {
        return Cookie;
    }

    public static void setCookie(String cookie) {
        Cookie = cookie;
    }
}
