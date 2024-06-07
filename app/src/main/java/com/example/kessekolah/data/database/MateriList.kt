package com.example.kessekolah.data.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MateriList(
    val title: String,
    val category: String,
    val timeStamp: String,
    val icon: Int
) : Parcelable