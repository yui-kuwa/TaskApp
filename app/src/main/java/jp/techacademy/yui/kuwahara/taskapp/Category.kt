package jp.techacademy.yui.kuwahara.taskapp

import java.io.Serializable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//スピナーでカテゴリーのリストを表示する用
open class Category :RealmObject(), Serializable{
    var category: String = ""   //カテゴリー

    // idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}