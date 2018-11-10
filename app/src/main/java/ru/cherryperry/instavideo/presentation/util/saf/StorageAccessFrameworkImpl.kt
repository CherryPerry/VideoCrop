package ru.cherryperry.instavideo.presentation.util.saf

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.util.MimeTypes
import javax.inject.Inject

class StorageAccessFrameworkImpl @Inject constructor() : StorageAccessFramework {

    companion object {
        const val OPEN_REQUEST_CODE = 100
        const val CREATE_REQUEST_CODE = 101
    }

    override fun open(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MimeTypes.BASE_TYPE_VIDEO + "/*"
        }
        fragment.startActivityForResult(intent, OPEN_REQUEST_CODE)
    }

    override fun onActivityResultOpen(requestCode: Int, resultCode: Int, resultData: Intent?): Uri? {
        if (requestCode == OPEN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            return resultData?.data
        }
        return null
    }

    override fun create(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MimeTypes.VIDEO_MP4
        }
        fragment.startActivityForResult(intent, CREATE_REQUEST_CODE)
    }

    override fun onActivityResultCreate(requestCode: Int, resultCode: Int, resultData: Intent?): Uri? {
        if (requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            return resultData?.data
        }
        return null
    }
}
