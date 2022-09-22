package com.generator.poglite.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.generator.poglite.R
import com.generator.poglite.events.TimerEvent
import com.generator.poglite.events.UrlLoadedEvent
import com.generator.poglite.model.Wordpress
import org.greenrobot.eventbus.EventBus


class LoaderAdapter(var activity: Activity, var data: MutableList<Wordpress.Result>) :
    RecyclerView.Adapter<LoaderAdapter.LoaderHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaderHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val layout = inflater.inflate(R.layout.loader_view, parent, false)
        return LoaderHolder(layout)
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LoaderHolder, position: Int) {
        holder.page.webViewClient = MyBrowser(activity, holder.page)
        holder.page.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            useWideViewPort = true
//            blockNetworkImage = true
//            setRenderPriority(WebSettings.RenderPriority.HIGH)
            loadWithOverviewMode = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = 0
            pluginState = WebSettings.PluginState.ON
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = false
            safeBrowsingEnabled = false
            setSupportMultipleWindows(true)
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // chromium, enable hardware acceleration
//            holder.page.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        } else {
//            // older android version, disable hardware acceleration
//            holder.page.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }

        holder.page.loadUrl(data[position].link)
        holder.title.text = data[position].link
    }

    class LoaderHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var title: TextView = itemview.findViewById(R.id.titlePage)
        var page: WebView = itemview.findViewById(R.id.urlPage)
    }

    class MyBrowser(var activity: Activity, var conView: WebView?) :
        WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: String
        ): Boolean {
            view.loadUrl(request)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url != "about:blank") {
                EventBus.getDefault().post(TimerEvent())
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view!!.clearCache(true)
            view.clearHistory()
            view.clearMatches()
            view.clearSslPreferences()
            if (url == "about:blank") {
                EventBus.getDefault().post(UrlLoadedEvent())
            }
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler,
            error: SslError?
        ) {
            println("--onReceivedSslError")
            handler.proceed()
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onSafeBrowsingHit(
            view: WebView?,
            request: WebResourceRequest?,
            threatType: Int,
            callback: SafeBrowsingResponse
        ) {
            super.onSafeBrowsingHit(view, request, threatType, callback)
            callback.proceed(true)
        }
    }
}