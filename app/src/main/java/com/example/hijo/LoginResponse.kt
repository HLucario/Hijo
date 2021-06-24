package com.example.hijo

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("email") val email: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ap_pat") val ap_pat: String,
    @SerializedName("ap_Mat") val ap_Mat: String,
    @SerializedName("edad") val edad: Int,
    @SerializedName("password") val password: String,
)