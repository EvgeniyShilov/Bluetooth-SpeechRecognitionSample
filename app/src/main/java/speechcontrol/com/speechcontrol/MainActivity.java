package speechcontrol.com.speechcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    BluetoothSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 321);
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        try{
            BluetoothDevice device = bluetooth.getRemoteDevice("20:16:03:08:53:65");
            Method m = device.getClass().getMethod(
                    "createRfcommSocket", new Class[] {int.class});
            clientSocket = (BluetoothSocket) m.invoke(device, 1);
            clientSocket.connect();
            Toast.makeText(getApplicationContext(), "Подключено", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.go).setOnClickListener(this);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.left).setOnClickListener(this);
        findViewById(R.id.right).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            OutputStream outStream = clientSocket.getOutputStream();
            switch (v.getId()) {
                case R.id.button:
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Произнесите команду");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
                    startActivityForResult(intent, 123);
                    break;
                case R.id.stop:
                    outStream.write(3);
                    break;
                case R.id.left:
                    outStream.write(5);
                    break;
                case R.id.right:
                    outStream.write(4);
                    break;
                case R.id.back:
                    outStream.write(2);
                    break;
                case R.id.go:
                    outStream.write(1);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Ошибка передачи байта", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 123) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) Toast.makeText(this, matches.get(0), Toast.LENGTH_LONG).show();
            try {
                OutputStream outStream = clientSocket.getOutputStream();
                if (matches.get(0).toUpperCase().equals("ВПЕРЕД")) {
                    outStream.write(1);
                }
                if (matches.get(0).toUpperCase().equals("СТОП")) {
                    outStream.write(3);
                }
                if (matches.get(0).toUpperCase().equals("НАЗАД")) {
                    outStream.write(2);
                }
                if (matches.get(0).toUpperCase().equals("ЛЕВО") ||
                        matches.get(0).toUpperCase().equals("ЛЕГО")) {
                    outStream.write(5);
                }
                if (matches.get(0).toUpperCase().equals("В ПРАВО") ||
                        matches.get(0).toUpperCase().equals("ПРАВA")) {
                    outStream.write(4);
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Ошибка передачи байта", Toast.LENGTH_SHORT).show();
            }
        }
    }
}