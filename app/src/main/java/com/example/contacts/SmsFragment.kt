package com.example.contacts
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class SmsFragment : Fragment() {
    private var phone: String = ""
    private var name: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phone = requireArguments().getString(ARG_PHONE, "")
        name = requireArguments().getString(ARG_NAME, "")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val etMessage: EditText = view.findViewById(R.id.etMessage)
        val btnSend: Button = view.findViewById(R.id.btnSendSms)
        tvPhone.text = "To: $name ($phone)"
        btnSend.setOnClickListener {
            val msg = etMessage.text.toString()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phone")
                putExtra("sms_body", msg)
            }
            startActivity(intent)
        }
    }
    companion object {
        private const val ARG_PHONE = "ARG_PHONE"
        private const val ARG_NAME = "ARG_NAME"
        fun newInstance(phone: String, name: String) = SmsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PHONE, phone)
                putString(ARG_NAME, name)
            }
        }
    }
}