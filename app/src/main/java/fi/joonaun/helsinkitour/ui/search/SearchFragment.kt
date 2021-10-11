package fi.joonaun.helsinkitour.ui.search

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentSearchBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.search.bottomsheet.InfoBottomSheet
import fi.joonaun.helsinkitour.utils.HelsinkiType
import org.osmdroid.util.BoundingBox

class SearchFragment : Fragment(R.layout.fragment_search),
    MaterialButtonToggleGroup.OnButtonCheckedListener, CellClickListener {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setHasOptionsMenu(true)
        initUI()
        initObservers()

        return binding.root
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        when (checkedId) {
            R.id.groupBtnActivities -> if (isChecked) viewModel.setSelectedType(HelsinkiType.ACTIVITY)
            R.id.groupBtnEvents -> if (isChecked) viewModel.setSelectedType(HelsinkiType.EVENT)
            R.id.groupBtnPlaces -> if (isChecked) viewModel.setSelectedType(HelsinkiType.PLACE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_map, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuShowOnMap -> showOnMap()
        }
        return super.onOptionsItemSelected(item)
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
     * Initializes recyclerView and add OnButtonCheckedListener to searchButtonGroup
     */
    private fun initUI() {
        binding.resultRv.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SearchRecyclerViewAdapter(this@SearchFragment)
        }
        binding.btnGroup.searchButtonGroup.addOnButtonCheckedListener(this@SearchFragment)
    }

    /**
     * Initializes observers to viewModels searchResults and selectedType LiveData
     */
    private fun initObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner, helsinkiObserver)
        viewModel.selectedType.observe(viewLifecycleOwner, typeObserver)
    }

    /**
     * Observer for List<Helsinki> LiveData
     */
    private val helsinkiObserver = Observer<List<Helsinki>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.resultRv.rvResults.adapter as SearchRecyclerViewAdapter)
        adapter.clearItems()
        adapter.addItems(it)
    }

    /**
     * Observer for HelsinkiType LiveData
     */
    private val typeObserver = Observer<HelsinkiType> { viewModel.doSearch() }

    /**
     * Sets bounding box and sends it and list on search results to map
     */
    private fun showOnMap() {
        val list = viewModel.searchResults.value
        if (list == null || list.isEmpty()) return

        val mapLimit = 85.05112877980658
        var north = -mapLimit
        var south = mapLimit
        var west = -mapLimit
        var east = mapLimit

        list.forEach {
            if (it.location.lat > north && it.location.lat < mapLimit)
                north = it.location.lat
            if (it.location.lat < south && it.location.lat > -mapLimit)
                south = it.location.lat
            if (it.location.lon > west && it.location.lon < mapLimit)
                west = it.location.lon
            if (it.location.lon < east && it.location.lon > -mapLimit)
                east = it.location.lon
        }

        val bounds = BoundingBox(north, east, south, west)
        val bundle = bundleOf("helsinkiList" to list, "bounds" to bounds)
        val navController = findNavController()
        navController.navigate(R.id.navMap, bundle)
    }
}