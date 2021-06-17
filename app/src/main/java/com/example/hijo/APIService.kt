package com.example.hijo

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface APIService
{
    @POST("Login")
    fun Login(@Query("email")email: String, @Query("pass")pass: String): Call<LoginResponse>
    @GET("listarHijos")
    fun listarHijos(@Query("email")email:String):Call <List<HijoResponse>>
    @POST("actualizaHijo")
    fun actualizaHijo(@Body hijo:HijoNetwork): Call<ResponseBody>
    @POST("/analizarTexto")
    fun analizarTexto(@Query("texto")texto:String,@Query("hijo_id")hijo_id:Int,@Query("email")email:String)
}