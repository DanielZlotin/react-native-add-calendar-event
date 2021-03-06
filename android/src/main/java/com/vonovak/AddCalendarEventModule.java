package com.vonovak;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.provider.CalendarContract.Events;
import com.facebook.react.bridge.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;


public class AddCalendarEventModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final String ADD_EVENT_MODULE_NAME = "AddCalendarEvent";
    private final int ADD_EVENT_REQUEST_CODE = 11;
    private static final String DATE_PARSING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private Promise promise = null;


    public AddCalendarEventModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return ADD_EVENT_MODULE_NAME;
    }

    private static long getTimestamp(String dateAsString) throws ParseException {
        SimpleDateFormat datetimeFormatter = new SimpleDateFormat(DATE_PARSING_FORMAT);
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return datetimeFormatter.parse(dateAsString).getTime();
    }

    @ReactMethod
    public void presentEventDialog(ReadableMap config, Promise eventPromise) {
        promise = eventPromise;

        try {
            final Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
            calendarIntent
                    .setData(Events.CONTENT_URI)
                    .putExtra(Events.TITLE, config.getString("title"));

            if (config.hasKey("startDate")) {
                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getTimestamp(config.getString("startDate")));
            }

            if (config.hasKey("endDate")) {
                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, getTimestamp(config.getString("endDate")));
            }

            if (config.hasKey("location")
                    && config.getString("location") != null) {
                calendarIntent.putExtra(Events.EVENT_LOCATION, config.getString("location"));
            }

            if (config.hasKey("description")
                    && config.getString("description") != null) {
                calendarIntent.putExtra(Events.DESCRIPTION, config.getString("description"));
            }

            if (config.hasKey("allDay")) {
                calendarIntent.putExtra(Events.ALL_DAY, config.getBoolean("allDay"));
            }

            getReactApplicationContext().startActivityForResult(calendarIntent, ADD_EVENT_REQUEST_CODE, Bundle.EMPTY);
        } catch (Exception e) {
            rejectPromise(e);
        }
    }

    @Override
    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode != ADD_EVENT_REQUEST_CODE || promise == null) {
            return;
        }

        if (resultCode == RESULT_OK) {
            promise.resolve(true);
        } else {
            promise.reject(ADD_EVENT_MODULE_NAME, "cancelled");
        }
    }

    private void rejectPromise(Exception e) {
        if (promise == null) {
            Log.e(ADD_EVENT_MODULE_NAME, "promise is null");
            return;
        }
        promise.reject(ADD_EVENT_MODULE_NAME, e.getMessage());
    }


    @Override
    public void onNewIntent(Intent intent) {
    }
}
