package nu.huw.clarity.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.ui.MainActivity;

/**
 * Given a single file, downloads it and returns the file object. Designed to be used in an
 * asynchronous context with multiple files at once, along with `.get()` instead of callbacks.
 */
class DownloadFileTask extends AsyncTask<Object, Void, File> {

    private static final String TAG = DownloadFileTask.class.getSimpleName();
    private final TaskListener taskListener;

    DownloadFileTask(TaskListener listener) {

        taskListener = listener;
    }

    DownloadFileTask() {

        taskListener = null;
    }

    @Override protected File doInBackground(Object... params) {

        File                 file     = (File) params[0];
        AccountManagerHelper AMHelper = new AccountManagerHelper((Context) params[1]);
        HttpClient           client   = (HttpClient) params[2];

        GetMethod getFileMethod = new GetMethod(AMHelper.getOfocusURI() + file.getName());

        try {

            client.executeMethod(getFileMethod);

            if (getFileMethod.getStatusCode() == 200) {

                // Basically, we make a GetMethod using standard procedure. Then we
                // open its results up into a stream (making sure NOT to call
                // `.releaseConnection()` until we're done), and bitwise copy the in
                // stream to the out stream. THEN we close everything. File downloaded.

                InputStream input = getFileMethod.getResponseBodyAsStream();
                File outFile = File.createTempFile(file.getName(), null,
                                                   MainActivity.context.getCacheDir());
                RandomAccessFile output = new RandomAccessFile(outFile, "rw");

                // Copy input stream to output stream, bitwise
                byte data[] = new byte[4096];
                int  count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                // Be extra careful not to cross streams
                // First time this joke has been made, ever

                input.close();
                output.close();

                getFileMethod.releaseConnection();

                Log.v(TAG, params[0] + " successfully downloaded (" + file.length() + " bytes)");

                return file;
            } else {
                Log.e(TAG, "Unexpected WebDAV status " + getFileMethod.getStatusCode() + ": " +
                           getFileMethod.getStatusText());
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected " + e.getClass().getName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    @Override protected void onPostExecute(File result) {

        super.onPostExecute(result);

        if (this.taskListener != null) {
            this.taskListener.onFinished(result);
        }
    }

    interface TaskListener {

        void onFinished(File result);
    }
}
