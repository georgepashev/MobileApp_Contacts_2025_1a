package com.example.contacts

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class WeatherFragment : Fragment() {
    private var address: String = ""
    private var name: String = ""
    private val repo = WeatherRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = requireArguments().getString(ARG_ADDRESS, "")
        name = requireArguments().getString(ARG_NAME, "")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_weather, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvResult: TextView = view.findViewById(R.id.tvResult)
        val progress: ProgressBar = view.findViewById(R.id.progress)
        tvTitle.text = "Weather for $name"
        viewLifecycleOwner.lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            val latLng = geocode(address)
            if (latLng == null) {
                tvResult.text = "Cannot geocode address."
                progress.visibility = View.GONE
                return@launch
            }
            val wx = runCatching {
                try {
                    repo.getCurrent(latLng.latitude, latLng.longitude)
                }catch ( e: Exception){
                    viewLifecycleOwner.lifecycleScope.launch {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        tvResult.text = e.message
                    }
                    null
                }
            }.getOrNull()

            progress.visibility = View.GONE
            if (wx == null) {
                tvResult.text = "Weather fetch failed."
            } else {
                val city = wx.name ?: "—"
                val temp = wx.main?.temp
                val desc = wx.weather?.firstOrNull()?.description ?: "—"
                tvResult.text = "$city: ${"%.1f".format(temp)}°C, $desc"
            }
        }
    }
    private suspend fun geocode(addr: String): LatLng? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val res = runCatching { geocoder.getFromLocationName(addr, 1) }.getOrNull()
        val first = res?.firstOrNull() ?: return@withContext null
        LatLng(first.latitude, first.longitude)
    }
    companion object {
        private const val ARG_ADDRESS = "ARG_ADDRESS"
        private const val ARG_NAME = "ARG_NAME"
        fun newInstance(address: String, name: String) = WeatherFragment().apply {
            arguments = Bundle().apply {

                putString(ARG_ADDRESS, address)
                putString(ARG_NAME, name)
            }
        }
    }
}