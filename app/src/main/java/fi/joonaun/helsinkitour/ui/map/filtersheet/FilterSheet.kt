package fi.joonaun.helsinkitour.ui.map.filtersheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fi.joonaun.helsinkitour.databinding.ModalSheetFilterBinding

class FilterSheet: BottomSheetDialogFragment() {
    lateinit var binding: ModalSheetFilterBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalSheetFilterBinding.inflate(layoutInflater
        )

        return binding.root
    }
}