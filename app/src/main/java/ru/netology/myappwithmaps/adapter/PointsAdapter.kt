package ru.netology.myappwithmaps.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.myappwithmaps.databinding.CardPointBinding
import ru.netology.myappwithmaps.db.entity.PointEntity

interface OnInteractionListener {
    fun onPoint(point: PointEntity) {}
    fun onEdit(point: PointEntity) {}
}
class PointsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<PointEntity, PointsAdapter.PointViewHolder>(PointDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PointViewHolder {
        val binding = CardPointBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PointViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(
        holder: PointViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class PointViewHolder(
        private val binding: CardPointBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(point: PointEntity) {
            binding.apply {
                title.text = point.title
                description.text = point.description

                root.setOnClickListener {
                    onInteractionListener.onPoint(point)
                }
                edit.setOnClickListener {
                    onInteractionListener.onEdit(point)
                }
            }
        }
    }

    class PointDiffCallback : DiffUtil.ItemCallback<PointEntity>() {
        override fun areItemsTheSame(
            oldItem: PointEntity,
            newItem: PointEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PointEntity,
            newItem: PointEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}