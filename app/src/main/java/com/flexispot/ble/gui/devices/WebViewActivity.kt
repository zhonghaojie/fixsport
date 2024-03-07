
package com.flexispot.ble.gui.devices

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.flexispot.ble.R
import com.flexispot.ble.gui.view.StatusBarUtil



class WebViewActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var webView: WebView? = null
    private var title:TextView?=null
    private var progressBar: ProgressBar? = null
    var able = "CN"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        able = resources.configuration.locale.country

        StatusBarUtil.setStatusBarMode(this, true, R.color.white)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setNavigationOnClickListener(View.OnClickListener { finish() })
        webView = findViewById(R.id.wv_body)
        progressBar= findViewById(R.id.progressbar)

        title=findViewById(R.id.tv_walker_title);
        title!!.setText(getString(R.string.privacy))
        if (able == "CN") {
            webView!!.loadUrl("https://officeecoapi.loctek.com/static/html/agreement/view-agreements-8.html") //加载url
        } else {
            webView!!.loadUrl("https://officeecoapi.loctek.com/static/html/agreement/view-agreements-12.html") //加载url
        }

        //使用webview显示html代码
//        webView.loadDataWithBaseURL(null,"<html><head><title> 欢迎您 </title></head>" +
//                "<body><h2>使用webview显示 html代码</h2></body></html>", "text/html" , "utf-8", null);


        //使用webview显示html代码
//        webView.loadDataWithBaseURL(null,"<html><head><title> 欢迎您 </title></head>" +
//                "<body><h2>使用webview显示 html代码</h2></body></html>", "text/html" , "utf-8", null);
//        webView!!.addJavascriptInterface(this, "android") //添加js监听 这样html就能调用客户端
        webView!!.setWebViewClient(webViewClient)

        val webSettings: WebSettings = webView!!.getSettings()
        webSettings.setJavaScriptEnabled(true) //允许使用js
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE) //不使用缓存，只从网络获取数据.


        //支持屏幕缩放

        //支持屏幕缩放
        webSettings.setSupportZoom(true)
        webSettings.setBuiltInZoomControls(true)

        //不显示webview缩放按钮
//        webSettings.setDisplayZoomControls(false);


    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private val webViewClient: WebViewClient = object : WebViewClient() {

        override fun onPageFinished(
            view: WebView?,
            url: String?
        ) { //页面加载完成
            progressBar!!.visibility = View.GONE
        }

        override   fun onPageStarted(
            view: WebView?,
            url: String?,
            favicon: Bitmap?
        ) { //页面开始加载
            progressBar!!.visibility = View.VISIBLE
        }

        override  fun shouldOverrideUrlLoading(
            view: WebView?,
            url: String
        ): Boolean {
            Log.i("ansen", "拦截url:$url")
            if (url == "http://www.google.com/") {
                Toast.makeText(this@WebViewActivity, "国内不能访问google,拦截该url", Toast.LENGTH_LONG).show()
                return true //表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

}




