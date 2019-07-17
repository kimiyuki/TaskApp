package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_categories.view.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category

class CategoryAdapter(context: Context, val categories: MutableList<Category>,
                      val onItemClicked: (Category?) -> Unit):
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder{
        val view = mLayoutInflater.inflate(R.layout.row_categories, parent, false)
        val holder = CategoryViewHolder(view)
        view.setOnClickListener{
           val position = holder.adapterPosition
           val category = categories[position]
            onItemClicked(category)
        }
        return holder
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        Log.d("hello BindBiewHolder", categories[position]?.name.toString())
        holder.category_id.text = categories[position]?.id.toString()
        holder.category_name.text = categories[position]?.name.toString()
    }

    class CategoryViewHolder(view: View): RecyclerView.ViewHolder(view){
        var category_id = view.row_category_text1
        var category_name = view.row_category_text2
    }
}