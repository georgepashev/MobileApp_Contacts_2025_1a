package com.example.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File

class ContactAdapter : ListAdapter<Contact, ContactAdapter.VH>(DIFF) {


    object DIFF : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
    }
    class ItemContactBinding private constructor(val root: View) {
        val tvName: TextView = root.findViewById(R.id.tvName)
        val tvPhone: TextView = root.findViewById(R.id.tvPhone)
        val tvEmail: TextView = root.findViewById(R.id.tvEmail)
        val tvAddress: TextView = root.findViewById(R.id.tvAddress)
        val img: ImageView = root.findViewById(R.id.img)

        companion object {
            fun inflate(inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean): ItemContactBinding {
                val view = inflater.inflate(R.layout.item_contact, parent, attachToParent)
                return ItemContactBinding(view)
            }
        }
    }

    inner class VH(val b: ItemContactBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        //Toast.makeText(applicationContext, "DBG1.1", Toast.LENGTH_LONG).show()
        holder.b.tvName.text = c.name
        holder.b.tvPhone.text = c.phone
        holder.b.tvEmail.text = c.email
        holder.b.tvAddress.text = c.address
        //Toast.makeText(applicationContext, "DBG1.2", Toast.LENGTH_LONG).show()



        val imgView = holder.b.img
        when {
            !c.photoPath.isNullOrBlank() && File(c.photoPath).exists() -> imgView.load(File(c.photoPath))
            !c.photoUrl.isNullOrBlank() -> imgView.load(c.photoUrl)
            else -> imgView.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }
}