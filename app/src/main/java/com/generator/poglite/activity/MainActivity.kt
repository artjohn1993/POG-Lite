package com.generator.poglite.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.generator.poglite.R
import com.generator.poglite.api.GoogleSheetApi
import com.generator.poglite.defaultSettings
import com.generator.poglite.dialog.CloseDialog
import com.generator.poglite.dialog.UrlCheckerDialog
import com.generator.poglite.enum.WebOpenerDB
import com.generator.poglite.events.IdleCheckerEvent
import com.generator.poglite.local_db.DatabaseHandler
import com.generator.poglite.model.GoogleSheet
import com.generator.poglite.model.URLData
import com.generator.poglite.presenter.GoogleSheetPresenterClass
import com.generator.poglite.presenter.GoogleSheetView
import com.generator.poglite.services.IdleChecker
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), GoogleSheetView {
    var db = DatabaseHandler(this)
    var urlData: MutableList<URLData.Details> = ArrayList()
    var total = 0
    var closeDialog = CloseDialog()
    lateinit var urlCheckerDialog: UrlCheckerDialog
    var googleSheet : GoogleSheet.Result? = null

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val apiServer by lazy {
        GoogleSheetApi.create(this)
    }
    val presenter = GoogleSheetPresenterClass(this, apiServer)

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        urlCheckerDialog = UrlCheckerDialog(this)
        versionTxt.text = "Version 20230205.1"
        //POG_lite_v.20230205.1

        bind()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    override fun onResume() {
        super.onResume()
        start.imageResource = R.drawable.play2
        startService(Intent(this,IdleChecker::class.java))
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
        stopService(Intent(this,IdleChecker::class.java))
    }

    private fun bind() {
        setClickEvent()
    }

    private fun setClickEvent() {
        addURL.setOnClickListener {
            addURLEditText(null)
            if (total == 25) {
                addURL.isEnabled = false
            }
        }

        start.setOnClickListener {
            start.imageResource = R.drawable.pause2
            checkDataBase()
        }

        shuffleBtn.setOnClickListener {
            shuffleUrl()
        }

        arrangeBtn.setOnClickListener {
            var tempData = urlData.filter { item ->
                item.url != ""
            }.sortedBy { item -> item.url }
            removeUrlView()
            tempData.forEach { item ->
                addURLEditText(item.url)
            }
        }

        resetBtn.setOnClickListener {
            removeUrlView()
            defaultSettings?.url?.forEach { item ->
                addURLEditText(item)
            }
        }
    }

    private fun saveFactor() {
        db.deleteDatabase(WebOpenerDB.TABLE_FACTOR.getValue())
        db.insertFactor(factor.text.toString())
    }

    private fun saveUrl() {
        db.deleteDatabase(WebOpenerDB.TABLE_URL.getValue())
        urlData.shuffle()
        urlData.forEach { item ->
            db.insertURL(item)
        }
    }

    private fun shuffleUrl() {
        var shuffledUrl = urlData.shuffled()
            .filter { data ->
                data.url != ""
            }
        removeUrlView()

        shuffledUrl
        shuffledUrl.forEach { item ->
            addURLEditText(item.url)
        }
    }

    private fun removeUrlView() {
        urlData.clear()
        urlCon.removeAllViews()
    }

    private fun checkDataBase() {
        checkURL()
    }

    private fun checkURL() {
        presenter.getUrl("https://sheets.googleapis.com/v4/spreadsheets/13vq1wyaG7x9bPYak0OgYHspq14RAb60zvMNled9MyYs/values/pageone?dateTimeRenderOption=FORMATTED_STRING&majorDimension=ROWS&valueRenderOption=FORMATTED_VALUE&key=AIzaSyAdETbAw9fqHW5wCv5Hnipoc1kvGmCEfoA")
    }

    private fun addURLEditText(url: String?,days: String = "", pages:String = "", pauseFrom:String ="", pauseTo:String = "" ) {
        addURL.text = "add URL (${++total})"
        var editText = EditText(this)
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        editText.layoutParams = layout
        editText.id = View.generateViewId()
        editText.hint = "Enter URL"
        editText.setText(url)
        var data = URLData.Details(
            url,
            editText.id.toString(),
            days,
            pages,
            pauseFrom,
            pauseTo
        )
        urlData.add(data)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkArray(editText.id.toString(), s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        urlCon.addView(editText)
    }

    private fun checkArray(id: String, url: String) {
        var index = -1
        urlData.forEach { data ->
            index++
            if (id == data.id) {
                var newData = URLData.Details(url, id,"","","","")
                urlData[index] = newData
            }
        }
    }

    override fun onBackPressed() {
        closeDialog.showDialog(this)
    }

    override fun responseGetUrl(data: GoogleSheet.Result) {
        data.values.removeAt(0)

        googleSheet = data
        data.values.forEach { item ->
            addURLEditText(item[0],item[1],item[2],item[3], item[4] )
        }
        saveUrl()
        saveFactor()
        stopService(Intent(this,IdleChecker::class.java))
        startActivity<WordpressLoaderActivity>()
    }

    override fun responseGetUrlFailed(data: String) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIdleCheckerEvent(event: IdleCheckerEvent) {
        checkDataBase()
    }

}