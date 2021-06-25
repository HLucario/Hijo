package com.example.hijo

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Seleccion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion)
        val email = intent.getStringExtra("email")
        var hijosR = emptyList<HijoResponse>()
        val spinner = findViewById<Spinner>(R.id.mSpinner)
        RetrofitClient.instance.listarHijos(email.toString())
            .enqueue(object : Callback<List<HijoResponse>> {
                override fun onResponse(
                    call: Call<List<HijoResponse>>,
                    response: Response<List<HijoResponse>>
                ) {
                    if (response.code() == 200) {
                        hijosR = response.body()!!
                        var list = arrayListOf<String>()
                        list.add("")
                        var i = 0
                        while (i < hijosR.size) {
                            list.add(hijosR[i].nombre + " " + hijosR[i].ap_pat + " " + hijosR[i].ap_Mat)
                            i++
                        }
                        val adaptador = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_list_item_1,
                            list
                        )
                        spinner.adapter = adaptador

                        spinner.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                if (position > 0) {
                                    val modelo = Build.MODEL
                                    val hijo: HijoNetwork = Hijo(
                                        hijosR[position-1].id,
                                        hijosR[position-1].nombre,
                                        hijosR[position-1].ap_pat,
                                        hijosR[position-1].ap_Mat,
                                        hijosR[position-1].edad,
                                        modelo,
                                        hijosR[position-1].tutor_email
                                    ).asNetwork()
                                    RetrofitClient.instance.actualizaHijo(hijo)
                                        .enqueue(object : Callback<ResponseBody> {
                                            override fun onResponse(
                                                call: Call<ResponseBody>,
                                                response: Response<ResponseBody>
                                            ) {
                                                if (response.code() == 200) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        response.body().toString(),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    val intent = Intent(
                                                        this@Seleccion,
                                                        Navegador::class.java
                                                    )
                                                    Log.d("ID:",hijo.id.toString())
                                                    Log.d("NOMBRE:",hijo.nombre)
                                                    intent.putExtra("hijo_id", hijo.id)
                                                    intent.putExtra("email", hijo.tutor_email)
                                                    startActivity(intent)
                                                } else {
                                                    val message = response.errorBody()!!.string()
                                                    Toast.makeText(
                                                        applicationContext,
                                                        message,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<ResponseBody>,
                                                t: Throwable
                                            ) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    t.message,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        })
                                }

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                TODO("Not yet implemented")
                            }
                        }
                    } else {
                        hijosR = emptyList()
                    }
                }

                override fun onFailure(call: Call<List<HijoResponse>>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    hijosR = emptyList()
                }
            })
    }
}