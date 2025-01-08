package com.joyntech.filemanager;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Objects;

class main {
    interface DeleteHandlerCallback {
        void done(boolean result);
    }

    private static class FileNameSuffixFilter implements FilenameFilter {
        private final String filterValue;
        FileNameSuffixFilter(String value) {
            this.filterValue = value;
        }

        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(filterValue);
        }
    }

    /**
     * 刪除儲存在外部空間的語音留言檔案
     */
    public void DeleteVoiceMessageFile(Context cx, final String name, final DeleteHandlerCallback callback) {
        final boolean[] result = {false};
        Thread thread;
        if (Objects.equals(name, "ALL")) {
            thread = new Thread(() -> {
                FileNameSuffixFilter filter = new FileNameSuffixFilter(".amr");
                File mFileDir = cx.getExternalFilesDir(null);
                if (mFileDir != null) {
                    File[] files = mFileDir.listFiles(filter);
                    if (files != null) {
                        for (final File localFile : files) {
                            Log.d("刪除檔案", localFile.getName());
                            if (localFile.delete()) {
                                Log.d("刪除檔案", "完成");
                            } else {
                                Log.d("刪除檔案", "發生錯誤");
                            }
                        }
                    }
                }
                callback.done(true);
            });
        } else {
            thread = new Thread(() -> {
                result[0] = false;
                FileNameSuffixFilter filter = new FileNameSuffixFilter(".amr");
                File mFileDir = cx.getExternalFilesDir(null);
                if (mFileDir != null) {
                    File[] files = mFileDir.listFiles(filter);
                    if (files != null) {
                        for (final File localFile : files) {
                            if (Objects.equals(name, localFile.getName())) {
                                Log.d("刪除檔案", localFile.getName());
                                if (localFile.delete()) {
                                    Log.d("刪除檔案", "完成");
                                    result[0] = true;
                                } else {
                                    Log.d("刪除檔案", "發生錯誤");
                                }
                            }
                        }
                    }
                }
                callback.done(result[0]);
            });
        }
        thread.start();
    }

    /**
     * 讀取所有在外部空間的檔案
     */
    public File[] ListAllExternalFiles(Context cx) {
        File mFileDir = cx.getExternalFilesDir(null);
        if (mFileDir != null) {
            return mFileDir.listFiles();
        } else {
            return null;
        }
    }

    /**
     * 讀取所有在外部空間的語音留言檔案
     */
    public File[] ListVoiceMessageFiles(Context cx) {
        FileNameSuffixFilter filter = new FileNameSuffixFilter(".amr");
        File mFileDir = cx.getExternalFilesDir(null);
        if (mFileDir != null) {
            return mFileDir.listFiles(filter);
        } else {
            return null;
        }
    }

    /**
     * 單讀取jasper裝置清單檔案
     */
    public File getJasperDeviceFile(Context cx) {
        File mFileDir = cx.getExternalFilesDir(null);
        File result = null;

        if (mFileDir != null) {
            File[] files = mFileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("jasperDeviceList")) {
                        result = file;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 讀取指定的檔案
     */
    public File getSpecificFile(Context cx, String FileName) {
        File mFileDir = cx.getFilesDir();
        //File mFileDir = Environment.getExternalStorageDirectory();
        File result = null;
        if (mFileDir != null) {
            File[] files = mFileDir.listFiles();
            //Log.d("路徑", mFileDir.getPath());
            if (files != null) {
                for (File file : files) {
                    //Log.d("file name", file.getName());
                    if (file.getName().equals(FileName)) {
                        result = file;
                    }
                }
            }
        }
        return result;
    }

    public byte[] getFileData(Context cx, String FileName) {
        File sourceFile = getSpecificFile(cx, FileName);
        if (sourceFile != null) {
            long fileSize = sourceFile.length();
            if (fileSize > 20480) {
                boolean result = sourceFile.delete();
                Log.d("File Size Error", sourceFile.getName() + " -> " + fileSize);
                Log.d("File delete", String.valueOf(result));
                return null;
            }
            byte[] fileBuffer = new byte[(int) fileSize];
            try {
                FileInputStream srcFileStream = new FileInputStream(sourceFile);
                srcFileStream.read(fileBuffer);
                srcFileStream.close();
                return fileBuffer;
            } catch (IOException e) {
                Log.e("Exception", Objects.requireNonNull(e.getMessage()));
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean WriteDataToFile(Context cx, String fileName, byte[] data) {
        try {
            FileOutputStream outputStream = cx.openFileOutput(fileName, Context.MODE_PRIVATE);
            Log.d("OutputStream", outputStream.getFD().toString());
            outputStream.write(data);
            Log.d("儲存語音", "完成");
            outputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("Exception", Objects.requireNonNull(e.getMessage()));
            return false;
        }
    }

}
