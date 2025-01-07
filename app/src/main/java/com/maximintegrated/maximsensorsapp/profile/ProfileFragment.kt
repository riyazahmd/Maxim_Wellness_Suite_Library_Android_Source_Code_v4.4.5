package com.maximintegrated.maximsensorsapp.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.maximsensorsapp.MaximSensorsApp
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.addFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(), UserListener {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)
        userAdapter = UserAdapter(this)
        recyclerView.adapter = userAdapter
        userViewModel.users.observe(this) {
            userAdapter.submitList(it)
            emptyViews.isVisible = it.isEmpty()
            recyclerView.isVisible = it.isNotEmpty()
        }
        addNewUserFab.setOnClickListener {
            requireActivity().addFragment(UpdateProfileFragment.newInstance(User()))
        }
    }

    override fun onEditButtonClicked(user: User) {
        requireActivity().addFragment(UpdateProfileFragment.newInstance(user))
    }

    override fun onDeleteButtonClicked(user: User) {
        userViewModel.deleteUser(user)
    }

    override fun onUserSelected(user: User) {
        userViewModel.selectUser(user)
        userAdapter.notifyDataSetChanged()
    }
}