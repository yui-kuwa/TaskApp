package jp.techacademy.yui.kuwahara.taskapp

//import com.google.android.material.snackbar.Snackbar
//import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import io.realm.RealmConfiguration.Builder as Builder1

const val EXTRA_TASK = "jp.techacademy.yui.kuwahara.taskapp.TASK"

//Taskクラスのオブジェクト
private var mTask: Task? = null

private var mCategory: Category? = null

// 選択肢
//private var spinnerItems = arrayOf("学習", "仕事", "家事")

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm//Realmクラスを保持するmRealmを定義

    //RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    //TaskAdapterを生成
    private lateinit var mTaskAdapter: TaskAdapter//TaskAdapterクラスの関数や変数を使えるようにする

    //CategoryAdapterを生成
    private lateinit var mCategoryAdapter: CategoryAdapter//CategoryAdapterクラスの関数や変数を使えるようにする

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        /*//Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
        Realm.deleteRealm(realmConfig)*/

        // Realmの設定
        mRealm = Realm.getDefaultInstance()//オブジェクトを取得
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定　
        mTaskAdapter = TaskAdapter(this)

        mCategoryAdapter = CategoryAdapter(this)

        //reloadCategorySpinner()

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            //どのタスクを押したか
            val task = parent.adapter.getItem(position) as Task
            //選んだタスクをinputActivityに渡すためのIntentを作成
            val intent = Intent(this, InputActivity::class.java)
            //Intentを渡す
            intent.putExtra(EXTRA_TASK, task.id)
            //InputActivityでタスクを表示
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                //該当するタスクと同じIDのものを検索
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)//セットした時と同じIntent、PendingIntentを作成し、AlarmManagerクラスのcancelメソッドでキャンセル

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()//再描画
    }

    override fun onResume() {
        super.onResume()

        var categoryList = mutableListOf<String>()//String型の配列を作成
        var categoryRealmResults = mRealm.where(Category::class.java).findAll()//Realmにあるデータを取る

        categoryRealmResults.forEach { category ->//forEachで配列がある分繰り返す
            categoryList.add(category.category)//配列にcategory classのcategoryを追加
        }

        //アダプターを設定
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            categoryList
        )

        //categorySpinnerのアダプターにadapterをいれる
        categorySpinner.adapter = adapter

        //検索ボタンを押した時
        SearchButton.setOnClickListener { view ->
            val categoryText = categorySpinner.selectedItem.toString()

            //該当するcategoryと同じcategoryのものを検索
            val categoryResult =
                mRealm.where(Task::class.java).equalTo("category.category", categoryText)
                    .findAll()

            if (categoryResult.size > 0) {
                //categoryResultだけ表示する

                // 上記の結果を、TaskListとしてセットする　コピーしてアダプターに渡す
                //Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、直接渡すのではなく、このようにコピーして渡す必要がある
                // todo
                mTaskAdapter.mTaskList = //string型のmutableList
                    mRealm.copyFromRealm(categoryResult)//mTaskListはTaskAdapterクラスの配列

                // TaskのListView用のアダプタに渡す
                listView1.adapter = mTaskAdapter

                // 表示を更新するために、アダプターにデータが変更されたことを知らせる
                //notifyDataSetChangedメソッドを呼び出すことでデータが変わったことを伝えてリストを再描画
                mTaskAdapter.notifyDataSetChanged()
            }else{
                reloadListView()
            }

        }
    }

    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        //findAll ですべてのTaskデータを取得
        //sortで"date"（日時）を Sort.DESCENDING（降順）で並べ替えた結果を返す
        val taskRealmResults =
            mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskListとしてセットする　コピーしてアダプターに渡す
        //Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、直接渡すのではなく、このようにコピーして渡す必要がある
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)//mTaskListはTaskAdapterクラスの配列

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        //notifyDataSetChangedメソッドを呼び出すことでデータが変わったことを伝えてリストを再描画
        mTaskAdapter.notifyDataSetChanged()
    }

    private fun reloadCategorySpinner() {
        // Realmデータベースから、すべてのデータを取得
        val categoryRealmResults = mRealm.where(Category::class.java).findAll()

        // 上記の結果を、CategoryListとしてセットする
        //mCategoryAdapter.mCategoryList = mRealm.copyFromRealm(categoryRealmResults)

        // CategoryのcategorySpinner用のアダプタに渡す
        //categorySpinner.adapter = mCategoryAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mCategoryAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        //onDestroyメソッドはActivityが破棄されるときに呼び出されるメソッド
        super.onDestroy()

        mRealm.close()//最後にRealmクラスのオブジェクトを破棄
    }
}