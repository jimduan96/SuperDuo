package barqsoft.footballscores.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.FootballAppWidgetProvider;
import barqsoft.footballscores.R;

/**
 * Created by e39178 on 6/27/2015.
 */
public class WidgetDataFetchService extends Service {

    private static final String TAG = WidgetDataFetchService.class.getSimpleName();

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static double _mMatchId = 0;
    public static double getMatchId() {
        return _mMatchId;
    }
    public static String getMatchIdString() {
        return (Double.toString(_mMatchId)).split("\\.")[0];
    }
    public static void setMatchId(double id) {
        _mMatchId = id;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");

        if (intent != null && intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.v(TAG, "onStartCommand UPDATEREQ app widget id invalid");
            }

            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                String requestedAction = intent.getAction();

                if (requestedAction != null &&
                        requestedAction.equals(FootballAppWidgetProvider.UPDATEREQ)) {
                    Log.v(TAG, "onStartCommand process UPDATEREQ");

                    if (getMatchId() == 0) {
                        Toast.makeText(getApplicationContext(),
                                R.string.addmatchpls, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        ReadJsonTask rjt = new ReadJsonTask();
                        rjt.execute(getMatchIdString());

                        /*AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(this);
                        RemoteViews views = new RemoteViews(
                                this.getPackageName(), R.layout.football_widgetlayout);
                        appWidgetMan.updateAppWidget(mAppWidgetId, views);*/
                        //Log.v(TAG, "onStartCommand Send Back UPDATEREQ");
                        //this.stopSelf(); // need this?
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class ReadJsonTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... url) {
            //Creating fetch URL
            final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
            //http://api.football-data.org/alpha/fixtures/136784
            Uri fetch_build = Uri.parse(BASE_URL + "/" + url[0]);
            Log.v(TAG, fetch_build.toString()); //log spam

            HttpURLConnection m_connection = null;
            BufferedReader reader = null;
            String JSON_data = null;
            //Opening Connection
            try {
                URL fetch = new URL(fetch_build.toString());
                m_connection = (HttpURLConnection) fetch.openConnection();
                m_connection.setRequestMethod("GET");
                m_connection.setRequestProperty("Content-length", "0");
                //m_connection.addRequestProperty("X-Auth-Token", "e136b7858d424b9da07c88f28b61989a");
                m_connection.connect();
                Log.v(TAG, "after connect");

                // Read the input stream into a String
                InputStream inputStream = m_connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JSON_data = buffer.toString();
            } catch (Exception e) {
                Log.e(TAG, "Exception here " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (m_connection != null) {
                    m_connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error Closing Stream");
                    }
                }
            }

            return JSON_data;
        }

        @Override
        protected void onPostExecute(String JSONdata) {
            if (JSONdata == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.wrongnetworkdata, Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e(TAG, JSONdata);
            //JSON data
            final String SERIE_A = "357";
            final String PREMIER_LEGAUE = "354";
            final String CHAMPIONS_LEAGUE = "362";
            final String PRIMERA_DIVISION = "358";
            final String BUNDESLIGA = "351";
            final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
            final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
            final String FIXTURE = "fixture";
            final String LINKS = "_links";
            final String SOCCER_SEASON = "soccerseason";
            final String SELF = "self";
            final String MATCH_DATE = "date";
            final String HOME_TEAM = "homeTeamName";
            final String AWAY_TEAM = "awayTeamName";
            final String RESULT = "result";
            final String HOME_GOALS = "goalsHomeTeam";
            final String AWAY_GOALS = "goalsAwayTeam";
            final String MATCH_DAY = "matchday";

            //Match data
            String League = null;
            String mDate = null;
            String mTime = null;
            String Home = null;
            String Away = null;
            String Home_goals = null;
            String Away_goals = null;
            String match_id = null;
            String match_day = null;

            try {
                JSONObject match_data = new JSONObject(JSONdata).getJSONObject(FIXTURE);
                Log.e(TAG, "parsing match data");

                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString("href");
                match_id = match_id.replace(MATCH_LINK, "");

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0, mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate + mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0, mDate.indexOf(":"));
                } catch (Exception e) {
                    Log.d(TAG, "error here!");
                    Log.e(TAG, e.getMessage());
                }
                Home = match_data.getString(HOME_TEAM);
                Away = match_data.getString(AWAY_TEAM);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);

                int[] appWidgetIds = AppWidgetManager.getInstance(getApplicationContext())
                        .getAppWidgetIds(new ComponentName(
                                getApplicationContext(), FootballAppWidgetProvider.class));
                for(int i = 0; i < appWidgetIds.length; i++) {
                    AppWidgetManager appWidgetManager =
                            AppWidgetManager.getInstance(getApplicationContext());

                    RemoteViews remoteViews = new RemoteViews(
                            getApplicationContext().getPackageName(),
                            R.layout.football_widgetlayout);
                    remoteViews.setTextViewText(R.id.home_name, Home);
                    remoteViews.setTextViewText(R.id.away_name, Away);
                    remoteViews.setTextViewText(R.id.score_textview,
                            Home_goals + "-" + Away_goals);
                    remoteViews.setTextViewText(R.id.data_textview,
                            mTime + " " + mDate);

                    appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}