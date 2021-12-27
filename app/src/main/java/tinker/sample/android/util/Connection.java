package tinker.sample.android.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class Connection {
    // Private constructor prevents instantiation from other classes
    private Connection() {

    }

    public static String postAPIResponse(String url, String data) {

        HttpURLConnection con = null;
        InputStream inputStream;
        StringBuffer responses = null;
        try {
            URL urlObject = new URL(url);
            con = (HttpURLConnection) (urlObject.openConnection());

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("Content-Language", "en-US");
            if (Cookie.getCookie() != null)
                con.addRequestProperty("Cookie", Cookie.getCookie());

            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            //Get Response
            if (con.getResponseCode() == 200) {
                if (Cookie.getCookie() == null)
                    Cookie.setCookie(con.getHeaderField("Set-Cookie"));


                InputStream is = con.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                responses = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    responses.append(line);
                }
                rd.close();

            } else {

                inputStream = new BufferedInputStream(con.getErrorStream());
                return convertInputStreamToString(inputStream);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert con != null;
            con.disconnect();
        }
        return responses != null ? responses.toString() : "";
    }

    public static String getAPIResponse(String urlName) {
        String response = null;
        HttpURLConnection con = null;
        InputStream inputStream;

        try {

            URL url = new URL(urlName);
            con = (HttpURLConnection) (url.openConnection());

            con.setRequestProperty("Content-Type", "application/json");
            if (Cookie.getCookie() != null)
                con.addRequestProperty("Cookie", Cookie.getCookie());

            int statusCode = con.getResponseCode();
            if (statusCode == 200) {
                inputStream = new BufferedInputStream(con.getInputStream());
                response = convertInputStreamToString(inputStream);
                if (Cookie.getCookie() == null)
                    Cookie.setCookie(con.getHeaderField("Set-Cookie"));
            } else {
                inputStream = new BufferedInputStream(con.getErrorStream());
                response = convertInputStreamToString(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert con != null;
            con.disconnect();
        }
        return response != null ? response : "";
    }

    static public String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        /* Close Stream */
        inputStream.close();
        return result;
    }
}
