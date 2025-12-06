package com.codeclinic.i230011_i230646_i230509_medque.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codeclinic.i230011_i230646_i230509_medque.R
import com.codeclinic.i230011_i230646_i230509_medque.models.Doctor
import com.squareup.picasso.Picasso

class DoctorAdapter(
    private var doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorImage: ImageView = itemView.findViewById(R.id.doctorImage)
        val doctorName: TextView = itemView.findViewById(R.id.doctorName)
        val doctorSpecialty: TextView = itemView.findViewById(R.id.doctorSpecialty)
        val doctorLocation: TextView = itemView.findViewById(R.id.doctorLocation)
        val doctorRating: TextView = itemView.findViewById(R.id.doctorRating)
        val doctorReviews: TextView = itemView.findViewById(R.id.doctorReviews)
        val heartIcon: ImageView = itemView.findViewById(R.id.heartIcon)

        fun bind(doctor: Doctor) {
            doctorName.text = doctor.doctor_name
            doctorSpecialty.text = doctor.specialization
            doctorLocation.text = doctor.location ?: "Location not specified"
            doctorRating.text = String.format("%.1f", doctor.rating)
            doctorReviews.text = "${doctor.reviews_count} Reviews"

            // Load doctor image using Picasso
            if (!doctor.profile_picture.isNullOrEmpty()) {
                val imageUrl = "http://192.168.100.22/medque_app/uploads/${doctor.profile_picture}"
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.doctor1)
                    .error(R.drawable.doctor1)
                    .into(doctorImage)
            } else {
                doctorImage.setImageResource(R.drawable.doctor1)
            }

            itemView.setOnClickListener {
                onDoctorClick(doctor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount(): Int = doctors.size

    fun updateDoctors(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }
}
