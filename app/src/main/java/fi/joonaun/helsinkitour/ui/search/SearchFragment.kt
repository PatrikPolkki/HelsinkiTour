package fi.joonaun.helsinkitour.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentSearchBinding
import fi.joonaun.helsinkitour.network.Activities
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Helsinki

class SearchFragment : Fragment(R.layout.fragment_search),
    MaterialButtonToggleGroup.OnButtonCheckedListener {
    private val viewModel: SearchViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)

        initUI()
        initFirstObserver()

        return binding.root
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        when(checkedId) {
            R.id.groupBtnActivities -> {
                if (isChecked) {
                    mainViewModel.activities.observe(viewLifecycleOwner, helsinkiObserver)
                } else {
                    mainViewModel.activities.removeObservers(viewLifecycleOwner)
                }
            }
            R.id.groupBtnEvents -> {
                if (isChecked) {
                    mainViewModel.events.observe(viewLifecycleOwner, helsinkiObserver)
                } else {
                    mainViewModel.events.removeObservers(viewLifecycleOwner)
                }
            }
            R.id.groupBtnPlaces -> {
                if (isChecked) {
                    mainViewModel.places.observe(viewLifecycleOwner, helsinkiObserver)
                } else {
                    mainViewModel.places.removeObservers(viewLifecycleOwner)
                }
            }
        }
    }

    private fun initUI() {
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SearchRecyclerViewAdapter()
        }
        binding.searchButtonGroup.addOnButtonCheckedListener(this)
    }

    private fun initFirstObserver() {
        mainViewModel.activities.observe(viewLifecycleOwner, helsinkiObserver)
    }

    private val helsinkiObserver = Observer<List<Helsinki>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.rvResults.adapter as SearchRecyclerViewAdapter)
        adapter.clearItems()
        adapter.addItems(it)
    }
}