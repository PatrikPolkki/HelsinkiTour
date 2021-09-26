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
import androidx.recyclerview.widget.LinearLayoutManager
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentSearchBinding
import fi.joonaun.helsinkitour.network.Activities
import fi.joonaun.helsinkitour.network.Activity

class SearchFragment : Fragment(R.layout.fragment_search) {
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
        initObservers()

        return binding.root
    }

    private fun initUI() {
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SearchRecyclerViewAdapter()
        }
    }

    private fun initObservers() {
        mainViewModel.activities.observe(viewLifecycleOwner, activitiesObserver)
    }

    private val activitiesObserver = Observer<List<Activity>> {
        Log.d("Observer", "${it.size}")
        val adapter = (binding.rvResults.adapter as SearchRecyclerViewAdapter)
        adapter.results.clear()
        adapter.results.addAll(it)
    }
}