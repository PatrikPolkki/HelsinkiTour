package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fi.joonaun.helsinkitour.MainViewModel
import fi.joonaun.helsinkitour.databinding.ModalSheetInfoBinding

class InfoBottomSheet : BottomSheetDialogFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: ModalSheetInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalSheetInfoBinding.inflate(layoutInflater)

        initUI()

        return binding.root
    }

    private fun initUI() {
        val id = arguments?.get("id") as String
        val list = when ((arguments?.get("type") as Int)) {
            1 -> mainViewModel.events
            2 -> mainViewModel.places
            3 -> mainViewModel.activities
            else -> return
        }
        val item = list.value?.find { it.id == id } ?: return
        binding.helsinkiItem = item
    }

}