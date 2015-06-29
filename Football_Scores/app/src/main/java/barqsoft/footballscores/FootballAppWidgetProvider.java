package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.WidgetDataFetchService;

/**
 * Created by e39178 on 6/27/2015.
 */
public class FootballAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = FootballAppWidgetProvider.class.getSimpleName();
    
    public static final String UPDATEREQ = "updatereq";

    public static final String MATCH_ID = "matchid";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.v(TAG, "onUpdate");

        for (int i=0; i<appWidgetIds.length; i++) {
            //Log.v(TAG, "onUpdate -1");

            // set up Refresh button event
            Intent clickIntent = new Intent(context, WidgetDataFetchService.class);
            clickIntent.setAction(UPDATEREQ);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            PendingIntent pendingIntent = PendingIntent.getService(context, 2, clickIntent, 0);
            RemoteViews views = new RemoteViews(
                    context.getPackageName(), R.layout.football_widgetlayout);
            views.setOnClickPendingIntent(R.id.match_update_button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.v(TAG, "onReceive");
        //User is adding one match into widget
        if(intent.getAction()!=null && UPDATEREQ.equals(intent.getAction())) {
            //Log.v(TAG, "onReceive UPDATEREQ");

            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // get Match ID
                WidgetDataFetchService.setMatchId(
                        intent.getDoubleExtra(FootballAppWidgetProvider.MATCH_ID, 0.0));
                if (WidgetDataFetchService.getMatchId() == 0.0) {
                    Log.v(TAG, "onReceive invalid match id");
                }

                Intent updtIntent = new Intent(context, WidgetDataFetchService.class);
                updtIntent.setAction(UPDATEREQ);
                updtIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                updtIntent.setData(Uri.parse(updtIntent.toUri(Intent.URI_INTENT_SCHEME)));
                context.startService(updtIntent);

                /*AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.football_widgetlayout);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);*/
            }
        }

        super.onReceive(context, intent);
    }
}
