package com.nwa.smartgym.activities;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.android.apptools.OrmLiteCursorAdapter;
import com.j256.ormlite.android.apptools.OrmLiteCursorLoader;
import com.j256.ormlite.dao.Dao;
import com.nwa.smartgym.R;
import com.nwa.smartgym.api.interfaces.BuddyAPIInterface;
import com.nwa.smartgym.lib.DatabaseHelper;
import com.nwa.smartgym.lib.adapters.BuddyAdapter;
import com.nwa.smartgym.models.Buddy;

import java.sql.SQLException;
import java.util.UUID;

public class Buddies extends OrmLiteBaseListActivity<DatabaseHelper> {
    private Dao<Buddy, UUID> buddyDao;
    private OrmLiteCursorAdapter<Buddy, RelativeLayout> viewAdapter;
    private BuddyAPIInterface buddyAPIInterface;

    public Buddies() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddies);

        try {
            buddyDao = getHelper().getBuddyDao();
        } catch( SQLException e) {
            Log.e(this.getClass().getName(), "Unable to access database", e);
        }

        buddyAPIInterface = new BuddyAPIInterface(this, buddyDao);
        viewAdapter = new BuddyAdapter(this, buddyAPIInterface);
        setListAdapter(viewAdapter);

        getLoaderManager().initLoader(0, null, new android.app.LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                buddyAPIInterface.list();
                return new OrmLiteCursorLoader<>(getBaseContext(), buddyDao, buddyAPIInterface.getListQuery());
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                viewAdapter.changeCursor(cursor, ((OrmLiteCursorLoader<Buddy>) loader).getQuery());
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                viewAdapter.changeCursor(null,null);
            }
        });
    }
}
