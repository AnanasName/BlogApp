package com.codingwithmitch.openapi.ui.main.blog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.util.GenericViewHolder
import kotlinx.android.synthetic.main.layout_blog_list_item.view.*

class BlogListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NO_MORE_RESULT = -1
    private val BLOG_ITEM = 0
    private val NO_MORE_RESULTS_BLOG_MARKER = BlogPost(
        NO_MORE_RESULT.toString(),
        "",
        "",
        "",
        "",
        "",
        ""
    )

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlogPost>() {

        override fun areItemsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem.blogPk == newItem.blogPk
        }

        override fun areContentsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(
        BlogRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    internal inner class BlogRecyclerChangeCallback(
        private val adapter: BlogListAdapter
    ) : ListUpdateCallback {

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when(viewType){

            NO_MORE_RESULT -> {
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    )
                )
            }

            BLOG_ITEM -> {
                return BlogViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_blog_list_item,
                        parent,
                        false
                    ),
                    interaction,
                    requestManager
                )
            }

            else -> {
                return BlogViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_blog_list_item,
                        parent,
                        false
                    ),
                    interaction,
                    requestManager
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BlogViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].blogPk.equals(NO_MORE_RESULT.toString())) {
            return NO_MORE_RESULT
        }
        return BLOG_ITEM
    }

    fun submitList(list: List<BlogPost>?, isQueryExhausted: Boolean) {
        val newList = list?.toMutableList()
        if (isQueryExhausted){
            newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
        }
        val commitCallback = Runnable {
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback )
    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<BlogPost>
    ){
        for (blogPost in list){
            requestManager
                .load(blogPost.image)
                .preload()
        }
    }

    class BlogViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: BlogPost) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(itemView.blog_image)
            itemView.blog_title.text = item.title
            itemView.blog_author.text = item.username
            itemView.blog_update_date.text = item.date_updated.toString()
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: BlogPost)

        fun restoreListPosition()
    }
}