package fi.joonaun.helsinkitour.ui.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.FragmentStatsBinding
import fi.joonaun.helsinkitour.ui.search.CellClickListener
import fi.joonaun.helsinkitour.ui.search.bottomsheet.InfoBottomSheet

class StatsFragment : Fragment(R.layout.fragment_stats),
    MaterialButtonToggleGroup.OnButtonCheckedListener, CellClickListener {
    private lateinit var binding: FragmentStatsBinding
    private val viewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initUI()

        return binding.root
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

    override fun onCellClickListener(id: String, typeId: Int) {
        Log.d("CELL", "Cell clicked")
        val modalSheet = InfoBottomSheet()
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putInt("type", typeId)
        modalSheet.arguments = bundle
        modalSheet.show(parentFragmentManager, modalSheet.tag)
    }

    private fun initUI() {
        binding.resultRv.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FavoriteRecyclerViewAdapter(this@StatsFragment)
        }
        binding.btnGroup.searchButtonGroup.addOnButtonCheckedListener(this)
        viewModel.aFavorite.observe(viewLifecycleOwner, favoriteObserver)
    }

    private val favoriteObserver = Observer<List<Favorite>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.resultRv.rvResults.adapter as FavoriteRecyclerViewAdapter)
        adapter.clearItems()
        adapter.addItems(it)
    }
}