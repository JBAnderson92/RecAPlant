package edu.ggc.anderson.recaplant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class Informative extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informative);
    }

    public void browser1(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.thespruce.com/pictures-of-poisonous-plants-2132624"));
        startActivity(browserIntent);
    }
}
