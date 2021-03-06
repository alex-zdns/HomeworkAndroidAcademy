package ru.alexzdns.fundamentals.homework.ui.movieDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ru.alexzdns.fundamentals.homework.R
import ru.alexzdns.fundamentals.homework.domain.models.Actor


class ActorsAdapter(
    private val actors: List<Actor>,
) : RecyclerView.Adapter<ActorsAdapter.ActorsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsViewHolder = ActorsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.view_holder_actor, parent, false)
    )

    override fun onBindViewHolder(holder: ActorsViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    override fun getItemCount(): Int = actors.size


    class ActorsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.vha_avatar)
        private val name: TextView = itemView.findViewById(R.id.vha_cast_name)
        private val imageOption: RequestOptions = RequestOptions()
            .placeholder(R.drawable.vha_avatar_placeholder)
            .fallback(R.drawable.vha_avatar_placeholder)
            .transform(CenterCrop(), RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.vha_corner_radius)))

        fun bind(actor: Actor) {
            Glide.with(itemView.context)
                .load(actor.picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(imageOption)
                .into(avatar)

            name.text = actor.name
        }
    }
}
