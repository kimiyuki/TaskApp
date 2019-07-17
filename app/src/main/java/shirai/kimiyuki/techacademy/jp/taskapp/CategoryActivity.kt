package shirai.kimiyuki.techacademy.jp.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_category.*
import shirai.kimiyuki.techacademy.jp.taskapp.Models.Category

class CategoryActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    private lateinit var mCategoryAdapter: CategoryAdapter
    private var categoryList = mutableListOf<Category>()
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(t: Realm) {
            reloadListViewCategory(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        //Realm
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //listView
        mCategoryAdapter = CategoryAdapter(this@CategoryActivity)

        //show data
        reloadListViewCategory(null)

    }

    private fun reloadListViewCategory(query: String?) {
        val categoryRealmResults = if (query != null) {
            mRealm.where(Category::class.java)
                .contains("name", query)
                .findAll()
        } else {
            mRealm.where(Category::class.java).findAll()
        }
        //TODO
//        mCategoryAdapter = mRealm.copyFromRealm(categoryRealmResults)
//        listViewCategory.adapter = mCategoryAdapter
    }
}
