package jp.techacademy.yui.kuwahara.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//listViewに一覧で表示する用

//Realmのモデルクラス（Realmのモデルクラスを作るときは必ずopenとRealmObjectを書く必要がある）
//open修飾子を付けるのは、Realmが内部的にTaskを継承したクラスを作成して利用するため
open class Task : RealmObject(), Serializable {//Serializableインターフェイスを実装することでデータを丸ごとファイルに保存したり、TaskAppでいうと別のActivityに渡すことができる
    var title: String = ""      // タイトル
    var contents: String = ""   // 内容
    var date: Date = Date()     // 日時
    //var category: String = ""
    var category: Category? = null//カテゴリー

    // idをプライマリーキーとして設定
    @PrimaryKey   //@PrimaryKeyはRealmがプライマリーキー(主キー)と判断するために必要なもの
    var id: Int = 0
}