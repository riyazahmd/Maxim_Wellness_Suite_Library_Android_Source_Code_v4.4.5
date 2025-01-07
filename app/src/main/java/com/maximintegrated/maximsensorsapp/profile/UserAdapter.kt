package com.maximintegrated.maximsensorsapp.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.maximintegrated.maximsensorsapp.DeviceSettings
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.user_item.view.*

class UserAdapter(private val listener: UserListener) :
    ListAdapter<User, UserAdapter.UserViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    class UserViewHolder(private val parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.user_item, parent, false
        )
    ) {
        private val usernameTextView: TextView by lazy { itemView.usernameTextView }
        private val genderAgeTextView: TextView by lazy { itemView.genderAgeTextView }
        private val initialHeartRateTextView: TextView by lazy { itemView.initialHeartRateTextView }
        private val heightTextView: TextView by lazy { itemView.heightTextView }
        private val weightTextView: TextView by lazy { itemView.weightTextView }
        private val editButton: MaterialButton by lazy { itemView.editButton }
        private val selectButton: MaterialButton by lazy { itemView.selectButton }
        private val deleteImageView: ImageView by lazy { itemView.deleteImageView }
        private val userSelectedImageView: ImageView by lazy { itemView.userSelectedImageView }

        fun bind(item: User, listener: UserListener) {
            val context = parent.context
            usernameTextView.text = item.username
            val gender = if (item.isMale) context.getString(R.string.male) else context.getString(R.string.female)
            genderAgeTextView.text = parent.context.getString(R.string.gender_age_info, gender, item.age)
            initialHeartRateTextView.text = "${item.initialHr} bpm"
            var weightUnit = "kg"
            var heightUnit = "cm"
            if (!item.isMetric) {
                weightUnit = "lbs"
                heightUnit = "inch"
            }
            weightTextView.text = "${item.weight} $weightUnit"
            heightTextView.text = "${item.height} $heightUnit"
            editButton.setOnClickListener {
                listener.onEditButtonClicked(item)
            }
            selectButton.setOnClickListener {
                listener.onUserSelected(item)
            }
            deleteImageView.setOnClickListener {
                askUserForDeleteOperation(item, listener)
            }

            if(item.userId == DeviceSettings.selectedUserId) {
                userSelectedImageView.isVisible = true
                usernameTextView.setBackgroundResource(R.color.color_primary)
            } else {
                userSelectedImageView.isInvisible = true
                usernameTextView.setBackgroundResource(R.color.color_gray)
            }
        }

        private fun askUserForDeleteOperation(user: User, listener: UserListener) {
            val context = parent.context
            val alert = AlertDialog.Builder(context)
            alert.setMessage(context.getString(R.string.are_you_sure_to_delete_it))
            alert.setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                listener.onDeleteButtonClicked(user)
            }
            alert.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

interface UserListener {
    fun onEditButtonClicked(user: User)
    fun onDeleteButtonClicked(user: User)
    fun onUserSelected(user: User)
}