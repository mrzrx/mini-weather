package com.example.kk.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.kk.app.MyApplication;
import com.example.kk.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by kk on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackBtn;
    private ListView mList;
    private List<City> cityList;
    private MyApplication myApplication;
    //private List<String> ls;
    private ArrayList<String> ls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);      //加载选择城市布局

        mBackBtn=(ImageView) findViewById(R.id.title_back);  //添加返回单击事件
        mBackBtn.setOnClickListener(this);

        // final Map<String,String> map=new HashMap<String, String>();

        myApplication=(MyApplication) getApplication();   //获取城市列表
        cityList=myApplication.getCityList();
        ls =new ArrayList<String>();
/*        for (City city:cityList) {
            String cityName = city.getCity();
            ls.add(cityName);
            String cityCode=city.getNumber();
            map.put(cityName,cityCode);

        }*/
        for (int j=0;j<cityList.size();j++){
            String cityName=cityList.get(j).getCity();
            ls.add(cityName);
        }
        mList=(ListView)findViewById(R.id.title_list);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,ls);   //创建适配器
        mList.setAdapter(adapter);   //生成列表

        AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener(){
       // mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView,View view,int position,long l){   //为ListView添加单击事件
                String cityCode=cityList.get(position).getNumber();
                Log.d("cityCode",cityCode);
                Intent i=new Intent();
                i.putExtra("cityCode",cityCode);
                setResult(RESULT_OK,i);
                finish();
            }
        };
        mList.setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onClick(View v) {       //返回事件
        switch(v.getId()) {
            case R.id.title_back:
                Intent i = new Intent(this,MainActivity.class);   //传递数据
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }

}
