package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.row_categories.*
import kotlinx.android.synthetic.main.row_tasks.*
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

        button_category_backto_previous.setOnClickListener{
            finish()
        }

        button_category_create.setOnClickListener{
            val categoryRealmResults = mRealm.where(Category::class.java).findAll()
            var categoryArray = categoryRealmResults.map { it.name }.toTypedArray()
            if(categoryArray.contains(category_input.text.toString())){
                Toast.makeText(this, "重複してます", Toast.LENGTH_LONG)
            }else{
                Log.d("hello category", categoryArray.joinToString())
                //check_duplication?
                val category = Category()
                mRealm.executeTransaction {
                    category.id = categoryArray.size
                    category.name = category_input.text.toString()
                    mRealm.copyToRealmOrUpdate(category)
                }
                intent.putExtra("NEW_CATEGORY", category.id)
                setResult(RESULT_OK, intent);
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("aaa category", "resume")
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
