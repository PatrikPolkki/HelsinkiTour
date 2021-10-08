package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.ModalSheetInfoBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.search.SearchFragmentDirections

class InfoBottomSheet : BottomSheetDialogFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
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
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initUI()

        return binding.root
    }

    private fun initUI() {
        val id = arguments?.get("id") as String
        val list = when ((arguments?.get("type") as Int)) {
            1 -> mainViewModel.events
            2 -> mainViewModel.places
            3 -> mainViewModel.activities
            else -> return
        }
        val item = list.value?.find { it.id == id } ?: return
        binding.helsinkiItem = item
        viewModel.setHelsinkiItem(item)
        viewModel.favorite.observe(this, favoriteObserver)
        binding.btnShowOnMap.setOnClickListener(showOnMap)
    }

    private val favoriteObserver = Observer<Favorite?> {
        if(it == null) {
            binding.btnFavorite.setOnClickListener(addFavoriteAction)
        }
        else {
            binding.btnFavorite.setOnClickListener(removeFavoriteAction)
        }
    }

    private val addFavoriteAction = View.OnClickListener {
        viewModel.addFavourite()
    }

    private val removeFavoriteAction = View.OnClickListener {
        viewModel.deleteFavorite()
    }

    private val showOnMap = View.OnClickListener {
        val list = listOf(viewModel.helsinkiItem.value ?: return@OnClickListener)
        val action = SearchFragmentDirections.actionNavSearchToNavMap()
        findNavController().navigate(action)
        super.dismiss()
    }
}