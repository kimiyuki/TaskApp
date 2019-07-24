package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Task
import java.util.*

class TaskApp: Application(){

    private lateinit var mRealm: Realm

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(true) {
            val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
            Realm.setDefaultConfiguration(config)

            //this is a test app. should clear data when app start
            mRealm = Realm.getDefaultInstance()
            mRealm.executeTransaction{
                it.deleteAll()
            }
            addTaskForTest()
        }
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
        for(i in 0..5){
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