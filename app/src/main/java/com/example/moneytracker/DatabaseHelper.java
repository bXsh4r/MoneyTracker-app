package com.example.moneytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // History table
    public static final String MONEY_HISTORY_TABLE = "MoneyHistory_table";
    public static final String ID = "id";
    public static final String OPERATION = "operation";
    public static final String AMOUNT = "amount";
    public static final String DESCRIPTION = "description";
    public static final String DATE = "date";

    // New user table
    public static final String NEW_USER_TABLE = "NewUser_table";
    public static final String FIRST_TIME = "firstTime";
    public static final String FIRST_TIME_ID = "firstTimeID";

    // Total amount table
    public static final String TOTAL_MONEY_TABLE = "totalMoneyAmount_table";
    public static final String TOTAL_AMOUNT = "totalAmount";
    public static final String TOTAL_ID = "totatID";


    public DatabaseHelper(@Nullable Context context) {
        super(context, "MoneyTracking.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable1 = "CREATE TABLE " + MONEY_HISTORY_TABLE + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OPERATION + " TEXT, " +
                AMOUNT + " TEXT, " +
                DESCRIPTION + " TEXT, " +
                DATE + " TEXT)";

        String createTable2 = "CREATE TABLE " + NEW_USER_TABLE + " (" + FIRST_TIME_ID + " INTEGER PRIMARY KEY, " + FIRST_TIME + " INTEGER)";

        String createTable3 = "CREATE TABLE " + TOTAL_MONEY_TABLE + " (" + TOTAL_ID + " INTEGER PRIMARY KEY, " + TOTAL_AMOUNT + " TEXT)";

        db.execSQL(createTable1);
        db.execSQL(createTable2);
        db.execSQL(createTable3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Checks if the user is new
    public boolean isUserNew(){
        int newUser = 1;

        SQLiteDatabase db = this.getReadableDatabase();

        String checkQuery = "SELECT " + FIRST_TIME + " FROM " + NEW_USER_TABLE;

        Cursor cursor = db.rawQuery(checkQuery, null);

        if(cursor.moveToFirst()){
            newUser = cursor.getInt(0); // Note that the (columnIndex) is not the
                                                // index in the db but the index in the cursor that is determined by (getTotal)
        }

        cursor.close();
        db.close();

        return newUser == 1;
    }

    public void updateUserStatus(int status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(FIRST_TIME, status);

        int rowsUpdated = db.update(NEW_USER_TABLE, cv, FIRST_TIME_ID+"=?", new String[]{"1"}); // Updates firstTime to status where firstTimeID is 1 and return the number of rows updated

        if(rowsUpdated == 0){ // If no rows where updated then create a new row with id 1 and firstTime status
            cv.put(FIRST_TIME_ID, 1);
            db.insert(NEW_USER_TABLE, null, cv);
        }

        db.close();
    }

    public boolean insertToDataBase(String operation, String amount, String description, String date){
        long insert = -1;
        try(SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();

            cv.put(OPERATION, operation);
            cv.put(AMOUNT, amount);
            cv.put(DESCRIPTION, description);
            cv.put(DATE, date);

            insert = db.insert(MONEY_HISTORY_TABLE, null, cv);
        }

        return insert != -1;
    }

    // Updates the total money amount in the database
    public void updateTotalAmount(String totalAmount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(TOTAL_AMOUNT, totalAmount);

        int rowsUpdated = db.update(TOTAL_MONEY_TABLE, cv, TOTAL_ID+"=?", new String[]{"1"});

        if(rowsUpdated == 0){
            cv.put(TOTAL_ID, 1);
            db.insert(TOTAL_MONEY_TABLE, null, cv);
        }

        db.close();
    }


    // Get total money amount from the database
    public String getTotalAmount(){
        SQLiteDatabase db = this.getReadableDatabase();

        String getTotal = "SELECT " + TOTAL_AMOUNT + " FROM " + TOTAL_MONEY_TABLE;
        Cursor cursor = db.rawQuery(getTotal, null);

        if(cursor.moveToFirst()){
            return cursor.getString(0); // Note that the (columnIndex) is not the
                                                   // index in the db but the index in the cursor that is determined by (getTotal)
        }

        cursor.close();
        db.close();

        return "0";
    }

    public List<HistoryData> getHistory(){
        List<HistoryData> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + MONEY_HISTORY_TABLE + " ORDER BY " + ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                String operation = cursor.getString(1);
                String amount = cursor.getString(2);
                String description = cursor.getString(3);
                String date = cursor.getString(4);

                HistoryData history = new HistoryData(id, operation, amount + " IQD", description, date);
                historyList.add(history);

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return historyList;
    }

    public void delete(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + MONEY_HISTORY_TABLE + " WHERE " + ID + " = " + id;
        db.execSQL(query);
        db.close();
    }
}