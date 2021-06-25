package com.example.hijo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Navegador : AppCompatActivity() {
    private val BASE_URL = "https://google.com"
    private val SEARCH_PATH = "/search?q="
    private var hijoid: Int = 0
    private var email: String = ""
    private var evento = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navegador)
        val webView = findViewById<WebView>(R.id.webView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        hijoid = intent.getIntExtra("hijo_id", 0)
        email = intent.getStringExtra("email").toString()
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {

                p0?.let {
                    if (URLUtil.isValidUrl(it)) {
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
        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                searchView.setQuery(url, false)

                swipeRefresh.isRefreshing = true
                if (evento == 0) {
                    tareaPrincipal(webView)
                    evento = 1
                } else {
                    if (evento == 1) {
                        tareaPrincipal(webView)
                        evento = 2
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false

            }

        }
        val settings = webView.settings
        settings.javaScriptEnabled = true
        webView.loadUrl(BASE_URL)
    }

    fun tareaPrincipal(webView: WebView) {
        var bitmap:Bitmap?
        var text:String?
        bitmap = Screenshot.takeScreenshotOfRootView(webView)
        text = extraerTexto(bitmap)
        analisisTexto(text, webView, bitmap)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object Screenshot {
        private fun takeScreenshot(view: View): Bitmap {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            return b
        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v.rootView)
        }
    }

    private fun extraerTexto(bitmap: Bitmap): String {
        var tesseract = TessBaseAPI()
        val datapath = "$filesDir/tesseract/"
        val options = BitmapFactory.Options()
        var fresult = ""
        options.inSampleSize = 4
        lifecycleScope.launch {
            FileUtil.checkFile(
                this@Navegador,
                datapath.toString(),
                File(datapath + "tessdata/")
            )
            tesseract.init(datapath, "spa")
            tesseract.setImage(bitmap)
            var result = tesseract.utF8Text
            tesseract.end()
            val mresult = result!!.lowercase()
            fresult = mresult.replace("|", "%7C", ignoreCase = true)
            fresult = fresult.replace("[", "%5B", ignoreCase = true)
            fresult = fresult.replace("]", "%5D", ignoreCase = true)
            fresult = fresult.replace("{", "%7B", ignoreCase = true)
            fresult = fresult.replace("}", "%7D", ignoreCase = true)
            fresult = fresult.replace("^", "%5E", ignoreCase = true)
            fresult = fresult.replace("\\", "%5C", ignoreCase = true)
            fresult = fresult.replace("`", "%60", ignoreCase = true)
            fresult = fresult.replace("\"", "%22", ignoreCase = true)
            fresult = fresult.replace("<", "%3C", ignoreCase = true)
            fresult = fresult.replace(">", "%3E", ignoreCase = true)
        }
        return fresult
    }

    private fun analisisTexto(text: String, webView: WebView, bitmap: Bitmap) {
        var id = 0
        RetrofitClient.instance.analizarTexto(text, hijoid, email)
            .enqueue(object : Callback<ResponseBody> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        try {
                            val idS = response.body()!!.string()
                            id = idS.toInt()
                            val imgalerta = codificaImagen(bitmap)
                            enviaImagen(id, imgalerta)
                            tareaPrincipal(webView)

                        } catch (n: NumberFormatException) {
                            Log.d("NO", "NO GENERÓ")
                            id = 0
                            tareaPrincipal(webView)
                        }
                    } else {
                        val message = response.errorBody()!!.string()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviaImagen(id: Int, imgalerta: String) {
        /*val current = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Mexico/General"))
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val fecha = current.format(formatter)*/
        val fecha=Date(System.currentTimeMillis())
        Log.d("FECHA:", fecha.toString())
        Log.d("ID", id.toString())
        val captura: CapturaNetwork = Captura(hijoid, fecha, imgalerta, id).asNetwork()
        RetrofitClient.instance.insertaCaptura(captura).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    Log.d("Body", response.body()!!.string())
                } else {
                    Log.d("Body", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun codificaImagen(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        val imgString = Base64.encodeToString(b, Base64.DEFAULT)
        return imgString
    }
}