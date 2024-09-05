package com.example.runningtracker.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.runningtracker.model.RunDao;
import com.example.runningtracker.model.RunDatabase;

public class RunContentProvider extends ContentProvider {
    private RunDatabase runDb;
    private RunDao runDao;

    // defining authority of this content provider
    public static final String AUTHORITHY = "runningtracker.contentprovider.RunContentProvider";

    // Code for multiple items in run table
    private static final int CODE_ALL_RUN = 1;

    // Code for a single item in run table
    private static final int CODE_SINGLE_RUN = 2;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITHY, "run_table", CODE_ALL_RUN);
        MATCHER.addURI(AUTHORITHY, "run_table" + "/*", CODE_SINGLE_RUN);
    }

    @Override
    public boolean onCreate() {
        runDb = RunDatabase.getDatabase(getContext());
        runDao = runDb.runDao();
        return true;
    }

    @Nullable
    @Override
    // returns a cursor to a single run or all the runs depending on code provided in URI
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);

        // we are using a array here because cursor must be final
        // but if we just do a normal final cursor we cant declare it then assign a value
        // so a work around is to make it into an array, so that we can declare the array then
        // assign the variable.
        final Cursor[] cursor = new Cursor[1];
        if (code >= 1) {
            switch(code) {
                case CODE_ALL_RUN:
                    cursor[0] = runDao.getAllRuns();
                    break;
                case CODE_SINGLE_RUN:
                    cursor[0] = runDao.getRunById((int) ContentUris.parseId(uri));
                    break;
                default:
                    cursor[0] = null;
                    break;
            }
            cursor[0].setNotificationUri(getContext().getContentResolver(), uri);

            return cursor[0];
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
