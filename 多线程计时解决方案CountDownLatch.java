// CountDownLatch 介绍： https://blog.csdn.net/liuyi1207164339/article/details/51597408

package com.DomainBuyer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2018/9/30.
 */
public class TestDomainBuyer {

    public static void main(String[] args) {
        try {
            testHttpGet(10, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void testHttpGet(int threadNum, int domainNum) throws InterruptedException {

        int partNum = domainNum / threadNum;
        final   CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            Thread t = new Thread(new DomainThread(i * partNum, (i + 1) * partNum, countDownLatch));
            t.start();
        }
        countDownLatch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("多线程运行时间："+(endTime-startTime)+"ms");

    }
}

class DomainThread implements Runnable {

    private int startIn;
    private int endIn;
    private CountDownLatch latch;

        public DomainThread(int startIn, int endIn, CountDownLatch latch) {
        this.startIn = startIn;
        this.endIn = endIn;
        this.latch = latch;
    }

    @Override
    public void run() {
        Long sTime = System.currentTimeMillis();

        // 1. 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        List<String> strs = new ArrayList<>();

        for (int i = startIn; i < endIn; i++) {
            String url = "https://checkapi.aliyun.com/check/checkdomain?domain=" + i + ".com.cn&command=&token=Ydff62c7b0b868fabd6475d5d560f1374&ua=&currency=&site=&bid=&_csrf_token=";
            HttpGet httpGet = new HttpGet(url);

            try {
                // 设置http头部
                httpGet.setHeader("Accept", "application/json;charset=UTF-8");
                // 3. 执行GET请求
                response = httpClient.execute(httpGet);
                System.out.println(response.getStatusLine());
                // 4. 获取响应实体
                org.apache.http.HttpEntity entity = response.getEntity();
                // 5. 处理响应实体
                if (entity != null) {
                  /*  System.out.println("长度：" + entity.getContentLength());
                    System.out.println("内容：" + EntityUtils.toString(entity));*/

                    JSONObject data = JSONObject.parseObject(EntityUtils.toString(entity));
                    JSONArray modules = data.getJSONArray("module");
                    if(modules == null) continue;;
                    for (Object module : modules) {
                        JSONObject info = (JSONObject) module;
                        String avail = info.getString("avail");
                        if (Integer.valueOf(avail) == 1) {
                            strs.add("域名是 " + i + ".com 可以买");
                        }
                        break;
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Long ETime = System.currentTimeMillis();

        if (strs.size() == 0) {
            System.out.println("从" + startIn + "开始，一个可买域名都没有");
            System.out.println("从" + startIn + "开始，一个可买域名都没有");
            System.out.println("从" + startIn + "开始，一个可买域名都没有");
        } else {
            for (String str : strs) {
                System.out.println(str + ".com 是可用的");
            }
        }
        latch.countDown();
        System.out.println("线程 " + startIn/1000 + " 运行时间："+ (ETime - sTime) + " 毫秒");
    }
}
