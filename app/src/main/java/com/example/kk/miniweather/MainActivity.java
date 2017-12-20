package com.example.kk.miniweather;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.ArrayList;
import java.util.List;

import com.example.kk.bean.TodayWeather;
import com.example.kk.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import static com.baidu.location.d.j.v;

/**
 * Created by zhangqixun on 16/7/4.
 */
public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private String updateCityCode = "-1";
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ImageView LocateBtn;

    private ImageView[] dots;
    private int[] ids = {R.id.pointWeatherImg1, R.id.pointWeatherImg2};
    private List<View> views;
    private ViewPagerAdapter viewPagerAdapterWeather;
    private ViewPager viewPagerWeather;

    private TextView cityTv, timeTv, humidityTv, wenduTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg,weatherImg1,weatherImg2,weatherImg3,weatherImg4, pmImg;

    private TextView week1Tv, week2Tv, week3Tv, week4Tv, week5Tv, week6Tv, temperature1Tv, temperature2Tv, temperature3Tv,
            temperature4Tv,  temperature5Tv, temperature6Tv,wind1Tv, wind2Tv, wind3Tv, wind4Tv,wind5Tv,wind6Tv,
            climate1Tv, climate2Tv, climate3Tv, climate4Tv,climate5Tv,climate6Tv;

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

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);     //为选择城市添加单击事件
        mCitySelect.setOnClickListener(this);

        LocateBtn = (ImageView) findViewById(R.id.title_location);           //为定位添加单击事件
        LocateBtn.setOnClickListener(this);



        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {       //检查网络状态
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }

        updateCityCode = getIntent().getStringExtra("citycode");       //根据城市码更新天气
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

        initView();         //初始化各项数据为N/A
        initViews();
        initDots();

    }


    void initView() {              //初始化今日天气
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
    }


    private void initViews() {                     //加载滑动视图，并初始化未来四天天气
        LayoutInflater lf = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(lf.inflate(R.layout.future_one, null));
        views.add(lf.inflate(R.layout.future_two, null));
        viewPagerAdapterWeather = new ViewPagerAdapter(views, this);
        viewPagerWeather = (ViewPager) findViewById(R.id.viewpager_weather);
        viewPagerWeather.setAdapter(viewPagerAdapterWeather);
        viewPagerWeather.setOnPageChangeListener(this);

        //未来第一天
        week1Tv = (TextView) views.get(0).findViewById(R.id.week_day1);
        temperature1Tv = (TextView) views.get(0).findViewById(R.id.temperature1);
        climate1Tv = (TextView) views.get(0).findViewById(R.id.climate1);
        wind1Tv = (TextView) views.get(0).findViewById(R.id.wind1);
        weatherImg1 = (ImageView) findViewById(R.id.weather_img);
        week1Tv.setText("N/A");
        temperature1Tv.setText("N/A");
        climate1Tv.setText("N/A");
        wind1Tv.setText("N/A");

        //未来第二天
        week2Tv = (TextView) views.get(0).findViewById(R.id.week_day2);
        temperature2Tv = (TextView) views.get(0).findViewById(R.id.temperature2);
        climate2Tv = (TextView) views.get(0).findViewById(R.id.climate2);
        wind2Tv = (TextView) views.get(0).findViewById(R.id.wind2);
        weatherImg2 = (ImageView) findViewById(R.id.weather_img);
        week2Tv.setText("N/A");
        temperature2Tv.setText("N/A");
        climate2Tv.setText("N/A");
        wind2Tv.setText("N/A");

        //未来第三天
        week3Tv = (TextView) views.get(1).findViewById(R.id.week_day3);
        temperature3Tv = (TextView) views.get(1).findViewById(R.id.temperature3);
        climate3Tv = (TextView) views.get(1).findViewById(R.id.climate3);
        wind3Tv = (TextView) views.get(1).findViewById(R.id.wind3);
        weatherImg3 = (ImageView) findViewById(R.id.weather_img);
        week3Tv.setText("N/A");
        temperature3Tv.setText("N/A");
        climate3Tv.setText("N/A");
        wind3Tv.setText("N/A");

        //未来第四天
        week4Tv = (TextView) views.get(1).findViewById(R.id.week_day4);
        temperature4Tv = (TextView) views.get(1).findViewById(R.id.temperature4);
        climate4Tv = (TextView) views.get(1).findViewById(R.id.climate4);
        wind4Tv = (TextView) views.get(1).findViewById(R.id.wind4);
        weatherImg4 = (ImageView) findViewById(R.id.weather_img);
        week4Tv.setText("N/A");
        temperature4Tv.setText("N/A");
        climate4Tv.setText("N/A");
        wind4Tv.setText("N/A");
    }

    void initDots() {              //初始化导航小圆点
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }


    @Override
    public void onClick(View view) {               //各种单击事件
        if (view.getId() == R.id.title_city_manager) {    //选择城市事件
            Intent i = new Intent(this, SelectCity.class);
            startActivity(i);
        }

        if (view.getId() == R.id.title_location) {        //定位事件
            Log.d("click", "title_city_locate");
            Intent intent = new Intent(this, Locate.class);
            startActivity(intent);
        }

        if (view.getId() == R.id.title_update_btn) {           //更新按钮事件
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);//读取城市ID
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);


            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {    //检查网络状态
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }

        }

    }


    private void queryWeatherCode(String cityCode) {         //获取网络数据
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;     //接口
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {         //创建一个新线程
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);     //调用解析函数
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg = new Message();          //收到消息后更新数据
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


    private TodayWeather parseXML(String xmldata) {      //解析由网络中获取的数据
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

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
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
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
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {           //未来第一天
                                eventType = xmlPullParser.next();
                                Log.d("future1 date", xmlPullParser.getText());
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText());
                                Log.d("future1 low", xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText());
                                Log.d("future1 high", xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                Log.d("future1 type", xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                Log.d("future1 fengli", xmlPullParser.getText());
                                fengliCount++;

                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang1(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 2) {          //未来第二天
                                eventType = xmlPullParser.next();
                                Log.d("future2 date", xmlPullParser.getText());
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText());
                                Log.d("future2 low", xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText());
                                Log.d("future2 high", xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                Log.d("future2 type", xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                Log.d("future2 fengli", xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang2(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 3) {          //未来第三天
                                eventType = xmlPullParser.next();
                                Log.d("future3 date", xmlPullParser.getText());
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText());
                                Log.d("future3 low", xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText());
                                Log.d("future3 high", xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                Log.d("future3 type", xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                Log.d("future3 fengli", xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang3(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 4) {         //未来第四天
                                eventType = xmlPullParser.next();
                                Log.d("future4 date", xmlPullParser.getText());
                                todayWeather.setDate4(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow4(xmlPullParser.getText());
                                Log.d("future4 low", xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh4(xmlPullParser.getText());
                                Log.d("future4 high", xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType4(xmlPullParser.getText());
                                Log.d("future4 type", xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli4(xmlPullParser.getText());
                                Log.d("future4 fengli", xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang4(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            break;
                        }
// 判断当前事件是否为标签元素结束事件
                            case XmlPullParser.END_TAG:
                                break;
                        }
// 进入下一个元素并触发相应事件
                        eventType = xmlPullParser.next();
                }
            } catch(XmlPullParserException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return todayWeather;
        }




    void updateTodayWeather(TodayWeather todayWeather) {         //更新各项数据为当前值
        city_name_Tv.setText(todayWeather.getCity() + "天气");            //更新今日天气
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        wenduTv.setText("当前温度：" + todayWeather.getWendu() + "℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow() + "~" + todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText(todayWeather.getFengxiang() + ":" + todayWeather.getFengli());

        if (todayWeather.getPm25() != null) {
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

        if (todayWeather.getType() != null) {
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


        week1Tv.setText(todayWeather.getDate1());                    //更新未来第一天
        temperature1Tv.setText(todayWeather.getLow1() + "~" + todayWeather.getHigh1());
        climate1Tv.setText(todayWeather.getType1());
        wind1Tv.setText(todayWeather.getFengxiang1() + ":" + todayWeather.getFengli1());

        if (todayWeather.getType1() != null) {
            Log.d("type", todayWeather.getType1());
            switch (todayWeather.getType1()) {
                case "晴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }


        week2Tv.setText(todayWeather.getDate2());                    //更新未来第二天
        temperature2Tv.setText(todayWeather.getLow2() + "~" + todayWeather.getHigh2());
        climate2Tv.setText(todayWeather.getType2());
        wind2Tv.setText(todayWeather.getFengxiang2() + ":" + todayWeather.getFengli2());

        if (todayWeather.getType2() != null) {
            Log.d("type", todayWeather.getType2());
            switch (todayWeather.getType2()) {
                case "晴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }


        week3Tv.setText(todayWeather.getDate3());                     //更新未来第三天
        temperature3Tv.setText(todayWeather.getLow3() + "~" + todayWeather.getHigh3());
        climate3Tv.setText(todayWeather.getType3());
        wind3Tv.setText(todayWeather.getFengxiang3() + ":" + todayWeather.getFengli3());

        if (todayWeather.getType3() != null) {
            Log.d("type", todayWeather.getType3());
            switch (todayWeather.getType3()) {
                case "晴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }


        week4Tv.setText(todayWeather.getDate4());                     //更新未来第四天
        temperature4Tv.setText(todayWeather.getLow4() + "~" + todayWeather.getHigh4());
        climate4Tv.setText(todayWeather.getType4());
        wind4Tv.setText(todayWeather.getFengxiang4() + ":" + todayWeather.getFengli4());

        if (todayWeather.getType4() != null) {
            Log.d("type", todayWeather.getType4());
            switch (todayWeather.getType4()) {
                case "晴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }




    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {       //监听页面滑动

    }



    @Override
    public void onPageSelected(int position) {         //监听页面滑动
        for(int i=0;i<ids.length;i++)
        {
            Log.d("page select id",Integer.toString(i));
            if(i==position)
            {
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {          //监听页面滑动

    }



}