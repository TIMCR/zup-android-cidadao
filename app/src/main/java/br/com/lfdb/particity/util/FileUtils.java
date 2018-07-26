package br.com.lfdb.particity.util;

import android.content.Context;
import android.os.Environment;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;

import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import okio.BufferedSink;
import okio.Okio;

public class FileUtils {

    public static boolean imageExists(Context context, String filename) {
        File imagesFolder = getImagesFolder(context);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return new File(imagesFolder, filename).exists();
    }

    public static boolean imageExists(Context context, String subfolder, String filename) {
        File imagesFolder = getImagesFolder(context, subfolder);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return new File(imagesFolder, filename).exists();
    }

    public static File getImagesFolder(Context context) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images");
    }

    public static File getImagesFolder(Context context, String subfolder) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images" + File.separator + subfolder);
    }

    public static File getTempImagesFolder() {
        File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "temp");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return imagesFolder;
    }

    public static void downloadImage(Context context, String url) throws Exception {
        if (!url.startsWith("http")) url = Constantes.REST_URL + (url.startsWith("/") ? url : ("/" + url));

        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        if (!imageExists(context, filename)) {
            try {
                OkHttpClient client = ConstantesBase.OK_HTTP_CLIENT;
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                File file = new File(getImagesFolder(context), filename);
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                sink.writeAll(response.body().source());
                sink.close();
            } catch (Exception e) {
                new File(getImagesFolder(context), filename).delete();
                throw e;
            }
        }
    }

    public static void downloadImage(Context context, String subfolder, String url) throws Exception {
        if (!url.startsWith("http")) url = Constantes.REST_URL + (url.startsWith("/") ? url : ("/" + url));

        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        if (!imageExists(context, subfolder, filename)) {
            try {
                OkHttpClient client = ConstantesBase.OK_HTTP_CLIENT;
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                File file = new File(getImagesFolder(context, subfolder), filename);
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                sink.writeAll(response.body().source());
                sink.close();
            } catch (Exception e) {
                new File(getImagesFolder(context, subfolder), filename).delete();
                throw e;
            }
        }
    }
}
