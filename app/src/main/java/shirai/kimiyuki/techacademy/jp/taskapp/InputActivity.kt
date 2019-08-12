package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log

import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.util.*


class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null
    private var categories: List<String>? = null
    private lateinit var mRealm: Realm
    private var categoryAdapter: ArrayAdapter<String>? = null
    private var isUserInteract = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //actionbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //listeners in UI
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        setDataInUI()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        Log.d("hello user interact", isUserInteract.toString())
        isUserInteract = true
    }

    private fun setDataInUI() {
        //EXTRA_task
        mRealm = Realm.getDefaultInstance()
        val intent = getIntent()
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        mTask = mRealm.where(Task::class.java).equalTo("id", taskId).findFirst()
        mRealm.close()
        if (mTask == null) {
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            content_edit_text.setText(mTask!!.contents)
            title_edit_text.setText(mTask!!.title)
            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
            val dateString =
                mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
            date_button.text = dateString
            times_button.text = timeString
        }
        setCategorySpinner(mTask?.category?.name)
    }

    private fun setCategorySpinner(firstCategoryName: String?) {
        categories = mRealm.where(Category::class.java).findAll().map { it.name }.reversed() as List<String>
        val cat  = if (categories == null || categories?.size==0){
            listOf("select", "Add Category")
        } else {
            categories!! + listOf("Add Category")
        }
        categoryAdapter = ArrayAdapter( this, R.layout.category_spinner_row, cat)
        category_spinner.adapter = categoryAdapter
        categoryAdapter!!.setDropDownViewResource(R.layout.category_spinner_row)
        category_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, p: Int, id: Long) {
                Log.d("hello user interact in spinner", isUserInteract.toString())
                if(!isUserInteract) return
                isUserInteract = false
                val cat = parent?.adapter?.getItem(p) as String
                Log.d("hello cat_name", cat)
                if (view != null)
                    if (cat == "Add Category") {
                        val intent = Intent(this@InputActivity, CategoryActivity::class.java)
                        this@InputActivity.startActivityForResult(intent, 2)
                        Log.d("aaa yes this is the async proc", "no")
                    }
            }
            override fun onNothingSelected(parent: AdapterView<*>) { Log.d("hello_nothing", "on nothing") }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 2 || resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        val cat = mRealm.where(Category::class.java).equalTo(
            "id", data!!.getIntExtra("NEW_CATEGORY", 0)
        ).findFirst()
        setCategorySpinner(cat!!.name)
    }

    override fun onResume() {
        super.onResume()
        //set new category in the spinner
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dateOfWeek ->
                mYear = year; mMonth = month; mDay = dateOfWeek
                val dateString =
                    mYear.toString() + "/" + String.format("%2d", mMonth + 1) + "/" + String.format("%2d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minutes ->
                mHour = hour; mMinute = minutes
                val timeString = mHour.toString() + ":" + String.format("%2d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    private fun addTask() {
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
            mTask!!.category =
                it.where(Category::class.java)?.equalTo("name", category_spinner.selectedItem.toString())?.findFirst()
            realm.copyToRealmOrUpdate(mTask!!)
            setAlarm(calendar, mTask!!.id)
        }
        realm.close()
    }

    private fun setAlarm(calendar: GregorianCalendar, taskId: Int) {
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
