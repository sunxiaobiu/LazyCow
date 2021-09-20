package tinker.sample.android.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpSingleton {
    private static OkHttpSingleton singletonInstance;

    // No need to be static; OkHttpSingleton is unique so is this.
    private OkHttpClient client;

    public static OkHttpSingleton getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new OkHttpSingleton();
        }
        return singletonInstance;
    }

    // Private so that this cannot be instantiated.
    private OkHttpSingleton() {
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    // In case you just need the unique OkHttpClient instance.
    public OkHttpClient getClient() {
        return client;
    }
}
