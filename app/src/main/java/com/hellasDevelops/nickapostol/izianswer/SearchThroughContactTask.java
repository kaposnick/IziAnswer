package com.hellasDevelops.nickapostol.izianswer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by nickapostol on 24/1/2016.
 */
public class SearchThroughContactTask extends AsyncTask<ArrayList<String>, Integer, String> {

    private Context context;
    private ProgressDialog progressDialog;

    public SearchThroughContactTask(Context ctx) {
        this.context = ctx;
    }

    @Override
    protected void onPreExecute() {
        /* TODO: onPreExecure */
        super.onPreExecute();

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Searching through contacts");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(ArrayList<String>... params) {
        /* TODO: doInBackground*/
        ArrayList<String> numbers = params[0];

        String contactId = "";
        String contactName = "";

        String[] projection = new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.PhoneLookup.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        ContentResolver cr = context.getContentResolver();

        HashMap<String, String> entries = new HashMap<>();

        for (String phoneNumber : numbers) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Log.d("Info", "Searching for number " + phoneNumber);
            if (cr != null) {
                Cursor resultCur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
                if (resultCur != null) {
                    if (resultCur.getCount() <= 0) {
                        Log.d("Info", "No contact with this number found ");
                    } else {
                        while (resultCur.moveToNext()) {
                            contactId = resultCur.getString(resultCur.getColumnIndex(ContactsContract.PhoneLookup._ID));
                            contactName = resultCur.getString(resultCur.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            Log.e("Info", "Contact Id : " + contactId);
                            Log.e("Info", "Contact Display Name : " + contactName);
                            entries.put(contactName, phoneNumber);
                            break;
                        }
                        resultCur.close();
                    }
                }
            }
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        /* TODO: onProgressUpdate */
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String aString) {
        /* TODO: onPostExecute */
        super.onPostExecute(aString);
        progressDialog.dismiss();
    }
}
