package com.example.hijo

import com.google.gson.annotations.SerializedName
import java.util.*

data class CapturaNetwork(
    @SerializedName("id")
    val id: Int,
    @SerializedName("fecha")
    val fecha: Date,
    @SerializedName("img")
    val img: String,
    @SerializedName("alerta_id")
    val alerta_id: Int
)

fun Captura.asNetwork() = CapturaNetwork(
    id = id,
    fecha = fecha,
    img = img,
    alerta_id = alerta_id
)