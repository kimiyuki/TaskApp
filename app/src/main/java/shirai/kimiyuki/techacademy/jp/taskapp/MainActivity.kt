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
import android.widget.EditText
import android.widget.Toast
import io.realm.*
import io.realm.annotations.PrimaryKey

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row.*
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
//        text3.setOnClickListener {_ ->
//            Log.d("hello", "aaaaa")
//        }
        searchBox.afterTextChanged { text ->
            reloadListView(text)
            Log.d("hello", text)
        }

        listView1.setOnItemClickListener { parent, view, position, id ->
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
        val taskRealmResults = if(query != null) {
//            val categories = mRealm.where(Category::class.java)
//                .contains("name",query).findAll()
            mRealm.where(Task::class.java)
                //.`in`("category", categories.map { it.id }.toTypedArray())
                .contains("category.name", query)
                .findAll().sort( "date", Sort.DESCENDING )
        }else{
            mRealm.where(Task::class.java).findAll().sort( "date", Sort.DESCENDING )
        }
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }


    private fun addTaskForTest(){
        val str_categories:List<String> = listOf("sport", "book", "play")
        mRealm.beginTransaction()
        for(i in 0..2){
            val category = Category()
            category.id = i
            category.name = str_categories[i]
            mRealm.copyToRealmOrUpdate(category)
        }

        val categories = mRealm.where(Category::class.java).findAll()
        for(i in 0..20){
            val task = Task()
            task.title = "今日の作業から明日の作業" + i.toString()
            task.contents = "プログラムを書いてpushする"
            task.date = Date()
            task.id = i
            task.category =  categories.shuffled().take(1)[0]
            mRealm.copyToRealmOrUpdate(task)
        }
        mRealm.commitTransaction()
    }

}


open class Task: RealmObject(), Serializable{
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()
    //category: Category = Category()は、cannot be @Required or @NotNullになる。
    var category: Category? = null

    @PrimaryKey
    var id:Int = 0

}

open class Category: RealmObject(), Serializable {
    var name: String = ""
    @PrimaryKey
    var id:Int = 0
}
    /*
        上記のString型のcategoryを、クラスのCategoryへ変更してください
        追加で、タスク作成画面から遷移する画面を1つ作成してください
        その画面ではCategory（idとカテゴリ名を持つ）のクラスを作成できるようにしてください
        タスク作成画面でTaskを作成するときにCategoryを選択できるようにしてください
        一覧画面でCategoryを選択すると、Categoryに属しているタスクのみ表示されるようにしてください
     */
