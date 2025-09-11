package cl.isisur.tonos;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, light;

    private LinearLayout root;
    private TextView tvAccel, tvLight;
    private Button btnT1, btnT2, btnT3, btnT4;

    private SoundPool soundPool;
    private int s1, s2, s3, s4; // ids de sonido cargados

    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShake = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.root);
        tvAccel = findViewById(R.id.tvAccel);
        tvLight = findViewById(R.id.tvLight);
        btnT1 = findViewById(R.id.btnT1);
        btnT2 = findViewById(R.id.btnT2);
        btnT3 = findViewById(R.id.btnT3);
        btnT4 = findViewById(R.id.btnT4);

        // Sensores
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (accelerometer == null) Toast.makeText(this, "Sin acelerómetro", Toast.LENGTH_SHORT).show();
        if (light == null) Toast.makeText(this, "Sin sensor de luz", Toast.LENGTH_SHORT).show();

        // SoundPool
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA) // o GAME
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        // Cargar sonidos desde res/raw
        s1 = soundPool.load(this, R.raw.tono1, 1);
        s2 = soundPool.load(this, R.raw.tono2, 1);
        s3 = soundPool.load(this, R.raw.tono3, 1);
        s4 = soundPool.load(this, R.raw.tono4, 1);

        // Botones → reproducir
        btnT1.setOnClickListener(v -> play(s1, "Tono 1"));
        btnT2.setOnClickListener(v -> play(s2, "Tono 2"));
        btnT3.setOnClickListener(v -> play(s3, "Tono 3"));
        btnT4.setOnClickListener(v -> play(s4, "Tono 4"));
    }

    private void play(int soundId, String label) {
        if (soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
            Toast.makeText(this, "Reproduciendo: " + label, Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (light != null)
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI);
    }

    @Override protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            float ax = event.values[0], ay = event.values[1], az = event.values[2];
            tvAccel.setText(String.format("Acelerómetro: x=%.2f y=%.2f z=%.2f", ax, ay, az));

            float mag = (float) Math.sqrt(ax*ax + ay*ay + az*az);
            long now = System.currentTimeMillis();
            if (mag > SHAKE_THRESHOLD && (now - lastShake) > 1000) {
                lastShake = now;
                play(s2, "Shake → Tono 2");
            }
        }

        if (type == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            tvLight.setText(String.format("Luz: %.1f lx", lux));

            // Fondo y sonido por luz
            if (lux < 20) {
                root.setBackgroundColor(Color.DKGRAY);
                play(s3, "Luz baja → Tono 3");
            } else {
                root.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { /* no-op */ }
}
