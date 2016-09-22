package co.zero.signin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        View.OnClickListener{
    private static final int STATE_SIGNED_IN = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;

    private GoogleApiClient googleApiClient;
    private SignInButton buttonSignIn;
    private Button buttonSignOut;
    private Button buttonRevoke;
    private TextView status;
    private int currentStatus = STATE_SIGN_IN;

    private PendingIntent signInIntent;
    private int signInError;
    private final int RC_SIGN_IN = 0;
    private int DIALOG_PLAY_SERVICES_ERROR = 0;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googleApiClient = buildGoogleApiClient();

        buttonSignIn = (SignInButton)findViewById(R.id.btn_signin);
        buttonSignOut = (Button)findViewById(R.id.btn_signout);
        buttonRevoke = (Button)findViewById(R.id.btn_revoke);
        status = (TextView)findViewById(R.id.status);

        buttonSignIn.setOnClickListener(this);
        buttonSignOut.setOnClickListener(this);
        buttonRevoke.setOnClickListener(this);

        buttonRevoke.setEnabled(false);
        buttonSignOut.setEnabled(false);
    }

    /**
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        currentStatus = STATE_SIGNED_IN;
        buttonSignIn.setEnabled(false);
        buttonRevoke.setEnabled(true);
        buttonSignOut.setEnabled(true);

        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        status.setText(person.getDisplayName());
    }

    /**
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(this.getClass().getSimpleName(), "Connection Suspended Cause : " + i);
        googleApiClient.connect();
    }

    /**
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(this.getClass().getSimpleName(), "onConnectionFailed: ErrorCode = " + connectionResult.getErrorCode());

        if(currentStatus != STATE_IN_PROGRESS) {
            signInIntent = connectionResult.getResolution();
            signInError = connectionResult.getErrorCode();

            if(currentStatus == STATE_SIGNED_IN) {
                resolveSignInError();
            }
        }

        onSignOut();
    }

    @Override
    public void onClick(View v) {
        if(!googleApiClient.isConnecting()){
            switch (v.getId()){
                case R.id.btn_signin:
                    status.setText("Signing In ...");
                    resolveSignInError();
                    break;
                case R.id.btn_signout:
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    googleApiClient.connect();
                    break;
                case R.id.btn_revoke:
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
                    googleApiClient = buildGoogleApiClient();
                    googleApiClient.connect();
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if(googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RC_SIGN_IN:
                if(resultCode == RESULT_OK){
                    currentStatus = STATE_SIGN_IN;
                }else{
                    currentStatus = STATE_SIGN_IN;
                }

                if(!googleApiClient.isConnecting()){
                    googleApiClient.connect();
                }

                break;
        }
    }

    private void resolveSignInError(){
        if(signInIntent != null) {
            try {
                currentStatus = STATE_IN_PROGRESS;
                startIntentSenderForResult(signInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            }catch (IntentSender.SendIntentException e) {
                Log.i(this.getClass().getSimpleName(), e.getLocalizedMessage());
                currentStatus = STATE_SIGNED_IN;
                googleApiClient.connect();
            }
        }else {
            showDialog(DIALOG_PLAY_SERVICES_ERROR);
        }
    }

    private void onSignOut() {
        buttonSignIn.setEnabled(true);
        buttonSignOut.setEnabled(false);
        buttonRevoke.setEnabled(false);

        status.setText("Signed Out...");
    }

    private GoogleApiClient buildGoogleApiClient(){
        // Create an instance of GoogleAPIClient.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }
}
