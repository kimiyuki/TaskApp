package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context, var text3callback: (query:String) -> Unit) : BaseAdapter() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    var taskList = mutableListOf<Task>()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(R.layout.row, null)

        val textView1 = view.findViewById<TextView>(R.id.text1)
        textView1.text = taskList[position].title

        val textView2 = view.findViewById<TextView>(R.id.text2)
        val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm",Locale.JAPANESE)
        val date = taskList[position].date
        textView2.text = simpleDateFormat.format(date)

        val textView3 = view.findViewById<TextView>(R.id.text3)
        textView3.text = taskList[position].category?.name?.toString()
        textView3.setOnClickListener{ v ->
            text3callback(textView3.text.toString())
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return taskList.size
    }
}