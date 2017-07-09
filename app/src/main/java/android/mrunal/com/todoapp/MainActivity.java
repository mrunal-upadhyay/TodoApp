package android.mrunal.com.todoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 20;
    ArrayList<String> todoItems;
    ArrayAdapter<String> aToDoAdapter;
    ListView lvItems;
    EditText etEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvItems = (ListView) findViewById(R.id.lvItems);
        etEditText = (EditText) findViewById(R.id.etEditText);
        populateArrayItems();
        lvItems.setAdapter(aToDoAdapter);
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String removedItemText = todoItems.remove(i);
                aToDoAdapter.notifyDataSetChanged();
                deleteItem(removedItemText);
                return true;
            }
        });
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemToEdit = todoItems.get(i);
                launchEditActivity(itemToEdit, i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            String EditedItem = data.getStringExtra("UpdatedItem");
            int EditedItemPosition = data.getIntExtra("Position", 0);
            String oldText = todoItems.remove(EditedItemPosition);
            todoItems.add(EditedItemPosition, EditedItem);
            aToDoAdapter.notifyDataSetChanged();
            writeItems(oldText, EditedItem);
        }
    }

    private void launchEditActivity(String itemToEdit, int position) {
        Intent i = new Intent(MainActivity.this, EditItemActivity.class);
        i.putExtra("Item", itemToEdit);
        i.putExtra("Position", position);
        startActivityForResult(i, REQUEST_CODE);
    }

    private void populateArrayItems() {
        readItems();
        aToDoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, todoItems);

    }

    public void onAddItem(View view) {
        String text = etEditText.getText().toString();
        aToDoAdapter.add(text);
        writeItems(text, text);
        etEditText.setText("");
    }

    private void readItems() {
        // Get singleton instance of database
        TodoItemsDatabaseHelper databaseHelper = TodoItemsDatabaseHelper.getInstance(this);
        todoItems = databaseHelper.getAllItems();
    }

    private void deleteItem(String text) {
        TodoItemsDatabaseHelper databaseHelper = TodoItemsDatabaseHelper.getInstance(this);
        databaseHelper.deleteItem(text);
    }
    private void writeItems(String oldText, String newText) {
        TodoItemsDatabaseHelper databaseHelper = TodoItemsDatabaseHelper.getInstance(this);
        databaseHelper.addOrUpdateItem(oldText, newText);
    }
}
