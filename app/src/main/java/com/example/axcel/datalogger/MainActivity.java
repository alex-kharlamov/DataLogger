package com.example.axcel.datalogger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;




import static com.example.axcel.datalogger.R.id.fab;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    TextView accel_xCoords; // declare X axis object
    TextView accel_yCoords; // declare Y axis object
    TextView accel_zCoords; // declare Z axis object


    TextView gyro_xCoords; // declare X axis object
    TextView gyro_yCoords; // declare Y axis object
    TextView gyro_zCoords;

    CheckBox log_checker;

    String current_activity;
    int recorder_checker = 0;

    BufferedWriter file;

    EditText custom_input;
    TextView text_cur_activity;

    FloatingActionButton fab;

    Queue<Float> dataQueue = new PriorityQueue<>();

    Des_tree des_tree;

    String[] activities = {"Eating", "Riding on train", "SittingChair", "Walking"};

    int queue_checker = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder_checker == 0) {
                    Snackbar.make(view, "Recording is activated", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startRecording();
                    recorder_checker += 1;

                    fab.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    Snackbar.make(view, "Recording is deactivated", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    stopRecording();
                    recorder_checker -= 1;
                    fab.setImageResource(android.R.drawable.ic_media_play);
                }

            }
        });


        accel_xCoords =(TextView)findViewById(R.id.accel_1); // create X axis object
        accel_yCoords =(TextView)findViewById(R.id.accel_2); // create Y axis object
        accel_zCoords =(TextView)findViewById(R.id.accel_3); // create Z axis object

        gyro_xCoords =(TextView)findViewById(R.id.gyro_1); // create X axis object
        gyro_yCoords =(TextView)findViewById(R.id.gyro_2); // create Y axis object
        gyro_zCoords =(TextView)findViewById(R.id.gyro_3); // create Z axis object

        log_checker = (CheckBox) findViewById(R.id.checkBox);

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        // add listener. The listener will be  (this) class
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);


        String[] mine_activities = {"Walking", "Sleeping", "Eating", "Riding on train",
                "Driving", "Dancing", "Running", "Cycling", "Gym", "Custom"};


        custom_input = (EditText) findViewById(R.id.custom_text);

        text_cur_activity = (TextView) findViewById(R.id.cur_movement);

        Spinner spinner = (Spinner) findViewById(R.id.mine_activity);
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mine_activities);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);

        OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);

                current_activity = item;
                //custom_input.setText(item);
                if (item == "Custom"){
                    custom_input.setVisibility(View.VISIBLE);
                } else {
                    custom_input.setVisibility(View.INVISIBLE);
                }
                current_activity = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }


    public void onSensorChanged(SensorEvent event){

        if (recorder_checker == 1) {

            if (current_activity == "Custom") {
                current_activity = custom_input.getText().toString();
            }

            // check sensor type
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                // assign directions
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                accel_xCoords.setText("X: " + x);
                accel_yCoords.setText("Y: " + y);
                accel_zCoords.setText("Z: " + z);


                if (log_checker.isChecked()) {
                    write("Accel", current_activity, event.values);
                }


                dataQueue.add((float) 0);
                dataQueue.add(x);
                dataQueue.add(y);
                dataQueue.add(z);


            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                // assign directions
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                gyro_xCoords.setText("X: " + x);
                gyro_yCoords.setText("Y: " + y);
                gyro_zCoords.setText("Z: " + z);

                if (log_checker.isChecked()) {
                    write("Gyro", current_activity, event.values);
                }

                dataQueue.add((float) 1.0);
                dataQueue.add(x);
                dataQueue.add(y);
                dataQueue.add(z);

            }
        }

        if (dataQueue.size() >= 20) {
            float[] cur_data = new float[200];

            for (int i = 0; i < 20; ++i){
                cur_data[i] = dataQueue.poll();
            }

            int pred = des_tree.predict(cur_data);
            text_cur_activity.setText(activities[pred]);

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }



    private void startRecording() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Prepare data storage
        File dir = new File(Environment.getExternalStorageDirectory() + "/Download/Logs/");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String name = "Data" + System.currentTimeMillis() + ".csv";
        File filename = new File(dir, name);
        try {
            file = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String tag, String[] values) {
        if (file == null) {
            return;
        }

        String line = "";
        if (values != null) {
            for (String value : values) {
                line += "," + value;
            }
        }
        line = Long.toString(System.currentTimeMillis()) + "," + tag + line
                + "\n";

        try {
            file.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String tag, String current_activity, float[] values) {
        String[] array = new String[values.length + 1];
        array[0] = current_activity;
        for (int i = 0; i < values.length; i++) {
            array[i + 1] = Float.toString(values[i]);
        }
        write(tag, array);
    }

    private void write(String tag, double[] values) {
        String[] array = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = Double.toString(values[i]);
        }
        write(tag, array);
    }

    private void write(String tag) {
        write(tag, (String[]) null);
    }


}
