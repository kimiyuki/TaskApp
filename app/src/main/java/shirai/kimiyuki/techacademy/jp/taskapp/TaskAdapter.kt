package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.text.SimpleDateFormat
import java.util.*


class TaskAdapter(
    val context: Context, var categories: Array<String>,
    var update_category_at_task: (Task, String) -> Unit
) : BaseAdapter() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val simpleDateFormat = SimpleDateFormat("MM/dd HH", Locale.JAPANESE)
    var taskList = mutableListOf<Task>()
    var lastPosition = -1

    private class ViewHolder(view: View) {
        val text1 = view.findViewById<TextView>(R.id.text1)
        val text2 = view.findViewById<TextView>(R.id.text2)
        val spinner = view.findViewById<Spinner>(R.id.main_category_spinner)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(R.layout.row_tasks, null)

        val holder = ViewHolder(view)
        holder.text1.text = taskList[position].title
        holder.text2.text = simpleDateFormat.format(taskList[position].date)

        val _categories = if(categories.size==0) {
            arrayOf("select", "Add Category")
        } else{
            if(taskList[position].category != null){
                arrayOf(taskList[position].category?.name as String) +
                categories.filter{ it != taskList[position].category?.name as String} +
                arrayOf("Add Category")
            }else{
                categories + arrayOf("Add Category")
            }
        }
        val categoryAdapter = ArrayAdapter( context, R.layout.category_spinner_row, _categories )
        categoryAdapter.setDropDownViewResource(R.layout.category_spinner_row)
        holder.spinner.setSelection(Adapter.NO_SELECTION)  // must
        holder.spinner.adapter = categoryAdapter
        //https://stackoverflow.com/questions/14560733/spinners-onitemselected-callback-called-twice-after-a-rotation-if-non-zero-posi
        holder.spinner.post {
            holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, p: Int, id: Long) {
                    Log.d("hello adapter", parent?.adapter?.getItem(p).toString())
                    if (view != null) {
                        lastPosition = position
                        Log.d("aaa adapter lastPosition", lastPosition.toString())
                        if (parent?.adapter?.getItem(p).toString() == "Add Category") {
                            //TODO need to get returned value
                            val intent = Intent(context, CategoryActivity::class.java)
                            //ContextCompat.startActivity(context, intent, null)
                            (context as Activity).startActivityForResult(intent, 1)
                        } else {
                            update_category_at_task(taskList[position], parent?.adapter?.getItem(p).toString())
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Log.d("hello_nothing", "on nothing")
                }
            }
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