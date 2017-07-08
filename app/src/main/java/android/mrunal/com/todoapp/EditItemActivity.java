package android.mrunal.com.todoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditItemActivity extends AppCompatActivity {

    EditText etEditItem;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        String itemToAdd = getIntent().getStringExtra("Item");
        position = getIntent().getIntExtra("Position", 0);
        etEditItem = (EditText) findViewById(R.id.etEditItem);
        etEditItem.setText(itemToAdd);
    }

    public void saveItem(View view) {

        Intent data = new Intent(EditItemActivity.this, MainActivity.class);
        data.putExtra("UpdatedItem", etEditItem.getText().toString());
        data.putExtra("Position", position);
        setResult(RESULT_OK, data);
        finish();
    }
}
