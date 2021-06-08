package mx.tecnm.tepic.ladm_u3_practica1_sqliteconfirestore

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var db = BaseDatos(this,"APARTADO",null,1)
    var id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var extras = intent.extras
        id = extras!!.getString("idactualizar")!!
        try{
            var transaccion = db.writableDatabase
            var cursor = transaccion.query("APARTADO", arrayOf("NOMBRECLIENTE","PRODUCTO","PRECIO"),"IDAPARTADO=?",
                arrayOf(id),null,null,null)
            if(cursor.moveToFirst()) {
                txtclienteact.setText(cursor.getString(0))
                txtproductoact.setText(cursor.getString(1))
                txtprecioact.setText(cursor.getFloat(2).toString())
            }else{
                mensaje("ERROR! No se pudo recuperar la DATA de ID: ${id}")
            }
        }catch (evt: SQLiteException){
            mensaje(evt.message!!)
        }
        btnactualizar.setOnClickListener {
            actualizar()
        }
        btnregresar.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun actualizar() {
        try{
            var transaccion = db.writableDatabase
            var valores = ContentValues()
            valores.put("NOMBRECLIENTE",txtclienteact.text.toString())
            valores.put("PRODUCTO",txtproductoact.text.toString())
            valores.put("PRECIO",txtprecioact.text.toString().toFloat())
            var resultado = transaccion.update("APARTADO",valores,"IDAPARTADO=?", arrayOf(id))
            if(resultado>0){
                mensaje("Se actualizó correctamente")
            }else{
                mensaje("ERROR!")
            }
            transaccion.close()
        }catch(evt:SQLiteException){
            mensaje(evt.message!!)
        }
    }

    fun mensaje(m:String){
        AlertDialog.Builder(this).setMessage(m).show()
    }
}