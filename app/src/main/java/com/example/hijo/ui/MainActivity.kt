package com.example.hijo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.hijo.R
import com.example.hijo.api.RetrofitClient
import com.example.hijo.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnIniciar = findViewById<Button>(R.id.btnIniciar)
        btnIniciar.setOnClickListener {
            val editCorreo = findViewById<EditText>(R.id.txtCorreo)
            val correo = editCorreo.text.toString().trim()
            val editContra = findViewById<EditText>(R.id.txtContra)
            val password = editContra.text.toString().trim()
            if (correo.isEmpty())
            {
                editCorreo.error = "El correo es requerido"
                editCorreo.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty())
            {
                editContra.error = "La contraseña es requerida"
                editContra.requestFocus()
                return@setOnClickListener
            }
            RetrofitClient.instance.Login(correo, password).enqueue(object : Callback<LoginResponse>
            {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>)
                {
                    if (response.code() == 200)
                    {
                        val defaultResponse = response.body()!!
                        val intent = Intent(this@MainActivity, Seleccion::class.java)
                        intent.putExtra("email", defaultResponse.email)
                        intent.putExtra("nombre", defaultResponse.nombre)
                        intent.putExtra("ap_pat", defaultResponse.ap_pat)
                        intent.putExtra("ap_Mat", defaultResponse.ap_Mat)
                        intent.putExtra("edad", defaultResponse.edad)
                        startActivity(intent)
                    }
                    else
                    {
                        val message = response.errorBody()!!.string()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable)
                {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}