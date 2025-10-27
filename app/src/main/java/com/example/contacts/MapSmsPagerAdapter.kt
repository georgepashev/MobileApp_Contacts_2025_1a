package com.example.contacts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MapSmsPagerAdapter(
    fa: FragmentActivity,
    private val phone: String,
    private val address: String,
    private val name: String
) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int): Fragment {
        return if (position == 0)
            MapFragment.newInstance(address, name)
        else
            SmsFragment.newInstance(phone, name)
    }
}