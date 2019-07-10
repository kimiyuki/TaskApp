package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import io.realm.*
import io.realm.annotations.PrimaryKey

import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable
import java.util.*

const val EXTRA_TASK = "jp.techacademy.shirai.kimiyuki.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(t: Realm) {
            reloadListView(null)
        }
    }
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //listView
        mTaskAdapter = TaskAdapter(this@MainActivity)

        listView1.setOnItemClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        searchBox.setOnKeyListener{ v, keyCode, keyEvent ->
            Log.d("hello", "world")
            return@setOnKeyListener true
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

        reloadListView(null)
    }

    private fun reloadListView(query:String?){
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort(
            "date", Sort.DESCENDING
        )
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    private fun addTaskForTest(){
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてpushする"
        task.date = Date()
        task.category = "etc"
        task.id = 0
        val task2 = Task()
        task2.title = "作業"
        task2.contents = "プログラムを書いてpushする2"
        task2.date = Date()
        task2.id = 1
        task2.category = "book"
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.copyToRealmOrUpdate(task2)
        mRealm.commitTransaction()
    }

}


open class Task: RealmObject(), Serializable{
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()
    var category: String = ""

    @PrimaryKey
    var id:Int = 0

}
