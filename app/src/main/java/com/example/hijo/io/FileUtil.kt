package com.example.hijo.io

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil
{
    fun checkFile(context: Context, datapath: String, dir: File)
    {
        if (!dir.exists() && dir.mkdirs())
        {
            copyFiles(context, datapath)
        }
        if (dir.exists())
        {
            val datafilepath = "$datapath/tessdata/spa.traineddata"
            val datafile = File(datafilepath)
            if (!datafile.exists())
            {
                copyFiles(context, datapath)
            }
        }
    }

    private fun copyFiles(context: Context, DATA_PATH: String)
    {
        try
        {
            val path = "tessdata"
            val fileList = context.assets.list(path)
            for (fileName in fileList!!)
            {
                val pathToDataFile = "$DATA_PATH$path/$fileName"
                if (!File(pathToDataFile).exists())
                {
                    val inputStream = context.assets.open("$path/$fileName")
                    val out = FileOutputStream(pathToDataFile)
                    val buf = ByteArray(1024)
                    var len: Int
                    len = inputStream.read(buf)
                    while (len > 0)
                    {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    inputStream.close()
                    out.close()
                }
            }
        }
        catch (e: IOException)
        {
            Log.e("copyFiles", "Unable to copy files to tesssdata $e")
        }
    }
}