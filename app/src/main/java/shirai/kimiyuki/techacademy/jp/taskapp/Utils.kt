package shirai.kimiyuki.techacademy.jp.taskapp

import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.realm.Realm

class Utils {
}

fun <T> transformElementList(x:T?, y:List<T>?, lastword:T): MutableList<T>{
    if(y == null || y.isEmpty()) return listOf(lastword) as MutableList
    val i = if(x==null) 0 else y.indexOfFirst{ e -> e?.equals(x!!)!!}
    //print(i)
    val a = if(i==0) listOf<T>() else y.subList(0,i)
    val b = y.subList(i+1, y.size)
    val c = mutableListOf<T>(y[i])
    return (c + a + b + listOf(lastword)) as MutableList
}

fun AdapterView<ArrayAdapter<String>>.setSpinnerForCategory(
    context: Context, firstItem:String?, mRealm: Realm) {

    //categoryArray.map{ Log.d("hello_categoryArray", it) }
    //realm does NOT provide cursor interface?
    //https://stackoverflow.com/questions/29587215/get-cursor-by-using-realm-library
}