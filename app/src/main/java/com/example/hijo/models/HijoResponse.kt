package com.example.hijo.models

import com.google.gson.annotations.SerializedName

data class HijoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ap_pat") val ap_pat: String,
    @SerializedName("ap_Mat") val ap_Mat: String,
    @SerializedName("edad") val edad: Int,
    @SerializedName("dispositivo") val dispositivo: String,
    @SerializedName("tutor_email") val tutor_email: String
)
