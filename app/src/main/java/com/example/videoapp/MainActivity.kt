package com.example.videoapp

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val videosList = arrayListOf<ModelVideo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var list = getVideoDirectories(this);
        Log.e("TAG", "onCreate: =================== ${File(list!![0]).name}")

        loadVideos(File(list!![0]).name);
        Log.e("TAG", "onCreate: ********************** ${videosList}")


    }

    @SuppressLint("Range")
    fun getVideoDirectories(mContext: Context): ArrayList<String>? {
        val directories: ArrayList<String> = ArrayList()
        val contentResolver: ContentResolver = mContext.getContentResolver()
        val queryUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media.DATA
        )
        val includeImages = MediaStore.Video.Media.MIME_TYPE + " LIKE 'video/%' "
        val excludeGif =
            " AND " + MediaStore.Video.Media.MIME_TYPE + " != 'video/gif' " + " AND " + MediaStore.Video.Media.MIME_TYPE + " != 'video/giff' "
        val selection = includeImages + excludeGif
        val cursor: Cursor? = contentResolver.query(queryUri, projection, selection, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val photoUri: String = cursor.getString(cursor.getColumnIndex(projection[0]))
                if (!directories.contains(File(photoUri).getParent())) {
                    directories.add(File(photoUri).getParent())
                }
            } while (cursor.moveToNext())
        }

        Log.e("TAG", "getVideoDirectories:++++++++++++++++++ $directories")
        return directories
    }


    private fun loadVideos(name: String) {

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"

        // Here, using for reach the specific folder in gallery
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Video.Media.DATA + " like ? ",
            arrayOf("%$name%"),
            null
        )
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getInt(durationColumn)
                val data = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                var duration_formatted: String
                val sec = duration / 1000 % 60
                val min = duration / (1000 * 60) % 60
                val hrs = duration / (1000 * 60 * 60)
                duration_formatted = if (hrs == 0) {
                    min.toString() + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
                } else {
                    hrs.toString() + ":" + java.lang.String.format(
                        Locale.UK, "%02d", min
                    ) + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
                }
                videosList.add(ModelVideo(id, data, title, duration_formatted))
            }
        }

    }
}


