package com.example.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import okio.Inflater


class MainActivity : AppCompatActivity() {



    private var etName: EditText? = null
    private var etPhone: EditText? = null
    private var etEmail: EditText? = null
    private var etAddress: EditText? = null
    private var etPhotoUrl : EditText?  = null
    private var btnPick: Button? = null
    private var btnAdd: Button? = null
    private var ivPreview : ImageView? = null
    private var rvContacts : RecyclerView? = null
    private var  adapter : ContactAdapter? = null


    private val vm: ContactViewModel by viewModels()
    private lateinit var photoRepo: PhotoRepository

    private var pickedPhotoPath: String? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            pickedPhotoPath = photoRepo.saveFromUri(it)
            ivPreview?.load(pickedPhotoPath)
        }
    }

    fun matchesRegex(regex: String, text: String): Boolean {
        return try {
            Regex(regex).matches(text)
        } catch (e: Exception) {
            false // in case of invalid regex
        }
    }
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return matchesRegex(emailRegex, email)
    }
    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^\\+?[0-9]{7,15}$"
        return matchesRegex(phoneRegex, phone)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RefreshList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etPhotoUrl = findViewById(R.id.etPhotoUrl)
        btnPick = findViewById(R.id.btnPick)
        btnAdd = findViewById(R.id.btnAdd)
        ivPreview = findViewById(R.id.ivPreview)
        rvContacts = findViewById(R.id.rvContacts)

        adapter = ContactAdapter()
        rvContacts?.layoutManager = LinearLayoutManager(this)
        rvContacts?.adapter = adapter
        photoRepo = PhotoRepository(this)

        adapter?.lambdaOnClick = {  contact ->
            val i = contact?.let {
                Intent(applicationContext, UpdateDeleteActivity::class.java)
                    .putExtra("contact_id", it.id)
            }
            //startActivity(i)
            if (i != null) {
                startActivityForResult(i, 200)
            }
        }

        adapter?.lambdaOnMapClick = { contact ->

            val intent = Intent(this, MapSmsActivity::class.java).apply {
                if (contact != null) {
                    putExtra("extra_name", contact.name)
                }
                if (contact != null) {
                    putExtra("extra_phone", contact.phone)
                }
                if (contact != null) {
                    putExtra("extra_address", contact.address)
                }
            }
            startActivity(intent)
        }

        RefreshList()
        etEmail?.addTextChangedListener { ValidateInput() }
        etPhone?.addTextChangedListener { ValidateInput() }



       // vm.contacts.observe(this) { adapter.submitList(it) }

        btnPick?.setOnClickListener { pickImage.launch(arrayOf("image/*")) }

        btnAdd?.setOnClickListener {
            val contact = Contact(
                name = etName?.text.toString().trim(),
                phone = etPhone?.text.toString().trim(),
                email = etEmail?.text.toString().trim(),
                address = etAddress?.text.toString().trim(),
                photoUrl = etPhotoUrl?.text.toString().trim().ifBlank { null },
                photoPath = pickedPhotoPath
            )




            //vm.add(contact)

            var t = Thread{
                var db = AppDatabase.get(applicationContext)
                db.contactDao().insert(contact)
                enqueueOneTimeSync(applicationContext)
                runOnUiThread {
                    RefreshList()
                    etName?.text?.clear()
                    etPhone?.text?.clear()
                    etEmail?.text?.clear()
                    etAddress?.text?.clear()
                    etPhotoUrl?.text?.clear()
                    pickedPhotoPath = null
                    ivPreview?.setImageResource(android.R.drawable.ic_menu_gallery)

                }
            }
            t.start()


// reset

        }
    }

    private fun ValidateInput() {
        if (!isValidEmail(etEmail?.text.toString())) {
            etEmail?.setBackgroundColor(0xff0000)
        }
        if (!isValidPhone(etPhone?.text.toString())) {
            etPhone?.setBackgroundColor(0xff0000)
        }
        if (isValidEmail(etEmail?.text.toString()) && isValidPhone(etPhone?.text.toString())) {
            btnAdd?.isEnabled = true
        } else {
            btnAdd?.isEnabled = false
        }
    }

    private fun RefreshList() {
        var t = Thread {
            var listContacts = AppDatabase.get(applicationContext).contactDao().getAll()

            //

            runOnUiThread {
                Toast.makeText(applicationContext, "DBG1 num: "+listContacts.size, Toast.LENGTH_LONG).show()

                adapter?.submitList(listContacts)


                Toast.makeText(applicationContext, "DBG2", Toast.LENGTH_LONG).show()
            }
            //

        }
        t.start()
    }
}
