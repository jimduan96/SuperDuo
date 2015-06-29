package barqsoft.footballscores;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by e39178 on 6/24/2015.
 */
public class WidgetConfigurationActivity extends Activity {

    private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.football_widgetlayout);
        setResult(RESULT_CANCELED);

        int[] ids = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(
                new ComponentName(getApplicationContext(), FootballAppWidgetProvider.class));

        // only once instance is allowed
        if(ids.length > 1){
            Toast.makeText(getApplicationContext(), R.string.onlyone, Toast.LENGTH_SHORT).show();
            Log.v(TAG, "onCreate: instance > 1");
            //do not add the widget
            finish();
            return;
        }

        // verify this ID
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(getApplicationContext(), R.string.onlyone, Toast.LENGTH_SHORT).show();
            //Log.v(TAG, "onCreate: invalid widget id");
            finish();
        }

        // show it on homescreen
        Intent intnt = new Intent();
        intnt.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, intnt);

        // finish this activity
        //Log.v(TAG, "onCreate success configuration");
        Toast.makeText(getApplicationContext(), R.string.add_one_pls, Toast.LENGTH_SHORT).show();
        finish();
    }
}
