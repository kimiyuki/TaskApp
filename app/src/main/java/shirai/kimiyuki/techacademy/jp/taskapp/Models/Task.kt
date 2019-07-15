package shirai.kimiyuki.techacademy.jp.taskapp.Models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
import java.io.Serializable

open class Task: RealmObject(), Serializable{
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()
    //category: Category = Category()は、cannot be @Required or @NotNullになる。
    var category: Category? = null

    @PrimaryKey
    var id:Int = 0

}
