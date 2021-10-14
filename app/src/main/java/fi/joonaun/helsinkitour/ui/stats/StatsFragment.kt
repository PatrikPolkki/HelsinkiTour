package fi.joonaun.helsinkitour.ui.stats

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
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
    private val filename = "profile.jpg"

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
            R.id.menuEditProfile -> {
                item.title = when (editProfile()) {
                    true -> getString(R.string.save)
                    false -> getString(R.string.edit)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editProfile(): Boolean {
        return if (!editProfileBoolean) {
            binding.username.visibility = View.GONE
            binding.usernameEditText.visibility = View.VISIBLE
            editProfileBoolean = true
            true
        } else {
            binding.username.visibility = View.VISIBLE
            binding.usernameEditText.visibility = View.GONE
            viewModel.setUsername(binding.usernameEditText.text.toString())
            editProfileBoolean = false
            hideKeyboard()
            false
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun addImage() {
        if (editProfileBoolean) {
            val builder = AlertDialog.Builder(context ?: return)
            builder.apply {
                builder.setMessage(R.string.add_image)
                setPositiveButton(R.string.from_gallery) { _, _ ->
                    galleryImage()
                }
                setNegativeButton(R.string.take_photo) { _, _ ->
                    askCameraPermission()
                }
            }
            builder.create()
            builder.show()
        }
    }

    private fun galleryImage() {
        openGallery.launch("image/*")
    }

    private val openGallery = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        val bitmap: Bitmap?
        val contentResolver = context?.contentResolver ?: return@registerForActivityResult
        try {
            val source = ImageDecoder.createSource(contentResolver, it)
            bitmap = ImageDecoder.decodeBitmap(source)
            binding.userImageView.setImageBitmap(bitmap)
            onSaveFile(bitmap)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }

    }

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                takePhoto()
            }
        }

    private fun askCameraPermission() {
        val camPerm = Manifest.permission.CAMERA

        val pm = activity?.packageManager ?: return
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) return

        context?.let {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(it, camPerm)
                -> takePhoto()
                else -> cameraPermissionRequest.launch(camPerm)
            }
        }
    }

    private fun takePhoto() {
        val fileName = "profile_photo"
        val imgPath = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile: File = File.createTempFile(fileName, ".jpg", imgPath) ?: return

        takePhotoPath = imageFile.absolutePath

            val photoURI: Uri = FileProvider.getUriForFile(
                context ?: return,
                "fi.joonaun.helsinkitour.ui.stats.StatsFragment",
                imageFile
            )
            takePicture.launch(photoURI)
    }

    private var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            var bitmap = BitmapFactory.decodeFile(takePhotoPath)

            val matrix = Matrix()
            val exif = ExifInterface(takePhotoPath)

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                6 -> {
                    matrix.postRotate(90F)
                }
                3 -> {
                    matrix.postRotate(180F)
                }
                8 -> {
                    matrix.postRotate(270F)
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

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

        return stream.toByteArray()
    }

    private fun onSaveFile(bitmap: Bitmap) {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(bitmapToByteArray(bitmap))
                    Log.d("FILE", it.toString())
                }
                Log.d("TOIMI", "TOIMI")
            }

        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }
    }

    private fun onShowFile() {

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageStream = requireContext().openFileInput(filename)
                val bitmap = BitmapFactory.decodeStream(imageStream)

                withContext(Dispatchers.Main) {
                    binding.userImageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
            }
        }


    }

    private fun savePref() {
        val preferences =
            activity?.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE) ?: return
        val editor = preferences.edit()

        editor.putString("USERNAME", viewModel.username.value)
        editor.apply()
    }

    private fun getPref() {
        val preferences =
            activity?.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE) ?: return
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
