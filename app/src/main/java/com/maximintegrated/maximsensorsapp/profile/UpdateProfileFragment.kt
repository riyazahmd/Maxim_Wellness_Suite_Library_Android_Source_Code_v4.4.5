package com.maximintegrated.maximsensorsapp.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.maximintegrated.maximsensorsapp.MaximSensorsApp
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.fragment_update_profile.*
import kotlinx.android.synthetic.main.fragment_update_profile.birthYearEditText
import kotlinx.android.synthetic.main.fragment_update_profile.genderChipGroup
import kotlinx.android.synthetic.main.fragment_update_profile.heightEditText
import kotlinx.android.synthetic.main.fragment_update_profile.maleChip
import kotlinx.android.synthetic.main.fragment_update_profile.weightEditText
import kotlinx.android.synthetic.main.include_spo2_fragment_content.*
import java.util.*

class UpdateProfileFragment : Fragment() {

    private var user = User()

    companion object {

        const val MIN_HR_VALUE = 0
        const val MAX_HR_VALUE = 200

        fun newInstance(user: User): UpdateProfileFragment {
            return UpdateProfileFragment().also {
                it.user = user
            }
        }
    }

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)
        if (user.username != DEFAULT_USER_NAME) {
            usernameEditText.setText(user.username)
        }
        birthYearEditText.setText(user.birthYear.toString())
        genderChipGroup.check(if (user.isMale) R.id.maleChip else R.id.femaleChip)
        if (user.isMetric) {
            unitChipGroup.check(R.id.metricsChip)
            weightLayout.hint = getString(R.string.weight_in_kg)
            heightLayout.hint = getString(R.string.height_in_cm)
        } else {
            unitChipGroup.check(R.id.usChip)
            weightLayout.hint = getString(R.string.weight_in_lbs)
            heightLayout.hint = getString(R.string.height_in_inch)
        }
        weightEditText.setText(user.weight.toString())
        heightEditText.setText(user.height.toString())
        initialHrEditText.setText(user.initialHr.toString())
        sleepRestingHrEditText.setText(user.sleepRestingHr.toString())

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val birthYear = birthYearEditText.text.toString().toIntOrNull()
            val isMale = maleChip.isChecked
            val isMetric = metricsChip.isChecked
            val weight = weightEditText.text.toString().toIntOrNull()
            val height = heightEditText.text.toString().toIntOrNull()
            val initialHr = initialHrEditText.text.toString().toIntOrNull()
            val sleepRestingHr = sleepRestingHrEditText.text.toString().toIntOrNull()
            if (!checkInputs(username, birthYear, weight, height, initialHr, sleepRestingHr)) {
                return@setOnClickListener
            } else {
                val isUserDefault = user.username == DEFAULT_USER_NAME
                user.update(
                    username,
                    isMale,
                    birthYear!!,
                    height!!,
                    weight!!,
                    initialHr!!,
                    sleepRestingHr!!,
                    isMetric
                )
                if (isUserDefault) {
                    userViewModel.addUser(user)
                } else {
                    userViewModel.updateUser(user)
                }
                requireActivity().onBackPressed()
            }
        }
        unitChipGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.metricsChip) {
                weightLayout.hint = getString(R.string.weight_in_kg)
                heightLayout.hint = getString(R.string.height_in_cm)
            } else {
                weightLayout.hint = getString(R.string.weight_in_lbs)
                heightLayout.hint = getString(R.string.height_in_inch)
            }
        }
    }

    fun checkInputs(
        username: String?,
        birthYear: Int?,
        weight: Int?,
        height: Int?,
        initialHr: Int?,
        sleepRestingHr: Int?
    ): Boolean {
        if (username == "") {
            showWarningMessage(getString(R.string.username_required))
            return false
        }
        if (birthYear == null) {
            showWarningMessage(getString(R.string.birth_year_required))
            return false
        } else if (birthYear !in 1900..Calendar.getInstance().get(Calendar.YEAR)) {
                showWarningMessage(
                    getString(
                        R.string.birth_year_out_of_range,
                        Calendar.getInstance().get(Calendar.YEAR)
                    )
                )
                return false
            }
        if (weight == null) {
            showWarningMessage(getString(R.string.weight_required))
            return false
        }
        if (height == null) {
            showWarningMessage(getString(R.string.height_required))
            return false
        }
        if (initialHr == null) {
            showWarningMessage(getString(R.string.initial_hr_required))
            return false
        } else if (initialHr !in MIN_HR_VALUE..MAX_HR_VALUE) {
            showWarningMessage(
                getString(
                    R.string.initial_hr_out_of_range, MIN_HR_VALUE, MAX_HR_VALUE
                )
            )
            return false
        }
        if (sleepRestingHr == null) {
            showWarningMessage(getString(R.string.sleep_resting_hr_required))
            return false
        } else if (sleepRestingHr !in MIN_HR_VALUE..MAX_HR_VALUE) {
            showWarningMessage(
                getString(
                    R.string.sleep_resting_hr_out_of_range, MIN_HR_VALUE, MAX_HR_VALUE
                )
            )
            return false
        }
        return true
    }

    private fun showWarningMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}