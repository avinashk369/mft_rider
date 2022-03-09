package com.techcamino.mft_rider.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        fun changeState(order: Order.Result.Orders, status: Boolean)
        fun viewMap(order: Order.Result.Orders)
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
            .error(R.drawable.gift)
            .into(holder.imageView);
        // sets the text to the textview from our itemHolder class
        holder.delAddress.text = order.address
        holder.orderId.text = "#${order.orderId}"
        holder.delMethod.text=order.shippingmethod

        when (order.riderStatus?.lowercase()) {
            "accepted" -> {
                holder.viewMap.visibility = View.VISIBLE
                holder.decline.visibility = View.GONE
                holder.acptText.text = context.resources.getString(R.string.accepted)

            }
            "declined" -> {
                holder.decText.text = context.resources.getString(R.string.declined)

            }
            else -> {
                holder.viewMap.visibility = View.GONE
                holder.decline.visibility = View.VISIBLE
            }

        }
        holder.bind(order, itemClickListener)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val delAddress: TextView = itemView.findViewById(R.id.del_address)
        val orderId: TextView = itemView.findViewById(R.id.order_id)
        val decText: TextView = itemView.findViewById(R.id.dec_text)
        val delMethod: TextView = itemView.findViewById(R.id.del_method)
        val acptText: TextView = itemView.findViewById(R.id.acpt_text)
        val decline: CardView = itemView.findViewById(R.id.decline_btn)
        val accept: CardView = itemView.findViewById(R.id.accept_btn)
        val viewMap: CardView = itemView.findViewById(R.id.view_map)
        fun bind(item: Order.Result.Orders, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(item) }
            //if (item.riderStatus?.lowercase() != "declined")
            decline.setOnClickListener {
                listener.changeState(
                    item,
                    false
                )
            }
            //if (item.riderStatus?.lowercase() != "accepted")
            accept.setOnClickListener {
                listener.changeState(
                    item,
                    true
                )
            }
            viewMap.setOnClickListener { listener.viewMap(item) }
        }
    }
}