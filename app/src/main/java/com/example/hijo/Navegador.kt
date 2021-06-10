package com.example.hijo

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.SearchView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class Navegador : AppCompatActivity() {
    private val BASE_URL="https://google.com"
    private val SEARCH_PATH = "/search?q="
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
        //val filePath=Environment.getExternalStorageDirectory().toString()+"/Download/"+Calendar.getInstance().time.toString()+".jpg"

        //val fileScreenshot=File(filePath)
        //Log.d("RUTA:",fileScreenshot.toString())
        //lateinit var fileOutputStream: FileOutputStream
        btnS.setOnClickListener {
            val b:Bitmap=Screenshot.takeScreenshotOfRootView(webView)
            val mTessOCR=TessOCR(this,"spa")
            doOCR(b,mTessOCR)
            /*try{
                fileOutputStream=FileOutputStream(fileScreenshot)
                b.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            }
            catch(e:Exception)
            {
                e.printStackTrace()
            }*/
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
    fun doOCR(bitmap: Bitmap,mTessOCR:TessOCR)
    {
        val thread = Thread(Runnable{
            val srcText=mTessOCR.getOCRResult(bitmap)
            if(srcText!=null && srcText != "")
            {
                Log.d("OCR:",srcText)
            }
            mTessOCR.onDestroy()
        } ).start()
    }

}