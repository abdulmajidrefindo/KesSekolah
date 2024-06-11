package com.example.kessekolah.ui.core.beranda.materi.editMateri

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kessekolah.data.database.MateriList
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class EditMateriViewModel: ViewModel() {
    private val materiRef = FirebaseDatabase.getInstance().getReference("materi")
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    suspend fun editMateri(newTitle: String, newYear: String, newIcon: Int, fileName: String): Boolean {
        _loading.value = true
        Log.d("EditMateriViewModel", fileName)
        val updates = mapOf(
            "judul" to newTitle,
            "tahun" to newYear,
            "icon" to newIcon,
        )

        return suspendCancellableCoroutine { continuation ->
            materiRef.child(fileName).updateChildren(updates)
                .addOnSuccessListener {
                    _loading.value = false
                    Log.d("EditMateriViewModel", "Materi berhasil diperbarui.")
                    continuation.resume(false)
                }
                .addOnFailureListener { e ->
                    _loading.value = false
                    Log.e("EditMateriViewModel", "Gagal memperbarui materi", e)
                    continuation.resume(true)
                }
        }
    }
}