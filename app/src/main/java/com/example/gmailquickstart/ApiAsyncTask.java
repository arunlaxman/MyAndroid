package com.example.gmailquickstart;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.services.gmail.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Gmail API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mActivity;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Gmail API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (Exception e) {
            mActivity.updateStatus("The following error occurred:\n" +
                    e.getMessage());
        }
        return null;
    }

    /**
     * Fetch a list of Gmail labels attached to the specified account.
     * @return List of Strings labels.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
        // Get the labels in the user's account.
        String user = "me";
        List<String> labels = new ArrayList<String>();
        ListLabelsResponse listLabelsResponse = mActivity.mService.users().labels().list(user).execute();
        for(Label label : listLabelsResponse.getLabels()) {
            Label labelResponse = mActivity.mService.users().labels().get(user, label.getId()).execute();
            labels.add(labelResponse.getName() + " : " + labelResponse.getMessagesTotal() + " : " + labelResponse.getMessagesUnread() + " : " + labelResponse.getType());
        }

        ListMessagesResponse listMessagesResponse = mActivity.mService.users().messages().list(user).setMaxResults(20L).execute();

        for (Message message : listMessagesResponse.getMessages()) {
            Message msg = mActivity.mService.users().messages().get(user, message.getId()).setFormat("metadata").setFields("payload/headers").execute();
            //labels.add(msg.toPrettyString());
            for(MessagePartHeader messagePartHeader : msg.getPayload().getHeaders()) {
                String headerName = messagePartHeader.getName();
                if(headerName.equalsIgnoreCase("Subject") || headerName.equalsIgnoreCase("From")) {
                    labels.add(headerName + " : " + messagePartHeader.getValue());
                }
            }
        }
        return labels;
    }

}