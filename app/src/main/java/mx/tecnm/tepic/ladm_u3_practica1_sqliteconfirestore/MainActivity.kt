package mx.tecnm.tepic.ladm_u3_practica1_sqliteconfirestore

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val db = BaseDatos(this,"APARTADO",null,1)
    var listaID = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cargarListaLocal()
        cargarListaOnline()
        btninsertar.setOnClickListener {
            insertar()
        }
        btnsync.setOnClickListener {
            sincronizar()
        }
    }

    private fun cargarListaOnline() {
    }

    private fun insertar() {
        try{
            var transaccion = db.writableDatabase
            var data = ContentValues()
            data.put("NOMBRECLIENTE","${txtcliente.text.toString()}")
            data.put("PRODUCTO","${txtproducto.text.toString()}")
            data.put("PRECIO","${txtprecio.text.toString().toFloat()}")
            var respuesta = transaccion.insert("APARTADO",null,data)
            if(respuesta == -1L){
                mensaje("ERROR! No se pudo insertar el dato")
            }else{
                mensaje("EXITO! Se inserto CORRECTAMENTE")
                limpiarCampos()
                cargarListaLocal()
            }
            transaccion.close()
        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun sincronizar() {
        TODO("Not yet implemented")
    }

    private fun cargarListaLocal(){
        try{
            var transaccion = db.readableDatabase
            var items = ArrayList<String>()
            var cursor = transaccion.query("APARTADO", arrayOf("*"),null,null,null,null,null )
            if(cursor.moveToFirst()){
                listaID.clear()
                do{
                    var data = "[${cursor.getInt(1)}] - ${cursor.getString(2)}\n"
                    items.add(data)
                    listaID.add(cursor.getInt(0).toString())
                }while(cursor.moveToNext())
                listalocal.setOnItemClickListener { parent, view, position, id ->
                    var idABorrar = listaID.get(position)
                    AlertDialog.Builder(this).setMessage("¿Qué deseas hacer con el ID: ${idABorrar}")
                            .setTitle("ATENCIÓN").setPositiveButton("Eliminar"){d,i->
                                eliminar(idABorrar)
                            }.setNegativeButton("Consultar"){d,i->
                                var cursor = transaccion.query("APARTADO", arrayOf("NOMBRECLIENTE","PRODUCTO","PRECIO"),
                                        "ID=?",arrayOf(idABorrar),null,null,null)
                                AlertDialog.Builder(this).setMessage("Item seleccionado:\nCliente: ${cursor.getString(0)}" +
                                        "\nProducto: ${cursor.getString(1)}\nPrecio: ${cursor.getFloat(2)}").show()
                            }.setNeutralButton("Actualizar"){d,i->
                                var intent = Intent(this,MainActivity2::class.java)
                                intent.putExtra("idactualizar",idABorrar)
                                startActivity(intent)
                            }.show()
                }
            }else{
                items.add("Sin datos locales")
            }
            listalocal.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items)
            transaccion.close()
        }catch (evt: SQLiteException){
            mensaje(evt.message!!)
        }
    }

    private fun eliminar(idABorrar: String) {
        try{
            var transaccion = db.writableDatabase
            var resultado = transaccion.delete("PERSONA","ID=?", arrayOf(idABorrar))
            if(resultado == 0){
                mensaje("No se encontró el ID: ${idABorrar}\nNo se pudo eliminar")
            }else{
                mensaje("Exitó\nSe ha borrado el ID: ${idABorrar} exitosamente")
            }
            transaccion.close()
            cargarListaLocal()
        }catch (evt: SQLiteException){
            mensaje(evt.message!!)
        }
    }
    private fun mensaje(m:String){
        AlertDialog.Builder(this).setMessage(m).show()
    }
    private fun limpiarCampos(){
        txtcliente.setText("")
        txtproducto.setText("")
        txtprecio.setText("")
    }
}