package com.playcode.runrunrun.utils;

import com.amap.api.maps.model.LatLng;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;


/**
 * Created by anpoz on 2016/4/3.
 */
public class BOSUtils {
    private final static String secretKey = "2c02b93def274d7e827f008ee2c05155";
    private final static String accessKey = "b43f8e4214e6497dbf6aee6315aa9b69";

    private final static String BUCKET_NAME = "codeczx";
    private final static String BOS_ENDPOINT = "http://gz.bcebos.com";

    private BosClient mBosClient;

    private static BOSUtils instance;

    private BOSUtils() {
        refreshBOSClient();
    }

    private void refreshBOSClient() {
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKey, secretKey));
        config.setMaxConnections(10);
        config.setEndpoint(BOS_ENDPOINT);    //传入Bucket所在区域域名
        config.setConnectionTimeoutInMillis(5000);
        config.setSocketTimeoutInMillis(3000);
        mBosClient = new BosClient(config);
    }

    public static BOSUtils getInstance() {
        if (instance == null) {
            synchronized (BOSUtils.class) {
                if (instance == null) {
                    instance = new BOSUtils();
                }
            }
        }
        return instance;
    }

    private InputStream getInputStreamWithKey(String key) {
        refreshBOSClient();
        return mBosClient
                .getObject(BUCKET_NAME, key)
                .getObjectContent();
    }

    public Observable<String> uploadFile(InputStream inputStream, String key) {
        refreshBOSClient();
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(mBosClient.putObject(BUCKET_NAME, key, inputStream).getETag());
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public Observable<File> getFileWithKey(File file, String key) {
        refreshBOSClient();
        return Observable.create(subscriber -> {
            try {
                OutputStream os = new FileOutputStream(file);
                InputStream in = getInputStreamWithKey(key);
                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                in.close();
                os.close();
                subscriber.onNext(file);
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public void deleteFile(String key) {
        refreshBOSClient();
        mBosClient.deleteObject(BUCKET_NAME, key);
    }

    public Observable<String> uploadPoints(String pointsStr, String pointskey) {
        refreshBOSClient();
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(mBosClient.putObject(BUCKET_NAME, pointskey, pointsStr).getETag());
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<LatLng>> downloadPoints(String key) {
        refreshBOSClient();
        return Observable.create(subscriber -> {
            InputStream is = mBosClient.getObject(BUCKET_NAME, key).getObjectContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Gson gson = new Gson();
                List<LatLng> points = new ArrayList<>();
                points = gson.fromJson(sb.toString(), new TypeToken<List<LatLng>>() {
                }.getType());
                subscriber.onNext(points);
                is.close();
            } catch (IOException e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }
}
