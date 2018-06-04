package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.pets.data.PetContract.PetEntry;

import java.util.Arrays;

public class PetProvider extends ContentProvider {
    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new PetDbHelper(getContext());
        return true;
    }

    // add _ID to the where clause
    private String newSelection(String selection) {

        if (selection == null) selection = "";
        else if (!selection.isEmpty())
            selection = "(" + selection + ") and ";

        selection += PetEntry._ID + "=?";
        return selection;
    }

    // add the _ID to the list of args
    private String[] newArgs(Uri uri, String[] selectionArgs) {
        String[] args;
        // find the _ID value
        String id = String.valueOf(ContentUris.parseId(uri));
        if (selectionArgs != null) {
            int last = selectionArgs.length;
            args = Arrays.copyOf(selectionArgs, last + 1);
            args[last] = id;
        } else args = new String[]{id};
        return args;
    }

    private void checkNew(ContentValues values) throws IllegalArgumentException {
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Pet requires a name");

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender))
            throw new IllegalArgumentException("Pet requires valid gender");

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0)
            throw new IllegalArgumentException("Pet weight must be positive");

    }

    private void checkUpdate(ContentValues values) throws IllegalArgumentException {
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name != null && name.isEmpty())// allow nulls
            throw new IllegalArgumentException("Pet requires a name");

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != null && !PetEntry.isValidGender(gender))// allow nulls
            throw new IllegalArgumentException("Pet requires valid gender");

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0)
            throw new IllegalArgumentException("Pet weight must be positive");

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                cursor = database.query(PetEntry.TABLE_NAME, projection, newSelection(selection), newArgs(uri, selectionArgs), null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        checkNew(values);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        if (id != -1) getContext().getContentResolver().notifyChange(uri, null);
        return (id == -1) ? null : ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int result;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                result = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                result = database.delete(PetEntry.TABLE_NAME, newSelection(selection), newArgs(uri, selectionArgs));
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (result > 0) getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (values.size() == 0) return 0;
        checkUpdate(values);
        int result;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                result = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PET_ID:
                result = database.update(PetEntry.TABLE_NAME, values, newSelection(selection), newArgs(uri, selectionArgs));
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        if (result > 0) getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
}
