package fi.joonaun.helsinkitour.ui.map.infofragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.FragmentMapBinding

class InfoFragment: Fragment(R.layout.fragment_info) {
    private lateinit var binding: FragmentMapBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater)
        return binding.root
    }
}