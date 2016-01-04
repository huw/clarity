package nu.huw.clarity.sync;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.huw.clarity.account.AccountHelper;

/**
 * Gets a list of files that should be downloaded in this sync. At the moment,
 * it lists all of the files because of basic validation. But in the future it
 * should be able to only return changed, useful files.
 */
public class GetFilesToDownloadTask extends AsyncTask<Void, Void, List<String>> {

    private static final String TAG = GetFilesToDownloadTask.class.getSimpleName();

    /**
     * Java callbacks are called listeners, and they allow you to extend a class
     * which implements this function, which you can then call for sure by
     * referencing your listener interface. Pretty simple. If you don't want a
     * listener, we support that too :)
     */
    public interface TaskListener {
        void onFinished(List<String> result);
    }

    private final TaskListener taskListener;

    public GetFilesToDownloadTask(TaskListener listener) {
        taskListener = listener;
    }

    public GetFilesToDownloadTask() {
        taskListener = null;
    }

    /**
     * Validation for files. Given a WebDAV response, it can determine whether
     * the file should be downloaded (and also if it isn't a file).
     *
     * TODO: Add more logic
     */
    private static boolean shouldDownloadFile(MultiStatusResponse response) {

        DavPropertySet properties = response.getProperties(200);
        DavProperty contentType = properties.get(DavPropertyName.GETCONTENTTYPE);

        if (contentType != null) {

            boolean isFolder = contentType.getValue().toString().equals("httpd/unix-directory");

            Log.v(TAG, response.getHref() + (isFolder ? " isn't " : " is ") + "valid");
            return !isFolder;
        } else {

            Log.v(TAG, response.getHref() + " has no content-type property, assuming valid");
            return true;
        }
    }

    /**
     * Using the user's data (from Synchroniser's `getHttpClient()`),
     * we can log into the server and use a WebDAV command to list all
     * of the files in /OmniFocus.ofocus/ that need to be downloaded.
     * We only add a file if the validation function gives us a yes.
     */
    @Override
    protected List<String> doInBackground(Void... params) {

        try {
            List<String> filesToDownload = new ArrayList<>();
            HttpClient client = Synchroniser.getHttpClient();

            // Just to be clear, a DavMethod is an extension of an HttpMethod, and is used
            // to send requests to servers. Like you'd execute a GetMethod to get a file
            // from a server, you execute a DavMethod to do non-http things like listing
            // files in a folder or telling the server to copy/move a file.

            DavMethod listAllFiles = new PropFindMethod(
                    AccountHelper.getOfocusURI(),
                    DavConstants.PROPFIND_ALL_PROP,
                    DavConstants.DEPTH_1
            );
            client.executeMethod(listAllFiles);
            listAllFiles.releaseConnection();

            MultiStatusResponse[] responses = listAllFiles
                    .getResponseBodyAsMultiStatus()
                    .getResponses();

            for (MultiStatusResponse response : responses) {
                if (shouldDownloadFile(response)) {

                    // Because we can recreate the URL, and because we
                    // need the name of the file (without the path)
                    // later on, we convert it to the raw name.

                    filesToDownload.add(
                            new File(response.getHref()).getName()
                    );

                }
            }

            Log.i(TAG, "Got " + filesToDownload.size() + " files to download");

            return filesToDownload;

        } catch (IOException | DavException e) {
            Log.e(TAG, "Problem creating/sending request", e);
        }

        return null;
    }

    /**
     * Call the callback function, and pass our result to it to be handled.
     */
    @Override
    protected void onPostExecute(List<String> result) {
        super.onPostExecute(result);

        if (this.taskListener != null) {
            this.taskListener.onFinished(result);
        }
    }
}
