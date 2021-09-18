package com.example.crowdtestinglibrary;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class downloadAndInstall {
    final String lazyCowPath = "/download/application_app-debug-origin.apk";

    public downloadAndInstall(Context c) throws DownloadException {
        String downloadLazyCowUrl = "http://118.138.236.244:8080/RemoteTest/device/downloadOriginAPK";
        String lazyCowPackageUri = "tinker.sample.android";
        boolean isAppInstalled = appInstalledOrNot(c, lazyCowPackageUri);

        String TAG = "LaunchLazyCow";
        if (isAppInstalled){
            //Launch the app when the application is installed
            runLazyCow(c, lazyCowPackageUri);
            Log.i(TAG,"LazyCow is installed, launching LazyCow.");
        }else{
            File lazyCowAPK = new File(Environment.getExternalStorageDirectory() + lazyCowPath);
            boolean exists = lazyCowAPK.exists();
            downloadLazyCow(downloadLazyCowUrl,c);
        }
    }

    private Boolean appInstalledOrNot(Context context, String  uri){
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
            //Package not found, print out exceptions.
        }

    }

    private void runLazyCow(Context context, String uri){
        Intent lauchLazyCow = context.getPackageManager()
                .getLaunchIntentForPackage(uri);
        context.startActivity(lauchLazyCow);
    }

    private void installLazyCow(Context context){
        Log.i("InstallLazyCow","Start Install Lazy Cow");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
                new File(Environment.getExternalStorageDirectory()+ lazyCowPath));
        intent.setDataAndType(
                uri, "application/vnd.android.package-archive");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.d("Install Path",new File(uri.getPath()).toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void downloadLazyCow(String url, Context context) {
        ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Downloading LazyCow");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        final DownloadTask downloadTask = new DownloadTask(context, mProgressDialog);
        downloadTask.execute(url);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true); //cancel the task
            }
        });

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private ProgressDialog mProgressDialog;

        public DownloadTask(Context context, ProgressDialog p) {
            this.context = context;
            mProgressDialog = p;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root+"/download");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                File file = new File(myDir,"application_app-debug-origin.apk");
                Log.d("Download path",file.getAbsolutePath().toString());
                output = new FileOutputStream(file);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10*60*1000L /*10 minutes*/);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
            installLazyCow(context);
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (write_permission != PackageManager.PERMISSION_GRANTED ) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //Install Permission
    private static final int REQUEST_INSTALL = 1;
    private static String[] PERMISSION_INSTALL = {
            Manifest.permission.INSTALL_PACKAGES
    };

    public static void verifyInstallPermission(Activity activity){
        int install_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.INSTALL_PACKAGES);

        if (install_permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_INSTALL,
                    REQUEST_INSTALL
            );
        }
    }
}
