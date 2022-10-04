# Edge Impulse and Empatica e4
Testing app for Empatica E4 with Edge-Impulse ML. The project is based on public [empatica e4 sample app](https://github.com/empatica/empalink-sample-project-android) and based on that example I used the Edge-Impulse Web Assembly deployment option to predict behaviours from the wristband data. 
If you want to run the example, check the empatica e4 example app link. 
If you want to know how I implemented Edge-Impulse ML [Web Assembly File]((https://docs.edgeimpulse.com/docs/deployment/webassembly/through-webassembly-browser)).
As a resume:

  * Download the webAssembly compressed file.
  * In android studio I built a WebView object and load [in-app content](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content).
  * Then, I copy the entire uncompressed _WebAssembly.zip_ files into the assets folder that you created in the previous step. If everything goes right, then you can load your own model like if It was running into a webpage and if you test it, you should be able to get a result.  
  Share data between Javascript Code and Java or Kotlin code. For that part I followed this tutorial [Tutorial 1](https://www.techyourchance.com/communication-webview-javascript-android/) and [Android Studio WebView Documentation](https://developer.android.com/reference/android/webkit/WebView#evaluateJavascript(java.lang.String,%20android.webkit.ValueCallback%3Cjava.lang.String%3E))
  - Basically here what I did was to save into a float array all the data that you need to run the impulse.
  - Then, I converted to a string for sharing between Java and Javascript interfaces.
  - Finally, I converted that string that comes from Java into a float array Javascript object (like in the original WebAssembly file: _index.html_ code) and I send that object as an input of the impulse and It started to work
  
It is not the best code (I recognize, it so spaghetti code :c ), but as an example it should works. Pay attention to the following things: 
  * **assets folder**: It includes all the files that I got from WebAssembly. Specially, pay attention to _index.html_ file.
  * **In main Actitivy**: _**class** LocalContentWebViewClient_ | _**class** MyJavascriptInterface_
  * in _protected void **OnCreate()**_ (from Android Documentation): 
    <code>
    ```    
    final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .build();
    mWebView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
    mWebView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    mWebView.addJavascriptInterface(JSInterface, "MyJavascriptInterface");    
    ```
    </code>
You can download the file project from here: [empatica-ML-project](https://drive.google.com/file/d/1cfhpZVZ4jCuRDPKkaxxu-0jaYqXC-spl/view?usp=sharing)
