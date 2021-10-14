package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.ModalSheetInfoBinding
import fi.joonaun.helsinkitour.network.HelsinkiRepository
import fi.joonaun.helsinkitour.utils.NavigatorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: InfoBottomSheetViewModel by viewModels {
        InfoBottomSheetViewModelFactory(requireContext(), arguments?.get("id") as String)
    }
    private lateinit var binding: ModalSheetInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalSheetInfoBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initUI()

        return binding.root
    }

    /**
     * Finds correct item from [HelsinkiRepository] based on argument "type" and "id".
     * Sets favorite observer and button OnClickListener
     */
    private fun initUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            val id = arguments?.get("id") as? String
            val list = when (arguments?.get("type") as? Int) {
                1 -> HelsinkiRepository.getAllEvents().data
                2 -> HelsinkiRepository.getAllPlaces().data
                3 -> HelsinkiRepository.getAllActivities().data
                else -> return@launch
            }
            val item = list.find { it.id == id } ?: return@launch
            binding.helsinkiItem = item
            withContext(Dispatchers.Main) {
                viewModel.setHelsinkiItem(item)
                viewModel.favorite.observe(viewLifecycleOwner, favoriteObserver)
                binding.sheet.btnShowOnMap.setOnClickListener(showOnMap)
            }
        }
    }

    /**
     * Observer for viewModels "favorite" LiveData
     */
    private val favoriteObserver = Observer<Favorite?> { fav ->
        binding.sheet.favouriteCheckBox.apply {
            isChecked = fav != null
            setOnClickListener {
                if (fav == null) {
                    viewModel.addFavourite()
                } else {
                    viewModel.deleteFavorite()
                }
            }
        }
    }

    /**
     * Sets bounding box and sends it and list on search results to map
     */
    private val showOnMap = View.OnClickListener {
        NavigatorHelper(requireContext(), findNavController())
            .showMapDialog(viewModel.helsinkiItem.value ?: return@OnClickListener)
        super.dismiss()
    }
}