package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import io.realm.*

import kotlinx.android.synthetic.main.activity_main.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task

const val EXTRA_TASK = "jp.techacademy.shirai.kimiyuki.taskapp.TASK"

fun EditText.afterTextChanged(callback: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            //callback.invoke(editable.toString())
            callback(editable.toString())
        }
    })
}

class MainActivity : AppCompatActivity() {

    private var mRealm: Realm = Realm.getDefaultInstance()
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(t: Realm) {
            Log.d("hello mRealm", t.toString())
            reloadListView(null,category = null)
        } }
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categoryRealmResults = mRealm.where(Category::class.java).findAll()
        var categoryArray = categoryRealmResults.map { it.name }.toTypedArray()
        mTaskAdapter = TaskAdapter(this@MainActivity, categoryArray,
            update_category_at_task= { task, categoryName ->
                //ISSUE endless execution? update invoke another selection item?
                mRealm.executeTransaction {
                    val cat = it.where(Category::class.java).equalTo("name", categoryName).findFirst()
                    task?.category = cat
                    it.copyToRealmOrUpdate(task!!)
                } })
        //Realm
        mRealm.addChangeListener(mRealmListener)


        //show data
        //addTaskForTest()
        //reloadListView(null)
        _setListeners()
        //mTaskAdapter.isStarting = false
        Log.d("hello done", "onCreate hs done")
    }

    override fun onResume() {
        super.onResume()
        val cat  = mRealm.where(Category::class.java).findAll().map{it.name}
        val categoryAdapter = ArrayAdapter(this, R.layout.category_spinner_row,
            listOf("All") + cat)
        categoryAdapter.setDropDownViewResource(R.layout.category_spinner_row)
        spinner_filter.adapter = categoryAdapter
        spinner_filter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val cat = parent?.adapter?.getItem(position) as String
                reloadListView(query=null, category = cat)
            }
        }
        reloadListView(null, category = null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.d("aaa MainActivity requestCode", requestCode.toString())
        Log.d("aaa resultCode", resultCode.toString())
        Log.d("aaa result", data?.getIntExtra("NEW_CATEGORY", -100).toString())
        Log.d("aaa position", mTaskAdapter?.lastPosition.toString())
        if(requestCode != 1 || resultCode != -1){
            Toast.makeText(this, "おかしい", Toast.LENGTH_LONG)
            return
        }
        val task = mTaskAdapter.getItem(mTaskAdapter?.lastPosition) as Task
        mRealm.beginTransaction()
        val cat  = mRealm.where(Category::class.java).equalTo(
            "id", data?.getIntExtra("NEW_CATEGORY", 0)).findFirst()
        task.category = cat
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
        refreshTasks()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!mRealm.isClosed) mRealm.close()
    }

    private fun _setListeners(){
        //listeners
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }
        searchBox.afterTextChanged { text ->
            reloadListView(text, category=null)
            Log.d("hello afterTextChanged", text)
        }

        listView1.setOnItemClickListener { parent, view, position, id ->
            Log.d("hello_task", "listview listener touched" )
            val task = parent.adapter.getItem(position) as Task
            val sendIntent = Intent(this@MainActivity, InputActivity::class.java)
            Log.d("hello_task", "${task.id}" )
            sendIntent.putExtra(EXTRA_TASK, task.id)
            startActivity(sendIntent)
        }

        listView1.setOnItemLongClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")
            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()
                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = getSystemService( Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView(null, category = null)
            }
            builder.setNegativeButton("CANCEL", null)
            val dialog = builder.create()
            dialog.show()
            Log.d("hello", "delete the task")
            true
        }
    }

    private fun reloadListView(query:String?, category:String?){
        if(query == null && category == null) refreshTasks()
        _reloadListView(query, category)
    }

    private fun _reloadListView(query:String?, iscategory: String?){
        val taskResults = if(query != null) {
            mRealm.where(Task::class.java)
                .contains("title", query)
                .findAll().sort( "date", Sort.DESCENDING )
        }else{
            mRealm.where(Task::class.java).findAll().sort( "date", Sort.DESCENDING )
        }
        Log.d("hello count", taskResults.size.toString())
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskResults.sort("date"))
        mTaskAdapter.notifyDataSetChanged()

        listView1.adapter = mTaskAdapter
    }

    private fun refreshTasks() {
        Log.d("aaa refresh task", "a")
        //set up mTaskAdapter`
        val categoryRealmResults = mRealm.where(Category::class.java).findAll()
        var categoryArray = categoryRealmResults.map { it.name }.toTypedArray()
        mTaskAdapter.categories = categoryArray
        mTaskAdapter.notifyDataSetChanged()
    }
}



