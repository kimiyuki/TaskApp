package shirai.kimiyuki.techacademy.jp.taskapp

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class CategoryViewHolder(view: View): RecyclerView.ViewHolder(view) {

    interface ItemClickListener{ fun onItemClick(view:View, position:Int)}

    val itemTextView1: TextView = view.findViewById(R.id.row_category_text1)
    val itemTextView2: TextView = view.findViewById(R.id.row_category_text2)

    init{

    }
}