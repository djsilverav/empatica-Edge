package com.empatica.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.io.File;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String EMPATICA_API_KEY = "009530a6bb254c9199a09bf845ea7c77"; // TODO insert your API Key here
    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView bvpLabel;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private LinearLayout dataCnt;
    private TextView stressResult;

    int[][] array = new int[1][2];
    int signalEventCount = 0;
    int rawDataSize = 46080;
    int featureDataSize = 7913;
    String [] dataMatrix=new String [rawDataSize];
    float [] featureData=new float [featureDataSize];
    String [] dataCommaSeparated = null;
    float accx_var=0, accy_var=0, accz_var=0, bvp_var=0, temp_var=0, eda_var=0, time_var=0;
    String fileName = "dataSignal";
    String fileNameDirectory="/E4-data/";
    String dataString;
    File dataFile;
    String file_path="";
    EditText emp4Text;
    String name="";
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(7913*4); //7913 features input
    //WebView mWebView = (WebView) findViewById(R.id.webView_stress);

    String formatoFechaCompleto = "yyyy-MM-dd HH:mm:ss.SSS";
    SimpleDateFormat formatedorFecha = new SimpleDateFormat(formatoFechaCompleto);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
    Date now = new Date();
    //String fileName = formatter.format(now) + ".txt";//like 2016_01_12.txt

    private static final String log_FILE_NAME = "log.txt"; //bat+date+bluetooth connected
    private static final String bvp_FILE_NAME = "bvp.txt";
    private static final String acc_FILE_NAME = "acc.txt";
    private static final String gsr_FILE_NAME = "gsr.txt";
    private static final String ibi_FILE_NAME = "ibi.txt";
    private static final String temp_FILE_NAME = "temp.txt";
    private static final String FILE_NAME = "example.txt";
    String JS_DataMatrix = String.join(",", dataMatrix);
    EditText meditTxt;
    MyJavascriptInterface JSInterface = new MyJavascriptInterface();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Llama al layout Main
        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);
        dataCnt = (LinearLayout) findViewById(R.id.dataArea);
        accel_xLabel = (TextView) findViewById(R.id.accel_x);
        accel_yLabel = (TextView) findViewById(R.id.accel_y);
        accel_zLabel = (TextView) findViewById(R.id.accel_z);
        bvpLabel = (TextView) findViewById(R.id.bvp);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        WebView mWebView = (WebView) findViewById(R.id.webView_stress);
        mWebView.getSettings().setJavaScriptEnabled(true);

        //Create the directory to save E4 Data
        //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "emp4Files");
        final Button disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Espacio para colocar todo aquello que se ejecute una vez se presione el boton*/
                Environment.getExternalStorageState();
                //deletingFile(log_FILE_NAME);

                if (deviceManager != null) {
                    deviceManager.disconnect();
                }
            }
        });
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .build();
        mWebView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
        mWebView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        mWebView.addJavascriptInterface(JSInterface, "MyJavascriptInterface");
        /* ZONA DE PRUEBA DE METODOS PARA ENVIAR LA INFORACION A JS */
        //mWebView.loadData("<html><body><script> classify_func(JS_DataMatrix) </html></body></script>");
        //mWebView.evaluateJavascript("res = classify_func('${DataMatrix}')",null);
        /* FIN ZONA DE PRUEBA DE METODOS PARA ENVIAR LA INFORACION A JS */
        initEmpaticaDeviceManager();
        //System.out.println("OnCreate LOOP");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
        deletingFile(log_FILE_NAME);
        deletingFile(bvp_FILE_NAME);
        deletingFile(acc_FILE_NAME);
        deletingFile(gsr_FILE_NAME);
        deletingFile(ibi_FILE_NAME);
        deletingFile(temp_FILE_NAME);
        //System.out.println("initEmpaticaDeviceManager Method");
    }

    public void save(String filenameStr, float data1, float data2, float data3, double timeStamp){
        /*
        Metodo para crear un archivo en la carpeta propia de la aplicacion. No se puede usar el metodo .append
        hace falta un metodo para dejar los archivos libres.
        */
        //Falta crear una carpeta con la fecha
        File directoryToStore = getBaseContext().getExternalFilesDir("TestFolder");
        if (!directoryToStore.exists()) {
            if (directoryToStore.mkdir()) ; //directory is created;
        }
        File file= new File(directoryToStore, "config.txt");

        //System.out.println("Save Method Has Been Tested");
        String text = String.valueOf(timeStamp)+","+String.valueOf(data1)+","+String.valueOf(data2)+","+String.valueOf(data3)+"\n";
        //boolean append=true;
        FileOutputStream fos=null;
        try {
            fos =openFileOutput(filenameStr, MODE_APPEND);
            fos.write(text.getBytes());
            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final int CREATE_FILE = 2;
    /*private void createFile2() {
        //Metodo para crear un archivo en un directorio indicado por el usuario.
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/txt");
        intent.putExtra(Intent.EXTRA_TITLE, "pulsera.txt");
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        startActivityForResult(intent, CREATE_FILE);
    }*/
    public String getArray(String [] myArr){
        String returnStringArray = Arrays.toString(myArr);
        //System.out.println(returnStringArray);
        return returnStringArray;
    }
    public void deletingFile(String myFileName){
        File dir = getFilesDir();
        File file = new File(dir, myFileName);
        boolean deleted = file.delete();
    }
     @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php

        Log.i(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed);

        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e);
            }
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {
        
        /*
         A system error occurred while scanning.
         @see https://developer.android.com/reference/android/bluetooth/le/ScanCallback
        */
        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                Log.e(TAG,"Scan failed: a BLE scan with the same settings is already started by the app");
                break;
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Log.e(TAG,"Scan failed: app cannot be registered");
                break;
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Log.e(TAG,"Scan failed: power optimized scan feature is not supported");
                break;
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                Log.e(TAG,"Scan failed: internal error");
                break;
            default:
                Log.e(TAG,"Scan failed with unknown error (errorCode=" + errorCode + ")");
                break;
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothStateChanged() {
        // E4link detected a bluetooth adapter change
        // Check bluetooth adapter and update your UI accordingly.
        boolean isBluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
        Log.i(TAG, "Bluetooth State Changed: " + isBluetoothOn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());
        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection

            hide();

        } else if (status == EmpaStatus.CONNECTED) {

            show();
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            //AQUI DEBO LEVANTAR EL FLAG PARA METER EN EL ARCHIVO DE LOG
            updateLabel(deviceNameLabel, "");
            hide();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        accx_var = x;
        updateLabel(accel_yLabel, "" + y);
        accy_var = y;
        updateLabel(accel_zLabel, "" + z);
        accz_var = z;
        save(acc_FILE_NAME,x,y,z,timestamp);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        updateLabel(bvpLabel, "" + bvp);
        save(bvp_FILE_NAME, bvp, 0, 0, timestamp);
        bvp_var = bvp;
        dataMatrix[0 + signalEventCount] = String.valueOf(accx_var);
        dataMatrix[1 + signalEventCount] = String.valueOf(accy_var);
        dataMatrix[2 + signalEventCount] = String.valueOf(accz_var);
        dataMatrix[3 + signalEventCount] = String.valueOf(eda_var);
        dataMatrix[4 + signalEventCount] = String.valueOf(temp_var);
        dataMatrix[5 + signalEventCount] = String.valueOf(bvp_var);
        signalEventCount += 6;
        if (signalEventCount == rawDataSize) {
            System.out.println("Matrix2Analyze");
            //Convierto la matrix de datos en un array con comas
            JS_DataMatrix = getArray(dataMatrix);
            //Asigno el array en la clase de envio a JS
            JSInterface.setJSMatrix(JS_DataMatrix);
            //JSInterface.testFunc();
            //Reinicio el contador para sobreescribir la matriz
            signalEventCount = 0;
        }
    }


    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
        save(log_FILE_NAME,battery,0,0,timestamp);
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, "" + gsr);
        eda_var  = gsr;
        save(gsr_FILE_NAME,gsr,0,0,timestamp);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
        save(ibi_FILE_NAME,ibi,0,0,timestamp);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
        temp_var = temp;
        save(temp_FILE_NAME,temp,0,0,timestamp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
                //System.out.println("updateLabel&run");
            }
        });

    }

    @Override
    public void didReceiveTag(double timestamp) {
        save(log_FILE_NAME,0,0,0,timestamp);
    }

    @Override
    public void didEstablishConnection() {
        save(log_FILE_NAME,0,0,1,0);
        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
                }
                else {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
                }
            }
        });
        save(log_FILE_NAME,0,status,0,0);
    }

    void show() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataCnt.setVisibility(View.VISIBLE);
            }
        });
    }

    void hide() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.INVISIBLE);
            }
        });
    }
}
class MyJavascriptInterface {
    String JSMatrix = null;
    public void testFunc(){
        System.out.println("Func Test");
    }
    public void setJSMatrix(String matrix){
         JSMatrix = matrix;
    }
    @JavascriptInterface
    public String getDataMatrix(){
        String dataCommaSeparated = JSMatrix;
        return dataCommaSeparated;
    }
}

class LocalContentWebViewClient extends WebViewClientCompat {

    private final WebViewAssetLoader mAssetLoader;

    LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
        mAssetLoader = assetLoader;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        return mAssetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    @SuppressWarnings("deprecation") // to support API < 21
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
    }
}
