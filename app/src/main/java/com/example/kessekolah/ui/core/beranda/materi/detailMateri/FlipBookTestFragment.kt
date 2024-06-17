package com.example.kessekolah.ui.core.beranda.materi.detailMateri

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.kessekolah.R
import com.example.kessekolah.data.database.MateriData
import com.example.kessekolah.databinding.FragmentFlipBookTestBinding
import com.example.kessekolah.model.ListMateriViewModel
import com.wajahatkarim3.easyflipviewpager.BookFlipPageTransformer2
import com.example.kessekolah.ui.adapter.ScreenSlideRecyclerAdapter
import com.example.kessekolah.ui.core.beranda.materi.editMateri.EditMateriFragmentArgs
import com.example.kessekolah.viewModel.ViewModelFactoryBookMark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class FlipBookTestFragment : Fragment() {

    private var _binding: FragmentFlipBookTestBinding? = null

    private val binding get() = _binding!!

    private val args : FlipBookTestFragmentArgs by navArgs()
    private lateinit var data: MateriData

    private lateinit var viewModel: ListMateriViewModel

    private var materiBookMark: MateriData? = null
    private var isFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlipBookTestBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = args.materi

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val vmFactory = ViewModelFactoryBookMark.getInstance(requireActivity().application)

        viewModel = ViewModelProvider(
            requireActivity(),
            vmFactory
        )[ListMateriViewModel::class.java]

        if (data != null) {
            materiBookMark = data
        } else {
            Toast.makeText(requireContext(), "Data tidak tersedia", Toast.LENGTH_SHORT).show()
        }

        viewModel.getFavoriteData().observe(viewLifecycleOwner) { materiBM ->
            if (materiBM != null) {
                isFavorite = materiBM.any { it.judul == data?.judul }
                val bookmarkMenuItem = binding.topAppBar.menu.findItem(R.id.bookmark_bar)
                bookmarkMenuItem.setIcon(if (isFavorite) R.drawable.baseline_bookmark_24 else R.drawable.baseline_bookmark_border_24)
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.bookmark_bar -> {
                    if (!isFavorite) {
                        isFavorite = true
                        item.setIcon(R.drawable.baseline_bookmark_24)
                        viewModel.insertMateriBookMark(materiBookMark!!)
                        Toast.makeText(requireContext(), "Berhasil menambahkan ke Bookmark", Toast.LENGTH_SHORT).show()
                    } else {
                        isFavorite = false
                        item.setIcon(R.drawable.baseline_bookmark_border_24)
                        viewModel.deleteMateriBookMark(materiBookMark!!.id)
                        Toast.makeText(requireContext(), "Menghapus dari Bookmark", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

//        bookFLip()
        pdfToBitmap(data)
        setData()
    }

    private fun setData() = with(binding.bannerMateri) {
        ilusBanner.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                requireContext().getResources()
                    .getIdentifier("ilus_banner_${data.dataIlus}", "drawable", requireContext().getPackageName())
            )
        )
        backColorBanner.setBackgroundColor(
            Color.parseColor(data.backColorBanner)
        )
        textNews.text = data.judul
    }

    private fun pdfToBitmap(data: MateriData) {
        lifecycleScope.launch {
            try {
                // Unduh file PDF di latar belakang
                val pdfFile = withContext(Dispatchers.IO) {
                    downloadPdfFile(data.fileUrl)
                }

                // Buat Uri untuk file PDF yang diunduh
                val uri = Uri.fromFile(pdfFile)

                // Buka PDF menggunakan PdfRenderer
                val parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)

                // Daftar untuk menyimpan semua bitmap
                val bitmapList = mutableListOf<Bitmap>()

                // Proses setiap halaman PDF
                for (pageIndex in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(pageIndex)

                    val width = page.width
                    val height = page.height

                    // Buat bitmap untuk halaman saat ini
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapList.add(bitmap)

                    page.close()
                }

                pdfRenderer.close()

                // Gunakan bitmapList sesuai kebutuhan
                bookFlip(bitmapList)

            } catch (ex: Exception) {
                // Tangani error
                Log.e("PDF TO BITMAP", ex.toString())
            }
        }
    }

    // Fungsi untuk mengunduh file PDF
    private suspend fun downloadPdfFile(pdfUrl: String): File = withContext(Dispatchers.IO) {
        val url = URL(pdfUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val inputStream = connection.inputStream
        val file = File(requireContext().cacheDir, "downloaded_file.pdf")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        outputStream.close()
        inputStream.close()

        file
    }

    private fun bookFlip(bitmap: List<Bitmap>) = with(binding) {

        var sliderAdapter = ScreenSlideRecyclerAdapter(ArrayList<Bitmap>())
        sliderAdapter.setItems(bitmap)

        flipViewPager.adapter = sliderAdapter

        var bookTransformer = BookFlipPageTransformer2()
        flipViewPager.setPageTransformer(bookTransformer)
        flipViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

    }

}