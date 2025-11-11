package com.example.contacts
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MapSmsActivity : AppCompatActivity() {
    lateinit var pager: ViewPager2
    lateinit var tabs: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_sms)
        pager = findViewById(R.id.viewPager)
        tabs = findViewById(R.id.tabLayout)
        val name = intent.getStringExtra("extra_name") ?: ""
        val phone = intent.getStringExtra("extra_phone") ?: ""
        val address = intent.getStringExtra("extra_address") ?: ""
        val adapter = MapSmsPagerAdapter(this, phone, address, name)
        pager.adapter = adapter
        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Map"
                1 -> "SMS"
                else -> "Weather"
            }
        }.attach()
    }
}