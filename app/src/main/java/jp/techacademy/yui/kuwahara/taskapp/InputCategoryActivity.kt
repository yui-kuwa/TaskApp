package jp.techacademy.yui.kuwahara.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import kotlinx.android.synthetic.main.content_input_category.*
import java.util.*

class InputCategoryActivity : AppCompatActivity() {

    //Category classのオブジェクト
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_category)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)//ツールバーをActionBarとして使えるように設定
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)//ActionBarに戻るボタンを表示
        }

        //カテゴリー追加ボタンのリスナー
        done_category_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
//        val intent = intent
//        //Taskのidを取り出し、もしEXTRA_TASKが設定されていないと taskId には第二引数で指定している既定値 -1 が代入される
//        val categoryId = intent.getIntExtra(EXTRA_TASK, -1)
//        val realm = Realm.getDefaultInstance()
//        mCategory = realm.where(Category::class.java).equalTo("id", categoryId).findFirst()
//        realm.close()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addCategory()//Realmに保存/更新
        finish()//InputCategoryActivityを閉じて前の画面（InputActivity）に戻る
    }

    private fun addCategory() {
        val realm = Realm.getDefaultInstance()//Realmオブジェクトを取得

        realm.beginTransaction()//Realmでデータを追加、削除など変更を行う場合はbeginTransactionメソッドを呼び出し

        if (mCategory == null) {
            // 新規作成の場合
            mCategory = Category()//Categoryクラスを生成

            val categoryRealmResults = realm.where(Category::class.java).findAll()

            val identifier: Int =
                if (categoryRealmResults.max("id") != null) {
                    categoryRealmResults.max("id")!!.toInt() + 1//保存されているタスクの中の最大のidの値に1を足したものを設定
                } else {
                    0
                }

            mCategory!!.id = identifier
        }

        val category = category_edit_text.text.toString()//入力した文字列をcategoryに代入

        if(category.length != 0) {
            val sameCategory = realm.where(Category::class.java).equalTo("category", category).findAll()
            if(sameCategory.isEmpty()) {
                //カテゴリーをmCategoryに設定
                mCategory!!.category = category
                //mTask!!.category = mCategory

                //データの保存・更新
                //設定したものをデータベースに保存
                realm.copyToRealmOrUpdate(mCategory!!)//引数で与えたオブジェクトが存在していれば更新、なければ追加を行うメソッド
                realm.commitTransaction()
            }
        }
        realm.close()
    }
}