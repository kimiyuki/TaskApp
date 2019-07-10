package shirai.kimiyuki.techacademy.jp.taskapp

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class TaskApp: Application(){
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(true) {
            val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
            Realm.setDefaultConfiguration(config)
        }
    }
}