package shirai.kimiyuki.techacademy.jp.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_category.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category

class CategoryActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    private lateinit var mCategoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener{reloadListViewCategory(null)}

        //show data
        reloadListViewCategory(null)
    }

    private fun reloadListViewCategory(query: String?) {
        //listView
        val categoryRealmResults = if (query != null) {
            mRealm.where(Category::class.java).contains("name", query).findAll()
        } else {
            mRealm.where(Category::class.java).findAll()
        }
        mCategoryAdapter = CategoryAdapter(this@CategoryActivity,
            categoryRealmResults.toMutableList()) {
            Log.d("hello_category", it?.name)
        }
        Log.d("hello reload", categoryRealmResults.size.toString())
        listViewCategory.adapter = mCategoryAdapter
        listViewCategory.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false)
    }
}
