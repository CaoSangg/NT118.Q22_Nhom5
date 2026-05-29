package com.example.nhom5projectmobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MangaOfflineDB";
    private static final int DATABASE_VERSION = 2; // Tăng lên 2 để cập nhật database

    public static final String TABLE_STORIES = "offline_stories";
    public static final String TABLE_CHAPTERS = "offline_chapters";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStories = "CREATE TABLE " + TABLE_STORIES + " ("
                + "story_id TEXT PRIMARY KEY, "
                + "title TEXT, "
                + "cover_image TEXT)";
        db.execSQL(createTableStories);

        String createTableChapters = "CREATE TABLE " + TABLE_CHAPTERS + " ("
                + "chapter_id TEXT PRIMARY KEY, "
                + "story_id TEXT, "
                + "chapter_title TEXT, "
                + "local_paths TEXT, "
                + "order_index INTEGER)"; // Thêm cột thứ tự
        db.execSQL(createTableChapters);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTERS);
        onCreate(db);
    }

    public void insertOfflineStory(String storyId, String title, String coverLocalPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("story_id", storyId);
        values.put("title", title);
        values.put("cover_image", coverLocalPath);
        db.insertWithOnConflict(TABLE_STORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void insertOfflineChapter(String chapterId, String storyId, String chapterTitle, String localPaths, long orderIndex) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chapter_id", chapterId);
        values.put("story_id", storyId);
        values.put("chapter_title", chapterTitle);
        values.put("local_paths", localPaths);
        values.put("order_index", orderIndex); // Lưu thứ tự vào DB

        db.insertWithOnConflict(TABLE_CHAPTERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public boolean isChapterDownloaded(String chapterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CHAPTERS + " WHERE chapter_id = ?", new String[]{chapterId});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public List<Story> getAllOfflineStories() {
        List<Story> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STORIES, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("story_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String cover = cursor.getString(cursor.getColumnIndexOrThrow("cover_image"));
                list.add(new Story(id, title, "Ngoại tuyến", cover, 0, "Đã tải về", "Offline", null));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Chapter> getOfflineChapters(String storyId) {
        List<Chapter> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // BÍ QUYẾT LÀ ĐÂY: Sắp xếp ORDER BY order_index ASC
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CHAPTERS + " WHERE story_id = ? ORDER BY order_index ASC", new String[]{storyId});
        if (cursor.moveToFirst()) {
            do {
                String chapId = cursor.getString(cursor.getColumnIndexOrThrow("chapter_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("chapter_title"));
                long orderIdx = cursor.getLong(cursor.getColumnIndexOrThrow("order_index"));

                Chapter chapter = new Chapter();
                chapter.setChapterId(chapId);
                chapter.setTitle(title);
                chapter.setOrderIndex(orderIdx); // Nhận lại thứ tự
                list.add(chapter);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<String> getOfflineChapterPages(String chapterId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CHAPTERS + " WHERE chapter_id = ?", new String[]{chapterId});
        if (cursor.moveToFirst()) {
            String paths = cursor.getString(cursor.getColumnIndexOrThrow("local_paths"));
            if (paths != null && !paths.isEmpty()) {
                String[] arr = paths.split(",");
                for (String path : arr) {
                    list.add(path);
                }
            }
        }
        cursor.close();
        db.close();
        return list;
    }
}