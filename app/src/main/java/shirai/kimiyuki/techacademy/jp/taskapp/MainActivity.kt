package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import io.realm.*
import io.realm.annotations.PrimaryKey

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.Serializable
import java.util.*

const val EXTRA_TASK = "jp.techacademy.shirai.kimiyuki.taskapp.TASK"

fun EditText.afterTextChanged(callback: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            callback.invoke(editable.toString())
        }
    })
}

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

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //listView
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //show data
        addTaskForTest()
        reloadListView(null)

        _setListeners()
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
            reloadListView(text)
            Log.d("hello", text)
        }

//        searchBox.setOnKeyListener{ v, keyCode, keyEvent ->
//            Log.d("helloKey", keyEvent.action.toString())
//            //if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER ){
//            if (keyEvent.action == KeyEvent.ACTION_DOWN){
//                Log.d("hello", "enter")
//                //reloadListView(v.searchBox.text.toString())
//            }
//            reloadListView(v.searchBox.text.toString())
//            Log.d("hello", "world")
//            return@setOnKeyListener true
//        }

        listView1.setOnItemClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
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
        val taskRealmResults = if(query != null) {
            mRealm.where(Task::class.java) .contains("category", query).findAll().sort( "date", Sort.DESCENDING )
        }else{
            mRealm.where(Task::class.java).findAll().sort( "date", Sort.DESCENDING )
        }
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }


    private fun addTaskForTest(){
        mRealm.beginTransaction()
        val lst = listOf("book", "sport", "study")

        for(i in 0..20){
            val task = Task()
            task.title = "作業" + i.toString()
            task.contents = "プログラムを書いてpushする"
            task.date = Date()
            task.category = lst.shuffled().take(1)[0]
            task.id = i
            mRealm.copyToRealmOrUpdate(task)
        }
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
