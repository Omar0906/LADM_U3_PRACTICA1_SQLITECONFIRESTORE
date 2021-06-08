package mx.tecnm.tepic.ladm_u3_practica1_sqliteconfirestore

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == Activity.RESULT_OK){
            cargarListaLocal()
        }
    }
    val db = BaseDatos(this,"APARTADO",null,1)
    val dbOnline = FirebaseFirestore.getInstance()
    var listaID = ArrayList<String>()
    var listaIDOnline = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cargarListaOnline()
        cargarListaLocal()
        btninsertar.setOnClickListener {
            insertar()
        }
        btnsync.setOnClickListener {
            sincronizar()
        }
    }

    private fun sincronizar() {
        var transaccion = db.readableDatabase
        try{
            var cursor = transaccion.query("APARTADO", arrayOf("*"),null,null,null,null,null )
            if(cursor.moveToFirst()){
                do{
                    var data = hashMapOf(
                            "IDAPARTADO" to cursor.getInt(0),
                            "NOMBRECLIENTE" to cursor.getString(1),
                            "PRODUCTO" to cursor.getString(2),
                            "PRECIO" to cursor.getString(3).toFloat()
                    )
                    dbOnline.collection("APARTADO").add(data)
                            .addOnSuccessListener {
                                mensaje("Insertado correctamente")
                            }.addOnFailureListener{
                                mensaje("Error al inserta a la nube: \n${it.message}")
                            }
                }while(cursor.moveToNext())
                transaccion.execSQL("DELETE FROM APARTADO")
                transaccion.close()
                cargarListaLocal()
            }else{
                alerta("No hay registros locales")
            }
        }catch (evt:SQLiteException){
                mensaje(evt.message!!)

        }
    }

    private fun alerta(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_LONG)
    }

    private fun cargarListaOnline() {
        dbOnline.collection("APARTADO").addSnapshotListener{querySnapshot,error->
            if(error != null){
                mensaje(error.message!!)
                return@addSnapshotListener
            }
            var items = ArrayList<String>()
            listaIDOnline.clear()
            for(document in querySnapshot!!){
                var cadena = "Cliente: ${document.getString("NOMBRECLIENTE")} - Producto: ${document.getString("PRODUCTO")} - Precio: ${document.get("PRECIO").toString()}"
                items.add(cadena)
                listaIDOnline.add(document.id.toString())
            }
            listaonline.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items)
            listaonline.setOnItemClickListener { parent, view, position, id ->
                var idElegido = listaIDOnline.get(position)
                AlertDialog.Builder(this).setMessage("¿Qué deseas hacer con el ID: ${idElegido}")
                        .setTitle("ATENCIÓN").setPositiveButton("Consultar"){d,i->
                            dbOnline.collection("APARTADO").document(idElegido).get().addOnSuccessListener {
                                mensaje("ID: ${it.id}\nCliente: ${it.getString("NOMBRECLIENTE")}\n" +
                                        "Producto: ${it.getString("PRODUCTO")}\nPrecio: ${it.get("PRECIO").toString()}")
                            }
                        }.setNegativeButton("Eliminar"){d,i->
                            dbOnline.collection("APARTADO").document(idElegido).delete().addOnSuccessListener {
                                mensaje("ELIMINADO CON EXITO")
                            }.addOnFailureListener{
                                mensaje("NO SE HA PODIDO ELIMINAR ${idElegido}\n${it.message}")
                            }
                        }.setNeutralButton("Cancelar"){d,i->
                            d.dismiss()
                        }.show()
            }
        }
    }

    private fun cargarListaLocal(){
        try{
            var transaccion = db.readableDatabase
            var items = ArrayList<String>()
            var cursor = transaccion.query("APARTADO", arrayOf("*"),null,null,null,null,null )
            if(cursor.moveToFirst()){
                listaID.clear()
                do{
                    var data = "[${cursor.getString(1)}] - ${cursor.getString(2)} - ${cursor.getFloat(3)}\n"
                    items.add(data)
                    listaID.add(cursor.getInt(0).toString())
                }while(cursor.moveToNext())
                listalocal.setOnItemClickListener { parent, view, position, id ->
                    var idABorrar = listaID.get(position)
                    AlertDialog.Builder(this).setMessage("¿Qué deseas hacer con el ID: ${idABorrar}")
                            .setTitle("ATENCIÓN").setPositiveButton("Eliminar"){d,i->
                                eliminar(idABorrar)
                            }.setNegativeButton("Consultar"){d,i->
                                consultar(idABorrar)
                            }.setNeutralButton("Actualizar"){d,i->
                                var intent = Intent(this,MainActivity2::class.java)
                                intent.putExtra("idactualizar",idABorrar)
                                result.launch(intent)
                            }.show()
                }
            }else{
                items.add("Sin datos locales")
                listalocal.setOnItemClickListener { parent, view, position, id ->  }
            }
            listalocal.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items)
            transaccion.close()
        }catch (evt: SQLiteException){
            mensaje(evt.message!!)
        }
    }
    private fun consultar(idABorrar: String) {
        try{
            var transacction = db.readableDatabase
            var cursor = transacction.query("APARTADO", arrayOf("NOMBRECLIENTE","PRODUCTO","PRECIO"),
                    "IDAPARTADO=?",arrayOf(idABorrar),null,null,null)
            if(cursor.moveToFirst()){
                AlertDialog.Builder(this).setMessage("Item seleccionado:\nCliente: ${cursor.getString(0)}" +
                        "\nProducto: ${cursor.getString(1)}\nPrecio: ${cursor.getFloat(2)}").show()
            }
            transacction.close()
        }catch (evt:SQLiteException){
            mensaje("NO SE PUDO RECUPERAR LA INFORMACION\n${evt.message}")
        }
    }

    private fun eliminar(idABorrar: String) {
        try{
            var transaccion = db.writableDatabase
            var resultado = transaccion.delete("APARTADO","IDAPARTADO=?", arrayOf(idABorrar))
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
    private fun mensaje(m:String){
        AlertDialog.Builder(this).setMessage(m).show()
    }
    private fun limpiarCampos(){
        txtcliente.setText("")
        txtproducto.setText("")
        txtprecio.setText("")
    }
}