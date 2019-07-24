package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import io.realm.*

import kotlinx.android.synthetic.main.activity_main.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.util.*

const val EXTRA_TASK = "jp.techacademy.shirai.kimiyuki.taskapp.TASK"
var can_go = false

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

var isUserInteract:Boolean = false

class MainActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm

    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(t: Realm) {
            Log.d("hello mRealm", t.toString())
            reloadListView(null)
        }
    }
    private lateinit var mTaskAdapter: TaskAdapter


    override fun onUserInteraction(){
        isUserInteract = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isUserInteract = false

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)


        //show data
        //addTaskForTest()
        reloadListView(null)
        _setListeners()
        //mTaskAdapter.isStarting = false
        Log.d("hello done", "onCreate hs done")
    }

    override fun onResume(){
        super.onResume()
        //set new cateogry in the spinner
        //create your spinner
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
//        text3.setOnClickListener {_ ->
//            Log.d("hello", "aaaaa")
//        }
        searchBox.afterTextChanged { text ->
            reloadListView(text)
            Log.d("hello", text)
        }

        listView1.setOnItemClickListener { parent, view, position, id ->
            Log.d("hello_task", "listview listener touched" )
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            Log.d("hello_task", "${task.id}" )
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
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

                reloadListView(null)
            }
            builder.setNegativeButton("CANCEL", null)
            val dialog = builder.create()
            dialog.show()
            Log.d("hello", "delete the task")
            true
        }
    }

    private fun reloadListView(query:String?){

        //set up mTaskAdapter`
        val categoryRealmResults = mRealm.where(Category::class.java).findAll()
        var categoryArray = categoryRealmResults.map{it.name}.toTypedArray()
        //callback(task, categoryName ->) works for Spinner when item is selected
        mTaskAdapter = TaskAdapter(this@MainActivity, categoryArray){ task, categoryName ->
            Log.d("hello2", task.title.toString())
            Log.d("hello2", categoryName)
            //ISSUE endless execution? update invoke another selection item?
            mRealm.executeTransaction {
                val cat = mRealm.where(Category::class.java).equalTo("name", categoryName).findFirst()
                task?.category = cat
                mRealm.copyToRealmOrUpdate(task!!)
            }
        }

        val taskRealmResults = if(query != null) {
            mRealm.where(Task::class.java)
                .contains("category.name", query)
                .findAll().sort( "date", Sort.DESCENDING )
        }else{
            mRealm.where(Task::class.java).findAll().sort( "date", Sort.DESCENDING )
        }
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }
}



