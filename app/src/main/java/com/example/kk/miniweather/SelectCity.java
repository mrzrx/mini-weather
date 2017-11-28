package com.example.kk.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.kk.app.MyApplication;
import com.example.kk.bean.City;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by kk on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackBtn;
    private EditText searchEt;
    private ImageView searchBtn;
    private ListView mList;
    private List<City> mCityList;
    private EditText searchText;
    private MyApplication myApplication;
    //private List<String> ls;
    private ArrayList<String> ls;
    ArrayAdapter<String> adapter;
    private String updateCityCode="-1";
    private String selectNo;
    boolean searched = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);      //加载选择城市布局

       // mBackBtn=(ImageView) findViewById(R.id.title_back);  //添加返回单击事件
       // mBackBtn.setOnClickListener(this);

        searchEt = (EditText)findViewById(R.id.city_search);
        searchBtn = (ImageView)findViewById(R.id.city_search_button);
        searchBtn.setOnClickListener(this);
       // searchText=(EditText)findViewById(R.id.search_city);

        // final Map<String,String> map=new HashMap<String, String>();

        myApplication=(MyApplication) getApplication();   //获取城市列表
        mCityList=myApplication.getCityList();
        ls =new ArrayList<String>();
/*        for (City city:cityList) {
            String cityName = city.getCity();
            ls.add(cityName);
            String cityCode=city.getNumber();
            map.put(cityName,cityCode);

        }*/
        for (int i=0;i<mCityList.size();i++){
            String No_ = Integer.toString(i+1);
            String number= mCityList.get(i).getNumber();
            String provinceName = mCityList.get(i).getProvince();
            String cityName = mCityList.get(i).getCity();
            ls.add(provinceName+"-"+cityName);
        }
        mList=(ListView)findViewById(R.id.title_list);
        adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,ls);   //创建适配器
        adapter.notifyDataSetChanged();
        mList.setAdapter(adapter);   //生成列表

        final Intent intent = new Intent(this,MainActivity.class).setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(searched)
                {
                    updateCityCode = mCityList.get(Integer.parseInt(selectNo)).getNumber();
                }else {
                    updateCityCode = mCityList.get(position).getNumber();
                }
                Log.d("update city code",updateCityCode);

                intent.putExtra("citycode",updateCityCode);
                startActivity(intent);
            }
        };
        //为组件绑定监听
        mList.setOnItemClickListener(itemClickListener);
    }

    /*    mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id){   //为ListView添加单击事件
                Intent intent=new Intent();
                intent.putExtra("cityCode",mCityList.get(position).getNumber());
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }
*/
    @Override
    public void onClick(View v) {       //返回事件
        switch (v.getId()) {
            case R.id.city_search_button:
                String cityKey = searchEt.getText().toString();
                Log.d("Search", cityKey);
                //ArrayList<String> mSearchList = new ArrayList<String>();
                for (int i = 0; i < mCityList.size(); i++) {
                    String No_ = Integer.toString(i + 1);
                    String number = mCityList.get(i).getNumber();
                    String provinceName = mCityList.get(i).getProvince();
                    String cityName = mCityList.get(i).getCity();
                    if (number.equals(cityKey) || cityName.equals(cityKey)) {
                        searched = true;
                        selectNo = Integer.toString(i);
                        ls.clear();
                        ls.add(provinceName + "-" + cityName);
                        Log.d("changed adapter data", "NO." + No_ + ":" + number + "-" + provinceName + "-" + cityName);
                    }

                    adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1, ls);
                    mList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            default:
                break;
        }
    }
}
