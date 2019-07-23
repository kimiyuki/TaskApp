package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.realm.Realm
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.text.SimpleDateFormat
import java.util.*


class TaskAdapter(
    val context: Context, val categories: Array<String>, var update_category_in_task: (Task, String)->Unit) : BaseAdapter() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val simpleDateFormat = SimpleDateFormat("dd HH",Locale.JAPANESE)
    var taskList = mutableListOf<Task>()
    var isStarting = true

    private class ViewHolder(view:View){
        val text1 = view.findViewById<TextView>(R.id.text1)
        val text2 = view.findViewById<TextView>(R.id.text2)
        val spinner = view.findViewById<Spinner>(R.id.main_category_spinner)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(R.layout.row_tasks, null)

        val holder = ViewHolder(view)
        holder.text1.text = taskList[position].title
        holder.text2.text = simpleDateFormat.format(taskList[position].date)

        val _categories = transformElementList(
            taskList[position].category?.name, categories?.toList(), "Add Category")
        val categoryAdapter = ArrayAdapter(context, R.layout.category_spinner_row, _categories )
        categoryAdapter.setDropDownViewResource(R.layout.category_spinner_row)
        //holder.spinner.setSelected(false)  // must
        //holder.spinner.setSelection(0,true)
        holder.spinner.adapter = categoryAdapter
        holder.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, p: Int, id: Long) {
                if(isUserInteract) {
                    Log.d("hello_adapter", "onItem")
                    Log.d("hello_adapter", parent?.adapter?.getItem(p).toString())
                    if (parent?.adapter?.getItem(p).toString() == "Add Category") {
                        val intent = Intent(context, CategoryActivity::class.java)
                        ContextCompat.startActivity(context, intent, null)
                    } else {
                        Log.d("hello", "spinner item selected ${isUserInteract}")
                        update_category_in_task(taskList[position], parent?.adapter?.getItem(p).toString())
                        //re update to order categories
                        holder.spinner.adapter = categoryAdapter
                        Log.d("hello", "task update with category commited")
                    }
                    isUserInteract = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) { Log.d("hello_nothing", "on nothing") }
        }
        view.tag = holder
        return view
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
//        return 0
        return taskList[position].id.toLong()
    }

    override fun getCount(): Int {
        return taskList.size
    }
}