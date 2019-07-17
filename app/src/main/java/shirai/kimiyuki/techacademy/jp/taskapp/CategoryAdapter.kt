package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.row_categories.view.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category

class CategoryAdapter(context: Context, val onItemClicked: (Category?) -> Unit):
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var mRealm : Realm
    private lateinit var ret: RealmResults<Category>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder{
        mRealm = Realm.getDefaultInstance()
        ret = mRealm.where(Category::class.java).findAll()
        mRealm.close()

        val view = mLayoutInflater.inflate(R.layout.row_categories, parent, false)
        val holder = CategoryViewHolder(view)
        view.setOnClickListener{
           val position = holder.adapterPosition
           val category = ret[position]
            onItemClicked(category)
        }
        return holder
    }

    override fun getItemCount(): Int = ret.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.category_id.text = ret[position]?.id.toString()
        holder.category_name.text = ret[position]?.name.toString()
    }

    class CategoryViewHolder(view: View): RecyclerView.ViewHolder(view){
        var category_id = view.row_category_text1
        var category_name = view.row_category_text2
    }
}