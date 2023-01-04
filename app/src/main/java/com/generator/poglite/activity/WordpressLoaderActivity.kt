package com.generator.poglite.activity

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebStorage
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.generator.poglite.R
import com.generator.poglite.adapter.LoaderAdapter
import com.generator.poglite.adapter.LoaderLiteAdapter
import com.generator.poglite.api.WordpressApi
import com.generator.poglite.data.CalendarData
import com.generator.poglite.data.WordpressData
import com.generator.poglite.enum.DownloadStatus
import com.generator.poglite.events.TimerEvent
import com.generator.poglite.events.UrlLoadedEvent
import com.generator.poglite.local_db.DatabaseHandler
import com.generator.poglite.model.LoadingUrl
import com.generator.poglite.model.URLData
import com.generator.poglite.model.Wordpress
import com.generator.poglite.presenter.WordpressPresenterClass
import com.generator.poglite.presenter.WordpressView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_wordpress_loader.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class WordpressLoaderActivity : AppCompatActivity(), WordpressView {

    var wordpressResponse: MutableList<Wordpress.Result> = ArrayList()
    var wordpressLoadUrl: MutableList<Wordpress.Result> = ArrayList()
    var wordpressRawResponse: MutableList<Wordpress.Result> = ArrayList()
    var wordpressLiteData: MutableList<LoadingUrl.Data> = ArrayList()
    var urlData: MutableList<URLData.Details> = ArrayList()
    var db = DatabaseHandler(this)
    var factor = 0
    val calendar = CalendarData()
    val wordpressData = WordpressData()
    var page = 1
    var totalWordpress = 0
    var loadedWordpress = 0

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val apiServer by lazy {
        WordpressApi.create(this)
    }
    val presenter = WordpressPresenterClass(this, apiServer)
    val timer = object : CountDownTimer(10 * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            WebStorage.getInstance().deleteAllData()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
            removeRecyclerView()
            displayWordpress()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordpress_loader)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        title = ""
        bind()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
        downloadingCon.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    override fun responseGetLatestPost(data: List<Wordpress.Result>) {
        wordpressData.prepareDownloadedData(data, page) { status ->
            when (status) {
                DownloadStatus.EMPTY -> {
                    page = 1
                    downloadingCon.visibility = View.GONE
                    setAppTitle()
                    val message = "No data in ${urlData[0].url}"
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Handler().postDelayed({
                        urlData.removeAt(0)
                        setAppTitle()
                        displayWordpress()
                    }, 4000)
                }
                DownloadStatus.NEXT -> {
                    wordpressData.addWordpressData(wordpressRawResponse, data, { data ->
                        wordpressRawResponse = data
                        page++
                        downloadWordpress()
                    })
                }
                DownloadStatus.DONE -> {
                    prepareToDisplay()
                }
            }
        }
    }

    override fun responseGetLatestPostFailed(data: String) {
        if (page != 1) {
            prepareToDisplay()
        } else {
            downloadingCon.visibility = View.GONE
            setAppTitle()
            val message = "failed to download ${urlData[0].url}"
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()

            Handler().postDelayed({
                urlData.removeAt(0)
                downloadWordpress()
            }, 5000)
        }
    }

    private fun setAppTitle() {
        if (totalWordpress != 0 && loadedWordpress != 0 && !wordpressLiteData.isEmpty()) {
            var lastIndex = wordpressLiteData.count() - 1
            wordpressLiteData[lastIndex].totalWordpress = totalWordpress.toString()
            wordpressLiteData[lastIndex].loadedWordpress = loadedWordpress.toString()
            loadedRecycler.adapter!!.notifyDataSetChanged()
        }
    }

    private fun resetWordpress() {
        totalWordpress = 0
        loadedWordpress = 0
        setAppTitle()
    }

    private fun prepareToDisplay() {
        downloadingCon.visibility = View.GONE
        page = 1
        if (urlData[0].days == "0") {
            var singleWordpress: MutableList<Wordpress.Result> = ArrayList()
            for (i in 1..5) {
                singleWordpress.add(Wordpress.Result("",urlData[0].url!!,"",Wordpress.Title("")))
            }
            wordpressResponse = wordpressData.factorWordpress(singleWordpress, urlData[0].pages.toInt(), true)
        } else {
            wordpressResponse = wordpressData.factorWordpress(wordpressRawResponse, urlData[0].pages.toInt(), false)
        }
        urlData.removeAt(0)
        totalWordpress = wordpressResponse.count() / 2
        wordpressRawResponse.clear()
        displayWordpress()
    }

    private fun displayWordpress() {
        checkWordpressResponse {
            if (wordpressResponse[0].link != "about:blank") {
                loadedWordpress++
                setAppTitle()
            }
            wordpressLoadUrl.add(0, wordpressResponse[0])
            wordpressResponse.removeAt(0)
            recyclerWordpressLoader.adapter!!.notifyDataSetChanged()
//            recyclerWordpressLoader.scrollToPosition(wordpressLoadUrl.count() - 1)
            //wordpressLoadUrl.removeAt(0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun bind() {
        setRecycler()
        urlData = db.getURL()
        if (urlData.isEmpty()) {
            downloadingCon.visibility = View.GONE
            Snackbar.make(
                findViewById(android.R.id.content),
                "URL not available at this time",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            downloadWordpress()
        }
    }

    private fun downloadWordpress() {
        checkUrlData { item ->
            urlData = item
            resetWordpress()
            if(urlData[0].days == "0") {
                prepareToDisplay()
            } else {
                downloadingCon.visibility = View.VISIBLE
                downloadingUrlTxt.text = urlData[0].url
                presenter.getLatestPost("http://" + urlData[0].url + "/wp-json/wp/v2/posts?orderby=date&&page=${page}&&order=desc&&after=${calendar.getLastMonth(urlData[0].days)}")
            }
            }
    }

    private fun checkWordpressResponse(completionHandler: () -> Unit) {
        if (wordpressResponse.isEmpty()) {
            downloadWordpress()
        } else {
            completionHandler.invoke()
        }
    }

    private fun addLiteData(url: String) {
        var data = LoadingUrl.Data(
            url,
            totalWordpress.toString(),
            loadedWordpress.toString()
        )
        if (!wordpressLiteData.contains(data)) {
            wordpressLiteData.add(data)
        }
        loadedRecycler.adapter!!.notifyDataSetChanged()
    }

    private fun checkUrlData(completionHandler: (MutableList<URLData.Details>) -> Unit) {
        if (urlData.isEmpty()) {
            val timeToMatch = Calendar.getInstance()
            var currentHour = timeToMatch[Calendar.HOUR_OF_DAY]

            if(currentHour == 24 || currentHour == 12) {
                finish()
            } else {
                completionHandler.invoke(db.getURL())
            }
        } else {
            completionHandler.invoke(urlData)
        }
    }

    private fun removeRecyclerView() {
        wordpressLoadUrl.clear()
        recyclerWordpressLoader.adapter!!.notifyDataSetChanged()
    }

    @SuppressLint("WrongConstant")
    private fun setRecycler() {
        recyclerWordpressLoader.apply {
            layoutManager =
                LinearLayoutManager(this@WordpressLoaderActivity, LinearLayout.VERTICAL, false)
            adapter = LoaderAdapter(this@WordpressLoaderActivity, wordpressLoadUrl)
        }

        var loadedLayoutManager = LinearLayoutManager(this@WordpressLoaderActivity, LinearLayout.VERTICAL, false)
        loadedRecycler.apply {
            layoutManager = loadedLayoutManager
            adapter = LoaderLiteAdapter(wordpressLiteData.asReversed())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUrlLoadedEvent(event: UrlLoadedEvent) {
        timer.cancel()
        WebStorage.getInstance().deleteAllData()
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        android.webkit.CookieManager.getInstance().flush()
        removeRecyclerView()
        displayWordpress()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTimerEvent(event: TimerEvent) {
        timer.start()
    }
}