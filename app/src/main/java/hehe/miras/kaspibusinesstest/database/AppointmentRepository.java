package hehe.miras.kaspibusinesstest.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class AppointmentRepository {
    private final DatabaseHelper dbHelper;

    public AppointmentRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Добавление ID в базу
    public void addProcessedAppointment(int appointmentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ID, appointmentId);
        db.insertWithOnConflict(DatabaseHelper.TABLE_APPOINTMENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    // Проверка, был ли уже обработан appointment
    public boolean isAppointmentProcessed(int appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_APPOINTMENTS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(appointmentId)},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Очистка таблицы (если понадобится)
    public void clearProcessedAppointments() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_APPOINTMENTS, null, null);
        db.close();
    }
}