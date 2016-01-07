package nu.huw.clarity.sync;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nu.huw.clarity.account.AccountHelper;
import nu.huw.clarity.db.ParseSyncIn;

/**
 * This class handles all of the synchronisation methods.
 * A.K.A. Anything that needs an Apache library.
 */
public class Synchroniser {

    private static final String TAG = Synchroniser.class.getSimpleName();

    /**
     * This function will create a new HttpClient and automatically add the
     * account's credentials in an appropriate form.
     *
     * It's really useful, because we can just call `getHttpClient()` and
     * run WebDAV/HTTP methods on it straightaway, without having to
     * configure it.
     *
     * This code will be the appropriate setup code 100% of the time, AFTER
     * AN ACCOUNT HAS BEEN MADE.
     */
    public static HttpClient getHttpClient() {

        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                AccountHelper.getUsername(),
                AccountHelper.getPassword()
        );
        AuthScope newHostScope = new AuthScope(
                AccountHelper.getServerDomain(),
                AccountHelper.getServerPort(),
                AuthScope.ANY_REALM
        );

        client.getState().setCredentials(newHostScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);
        return client;
    }

    /**
     * This function gets a list of files to download, and then downloads
     * them (wow!). However, it's all a little tricky because it downloads
     * the files asynchronously across the device's available cores for
     * speed.
     *
     * Then, for extra speedy speed, it adds them all to the database (TODO),
     * AS THEY ARRIVE. But we can't use callbacks because we need a specific
     * order. So over in `runEachFile()` we get each file as it finishes, in
     * the order that we hand the files to `downloadFiles()` in.
     *
     * A slow sync is a disruptive sync. A killer sync, like this sync, is
     * a fucking achievement of programming. And impossible to represent in
     * a goddamn flowchart.
     */
    public static void synchronise() {

        new GetFilesToDownloadTask(new GetFilesToDownloadTask.TaskListener() {
            @Override
            public void onFinished(List<String> result) {

                List<DownloadFileTask> downloadList = downloadFiles(result);

                runEachFile(downloadList, new TaskListener() {
                    @Override
                    public void onFinished(File file) {

                        try {

                            ZipFile zipFile = new ZipFile(file);
                            ZipEntry contentsXml = zipFile.getEntry("contents.xml");
                            InputStream input = zipFile.getInputStream(contentsXml);

                            ParseSyncIn.parse(input);

                        } catch (IOException e) {
                            Log.e(TAG, "Error reading downloaded zip file", e);
                        }
                    }
                });

            }
        }).execute();
    }

    private static List<DownloadFileTask> downloadFiles(List<String> nameList) {

        List<DownloadFileTask> downloadList = new ArrayList<>();

        /**
         * The setup we want for the threading is as follows:
         * | 1  | 2 - n-1  |   n   |
         * | UI | Download | Parse |
         * So we need to create a thread pool with 1 less thread than the maximum,
         * to leave a free thread for parsing.
         *
         * We want to do this because the parsing subprocess is sequential (so has
         * a maximum of 1 thread), but is also bottlenecked by the download process.
         * We may as well start it as soon as possible (i.e. after the first file
         * has downloaded), and it should run parallel to the downloads.
         */
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        ExecutorService downloadPool = Executors.newFixedThreadPool(NUMBER_OF_CORES - 1);

        for (String filename: nameList) {

            // Immediately add each file to the downloadList while they're
            // starting to download in the background threads. Then return
            // a list of these objects to be called up later.

            DownloadFileTask download = new DownloadFileTask();
            download.executeOnExecutor(downloadPool, filename);
            downloadList.add(download);

        }

        return downloadList;
    }

    private interface TaskListener {
        void onFinished(File file);
    }

    /**
     * These are all run in a background thread so that we don't
     * block the UI thread. Respect it.
     */
    private static void runEachFile(final List<DownloadFileTask> downloadList, final TaskListener listener) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                for (DownloadFileTask download: downloadList) {
                    try {

                        // Holding the thread until the download is done is perfect
                        // for this style of execution. This way, we can make these
                        // callbacks one-by-one in the order that we wanted to.

                        File file = download.get();

                        listener.onFinished(file);

                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, "Unexpected error while getting result of DownloadFileTask", e);
                    }
                }

                return null;
            }
        }.execute();
    }
}