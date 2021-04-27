package jp.techacademy.yui.kuwahara.taskapp

import android.app.Application
import io.realm.Realm

//モデルと接続するRealmデータベースの準備
class TaskApp: Application(){//Applicationクラスを継承
    override fun onCreate() {
        super.onCreate()
    //デフォルトの設定を使う場合はこのように記述
        Realm.init(this)//Realmを初期化
    }
}