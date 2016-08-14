package nu.huw.clarity.sync;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.crypto.OmniSyncDecrypter;

class DecrypterLoaderTask extends AsyncTask<File, Void, OmniSyncDecrypter> {

    private static final String TAG = DecrypterLoaderTask.class.getSimpleName();
    private final DecrypterLoaderTask.TaskListener taskListener;

    DecrypterLoaderTask(DecrypterLoaderTask.TaskListener listener) {

        taskListener = listener;
    }

    public DecrypterLoaderTask() {

        taskListener = null;
    }

    @Override protected OmniSyncDecrypter doInBackground(File[] params) {

        String passphrase = AccountManagerHelper.getPassword();

        try {

            return new OmniSyncDecrypter(params[0], passphrase);
        } catch (Exception e) {

            Log.e(TAG, "Failed to load Omni Sync Decrypter", e);
            return null;
        }
    }

    @Override protected void onPostExecute(OmniSyncDecrypter omniSyncDecrypter) {

        if (taskListener != null) {
            taskListener.onFinished(omniSyncDecrypter);
        }
    }

    interface TaskListener {

        void onFinished(OmniSyncDecrypter result);
    }
}