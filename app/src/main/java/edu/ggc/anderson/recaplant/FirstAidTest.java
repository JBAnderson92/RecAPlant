package edu.ggc.anderson.recaplant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class FirstAidTest extends AppCompatActivity {
    public static final String INFORMATIVE = "Informative";
    public static final int REQUEST_CODE = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_aid_test);

        final ImageButton getInformedButton = findViewById(R.id.getInformedButton2);
        getInformedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(INFORMATIVE, "Clicked");

                Intent intent = new Intent(FirstAidTest.this, Informative.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }
    public void moreInfo(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cdc.gov/niosh/topics/plants/symptoms.html"));
        startActivity(browserIntent);
    }
}
