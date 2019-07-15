package shirai.kimiyuki.techacademy.jp.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category

class CategoryActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm


    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(t: Realm) {
            TODO()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

    }
}
