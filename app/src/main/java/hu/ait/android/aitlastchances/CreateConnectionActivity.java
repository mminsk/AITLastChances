package hu.ait.android.aitlastchances;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateConnectionActivity extends AppCompatActivity {
    public static final String NAME = "NAME";
    private EditText etFirst;
    private EditText etLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_connection);



        etFirst = (EditText) findViewById(R.id.etFirst);
        etLast = (EditText) findViewById(R.id.etLast);

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEntryValid()) {
                    Intent intent = new Intent();
                    String username = etFirst.getText().toString() + " " + etLast.getText().toString();
                    intent.putExtra(NAME, username);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    public boolean isEntryValid() {
        return (!TextUtils.isEmpty(etFirst.getText().toString()) && !TextUtils.isEmpty(etLast.getText().toString()));
    }
}
