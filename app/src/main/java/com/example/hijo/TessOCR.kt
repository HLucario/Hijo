package com.example.hijo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI


class TessOCR(context: Context, language: String?) {
    private val mTess: TessBaseAPI?
    fun getOCRResult(bitmap: Bitmap?): String {
        mTess!!.setImage(bitmap)
        return mTess.utF8Text
    }

    fun onDestroy() {
        mTess?.end()
    }

    init {
        mTess = TessBaseAPI()
        val datapath = context.filesDir.toString() + "/tesseract/"
        Log.d("PATH",datapath)
        mTess.init(datapath, language)
    }
}