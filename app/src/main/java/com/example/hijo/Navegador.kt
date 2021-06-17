package com.example.hijo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.googlecode.tesseract.android.TessBaseAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class Navegador : AppCompatActivity() {
    private val BASE_URL="https://google.com"
    private val SEARCH_PATH = "/search?q="
    private var bitmap:Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navegador)
        val webView = findViewById<WebView>(R.id.webView)
        val searchView=findViewById<SearchView>(R.id.searchView)
        val swipeRefresh=findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {

                p0?.let {
                    if(URLUtil.isValidUrl(it)) {
                        // Es una url
                        webView.loadUrl(it)
                    } else {
                        // No es una url
                        webView.loadUrl("$BASE_URL$SEARCH_PATH$it")
                    }
                }

                return false
            }

        })
        webView.webChromeClient =object : WebChromeClient(){
        }
        webView.webViewClient=object: WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                searchView.setQuery(url, false)

                swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                swipeRefresh.isRefreshing = false
            }

        }
        val btnS=findViewById<Button>(R.id.btnS)
        btnS.setOnClickListener {
            bitmap=Screenshot.takeScreenshotOfRootView(webView)
            ConvertTask().execute(bitmap)

        }
        val settings =webView.settings
        settings.javaScriptEnabled=true
        webView.loadUrl(BASE_URL)

    }
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if(webView.canGoBack())
        {
            webView.goBack()
        }
        else
        {
            super.onBackPressed()
        }
    }
    companion object Screenshot{
        private fun takeScreenshot(view: View):Bitmap{
            view.isDrawingCacheEnabled=true
            view.buildDrawingCache(true)
            val b=Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled=true
            return b
        }
        fun takeScreenshotOfRootView(v:View):Bitmap{
            return takeScreenshot(v.rootView)
        }
    }
    private inner class ConvertTask: AsyncTask<Bitmap, Void, String>(){
        internal var tesseract = TessBaseAPI()
        override fun onPreExecute() {
            super.onPreExecute()
            val datapath="$filesDir/tesseract/"
            FileUtil.checkFile(
                this@Navegador,
                datapath.toString(),
                File(datapath + "tessdata/")
            )
            tesseract.init(datapath,"spa")
        }

        override fun doInBackground(vararg files:Bitmap): String {
            val options= BitmapFactory.Options()
            options.inSampleSize=4
            tesseract.setImage(bitmap)
            val result=tesseract.utF8Text
            tesseract.end()
            return result
        }

        override fun onPostExecute(result: String?){
            super.onPostExecute(result)
            Log.d("TEXT:",result!!)
        }
    }
    private fun analisisTexto(text:String)
    {
        RetrofitClient.instance.analizarTexto(text).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>)
                {
                    if (response.code() == 200)
                    {
                        val defaultResponse = response.body()!!
                        val intent = Intent(this@Navegador, Seleccion::class.java)
                        intent.putExtra("email", defaultResponse.email)
                        intent.putExtra("nombre", defaultResponse.nombre)
                        intent.putExtra("ap_pat", defaultResponse.ap_pat)
                        intent.putExtra("ap_Mat", defaultResponse.ap_Mat)
                        intent.putExtra("edad", defaultResponse.edad)
                        startActivity(intent)
                    }
                    else {

                        val message = response.errorBody()!!.string()
                        Log.d("NO ENTRÓ",message)
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }

}