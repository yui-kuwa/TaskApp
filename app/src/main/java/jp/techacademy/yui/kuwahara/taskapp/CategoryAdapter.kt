package jp.techacademy.yui.kuwahara.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CategoryAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater

    var mCategoryList= mutableListOf<Category>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {//アイテム（データ）の数を返す
        return mCategoryList.size
    }

    override fun getItem(position: Int): Any {//アイテム（データ）を返す
        return mCategoryList[position]
    }

    override fun getItemId(position: Int): Long {//アイテム（データ）のIDを返す
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {//Viewを返す
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        //val textView2 = view.findViewById<TextView>(android.R.id.text2)

        // Categoryクラスから情報を取得
        textView1.text = mCategoryList[position].category

        return view
    }
}