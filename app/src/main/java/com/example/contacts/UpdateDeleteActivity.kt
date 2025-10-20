package com.example.contacts
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import coil.load
import java.io.File

class UpdateDeleteActivity : AppCompatActivity() {
    private var etName: EditText? = null
    private var etPhone: EditText? = null
    private var etEmail: EditText? = null
    private var etAddress: EditText? = null
    private var etPhotoUrl: EditText? = null
    private var btnPick: Button? = null
    private var btnUpdate: Button? = null
    private var btnDelete: Button? = null
    private var btnCancel: Button? = null
    private var ivPreview: ImageView? = null
    private lateinit var photoRepo: PhotoRepository
    private var current: Contact? = null
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

    override fun finish() {

        super.setResult(200)
        super.finish()
        var i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }


    // същата валидация като в MainActivity
    private fun matchesRegex(regex: String, text: String): Boolean {
        return try { Regex(regex).matches(text) } catch (e: Exception) { false }
    }
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return matchesRegex(emailRegex, email)
    }
    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^\\+?[0-9]{7,15}$"
        return matchesRegex(phoneRegex, phone)
    }
    private fun validateEnableUpdate() {
        val okEmail = isValidEmail(etEmail?.text?.toString() ?: "")
        val okPhone = isValidPhone(etPhone?.text?.toString() ?: "")

        btnUpdate?.isEnabled = okEmail && okPhone
// по желание визуална индикация:
// etEmail?.setBackgroundColor(if (okEmail) 0x00000000 else 0x22FF0000)
// etPhone?.setBackgroundColor(if (okPhone) 0x00000000 else 0x22FF0000)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_delete)
        photoRepo = PhotoRepository(this)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etPhotoUrl = findViewById(R.id.etPhotoUrl)
        btnPick = findViewById(R.id.btnPick)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnCancel = findViewById(R.id.btnCancel)
        ivPreview = findViewById(R.id.ivPreview)
        etEmail?.addTextChangedListener { validateEnableUpdate() }
        etPhone?.addTextChangedListener { validateEnableUpdate() }
        val id = intent.getIntExtra("contact_id", -1)
        if (id == -1) {
            Toast.makeText(this, "Missing contact id", Toast.LENGTH_LONG).show()
            finish()
            return
        }
// Зареди контакта от БД на фонов нишка
        Thread {
            val dao = AppDatabase.get(applicationContext).contactDao()
            val c = dao.getById(id)
            runOnUiThread {
                if (c == null) {
                    Toast.makeText(this, "Contact not found", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    current = c
                    bindContact(c)

                }
            }
        }.start()
        btnPick?.setOnClickListener { pickImage.launch(arrayOf("image/*")) }
        btnUpdate?.setOnClickListener {
            val base = current ?: return@setOnClickListener
            val updated = Contact(
                id = base.id,
                name = etName?.text.toString().trim(),
                phone = etPhone?.text.toString().trim(),
                email = etEmail?.text.toString().trim(),
                address = etAddress?.text.toString().trim(),
                photoUrl = etPhotoUrl?.text.toString().trim().ifBlank { null },
// ако е избрана нова локална снимка – записваме новия път; иначе пазим

                        photoPath = pickedPhotoPath ?: base.photoPath
            )
            if (!isValidEmail(updated.email) || !isValidPhone(updated.phone)) {
                Toast.makeText(this, "Invalid email or phone", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Thread {
                val dao = AppDatabase.get(applicationContext).contactDao()
// ако има нова снимка – може да изтрием старата (по желание)
                if (pickedPhotoPath != null && base.photoPath != null && base.photoPath !=
                    pickedPhotoPath) {

                    photoRepo.deletePhoto(base.photoPath)
                }
                dao.update(updated)
                runOnUiThread {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
        btnDelete?.setOnClickListener {
            val toDelete = current ?: return@setOnClickListener
            Thread {

                val dao = AppDatabase.get(applicationContext).contactDao()
                dao.delete(toDelete)
// чистим и локалната снимка (ако има)
                photoRepo.deletePhoto(toDelete.photoPath)
                runOnUiThread {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
        btnCancel?.setOnClickListener { finish() }
    }
    private fun bindContact(c: Contact) {
        etName?.setText(c.name)
        etPhone?.setText(c.phone)
        etEmail?.setText(c.email)
        etAddress?.setText(c.address)
        etPhotoUrl?.setText(c.photoUrl ?: "")
        when {
            !c.photoPath.isNullOrBlank() && File(c.photoPath).exists() ->
                ivPreview?.load(File(c.photoPath))
            !c.photoUrl.isNullOrBlank() -> ivPreview?.load(c.photoUrl)
            else -> ivPreview?.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        validateEnableUpdate()
    }
}