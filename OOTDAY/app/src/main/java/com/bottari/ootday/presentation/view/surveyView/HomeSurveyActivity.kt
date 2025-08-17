package com.bottari.ootday.presentation.view.surveyView

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import com.bottari.ootday.data.model.surveyModel.HomeSurveyViewModel
import com.bottari.ootday.databinding.HomeSurveyActivityBinding
import com.bottari.ootday.presentation.view.mainView.activities.MainActivity

class HomeSurveyActivity : AppCompatActivity()  {
    private lateinit var binding: HomeSurveyActivityBinding
    private val viewModel: HomeSurveyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeSurveyActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        // 성별 선택 리스너
        binding.sexualMan.setOnClickListener {
            updateGenderSelection(it)
            viewModel.onGenderSelected("MALE")
        }
        binding.sexualWoman.setOnClickListener {
            updateGenderSelection(it)
            viewModel.onGenderSelected("FEMALE")
        }

        // 퍼스널 컬러 선택 리스너
        binding.colorItem1.setOnClickListener {
            updateColorSelection(it)
            viewModel.onColorSelected("SPRING_WARM")
        }
        binding.colorItem2.setOnClickListener {
            updateColorSelection(it)
            viewModel.onColorSelected("SUMMER_COOL")
        }
        binding.colorItem3.setOnClickListener {
            updateColorSelection(it)
            viewModel.onColorSelected("AUTUMN_WARM")
        }
        binding.colorItem4.setOnClickListener {
            updateColorSelection(it)
            viewModel.onColorSelected("WINTER_COOL")
        }
    }

    private fun observeViewModel() {
        // ViewModel에서 내비게이션 이벤트가 발생했는지 관찰
        viewModel.navigateToMain.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                // 메인 화면으로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    // 성별 선택 시 UI 업데이트
    private fun updateGenderSelection(selectedView: View) {
        binding.sexualMan.isSelected = (selectedView == binding.sexualMan)
        binding.sexualWoman.isSelected = (selectedView == binding.sexualWoman)
    }

    // 퍼스널 컬러 선택 시 UI 업데이트
    private fun updateColorSelection(selectedView: View) {
        binding.colorItem1.isSelected = (selectedView == binding.colorItem1)
        binding.colorItem2.isSelected = (selectedView == binding.colorItem2)
        binding.colorItem3.isSelected = (selectedView == binding.colorItem3)
        binding.colorItem4.isSelected = (selectedView == binding.colorItem4)
    }
}