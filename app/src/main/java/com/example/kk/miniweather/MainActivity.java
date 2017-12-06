package com.example.kk.miniweather;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.kk.bean.TodayWeather;
import com.example.kk.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import static com.baidu.location.d.j.v;

/**
 * Created by zhangqixun on 16/7/4.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private String updateCityCode="-1" ;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ImageView LocateBtn;

    private TextView cityTv, timeTv, humidityTv,wenduTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private  TextView week1Tv,week2Tv,week3Tv,temperature1Tv,temperature2Tv,temperature3Tv,
            wind1Tv,wind2Tv,wind3Tv,climate1Tv,climate2Tv,climate3Tv;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);    //收到消息后更新数据
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);     //加载布局
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);   //为更新按钮添加单击事件
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {       //检查网络状态
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);     //为选择城市添加单击事件
        mCitySelect.setOnClickListener(this);

        LocateBtn = (ImageView) findViewById(R.id.title_location);
        LocateBtn.setOnClickListener(this);

        initView();         //初始化各项数据为N/A


        updateCityCode = getIntent().getStringExtra("citycode");
        if (updateCityCode != "-1" && updateCityCode != null) {
            queryWeatherCode(updateCityCode);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    "CityCodePreference", Activity.MODE_PRIVATE);
            String defaultCityCode = sharedPreferences.getString("citycode", "");
            if (defaultCityCode != null) {
                Log.d("defaultCityCode", defaultCityCode);
                queryWeatherCode(defaultCityCode);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.title_city_manager){    //选择城市事件
            Intent i=new Intent(this,SelectCity.class);
            startActivity(i);
            //startActivityForResult(i,1);     //处理事件同时传递数据

        }

        if(view.getId()== R.id.title_location) {
            Log.d("click", "title_city_locate");
            Intent intent = new Intent(this, Locate.class);
            startActivity(intent);
        }

        if (view.getId() == R.id.title_update_btn){           //更新按钮事件
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);//读取城市ID
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {    //检查网络状态
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }


/*    protected void onActivityResult(int requestCode, int resultCode, Intent data) {       //接收返回的数据
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }

    }*/




    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {         //获取网络数据
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {         //创建一个新线程
                HttpURLConnection con=null;
                TodayWeather todayWeather=null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);     //调用解析函数
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg =new Message();          //收到消息后更新数据
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }




    private TodayWeather parseXML(String xmldata){      //解析由网络中获取的数据
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
// 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
// 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {   //解析出城市名称
                                    eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());       //将解析的数据放在TodayWeather中
                        } else if (xmlPullParser.getName().equals("updatetime")) {     //解析出更新时间
                        eventType = xmlPullParser.next();
                        todayWeather.setUpdatetime(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("shidu")) {            //解析出湿度
                        eventType = xmlPullParser.next();
                        todayWeather.setShidu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("wendu")) {            //解析出温度
                        eventType = xmlPullParser.next();
                        todayWeather.setWendu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("pm25")) {             //解析出PM2.5数值
                        eventType = xmlPullParser.next();
                        todayWeather.setPm25(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("quality")) {         //解析出PM2.5等级
                        eventType = xmlPullParser.next();
                        todayWeather.setQuality(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {    //解析出风向
                        eventType = xmlPullParser.next();
                        todayWeather.setFengxiang(xmlPullParser.getText());
                        fengxiangCount++;
                    } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {          //解析出风力
                        eventType = xmlPullParser.next();
                        todayWeather.setFengli(xmlPullParser.getText());
                        fengliCount++;
                    } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {              //解析出日期
                        eventType = xmlPullParser.next();
                        todayWeather.setDate(xmlPullParser.getText());
                        dateCount++;
                    } else if (xmlPullParser.getName().equals("high") && highCount == 0) {               //解析出最高温度
                        eventType = xmlPullParser.next();
                        todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                        highCount++;
                    } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {                   //解析出最低温度
                        eventType = xmlPullParser.next();
                        todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                        lowCount++;
                    } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {              //解析出天气类型
                        eventType = xmlPullParser.next();
                        todayWeather.setType(xmlPullParser.getText());
                        typeCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 1){
                                eventType = xmlPullParser.next();
                                Log.d("future1 date",xmlPullParser.getText());
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 1 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText());
                                Log.d("future1 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText());
                                Log.d("future1 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 1){
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                Log.d("future1 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                Log.d("future1 fengli", xmlPullParser.getText());
                                fengliCount++;

                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 1) {
                                    eventType = xmlPullParser.next();
                                    todayWeather.setFengxiang1(xmlPullParser.getText());
                                    fengxiangCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 2){
                                eventType = xmlPullParser.next();
                                Log.d("future2 date",xmlPullParser.getText());
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 2 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText());
                                Log.d("future2 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText());
                                Log.d("future2 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                Log.d("future2 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 2){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                Log.d("future2 fengli",xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang2(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if( xmlPullParser.getName().equals("date") && dateCount == 3){
                                eventType = xmlPullParser.next();
                                Log.d("future3 date",xmlPullParser.getText());
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            } else if(xmlPullParser.getName().equals("low") && lowCount == 3 ){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText());
                                Log.d("future3 low",xmlPullParser.getText());
                                lowCount++;
                            } else if(xmlPullParser.getName().equals("high") && highCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText());
                                Log.d("future3 high",xmlPullParser.getText());
                                highCount++;
                            } else if(xmlPullParser.getName().equals("type") && typeCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                Log.d("future3 type",xmlPullParser.getText());
                                typeCount++;
                            } else if(xmlPullParser.getName().equals("fengli") && fengliCount == 3){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                Log.d("future3 fengli",xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang3(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                }
                break;
// 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    break;
            }
// 进入下一个元素并触发相应事件
            eventType = xmlPullParser.next();
        }
    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
return todayWeather;
}


    void initView(){              //初始化控件内容
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        wenduTv = (TextView) findViewById(R.id.wendu);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        //future
        week1Tv = (TextView)findViewById(R.id.week_day1);
        temperature1Tv = (TextView)findViewById(R.id.temperature1);
        climate1Tv = (TextView)findViewById(R.id.climate1);
        wind1Tv = (TextView)findViewById(R.id.wind1);

        week2Tv = (TextView)findViewById(R.id.week_day2);
        temperature2Tv = (TextView)findViewById(R.id.temperature2);
        climate2Tv = (TextView)findViewById(R.id.climate2);
        wind2Tv = (TextView)findViewById(R.id.wind2);

        week3Tv = (TextView)findViewById(R.id.week_day3);
        temperature3Tv = (TextView)findViewById(R.id.temperature3);
        climate3Tv = (TextView)findViewById(R.id.climate3);
        wind3Tv = (TextView)findViewById(R.id.wind3);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        wenduTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        week1Tv.setText("N/A");
        temperature1Tv.setText("N/A");
        climate1Tv.setText("N/A");
        wind1Tv.setText("N/A");

        week2Tv.setText("N/A");
        temperature2Tv.setText("N/A");
        climate2Tv.setText("N/A");
        wind2Tv.setText("N/A");

        week3Tv.setText("N/A");
        temperature3Tv.setText("N/A");
        climate3Tv.setText("N/A");
        wind3Tv.setText("N/A");
    }



    void updateTodayWeather(TodayWeather todayWeather){         //更新各项数据为当前值
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        wenduTv.setText("当前温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText(todayWeather.getFengxiang()+":"+todayWeather.getFengli());


        week1Tv.setText(todayWeather.getDate1());
        temperature1Tv.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        climate1Tv.setText(todayWeather.getType1());
        wind1Tv.setText(todayWeather.getFengxiang1()+":"+todayWeather.getFengli1());


        week2Tv.setText(todayWeather.getDate2());
        temperature2Tv.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        climate2Tv.setText(todayWeather.getType2());
        wind2Tv.setText(todayWeather.getFengxiang2()+":"+todayWeather.getFengli2());


        week3Tv.setText(todayWeather.getDate3());
        temperature3Tv.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        climate3Tv.setText(todayWeather.getType3());
        wind3Tv.setText(todayWeather.getFengxiang3()+":"+todayWeather.getFengli3());

        if(todayWeather.getPm25()!=null) {
            int pm25 = Integer.parseInt(todayWeather.getPm25());
            if (pm25 <= 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25 >= 51 && pm25 <= 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25 >= 101 && pm25 <= 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25 >= 151 && pm25 <= 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25 >= 201 && pm25 <= 300) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            }
        }
        if(todayWeather.getType()!=null) {
            Log.d("type", todayWeather.getType());
            switch (todayWeather.getType()) {
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }



}