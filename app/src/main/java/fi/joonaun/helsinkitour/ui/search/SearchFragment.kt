package fi.joonaun.helsinkitour.ui.search

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
import fi.joonaun.helsinkitour.databinding.FragmentSearchBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.search.bottomsheet.InfoBottomSheet
import fi.joonaun.helsinkitour.utils.HelsinkiType

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
        binding.btnGroup.searchButtonGroup.addOnButtonCheckedListener(this)
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
}