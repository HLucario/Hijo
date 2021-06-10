package com.example.hijo

import com.google.gson.annotations.SerializedName

data class HijoNetwork(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("ap_pat")
    val apellidoPaterno: String,
    @SerializedName("ap_Mat")
    val apellidoMaterno: String,
    @SerializedName("edad")
    val edad: Int,
    @SerializedName("dispositivo")
    val dispositivo: String,
    @SerializedName("tutor_email")
    val tutor_email: String,
)

fun Hijo.asNetwork() = HijoNetwork(
    id = id,
    nombre = nombre,
    apellidoPaterno = ap_pat,
    apellidoMaterno = ap_Mat,
    edad = edad,
    dispositivo = dispositivo,
    tutor_email = tutor_email
)