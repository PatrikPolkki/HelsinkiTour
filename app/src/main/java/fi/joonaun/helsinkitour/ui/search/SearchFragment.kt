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

    companion object {
        private const val MINIMUM_SCROLL_DISTANCE = 25
    }

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding

    private var searchVisible = true
    private var scrollDistance = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
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
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SearchRecyclerViewAdapter(this@SearchFragment)
        }
        binding.searchButtonGroup.addOnButtonCheckedListener(this)
    }

    private fun initObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner, helsinkiObserver)
        viewModel.selectedType.observe(viewLifecycleOwner, typeObserver)
    }

    private val helsinkiObserver = Observer<List<Helsinki>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.rvResults.adapter as SearchRecyclerViewAdapter)
        adapter.clearItems()
        adapter.addItems(it)
    }

    private val typeObserver = Observer<HelsinkiType> { viewModel.doSearch() }
}