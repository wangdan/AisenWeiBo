package org.aisen.android.common.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileUtils {

	private static final String TAG = FileUtils.class.getSimpleName();

	public static void writeFile(InputStream in, File file) {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		try {
			FileOutputStream out = new FileOutputStream(file);
			byte[] buffer = new byte[1024 * 128];
			int len = -1;
			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readAssetsFile(String file, Context context) {
		StringBuffer sb = new StringBuffer();
		try {
			InputStream is = context.getResources().getAssets().open(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String readLine = null;
			while ((readLine = reader.readLine()) != null) {
				sb.append(readLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static String readFileToString(File file) {
		StringBuffer sb = new StringBuffer();
		try {
			InputStream is = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String readLine = null;
			while ((readLine = reader.readLine()) != null) {
				sb.append(readLine);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		String content = sb.toString();
		Logger.d(TAG, String.format("read file's content = %s", content.length() >= 150 ? content.substring(0, 150) : content));
		return sb.toString();
	}

	public static byte[] readFileToBytes(File file) {
		try {
			return readStreamToBytes(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readStreamToBytes(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 8];
		int length = -1;
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		out.flush();
		byte[] result = out.toByteArray();
		in.close();
		out.close();
		return result;
	}

	public static boolean writeFile(File file, String content) {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.flush();

			Logger.d(TAG, String.format("write file's content = %s", content.length() >= 150 ? content.substring(0, 150) : content));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (Exception e2) {
				}
		}

		return true;
	}
	
	public static boolean writeFile(File file, byte[] bytes) {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		FileOutputStream out = null;
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try {
			out = new FileOutputStream(file);

			byte[] buffer = new byte[1024 * 8];
			int length = -1;
			
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (Exception e2) {
				}
		}

		return true;
	}

	public static void copyFile(File sourceFile, File targetFile) throws Exception {
		FileInputStream in = new FileInputStream(sourceFile);
		byte[] buffer = new byte[128 * 1024];
		int len = -1;
		FileOutputStream out = new FileOutputStream(targetFile);
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
		out.flush();
		out.close();
		in.close();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T readObject(File file, Class<T> clazz) throws Exception {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			
			return (T) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
		}
		
		return null;
	}
	
	public static void writeObject(File file, Serializable object) throws Exception {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			
			out.writeObject(object);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	@SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
