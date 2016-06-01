package com.nwa.smartgym.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.nwa.smartgym.R;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String SESSION_NAME = "S7";
    private final String tag = "NWA";

    private long startTime;
    private long endTime;

    private GoogleApiClient mGoogleApiClient = null;
    private Session mSession;

    private Map<Field, Value> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//
//            } else {
//
//                 No explanation needed, we can request the permission.
//                 result of the request.
//            }
//        }
//
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SESSIONS_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();

        Log.i(tag, String.valueOf(mGoogleApiClient));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_start_session)
    public void startSession() {
        if (!mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            Log.i(tag, "Google fit was nog niet klaar.");
            return;
        }

        startTime = Calendar.getInstance().getTimeInMillis();
        mSession = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Rennende sessie hoor")
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING_TREADMILL)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.startSession(mGoogleApiClient, mSession);

        pendingResult.setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            String message = "JAWEL GESTART";
                            Log.i(tag, message);
                        } else {
                            String message = "NEE NEE GEFAALT";
                            Log.i(tag, message + " " + status.getStatusMessage());
                        }
                    }
                }
        );
    }

    @OnClick(R.id.btn_stop_session)
    public void stopSession() {
        if (mSession == null) {
            Log.i(tag, "Geen sessie actief");
            return;
        }

         PendingResult<SessionStopResult> pendingResult =
                Fitness.SessionsApi.stopSession(mGoogleApiClient, mSession.getIdentifier());

        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(@NonNull SessionStopResult sessionStopResult) {
                if (sessionStopResult.getStatus().isSuccess()) {
                    String message = "JAH JAH GOEDZO GESTOPT";
                    Log.i(tag, message);
                } else {
                    String message = "AND YOU FAILED";
                    Log.i(tag, message + " " + sessionStopResult.getStatus().getStatusMessage());
                }
            }
        });

        endTime = Calendar.getInstance().getTimeInMillis();
    }

    public void setData(Value data) {
//        if (this.data == null) {
//            this.data = new HashMap<>();
//        } else {
//            this.data.put(null,null);
//        }
    }

    public Map<Field, Value> getField(List<DataType> dataTypeList) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        startTime = cal.getTimeInMillis();

        this.data = new HashMap<>();

        for (DataType dataType : dataTypeList) {
            SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .setSessionName(SESSION_NAME)
                    .read(dataType)
                    .build();

            PendingResult<SessionReadResult> sessionReadResult = Fitness.SessionsApi.readSession(mGoogleApiClient, sessionReadRequest);

            sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
                @Override
                public void onResult(@NonNull SessionReadResult sessionReadResult) {
                    Log.i(tag, " on result called!~");

                    if (sessionReadResult.getStatus().isSuccess()) {
                        List<Session> sessions = sessionReadResult.getSessions();
                        for (Session session : sessions) {
                            for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                    for (Field field : dataPoint.getDataType().getFields()) {
                                        Value value = dataPoint.getValue(field);
                                        MainActivity.this.data.put(field, value);
                                    }
                                }
                            }
                        }
                    }

                    Log.i(tag, String.valueOf(MainActivity.this.data));

                    TextView viewById = (TextView) findViewById(R.id.textView3);
                    if (viewById != null) {
                        viewById.setText(String.valueOf(MainActivity.this.data));
                    }
                }
            });

        }




        return data;
    }

    @OnClick(R.id.btn_read_session)
    public void readSession() {
        final List<DataType> dataTypes = Arrays.asList(DataType.TYPE_DISTANCE_DELTA);

        long start = System.currentTimeMillis();


        Map<Field, Value> field = getField(dataTypes);

        Log.i(tag, "in de read knop " + String.valueOf(field));

        Log.i(tag, String.valueOf(System.currentTimeMillis() - start));




//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//        FutureTask<Map<Field, Value>> future =
//                new FutureTask<>(new Callable<Map<Field, Value>>() {
//                    public Map<Field, Value> call() {
//                        Map<Field, Value> field = getField(dataTypes);
//
//
//                        TextView viewById = (TextView) findViewById(R.id.textView3);
//                        if (viewById != null) {
//                            viewById.setText(String.valueOf(field));
//                        }
//                        return field;
//                    }
//                });
//
//
//        executorService.execute(future);
//
//
//
//        try {
//            Map<Field, Value> values = future.get();
//
//            TextView viewById = (TextView) findViewById(R.id.textView3);
//            if (viewById != null) {
//                viewById.setText(String.valueOf(values));
//            }
//
//            Log.i(tag, "2 "+ String.valueOf(values));
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }





//        Set<DataType> aggregateInputTypes = DataType.AGGREGATE_INPUT_TYPES;
//        SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder()
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//                .setSessionName(SESSION_NAME)
//                .read(DataType.TYPE_DISTANCE_DELTA)
//                .build();
//
//        PendingResult<SessionReadResult> sessionReadResult = Fitness.SessionsApi.readSession(mGoogleApiClient, sessionReadRequest);
//
//
//        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
//            @Override
//            public void onResult(@NonNull SessionReadResult sessionReadResult) {
//                if (sessionReadResult.getStatus().isSuccess()) {
//                    String message = "GOEDZO READ SESSION GELUKT";
//                    Log.i(tag, message);
//
//                    List<Session> sessions = sessionReadResult.getSessions();
//                    for (Session session : sessions) {
//                        if (!SESSION_NAME.equals(session.getIdentifier())) {
//                            continue;
//                        }
//
//                        Log.i(tag, "Sessie naampje: " + session.getName());
//
//                        List<DataSet> datasetList = sessionReadResult.getDataSet(session);
//                        for (DataSet dataSet : datasetList) {
//                            DataSource dataSource = dataSet.getDataSource();
//                            DataType dataType = dataSet.getDataType();
//                            List<DataPoint> dataPoints = dataSet.getDataPoints();
//
//                            Log.i(tag, "FDFDF ! " + String.valueOf(dataSet.getDataType().getFields()));
//                            for (DataPoint dataPoint : dataPoints) {
//                                List<Field> fields = dataPoint.getDataType().getFields();
//
//                                Log.i(tag, String.valueOf(fields));
////                                Log.i(tag, "Afstand gerent hoor: " + dataPoint.getValue(Field.FIELD_DISTANCE));
//                                Log.i(tag, "Distance: " + dataPoint.getValue(Field.FIELD_DISTANCE));
//                            }
//                        }
//                    }
//                } else {
//                    String message = "AND YOU FAILED AGAIN READING";
//                    Log.i(tag, message);
//                }
//            }
//        });
    }

    @OnClick(R.id.btn_delete_session)
    public void deleteSessions() {
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .deleteAllSessions()
                .build();

        PendingResult<Status> deleteRequest = Fitness.HistoryApi.deleteData(mGoogleApiClient, request);
        deleteRequest.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if( status.isSuccess() ) {
                    Log.i(tag, "Successfully deleted sessions");
                } else {
                    Log.i(tag, "Failed to delete sessions");
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
