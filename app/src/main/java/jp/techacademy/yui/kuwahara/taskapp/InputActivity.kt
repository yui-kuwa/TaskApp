package jp.techacademy.yui.kuwahara.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent

class InputActivity : AppCompatActivity() {
    //タスクの日時を保持する変数
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    //Taskクラスのオブジェクト
    private var mTask: Task? = null

    //日付を設定するButtonのリスナー
    private val mOnDateClickListener = View.OnClickListener {
        //mYear、mMonth、mDayを引数に与えて生成し, 入力された日付で更新
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間を設定するButtonのリスナー
    private val mOnTimeClickListener = View.OnClickListener {
        //mHour、mMinuteを引数に与えて生成し、入力された時間で更新
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定Buttonのリスナー
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()//Realmに保存/更新
        finish()//InputActivityを閉じて前の画面（MainActivity）に戻る
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)//ツールバーをActionBarとして使えるように設定
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)//ActionBarに戻るボタンを表示
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val intent = intent
        //Taskのidを取り出し、もしEXTRA_TASKが設定されていないと taskId には第二引数で指定している既定値 -1 が代入される
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category_edit_text.setText(mTask!!.category)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()//Realmオブジェクトを取得

        realm.beginTransaction()//Realmでデータを追加、削除など変更を行う場合はbeginTransactionメソッドを呼び出し

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()//Taskクラスを生成

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1//保存されているタスクの中の最大のidの値に1を足したものを設定
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category_edit_text.text.toString()

        //タイトル、内容、日時、カテゴリーをmTaskに設定
        mTask!!.title = title
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date
        mTask!!.category = category

        //データの保存・更新
        //設定したものをデータベースに保存
        realm.copyToRealmOrUpdate(mTask!!)//引数で与えたオブジェクトが存在していれば更新、なければ追加を行うメソッド
        realm.commitTransaction()

        realm.close()

        //これはTaskAlarmReceiverがブロードキャストを受け取った後、タスクのタイトルなどを表示する通知を発行するためにタスクの情報が必要になるから
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)//TaskAlarmReceiverを起動するIntentを作成
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)//Extraにタスクを設定

        //タスクを削除する際に指定したアラームも合わせて削除する必要がある
        //PendingIntentを作成。第2引数にタスクのIDを指定
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT//既存のPendingIntentがあれば、それはそのままでextraのデータだけ置き換えるという指定
        )
        //getSystemService メソッドはシステムレベルのサービスを取得するためのメソッド
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }

}
