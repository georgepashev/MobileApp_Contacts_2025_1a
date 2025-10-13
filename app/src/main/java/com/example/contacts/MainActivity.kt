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

        RefreshList()












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
