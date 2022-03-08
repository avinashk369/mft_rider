package com.techcamino.mft_rider.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.models.orders.Order

class OrderAdapter(
    private val mList: List<Order.Result.Orders>,
    private val context: Context,
    val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(order: Order.Result.Orders)
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val order = mList[position]

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(order.images[0])
        Glide.with(context)
            .load(order.images[0])
            .override(600, 600)
            .fitCenter()
            .centerCrop()
            .into(holder.imageView);
        // sets the text to the textview from our itemHolder class
        holder.textView.text = order.address
        holder.bind(order,itemClickListener)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
        fun bind(item: Order.Result.Orders, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(item) }
        }
    }
}