package fi.joonaun.helsinkitour.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentStatsBinding

class StatsFragment : Fragment(R.layout.fragment_stats) {
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

        return binding.root
    }
}