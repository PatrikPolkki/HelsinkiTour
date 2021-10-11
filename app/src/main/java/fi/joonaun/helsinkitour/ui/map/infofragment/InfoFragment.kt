package fi.joonaun.helsinkitour.ui.map.infofragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.MainViewModelFactory
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.databinding.FragmentInfoBinding
import fi.joonaun.helsinkitour.utils.HelsinkiType
import fi.joonaun.helsinkitour.utils.addFavouriteToDatabase
import fi.joonaun.helsinkitour.utils.deleteFavoriteFromDatabase
import kotlinx.coroutines.launch

class InfoFragment : Fragment(R.layout.fragment_info) {
    private lateinit var binding: FragmentInfoBinding
    private val args: InfoFragmentArgs by navArgs()
    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        val id = args.id
        Log.d("ARGS", args.type.toString())
        val list = when (args.type) {
            HelsinkiType.EVENT -> mainViewModel.events
            HelsinkiType.PLACE -> mainViewModel.places
            HelsinkiType.ACTIVITY -> mainViewModel.activities
        }
        val item = list.value?.find { it.id == id } ?: return
        binding.helsinkiItem = item

        val imageList: MutableList<SlideModel> = mutableListOf()

        item.description.images?.forEach {
            imageList.add(SlideModel(it.url, "", ScaleTypes.CENTER_CROP))
        }
        binding.imageSlider.setImageList(imageList)


        val db = AppDatabase.get(requireContext()).favoriteDao()
        val favourite = db.get(args.id)

        favourite.observe(viewLifecycleOwner) { fav ->
            binding.favouriteCheckBox.apply {
                isChecked = fav != null
                setOnClickListener {
                    if (fav == null) {
                        lifecycleScope.launch {
                            addFavouriteToDatabase(item, db)
                        }
                    } else {
                        lifecycleScope.launch {
                            deleteFavoriteFromDatabase(item, db)
                        }
                    }
                }
            }
        }
    }
}