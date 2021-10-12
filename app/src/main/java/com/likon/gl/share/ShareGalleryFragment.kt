package com.likon.gl.share

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.adapters.GalleryAdapter
import com.likon.gl.databinding.FragmentShareGalleryBinding
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.ImageFoldersModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

private const val TAG = "ShareGalleryFragment"

class ShareGalleryFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                           private val onBackPressed : OnFragmentBackPressed) : Fragment(
    R.layout.fragment_share_gallery), GalleryAdapter.OnGalleryItemClickable {

    private var _binding: FragmentShareGalleryBinding? = null
    private val binding get() = _binding!!
    private val imageFolderModels: MutableList<ImageFoldersModel> = ArrayList()
    private  lateinit var galleryAdapter : GalleryAdapter
    private lateinit var mActivity: Activity
    private lateinit var alertDialog : AlertDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is ShareActivity){
            mActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareGalleryBinding.bind(view)
        galleryAdapter = GalleryAdapter(imageFolderModels,this)
        binding.apply {
            gallery.adapter = galleryAdapter
            backArrow.setOnClickListener {
                Log.d(TAG, "onViewCreated: dddddddddddddddddddddddddddd")
                onBackPressed.onBackPress()
            }
            alertDialog = AlertDialog.Builder(mActivity).apply {
                setView(R.layout.dialog_progress_layout)
                setCancelable(false)
            }.create()
            alertDialog.show()
            getImages()
        }

    }


    private fun getImages() {
        val picFolder = ArrayList<String>()
        var allImagesUri: Uri
        var projection: Array<String>

        CoroutineScope(Dispatchers.IO).launch{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                allImagesUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID)


            } else {
                allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DEFAULT_SORT_ORDER)
            }
            val cursor: Cursor? = requireContext().contentResolver.query(allImagesUri, projection, null, null, null)
            try {
                while (cursor?.moveToNext()!!) {

                    var imagePath: Uri
                    val folder: String = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                    } else {
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DEFAULT_SORT_ORDER))
                    }
                    val idLong: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    imagePath = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idLong)
                    if (!picFolder.contains(folder)) {
                        picFolder.add(folder)
                        val fold = ImageFoldersModel()
                        fold.folderName = folder
                        fold.firstPic = imagePath

//                    fold.setFirstPic(imagePath) //if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                        fold.addImages(imagePath)
                        imageFolderModels.add(fold)
                    } else {
                        for (i in imageFolderModels.indices) {
                            if (imageFolderModels[i].folderName.equals(folder)) {
                                imageFolderModels[i].firstPic = imagePath
                                imageFolderModels[i].addImages(imagePath)
                            }
                        }
                    }
                }
                cursor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main){
                alertDialog.dismiss()

                galleryAdapter.notifyDataSetChanged()

            }

        }


//        for (i in imageFolderModels.indices) {
//            Log.d("picture folders", imageFolderModels[i].folderName.toString() + " and path = " + imageFolderModels[i].numberOfPics + "let test it " + imageFolderModels[i].imagesUri.size)
//        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
       onFragmentChangeListener.onChange(this, R.integer.selected_folder,
           Bundle().apply {  putParcelable("selected folder", imageFolderModels[position])})
    }

}