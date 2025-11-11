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
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment.newInstance(address, name)
            1 -> SmsFragment.newInstance(phone, name)
            else -> WeatherFragment.newInstance(address, name)
        }
    }
}