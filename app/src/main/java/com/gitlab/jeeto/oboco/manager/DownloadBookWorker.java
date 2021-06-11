package com.gitlab.jeeto.oboco.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import okhttp3.ResponseBody;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadBookWorker extends Worker {
    private final static String CHANNEL_ID = "download";
    private final static String CHANNEL_NAME = "download";
    private final static String CHANNEL_DESCRIPTION = "download";
    private final static int NOTIFICATION_ID = (int) System.currentTimeMillis();
    private NotificationManager notificationManager;

    public DownloadBookWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public Result doWork() {
        Data data = getInputData();

        Long bookId = data.getLong("bookId", 0);

        if(bookId == 0) {
            return Result.failure();
        }

        try {
            download(bookId);
        } catch(Exception e) {
            //e.printStackTrace();

            return Result.failure();
        }

        return Result.success();
    }

    private ApplicationService createApplicationService(Context context) {
        SharedPreferences sp = context.getSharedPreferences("application", Context.MODE_PRIVATE);
        String baseUrl = sp.getString("baseUrl", "");

        AuthenticationManager authenticationManager = new AuthenticationManager(context);

        ApplicationService applicationService = new ApplicationService(context, baseUrl, authenticationManager);

        return applicationService;
    }

    private void download(Long bookId) throws Exception {
        Context context = getApplicationContext();

        long now = new Date().getTime();
        for(File file: context.getExternalCacheDir().listFiles()) {
            if(file.isFile() && file.getName().endsWith(".download")) {
                // 1 day = 24 * 60 * 60 * 1000
                if((now - file.lastModified()) > 86400000) {
                    file.delete();
                }
            }
        }

        ApplicationService applicationService = createApplicationService(context);

        BookDto book = applicationService.getBook(bookId, "(bookCollection)").blockingGet();

        setForegroundAsync(createForegroundInfo(book.getName()));

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getBookCollection().getName() + File.separator  + book.getName() +  ".cbz");

        if(file.isFile() == false) {
            ResponseBody responseBody = applicationService.downloadBook(bookId).blockingGet();

            File downloadFile = new File(context.getExternalCacheDir(), book.getName() +  ".cbz."  + now + ".download");

            try {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = responseBody.byteStream();
                    outputStream = new FileOutputStream(downloadFile, false);

                    byte[] buffer = new byte[8 * 1024];
                    int bufferSize;
                    while ((bufferSize = inputStream.read(buffer)) != -1) {
                        if (isStopped()) {
                            break;
                        }

                        outputStream.write(buffer, 0, bufferSize);
                    }
                    outputStream.flush();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            // pass
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            // pass
                        }
                    }
                }
            } catch(Exception e) {
                downloadFile.delete();

                throw e;
            }

            if(isStopped()) {
                downloadFile.delete();
            } else {
                File parentFile = file.getParentFile();
                if (parentFile.isDirectory() == false) {
                    parentFile.mkdirs();
                }
                downloadFile.renameTo(file);
            }
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String info) {
        Context context = getApplicationContext();

        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setTicker("Download book")
                .setContentTitle("Download book")
                .setContentText(info)
                .setSmallIcon(R.drawable.notification_action_background)
                .setOngoing(true)
                .setSilent(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(R.drawable.outline_clear_black_24, "Stop", intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationBuilder, CHANNEL_ID);
        }

        return new ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(NotificationCompat.Builder notificationBuilder, String id) {
        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationChannel channel = new NotificationChannel(id, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESCRIPTION);

        notificationManager.createNotificationChannel(channel);
    }

}

