package fi.joonaun.helsinkitour.ui.map.infofragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.MainViewModelFactory
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.AppDatabase
import fi.joonaun.helsinkitour.databinding.FragmentInfoBinding
import fi.joonaun.helsinkitour.utils.HelsinkiType
import fi.joonaun.helsinkitour.utils.NavigatorHelper
import fi.joonaun.helsinkitour.utils.addFavouriteToDatabase
import fi.joonaun.helsinkitour.utils.deleteFavoriteFromDatabase
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initAddressClickListener()
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

    private fun initAddressClickListener() {
        binding.btnShowOnMap.setOnClickListener {
            NavigatorHelper(requireContext(), findNavController())
                .showMapDialog(binding.helsinkiItem ?: return@setOnClickListener)
        }
    }
}