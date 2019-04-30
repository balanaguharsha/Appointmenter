package com.example.researcher.appointmenter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class EditProfile extends AppCompatActivity {
    ListView list;
    ArrayList<String> data=new ArrayList<>();
    myAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        list=findViewById(R.id.list);
        myAdapter=new myAdapter();
        list.setAdapter(myAdapter);
        data.add("Harsha");
        data.add("Vardhan");
        data.add("Testing");



    }
    class myAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v=LayoutInflater.from(getApplicationContext()).inflate(R.layout.each_appointment,null);
            TextView title=v.findViewById(R.id.title);
            TextView full =v.findViewById(R.id.fullName);
            title.setText(data.get(position).charAt(0)+"");
            full.setText(data.get(position));
            return v;
        }
    }
}
