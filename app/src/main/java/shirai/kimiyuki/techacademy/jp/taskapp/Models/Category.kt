package shirai.kimiyuki.techacademy.jp.taskapp.Models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Category: RealmObject() {
    var name: String = ""
    @PrimaryKey
    var id:Int = 0
    //TODO: ensure name is unique
    // How? options
    // -1 use composite PrimaryKey? in this model
    // -2 validate in App(controller?)
}
/*
nameをuniqueにするには？
https://github.com/realm/realm-java/issues/6108
*/
/*
    上記のString型のcategoryを、クラスのCategoryへ変更してください
    追加で、タスク作成画面から遷移する画面を1つ作成してください
    その画面ではCategory（idとカテゴリ名を持つ）のクラスを作成できるようにしてください
    タスク作成画面でTaskを作成するときにCategoryを選択できるようにしてください
    一覧画面でCategoryを選択すると、Categoryに属しているタスクのみ表示されるようにしてください
 */
