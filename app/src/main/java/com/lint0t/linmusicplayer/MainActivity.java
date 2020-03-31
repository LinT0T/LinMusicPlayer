package com.lint0t.linmusicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lint0t.linmusicplayer.bean.Bean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static bzu.zb_tjw.blurimageview.BlurImageView.BoxBlurFilter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Bean> datas = new ArrayList<>();
    private RecyclerView mrv;
    private ImageView mimg_last, mimg_stop, mimg_next, mimg_cover;
    private MyAdapter adapter;
    private int nowPos = -1, playTime = 0;
    private TextView mtv_name, mtv_singer;
    private MediaPlayer mediaPlayer;
    private ServiceReceiver receiver;
    private MyThreadPool myThreadPool = new MyThreadPool();
    private NotificationManager manager;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        init();
        mediaPlayer = new MediaPlayer();
        adapter = new MyAdapter(datas, this);
        mrv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mrv.setLayoutManager(linearLayoutManager);
        loadMusicData();
        setMyListener();
        showCustomView();

    }

    private void setMyListener() {
        adapter.setOnItemClickListener(new MyAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                nowPos = position;
                Bean bean = datas.get(position);
                playNewMusic(bean);
            }
        });
    }

    private void playNewMusic(Bean bean) {
        mtv_name.setText(bean.getName());
        mtv_singer.setText(bean.getSinger());
        Bitmap bitmap = bean.getBitmap();
        if (bitmap != null) {
            mimg_cover.setImageBitmap(bean.getBitmap());
            linearLayout.setBackground(BoxBlurFilter(bean.getBitmap()));
        }

        stopMusic();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(bean.getPath());
            playMusic();
            showCustomView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (playTime == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                mediaPlayer.seekTo(playTime);
                mediaPlayer.start();
            }
            mimg_stop.setImageResource(R.drawable.stop);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            playTime = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            mimg_stop.setImageResource(R.drawable.play);
        }
    }

    private void loadMusicData() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = resolver.query(uri, null, null, null, null);
                int id = 0;
                while (cursor.moveToNext()) {
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if (duration >= 30000) {
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                        String time = simpleDateFormat.format(new Date(duration));
                        Bitmap bitmap = loadCover(path);
                        id++;
                        String sid = String.valueOf(id);
                        Bean bean = new Bean(name, singer, time, path, sid, bitmap);
                        datas.add(bean);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }


            }
        };
        myThreadPool.setMode(MyThreadPool.USE_FIXEDTHREADPOOL).setRunnable(r).go();

    }

    private void init() {
        mrv = findViewById(R.id.rv_main);
        mimg_last = findViewById(R.id.img_last);
        mimg_next = findViewById(R.id.img_next);
        mtv_name = findViewById(R.id.tv_main_name);
        mtv_singer = findViewById(R.id.tv_main_singer);
        mimg_stop = findViewById(R.id.img_stop);
        mimg_cover = findViewById(R.id.img_bottom_cover);
        linearLayout = findViewById(R.id.root);
        mimg_stop.setOnClickListener(this);
        mimg_next.setOnClickListener(this);
        mimg_last.setOnClickListener(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        receiver = new ServiceReceiver();//----注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_LAST);
        intentFilter.addAction(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_PLAY);
        intentFilter.addAction(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_NEXT);

        registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_last:
                lastMusic();
                break;
            case R.id.img_stop:
                stopMusicBtn();
                break;
            case R.id.img_next:
                nextMusic();
                break;
        }
    }

    private void stopMusicBtn() {
        if (nowPos == -1) {
            Toast.makeText(this, "未选择歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer.isPlaying()) {
            pauseMusic();
        } else {
            playMusic();
        }
        showCustomView();
    }

    private void nextMusic() {
        if (nowPos == datas.size() - 1) {
            Toast.makeText(this, "已是最后一首", Toast.LENGTH_SHORT).show();
            return;
        }
        nowPos += 1;
        Bean next = datas.get(nowPos);
        playNewMusic(next);
    }

    private void lastMusic() {
        if (nowPos == 0 || nowPos == -1) {
            Toast.makeText(this, "已是第一首或未播放歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        nowPos -= 1;
        Bean last = datas.get(nowPos);
        playNewMusic(last);
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playTime = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            mimg_stop.setImageResource(R.drawable.play);
        }
    }

    private void showCustomView() {
        String channelId = "ChannelId";
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification);
        if (nowPos != -1) {
            remoteViews.setTextViewText(R.id.widget_title, datas.get(nowPos).getName()); //设置textview
            remoteViews.setTextViewText(R.id.widget_artist, datas.get(nowPos).getSinger());
            remoteViews.setImageViewBitmap(R.id.widget_album, datas.get(nowPos).getBitmap());

        } else {
            remoteViews.setTextViewText(R.id.widget_title, "选一首想听的歌曲吧"); //设置textview
            remoteViews.setTextViewText(R.id.widget_artist, "");

        }
        if (mediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.widget_play, R.drawable.stop);
        } else {
            remoteViews.setImageViewResource(R.id.widget_play, R.drawable.play);
        }


//设置按钮事件 -- 发送广播 --广播接收后进行对应的处理

        Intent buttonPlayIntent = new Intent(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_LAST); //----设置通知栏按钮广播
        PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(this, 0, buttonPlayIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_prev, pendButtonPlayIntent);//----设置对应的按钮ID监控


        Intent buttonPlayIntent1 = new Intent(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_PLAY); //----设置通知栏按钮广播
        PendingIntent pendButtonPlayIntent1 = PendingIntent.getBroadcast(this, 0, buttonPlayIntent1, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_play, pendButtonPlayIntent1);//----设置对应的按钮ID监控

        Intent buttonPlayIntent2 = new Intent(ServiceReceiver.NOTIFICATION_ITEM_BUTTON_NEXT); //----设置通知栏按钮广播
        PendingIntent pendButtonPlayIntent2 = PendingIntent.getBroadcast(this, 0, buttonPlayIntent2, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_next, pendButtonPlayIntent2);//----设置对应的按钮ID监控


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContent(remoteViews).setSmallIcon(R.drawable.back)
                .setOngoing(true).setCustomBigContentView(remoteViews)
                .setTicker("music is playing");

        manager.notify(1, builder.build());

    }

    public class ServiceReceiver extends BroadcastReceiver {
        public static final String NOTIFICATION_ITEM_BUTTON_LAST = "com.example.notification.ServiceReceiver.last";//----通知栏上一首按钮
        public static final String NOTIFICATION_ITEM_BUTTON_PLAY = "com.example.notification.ServiceReceiver.play";//----通知栏播放按钮
        public static final String NOTIFICATION_ITEM_BUTTON_NEXT = "com.example.notification.ServiceReceiver.next";//----通知栏下一首按钮

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(NOTIFICATION_ITEM_BUTTON_LAST)) {//----通知栏播放按钮响应事件
                Toast.makeText(context, "上一首", Toast.LENGTH_LONG).show();
                lastMusic();
            } else if (action.equals(NOTIFICATION_ITEM_BUTTON_PLAY)) {//----通知栏播放按钮响应事件
                Toast.makeText(context, "暂停", Toast.LENGTH_LONG).show();
                stopMusicBtn();
            } else if (action.equals(NOTIFICATION_ITEM_BUTTON_NEXT)) {//----通知栏下一首按钮响应事件
                Toast.makeText(context, "下一首", Toast.LENGTH_LONG).show();
                nextMusic();
            }
        }
    }

    private Bitmap loadCover(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
        if (cover != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            return bitmap;
        }
        return null;
    }

}