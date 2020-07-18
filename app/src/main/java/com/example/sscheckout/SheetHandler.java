package com.example.sscheckout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SheetHandler {

    public static GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    public static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS};

    public static List<List<Object>> getData(AppCompatActivity context, String spreadsheetId, String sheetName, String lastCell) {
        return getDataRange(context, spreadsheetId, sheetName, "A2:"+lastCell);
    }

    public static int getNumOfItems(AppCompatActivity context) throws Exception{
        Exception e = new Exception("Empty spreadsheet Id");
        if (SettingsActivity.spreadsheetId == "" || SettingsActivity.spreadsheetId == null){
            throw e;
        }
        List<List<Object>> rawRes = getDataRange(context, SettingsActivity.spreadsheetId, "Info", "B2:B2");
        return Integer.parseInt((String) rawRes.get(0).get(0));
    }

    public static int getNumOfTabs(AppCompatActivity context) throws Exception{
        Exception e = new Exception("Empty spreadsheet Id");
        if (SettingsActivity.spreadsheetId == "" || SettingsActivity.spreadsheetId == null){
            throw e;
        }
        List<List<Object>> rawRes = getDataRange(context, SettingsActivity.spreadsheetId, "Info", "B3:B3");
        return Integer.parseInt((String) rawRes.get(0).get(0));
    }

    public static List<TabInfo> getTabs (AppCompatActivity context) throws Exception{
        int tabNum = getNumOfTabs(context);
        String tabRange = "A2:B"+String.valueOf(2+tabNum-1);
        List<List<Object>> rawRes = getDataRange(context, SettingsActivity.spreadsheetId, "Tabs", tabRange);
        List<TabInfo> result = new ArrayList<>();
        for (int i = 0; i<tabNum; i++){
            List<Object> row = rawRes.get(i);
            String range = "A"+String.valueOf(2+i)+":B"+String.valueOf(2+i);
            String name = (String) row.get(0);
            double tab = Double.parseDouble((String) row.get(1));
            result.add(new TabInfo(name, tab, range));
        }
        return result;
    }

    public static void updateTab (AppCompatActivity context, TabInfo newTab) throws Exception{
        Exception e = new Exception("Empty spreadsheet Id");
        if (SettingsActivity.spreadsheetId == "" || SettingsActivity.spreadsheetId == null){
            throw e;
        }
        Object[] rawTab = {newTab.getName(), String.valueOf(newTab.getTab())};
        List<List<Object>> values = Arrays.asList(Arrays.asList(rawTab));
        updateDataRange(context, SettingsActivity.spreadsheetId, "Tabs", newTab.getRange(), values);
    }

    public static List<List<Object>> getDataRange(AppCompatActivity context, String spreadsheetId, String sheetName, String range){
        mCredential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        createPrerequisites(context);
        ReadRequestTask task = new ReadRequestTask(mCredential, context, spreadsheetId, sheetName, range);
        task.execute();
        while (task.isResultReady() == false){ }
        return task.getResponse();
    }

    public static void updateDataRange (AppCompatActivity context, String spreadsheetId, String sheetName, String range, List<List<Object>> values){
        mCredential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        createPrerequisites(context);
        WriteRequestTask task = new WriteRequestTask(mCredential, context, spreadsheetId, sheetName, range, values);
        task.execute();
        while (task.isResultReady() == false){

        }
    }

    public static void addNewTab(AppCompatActivity context, String name, double totalCost) throws Exception{
        Exception e = new Exception("Empty spreadsheet Id");
        if (SettingsActivity.spreadsheetId == "" || SettingsActivity.spreadsheetId == null){
            throw e;
        }
        Object[] valUpdate = {name, String.valueOf(totalCost)};
        List<List<Object>> values = Arrays.asList(Arrays.asList(valUpdate));
        int numOfTabs = getNumOfTabs(context);
        String range = "A"+String.valueOf(numOfTabs+2)+":"+"B"+String.valueOf(numOfTabs+2);
        updateDataRange(context, SettingsActivity.spreadsheetId, "Tabs", range, values);
    }

    public static void createPrerequisites(AppCompatActivity context) {
        if (! isGooglePlayServicesAvailable(context)) {
            Log.d("GoogleProblem", "acquire Google");
            acquireGooglePlayServices(context);
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("GoogleProblem", "choose account");
            chooseAccount(context);
        }
    }

    private static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private static void acquireGooglePlayServices(AppCompatActivity context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, context);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    static void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode, Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private static void chooseAccount(AppCompatActivity context) {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            Log.d("GoogleProblem", "Has Permission");
            String accountName = context.getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                Log.d("GoogleProblem", "account name =="+accountName);
                SheetHandler.mCredential.setSelectedAccountName(accountName);
                createPrerequisites(context);
            } else {
                // Start a dialog from which the user can choose an account
                Log.d("GoogleProblem", "start dialog for choosing account");
                context.startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            Log.d("GoogleProblem", "request GET_ACCOUNTS permission");
            EasyPermissions.requestPermissions(
                    context,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    private static class ReadRequestTask extends AsyncTask<Void, Void, List<List<Object>>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private AppCompatActivity context;

        private String spreadsheetId;
        private String sheetName;
        private String range;
        private GoogleAccountCredential credential;

        private List<List<Object>> response;
        private boolean resultReady = false;

        ReadRequestTask(GoogleAccountCredential credential, AppCompatActivity context, String spreadsheetId, String sheetName, String range) {
            this.context = context;
            this.spreadsheetId = spreadsheetId;
            this.sheetName = sheetName;
            this.range = range;
            this.credential = credential;
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<List<Object>> doInBackground(Void... params) {
            try {
                this.response = getDataFromApi(this.spreadsheetId, this.sheetName, this.range);
                Log.d("GoogleProblem", "Data Get Success");
                resultReady = true;
                return this.response;
            } catch (Exception e) {
                mLastError = e;
                Log.d("GoogleProblem", "Cancelled with Exception"+String.valueOf(e==null));
                cancel(true);
                resultReady = true;
                this.response = null;
                return this.response;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<List<Object>> getDataFromApi(String spreadsheetId, String sheetName, String range) throws Exception {
            while (mCredential.getSelectedAccountName() == null){
                Thread.sleep(100);
                Log.d("GoogleProblem", "waiting");
            }
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();

            range = sheetName+"!"+range;
            List<String> results = new ArrayList<String>();
            Log.d("GoogleProblem", "before sending");
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            Log.d("GoogleProblem", "after sending");
            List<List<Object>> values = response.getValues();
            Log.d("GoogleProblem", "result is "+String.valueOf(values == null));
            return values;
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode(), context);
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    context.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SheetHandler.REQUEST_AUTHORIZATION);
                }
                Log.e("GoogleProblem", "The following error occurred:\n"
                            + mLastError.getMessage()+mLastError.getStackTrace());
            }
        }

        public List<List<Object>> getResponse(){
            return this.response;
        }

        public boolean isResultReady() {
            return resultReady;
        }
    }

    private static class WriteRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private AppCompatActivity context;

        private String spreadsheetId;
        private String sheetName;
        private String range;
        private List<List<Object>> values;
        private GoogleAccountCredential credential;

        private boolean resultReady = false;

        WriteRequestTask(GoogleAccountCredential credential, AppCompatActivity context, String spreadsheetId, String sheetName, String range, List<List<Object>> values) {
            this.context = context;
            this.spreadsheetId = spreadsheetId;
            this.sheetName = sheetName;
            this.range = range;
            this.credential = credential;
            this.values = values;
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                writeDataToApi(this.spreadsheetId, this.sheetName, this.range, this.values);
                Log.d("GoogleProblem", "Data Write Success");
            } catch (Exception e) {
                mLastError = e;
                Log.d("GoogleProblem", "Cancelled with Exception"+String.valueOf(e==null));
                cancel(true);
            }
            resultReady = true;
            return null;
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private void writeDataToApi(String spreadsheetId, String sheetName, String range, List<List<Object>> values) throws Exception {
            while (mCredential.getSelectedAccountName() == null){
                Thread.sleep(100);
                Log.d("GoogleProblem", "waiting");
            }
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();

            range = sheetName+"!"+range;
            ValueRange body = new ValueRange().setValues(values);
            Log.d("GoogleProblem", "before sending");
            UpdateValuesResponse result = mService.spreadsheets().values().update(spreadsheetId, range, body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
            Log.d("GoogleProblem", "after sending");
            int updatedCells = result.getUpdatedCells();
            Log.d("GoogleProblem", String.valueOf(updatedCells)+" cell(s) updated");
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode(), context);
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    context.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SheetHandler.REQUEST_AUTHORIZATION);
                }
                Log.e("GoogleProblem", "The following error occurred:\n"
                        + mLastError.getMessage()+mLastError.getStackTrace());
            }
        }

        public boolean isResultReady() {
            return resultReady;
        }
    }
}
