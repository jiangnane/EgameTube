# EgameTube
A simple, clear Android network lib.

public class FastTube extends java.lang.Object

快速获取网络数据类

使用此类进行数据获取的基本步骤如下：

1.使用getInstance()方法获取唯一实例;

2.调用init(TubeConfig)方法进行初始化操作;

3.调用getString(String, StringTubeListener), getString(String, TubeOptions, StringTubeListener), getJSON(String, JSONTubeListener), getJSON(String, TubeOptions, JSONTubeListener)等方法获取数据;

4.在需要增加主机列表时调用addHosts(String, LinkedList)方法增加一组主机列表;

5.建议2步骤在Application的初始化中调用;

6.在不再使用时调用release()释放资源，由于全局唯一实例，此方法不必须

作者:
  Hein
  
#0. getInstance
```
FastTube mFastTube = FastTube.getInstance();
```

#1. GET
```
mFastTube.getJSON(TEST_URL1, NORMAL_OPTIONS, new JSONTubeListener<JSONObject>() {

            @Override
            public JSONObject doInBackground(JSONObject water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(JSONObject result) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
```
#2. POST
```
        // KeyValuePostBody
        
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("key", "value");

        TubeOptions opt = new TubeOptions.Builder().setPostBody(new KeyValuePostBody(body)).create();

        mFastTube.post("http://<posturl>/", opt, new StringTubeListener<String>() {

            @Override
            public String doInBackground(String water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(String result) {
                // TODO Auto-generated method stub
                Logger.i("HEIN", result);
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
        

        // JSONPostBody
        
        JSONObject body = new JSONObject();

        try {
            body.put("Name", "HEIN");
            body.put("Age", "30");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TubeOptions opt = new TubeOptions.Builder()
                .setPostBody(new JSONPostBody(body))
                .setPostInGzip(true)
                .create();

        mFastTube.post(testUrl1, opt, new StringTubeListener<String>() {

            @Override
            public String doInBackground(String water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(String result) {
                // TODO Auto-generated method stub
                Logger.i("HEIN", result);
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
        
```
#3. Custom 
```
        EgameTube tube = new EgameTube();
        tube.init(TubeConfig.getDefault());
        TubeOptions opt = new TubeOptions.Builder()
                .setSoTimeOut(15 * 1000)
                .setConnectionTimeOut(15 * 1000)
                .setRange(0, 100)
                .setReconnectionTimes(10).create();
        tube.get(TEST_URL3, opt, new TubeListener<Object, String>() {
            @Override
            public String doInBackground(Object water) throws Exception {

                if (water instanceof TubeResponse) {

                    TubeResponse resp = (TubeResponse) water;
                    InputStream is = resp.toStream();

                    byte[] buf = new byte[8 * 1024];
                    int len = is.read(buf);
                    Logger.d("HEIN", "TubeResponse is OK. Data length: " + len);
                }

                return "OK";
            }

            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onFailed(TubeException e) {
                Logger.e("HEIN", e.getMessage());
            }
        });
```
