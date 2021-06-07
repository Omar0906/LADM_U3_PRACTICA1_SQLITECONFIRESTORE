package mx.tecnm.tepic.ladm_u3_practica1_sqliteconfirestore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE APARTADO(IDAPARTADO INTEGER PRIMARY KEY AUTOINCREMENT,NOMBRECLIENTE VARCHAR(200),PRODUCTO VARCHAR(200),PRECIO FLOAT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}