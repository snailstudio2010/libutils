package com.snailstudio2010.libutils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class Cache {
    private static final String TAG = Cache.class.getSimpleName();
    public static String rootName;
    public static String spName;
    private static String name;
    private static Context context;
    private static String filename;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private FileInputStream fis;
    private DataInputStream dis;
    private FileOutputStream fos;
    private DataOutputStream dos;

    private Cache() {
    }

    public static void initialize(Context context, String name) {
        if (Cache.context == null) {
            Cache.context = context;
            Cache.name = name;
            setRootName(name);
            initSharedPreferences(name);
        }
    }

    public static Cache getInstance() {
        return new Cache();
    }

    public static void initSharedPreferences() {
        filename = context.getPackageName();
        initSharedPreferences(filename);
    }

    public static void initSharedPreferences(String name) {
        spName = name.replace(File.separator, "_");
        sharedPreferences = context.getSharedPreferences(spName,
                Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void setRootName(String name) {
        if (!TextUtils.isEmpty(name)) {
            rootName = getStoreDir().getPath()
                    + (name.startsWith(File.separator) ? name : File.separator + name);
            createRootDir();
        } else
            rootName = getStoreDir().getPath();
    }

    public static File getFile(String[] dirName, String fileName) {
        String path = rootName;
        if (dirName != null) {
            for (String name : dirName) {
                path += File.separator + name;
            }
        }
        return new File(path + File.separator + fileName);
    }

    public static Bitmap readBitmap(String[] cacheName, String fileName) {
        File file = getFile(cacheName, fileName);
        if (!file.exists())
            return null;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        return bitmap;
    }

    public static Bitmap readBitmap(File file) {
        if (!file.exists())
            return null;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(file.getPath());
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // SharedPreferences
    //////////////////////////////////////////////////////////////////////////////////////////////
    public static int readInt(String name, int arg) {
        return sharedPreferences.getInt(name, arg);
    }

    public static void writeInt(String name, int a) {
        editor.putInt(name, a);
        editor.commit();
    }

    public static float readFloat(String name, float arg) {
        return sharedPreferences.getFloat(name, arg);
    }

    public static void writeFloat(String name, float a) {
        editor.putFloat(name, a);
        editor.commit();
    }

    public static double readDouble(String name, double arg) {
        double result = arg;
        try {
            String str = sharedPreferences.getString(name, arg + "");
            result = Double.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public static void writeDouble(String name, double a) {
        editor.putString(name, a + "");
        editor.commit();
    }

    public static String readString(String name, String arg) {
        return sharedPreferences.getString(name, arg);
    }

    public static void writeString(String name, String a) {
        editor.putString(name, a);
        editor.commit();
    }

    public static Boolean readBoolean(String name, Boolean arg) {
        return sharedPreferences.getBoolean(name, arg);
    }

    public static void writeBoolean(String name, Boolean a) {
        editor.putBoolean(name, a);
        editor.commit();
    }

    public static String getFileStr(String path) {
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    // byte[] → Bitmap
    public static Bitmap convertBytes2Bimap(byte[] b) {
        if (b.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public static Bitmap getImageFromStr(String str) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理

        byte[] buffer = Base64.decode(str, Base64.DEFAULT);

        return convertBytes2Bimap(buffer);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 拷贝assets下的资源文件到SD卡中(可以超过1M)
    public static void copyBigDataToSD(String assetsFileName,
                                       String strOutFileName) throws IOException {
        File outFile = new File(strOutFileName);
        if (outFile.isFile() && outFile.exists())
            outFile.delete();
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(assetsFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public static String StringFilter(String str) {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 解压缩一个文件
     *
     * @param zipFile    要解压的压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static void unZipFile(File zipFile, String folderPath)
            throws ZipException, IOException {
        if (!zipFile.exists())
            return;
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }

        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), "GB2312");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                Log.d(TAG, "desFile.getPath : " + desFile.getPath());
                desFile.createNewFile();
            } else if (desFile.isFile()) {
                desFile.delete();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[1024];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
        }
    }

    /**
     * 解压缩一个文件
     *
     * @param zipFile      要解压的压缩文件
     * @param relativePath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static void unZipFileFromRelativePath(File zipFile,
                                                 String relativePath) throws ZipException, IOException {
        createDir(relativePath);
        unZipFile(zipFile, getRealFilePath(relativePath));
    }

    public static void unZipFileToCurrentPath(String zipFilePath) throws ZipException, IOException {
        File zipFile = new File(zipFilePath);
        unZipFile(zipFile, zipFile.getParent());
    }

    public static String createDir(String name) {
        createRootDir();
        return createDir(rootName, name);
    }

    public static String createDir(String[] name) {
        createRootDir();
        return createDir(rootName, name);
    }

    public static void createRootDir() {
        File rootFile = new File(rootName);
        if (!rootFile.exists() || !rootFile.isDirectory()) {
            try {
                rootFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                new File(rootName, ".nomedia").createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get root directory
     *
     * @return
     */
    public static File getStoreDir() {
        File dataDir = null;
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment
                .getExternalStorageState())) {
            dataDir = Environment.getExternalStorageDirectory();
        } else {
            dataDir = context.getApplicationContext().getFilesDir();
        }
        return dataDir;
    }

    /**
     * @param name CloudPath
     */
    private static String createDir(String path, String name) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(name))
            return null;
        if (path.endsWith(File.separator))
            path = path.substring(0, path.length() - 1);
        if (name.startsWith(File.separator))
            name = name.substring(1);
        if (name.endsWith(File.separator))
            name = name.substring(0, name.length() - 1);

        return createDir(path, name.split(File.separator));

    }

    private static String createDir(String path, String[] names) {
        if (names == null || names.length == 0)
            return path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs())
                return null;
        }
        for (String dir_name : names) {
            path += File.separator + dir_name;
            file = new File(path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
        }
        return path;
    }

    public static String getRealFilePath(String path) {
        createRootDir();
        if (path.startsWith(File.separator))
            return Cache.rootName + path;
        else
            return Cache.rootName + File.separator + path;
    }

    public static String getRealFilePath(String[] cacheName) {

        String pathename = rootName;
        if (cacheName != null) {
            int i, length = cacheName.length;
            for (i = 0; i < length; i++) {
                pathename += File.separator + cacheName[i];
            }
        }

        return pathename;
    }

    public static boolean hasSDCard() {
        return Environment.getExternalStorageState() != null
                && !Environment.getExternalStorageState().equals("removed");
    }

    private boolean initReadStream() {
        if (dis == null && fis != null)
            dis = new DataInputStream(fis);
        return (dis != null);
    }

    public boolean initRead(String file_path) {
        File f;
        boolean isHaveSDCard = false;
        if (Environment.getExternalStorageState() != null
                && !Environment.getExternalStorageState().equals("removed")) {
            isHaveSDCard = true;
        }

        try {
            if (isHaveSDCard) {
                f = new File(file_path);
                if (f == null || !f.exists()) {
                    return false;
                }
                fis = new FileInputStream(f);
            } else {
                fis = context.openFileInput(file_path);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return (fis != null);

    }

    public boolean initRead(String pathName, String fileName) {
        return initRead(pathName + File.separator + fileName);
    }

    public boolean initRead(String[] cacheName, String fileName) {
        try {
            if (hasSDCard()) {
                File file = getFile(cacheName, fileName);
                Log.d(TAG, "initRead:" + file.getPath());
                Log.d(TAG, "initRead:" + file.exists());
                if (!file.exists()) {
                    return false;
                }
                fis = new FileInputStream(file);
            } else {
                fis = context.openFileInput(fileName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return (fis != null);

    }

    public int readInt(int a) {
        if (!initReadStream())
            return a;
        int result = a;
        try {
            result = dis.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String readTXT(String txtType) {
        if (txtType == null)
            txtType = "UTF-8";

        String str = null;
        int length;
        try {
            length = fis.available();

            byte[] buffer = new byte[length];

            fis.read(buffer);

            str = EncodingUtils.getString(buffer, txtType);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return str;
    }

    public String readString() {
        if (!initReadStream())
            return null;
        String result = null;
        char c[] = new char[500], p;
        int i, j;
        try {
            i = 0;

            p = dis.readChar();

            while (p != '☆') {
                c[i++] = p;
                p = dis.readChar();
            }
            char s[] = new char[i];
            for (j = 0; j < i; j++)
                s[j] = c[j];
            result = String.valueOf(s);

        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<String> readStrings() {
        if (!initReadStream())
            return null;
        ArrayList<String> result = new ArrayList<String>();
        char c[] = new char[500], p;
        int i, j;
        try {
            while (true) {
                i = 0;

                p = dis.readChar();

                while (p != '☆') {
                    c[i++] = p;
                    p = dis.readChar();
                }
                char s[] = new char[i];
                for (j = 0; j < i; j++)
                    s[j] = c[j];
                result.add(String.valueOf(s));
            }

        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void saveRead() {
        try {
            if (fis != null)
                fis.close();
            if (dis != null)
                dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initWriteStream() {
        if (dos == null && fos != null)
            dos = new DataOutputStream(fos);
        return (dos != null);
    }

    public boolean initWrite(String file_path, boolean isSupple) {
        File f;

        try {
            if (Environment.getExternalStorageState() != null
                    && !Environment.getExternalStorageState().equals("removed")) {

                f = new File(file_path);

                File parent_file = f.getParentFile();
                if (!parent_file.exists()) {
                    if (!parent_file.mkdirs())
                        return false;
                }

                if (!f.exists()) {
                    Log.d(TAG, "create New File");
                    if (!f.createNewFile())
                        return false;
                }
                fos = new FileOutputStream(f, isSupple);
            } else {
                if (isSupple)
                    fos = context
                            .openFileOutput(file_path, Context.MODE_APPEND);
                else
                    fos = context.openFileOutput(file_path,
                            Context.MODE_PRIVATE);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return (fos != null);
    }

    public boolean initWrite(String pathName, String fileName, boolean isSupple) {
        File f;

        try {
            if (Environment.getExternalStorageState() != null
                    && !Environment.getExternalStorageState().equals("removed")) {

                File path = new File(pathName);
                if (!path.exists()) {
                    if (!path.mkdirs())
                        return false;
                }

                f = new File(pathName + File.separator + fileName);

                if (!f.exists()) {
                    Log.d(TAG, "create New File");
                    if (!f.createNewFile())
                        return false;
                }
                fos = new FileOutputStream(f, isSupple);
            } else {
                if (isSupple)
                    fos = context.openFileOutput(fileName, Context.MODE_APPEND);
                else
                    fos = context
                            .openFileOutput(fileName, Context.MODE_PRIVATE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return (fos != null);
    }

    public boolean initWrite(String[] cacheName, String fileName,
                             boolean isSupple) {
        File f;
        try {
            if (Environment.getExternalStorageState() != null
                    && !Environment.getExternalStorageState().equals("removed")) {

                createRootDir();

                String pathName = createDir(rootName, cacheName);
                if (pathName == null)
                    return false;

                f = new File(pathName + File.separator + fileName);

                if (!f.exists()) {
                    Log.d(TAG, "create New File : " + f.getPath());
                    f.createNewFile();
                }
                fos = new FileOutputStream(f, isSupple);
            } else {
                if (isSupple)
                    fos = context.openFileOutput(fileName, Context.MODE_APPEND);
                else
                    fos = context
                            .openFileOutput(fileName, Context.MODE_PRIVATE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return (fos != null);
    }

    public boolean writeInt(int a) {
        if (!initWriteStream())
            return false;
        try {
            dos.writeInt(a);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeString(String str) {
        if (!initWriteStream())
            return false;
        try {
            if (str != null) {
                int i;

                int len = str.length();
                for (i = 0; i < len; i++)
                    dos.writeChar(str.charAt(i));
            }
            dos.writeChar('☆');
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeTXT(String str, String txtType) {
        if (txtType == null)
            txtType = "UTF-8";
        byte[] bytes;
        try {
            bytes = str.getBytes(txtType);

            try {
                fos.write(bytes);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        return false;
    }

    public boolean writeBitmap(Bitmap bitmap) {
        return writeBitmap(bitmap, Bitmap.CompressFormat.PNG);
    }

    public boolean writeBitmap(Bitmap bitmap, Bitmap.CompressFormat format) {
        try {
            if (!bitmap.compress(format, 100, fos))
                return false;

            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean writeWhiteBitmap(int width, int height) {
        int[] pix = new int[width * height];

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int r = ((pix[index] >> 16) & 0xff) | 0xff;
                int g = ((pix[index] >> 8) & 0xff) | 0xff;
                int b = (pix[index] & 0xff) | 0xff;
                pix[index] = 0xff000000 | (r << 16) | (g << 8) | b;

            }

        Bitmap bitmap = Bitmap.createBitmap(pix, width, height,
                Bitmap.Config.ARGB_8888);
        return writeBitmap(bitmap);
    }

    public void saveWrite() {
        try {
            if (fos != null)
                fos.close();
            if (dos != null)
                dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getDataOutputStream() {
        initWriteStream();
        return dos;
    }

    public DataInputStream getDataInputStream() {
        initReadStream();
        return dis;
    }

}