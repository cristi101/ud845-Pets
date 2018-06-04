/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import java.io.File;



/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    /** Database helper that will provide us access to the database */
    private PetDbHelper mDbHelper;
    private CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        createDBDir(); // ensure the db path exists

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);

        mCursorAdapter = new PetCursorAdapter(this, null);

        ListView displayView = (ListView) findViewById(R.id.list_view_pet);
        displayView.setEmptyView(findViewById(R.id.empty_view));
        displayView.setAdapter(mCursorAdapter);

        displayView.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);

    }

    void createDBDir() {
        File dataDir = getDatabasePath("test.db").getParentFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            dataDir.setWritable(true);
            dataDir.setReadable(true);
        }
    }


    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        // Gets the database in write mode
        ContentResolver resolver = getContentResolver();

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
        Uri newRowId = resolver.insert(PetEntry.CONTENT_URI, values);
    }

    private void deletePets() {
        ContentResolver resolver = getContentResolver();
        resolver.delete(PetEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                deletePets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != 0) return null;
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id));
        startActivity(intent);
    }
}
