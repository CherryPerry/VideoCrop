package ru.cherryperry.instavideo.presentation.util.saf

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

interface StorageAccessFramework {

    fun open(fragment: Fragment)

    fun onActivityResultOpen(requestCode: Int, resultCode: Int, resultData: Intent?): Uri?

    fun create(fragment: Fragment)

    fun onActivityResultCreate(requestCode: Int, resultCode: Int, resultData: Intent?): Uri?
}
