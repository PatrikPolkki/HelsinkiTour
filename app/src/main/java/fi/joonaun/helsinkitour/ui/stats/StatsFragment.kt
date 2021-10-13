package fi.joonaun.helsinkitour.ui.stats

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.FragmentStatsBinding
import fi.joonaun.helsinkitour.ui.search.CellClickListener
import fi.joonaun.helsinkitour.ui.search.bottomsheet.InfoBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


class StatsFragment : Fragment(R.layout.fragment_stats),
    MaterialButtonToggleGroup.OnButtonCheckedListener, CellClickListener {
    private lateinit var binding: FragmentStatsBinding
    private val viewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(requireContext())
    }

    private var editProfileBoolean = false
    private lateinit var takePhotoPath: String
    private val FILENAME = "profile.jpg"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        getPref()
        initUI()
        setHasOptionsMenu(true)

        binding.userImageView.setOnClickListener {
            addImage()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        savePref()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onShowFile()
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        viewModel.aFavorite.removeObservers(viewLifecycleOwner)
        viewModel.eFavorite.removeObservers(viewLifecycleOwner)
        viewModel.pFavorite.removeObservers(viewLifecycleOwner)

        when (checkedId) {
            R.id.groupBtnActivities -> if (isChecked) {
                viewModel.aFavorite.observe(viewLifecycleOwner, favoriteObserver)
            }
            R.id.groupBtnEvents -> if (isChecked) {
                viewModel.eFavorite.observe(viewLifecycleOwner, favoriteObserver)
            }
            R.id.groupBtnPlaces -> if (isChecked) {
                viewModel.pFavorite.observe(viewLifecycleOwner, favoriteObserver)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_stats, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuEditProfile -> editProfile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addImage() {
        if (editProfileBoolean) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Add Image")
                .setPositiveButton("Gallery", DialogInterface.OnClickListener { dialog, id ->
                    Log.d("GALLERY", "WORKS")
                    galleryImage()
                })
                .setNegativeButton("Take Photo", DialogInterface.OnClickListener { dialog, id ->
                    Log.d("TAKE PHOTO", "WORKS")
                    takePhoto()
                })
            // Create the AlertDialog object and return it
            builder.create()
            builder.show()
        }
    }

    private fun galleryImage() {
        startForResult.launch("image/*")
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        var bitmap: Bitmap? = null
        val contentResolver = requireContext().contentResolver
        try {
            val source = ImageDecoder.createSource(contentResolver, it)
            bitmap = ImageDecoder.decodeBitmap(source)
            binding.userImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun onSaveFile(bitmap: Bitmap) {
        try {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                lifecycleScope.launch(Dispatchers.Default) {
                    val file =
                        File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILENAME)
                    file.appendBytes(bitmapToByteArray(bitmap))

//                    requireContext().openFileOutput(FILENAME, Context.MODE_APPEND).use {
//                        it.write(bitmapToByteArray(bitmap))
//                    }
                    Log.d("TOIMI", "TOIMI")
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun onShowFile() {
        if (Environment.getExternalStorageState() in setOf(
                Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY
            )
        ) {
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val file =
                        File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILENAME)
                    val bitmap = byteArrayToBitmap(file.readBytes())
                    withContext(Dispatchers.Main) {
                        binding.userImageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("ERROR", e.toString())
                }
            }
        }
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fileName = "profile_photo"
            val imgPath = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile: File? = File.createTempFile(fileName, ".jpg", imgPath)

            takePhotoPath = imageFile!!.absolutePath

            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "fi.joonaun.helsinkitour.ui.stats.StatsFragment",
                imageFile
            )
            takePicture.launch(photoURI)
        }
    }

    private var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            var bitmap = BitmapFactory.decodeFile(takePhotoPath)

            val matrix = Matrix()
            val exif = ExifInterface(takePhotoPath)

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                6 -> {
                    matrix.postRotate(90F);
                }
                3 -> {
                    matrix.postRotate(180F);
                }
                8 -> {
                    matrix.postRotate(270F);
                }
            }

            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            binding.userImageView.setImageBitmap(bitmap)
            onSaveFile(bitmap)
        }
    }

    private fun editProfile() {
        if (!editProfileBoolean) {
            binding.username.visibility = View.GONE
            binding.usernameEditText.visibility = View.VISIBLE
            editProfileBoolean = true
        } else {
            binding.username.visibility = View.VISIBLE
            binding.usernameEditText.visibility = View.GONE
            viewModel.setUsername(binding.usernameEditText.text.toString())
            editProfileBoolean = false
        }
    }

    private fun savePref() {
        val preferences =
            requireActivity().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("USERNAME", viewModel.username.value)
        editor.apply()
    }

    private fun getPref() {
        val preferences =
            requireActivity().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val username = preferences.getString("USERNAME", "")

        viewModel.initUsername(username ?: "")
    }

    /**
     * ClickListener for single recyclerView item
     */
    override fun onCellClickListener(id: String, typeId: Int) {
        Log.d("CELL", "Cell clicked")
        val modalSheet = InfoBottomSheet()
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putInt("type", typeId)
        modalSheet.arguments = bundle
        modalSheet.show(parentFragmentManager, modalSheet.tag)
    }

    /**
     * Initializes recyclerView. Add OnButtonCheckListener to filter button group.
     * Set observer for activityFavorite LiveData
     */
    private fun initUI() {
        binding.resultRv.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FavoriteRecyclerViewAdapter(this@StatsFragment)
        }
        binding.btnGroup.searchButtonGroup.addOnButtonCheckedListener(this)
        viewModel.aFavorite.observe(viewLifecycleOwner, favoriteObserver)
    }

    /**
     * Observer for favorite [List]
     */
    private val favoriteObserver = Observer<List<Favorite>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.resultRv.rvResults.adapter as FavoriteRecyclerViewAdapter)
        adapter.clearItems()
        adapter.addItems(it)
    }
}
