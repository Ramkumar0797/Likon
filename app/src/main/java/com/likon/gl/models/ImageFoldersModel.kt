package com.likon.gl.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.ArrayList

@Parcelize
class ImageFoldersModel(var folderName: String? = null,
                        var numberOfPics: Int = 0,
                        var firstPic: Uri? = null,
                         val imagesUris: MutableList<Uri> = ArrayList()) : Parcelable {

    fun addImages(uri: Uri)  {
        numberOfPics++
        imagesUris.add(uri)
    }


}