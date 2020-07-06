# EgameTube

一个简单高效的Android网络请求库。本库有以下几个特点：

## 1. 体积小，仅有几十K
## 2. 调用简单，最少只要一次调用即可获取数据
## 3. 不依赖第三方框架，仅调用系统API
## 4. 有URL防劫持能力，HTTPDNS+ReTry机制
## 5. 除了封装常见的String，JSON，KV等网络交换格式外，还支持自定义和RAW STREAM
## 6. 可配置HTTPS证书策略(忽略错误或强认证)

本库已在爱游戏和天翼云游戏等项目中应用5年以上，较稳定，可用于初创项目的快速搭建，也可用于稳定项目和外方SDK项目等

# 使用本库的一般步骤：

### 1.使用getInstance()方法获取唯一实例;
### 2.调用init(TubeConfig)方法进行初始化操作;
### 3.调用getString(String, StringTubeListener), getString(String, TubeOptions, StringTubeListener), getJSON(String, JSONTubeListener), getJSON(String, TubeOptions, JSONTubeListener)等方法获取数据;
### 4.在需要增加主机列表时调用addHosts(String, LinkedList)方法增加一组主机列表;
### 5.建议2步骤在Application的初始化中调用;
### 6.在不再使用时调用release()释放资源，由于全局唯一实例，此方法不必须

# 一些实例代码，详情可参考demo模块中的示例代码
  
### 0. getInstance
```
FastTube mFastTube = FastTube.getInstance();
```

### 1. GET
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
### 2. POST
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
### 3. Custom 
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
