package com.example.hijo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLEncoder
import java.util.*

class Navegador : AppCompatActivity() {
    private val BASE_URL="https://google.com"
    private val SEARCH_PATH = "/search?q="
    private var hijoid:Int=0
    private var email:String=""
    private var bitmap:Bitmap?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navegador)
        val webView = findViewById<WebView>(R.id.webView)
        val searchView=findViewById<SearchView>(R.id.searchView)
        val swipeRefresh=findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        hijoid=intent.getIntExtra("hijo_id",0)
        email=intent.getStringExtra("email").toString()
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
            ConvertTask().execute(bitmap).toString()
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
            var result=""
            result=tesseract.utF8Text
            tesseract.end()
            return result
        }

        override fun onPostExecute(result: String?){
            super.onPostExecute(result)
            val mresult=result!!.lowercase()
            val ENCODED_REF=URLEncoder.encode(mresult,"utf-8")
            val fresult=ENCODED_REF.replace("|","%7C",ignoreCase = true)
            Log.d("MENSAJE",fresult)
            analisisTexto(fresult)
        }
    }
    private fun analisisTexto(text:String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withTimeout(120000)
            {
                RetrofitClient.instance.analizarTexto(text,hijoid,email).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>)
                    {
                        if(response.code()==200)
                        {
                            Log.d("RESULT",response.message())
                        }
                        else
                        {
                            val message = response.errorBody()!!.string()
                            Log.d("NO ENTRÓ",message)
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.d("ENTRA: ",t.message.toString())
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }
    private fun enviaImagen(id:Int)
    {
        val imgalerta=codificaImagen()
        val fecha=Date()
        val captura:CapturaNetwork=Captura(hijoid,fecha,imgalerta,id).asNetwork()
        RetrofitClient.instance.insertaCaptura(captura).enqueue(object:Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>)
            {
                if(response.code()==200)
                {

                }

            }
            override fun onFailure(call: Call<ResponseBody>,t:Throwable)
            {
                Toast.makeText(applicationContext,t.message,Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun codificaImagen():String
    {
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val b = baos.toByteArray()
        val imgString=Base64.encodeToString(b,Base64.DEFAULT)
        return imgString
    }
}