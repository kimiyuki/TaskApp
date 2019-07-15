package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.util.*

class InputActivity : AppCompatActivity(){

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null
    private lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //actionbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //UI
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        mRealm = Realm.getDefaultInstance()

        val categoryRealmResults = mRealm.where(Category::class.java).findAll()
        //realm does NOT provide cursor interface?
        //https://stackoverflow.com/questions/29587215/get-cursor-by-using-realm-library
        val categoryAdapter = ArrayAdapter(this, R.layout.category_spinner_row
            , categoryRealmResults.map { it.name }.toTypedArray() + "Add Category"
        )
        category_spinner.adapter = categoryAdapter
        category_spinner.setOnItemSelectedListener(object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("hello_adapter", parent?.adapter?.getItem(position).toString())
                if(parent?.adapter?.getItem(position).toString() == "Add Category"){
                    val intent = Intent(this@InputActivity, CategoryActivity::class.java)
                    startActivity(intent)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("hello_adapter_nothing",parent.toString())
            }
        })
        //mRealm.close()

        //EXTRA_task
        val intent = getIntent()
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        mTask = mRealm.where(Task::class.java).equalTo("id", taskId).findFirst()
        mRealm.close()

        if(mTask == null){
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        }else{
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            //category_edit_text.setText(mTask!!.category?.name?.toString())

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }


    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener{
                _, year, month, dateOfWeek ->
                mYear = year; mMonth = month; mDay = dateOfWeek
                val dateString = mYear.toString() + "/" + String.format("%2d", mMonth+1) + "/" + String.format("%2d", mDay)
                date_button.text  = dateString
            }, mYear,mMonth,mDay)
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener{
                   _, hour, minutes ->
                mHour = hour; mMinute = minutes
                val timeString = mHour.toString() + ":" + String.format("%2d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    private fun addTask(){
        val realm = Realm.getDefaultInstance()
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        realm.executeTransaction {
            //realm.beginTransaction()
            if (mTask == null) {
                mTask = Task()
                val taskRealmResults = realm.where(Task::class.java).findAll()
                val identifier: Int =
                    if (taskRealmResults.max("id") != null) {
                        taskRealmResults.max("id")!!.toInt() + 1
                    } else {
                        0
                    }
                mTask!!.id = identifier
            }

            val title = title_edit_text.text.toString()
            val content = content_edit_text.text.toString()

            mTask!!.title = title
            mTask!!.contents = content
            val date = calendar.time
            mTask!!.date = date

            realm.copyToRealmOrUpdate(mTask!!)
            //realm.commitTransaction()
        }
        realm.close()
        //TODO: cancelTransactionになった場合に、setAlarmを実行したくない。
        //try catchでは、executeTransaction内の例外は処理済みにされてしまう？
        setAlarm(calendar, mTask!!.id)
    }

    private fun setAlarm(calendar: GregorianCalendar, taskId:Int) {
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, taskId)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this, taskId, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }

}
