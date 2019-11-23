package jp.techacademy.masahiro.fukushima.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*
import android.database.Cursor
import android.os.Handler
import android.support.v7.app.AlertDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var playbackStopButtonCount = 0
    var cursor: Cursor? = null
    private var timer: Timer? = null
    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        // [進む]ボタンが押された場合
        start_button.setOnClickListener {
            if (cursor!!.isLast()) {
                cursor!!.moveToFirst()
            }
            else {
                cursor!!.moveToNext()
            }
            getImage()
        }

        //[戻る]ボタンが押された場合
        back_button.setOnClickListener {
            if (cursor!!.isFirst()) {
                cursor!!.moveToLast()
            }
            else {
                cursor!!.moveToPrevious()
            }
            getImage()
        }

        //[実行]or[停止]ボタンが押された場合
        playback_stop_button.setOnClickListener {
            playbackStopButtonCount++
            if (playbackStopButtonCount % 2 == 0) {
                timer!!.cancel()
                playback_stop_button.text = "再生"
                start_button.isEnabled = true
                back_button.isEnabled = true
            }
            else {
                playback_stop_button.text = "停止"
                start_button.isEnabled = false
                back_button.isEnabled = false
                // タイマーの作成
                timer = Timer()

                // タイマーの始動
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        handler.post {
                            if (cursor!!.isLast()) {
                                cursor!!.moveToFirst()
                            }
                            else {
                                cursor!!.moveToNext()
                            }
                            getImage()
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
            }
        }
    }

    override fun onRestart() {
        super.onRestart()

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onStop() {
        super.onStop()

        // cursorがnullでないなら、closeする
        if (cursor != null) {
            cursor!!.close()
        }

        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
                else {
                    showAlertDialog()
                }
        }
    }

    private fun showAlertDialog() {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("アプリを終了します")
        alertDialogBuilder.setMessage("アクセス許可が降りなかったため、アプリを終了します")

        // 肯定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("承諾"){_, _ ->
            moveTaskToBack(true)
        }

        // AlertDialogを作成して表示する
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        cursor!!.moveToFirst()
        getImage()
    }
    private fun getImage() {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    }
}