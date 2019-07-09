package shirai.kimiyuki.techacademy.jp.taskapp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.Sort
import io.realm.annotations.PrimaryKey

import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(t: Realm) {
            reloadListView()
        }
    }
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //listView
        mTaskAdapter = TaskAdapter(this@MainActivity)

        listView1.setOnItemClickListener { parent, view, position, id ->
            //TODO move to another activity
        }
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            //TODO delete the task
            Log.d("hello", "delete the task")
            true}

        addTaskForTest()
        reloadListView()
    }

    private fun reloadListView(){
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
        task.id = 0
        val task2 = Task()
        task2.title = "作業"
        task2.contents = "プログラムを書いてpushする2"
        task2.date = Date()
        task2.id = 1
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

    @PrimaryKey
    var id:Int = 0

}
