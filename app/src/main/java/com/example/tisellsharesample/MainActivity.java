package com.example.tisellsharesample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imageButton1 = findViewById(R.id.imageButtonM);
        ImageButtonListener1 imageButtonlistener1 = new ImageButtonListener1();
        imageButton1.setOnClickListener(imageButtonlistener1);
        ImageButton imageButton2 = findViewById(R.id.imageButtonY);
        ImageButtonListener2 imageButtonlistener2 = new ImageButtonListener2();
        imageButton2.setOnClickListener(imageButtonlistener2);
    }

    private class ImageButtonListener1 implements View.OnClickListener{
        @Override
        public void onClick(View view){
            // drawableリソース(例: R.drawable.my_image)からBitmapを作成
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.vythdefault);
            shareAppData(MainActivity.this, bitmap, "Mercari");
        }
    }
    private class ImageButtonListener2 implements View.OnClickListener{
        @Override
        public void onClick(View view){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.vythdefault);
            shareAppData(MainActivity.this, bitmap, "Yahoo");
        }
    }

    public void shareAppData(Context context, Bitmap bitmap, String text) {
        try {
            //文字入力欄のid editText から 文字入力欄のオブジェクトEditTextを取得
            EditText editText1 = findViewById(R.id.editTextText1); // Title
            String textToCopy = editText1.getText().toString();
            // 1. ClipboardManagerを取得
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 2. ClipDataを作成 (ラベル, テキスト)
            ClipData clip = ClipData.newPlainText("simple text", textToCopy);
            // 3. クリップボードにセット
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                // (オプション) コピー完了をユーザーに通知
                Toast.makeText(this, "コピーしました", Toast.LENGTH_SHORT).show();
            }

            // --- 1. Bitmapを一時ファイル（キャッシュ）として保存 ---
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs(); // フォルダがなければ作成
            File file = new File(cachePath, "temp_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // 高画質で保存
            stream.close();
            // --- 2. FileProviderでURIを取得 ---
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            if (contentUri != null) {
                // --- 3. Intentの作成と起動 ---
//                Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 必須：読み取り権限付与
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));

                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri); // 画像
                //shareIntent.putExtra(Intent.EXTRA_TEXT, text);       // テキスト
                shareIntent.setType("image/png");
                if(Objects.equals(text, "Yahoo")){
                    shareIntent.setPackage("jp.co.yahoo.android.yauction"); // メルカリに限定
                } else if(Objects.equals(text, "Mercari")){
                    shareIntent.setPackage("com.kouzoh.mercari"); // メルカリに限定
                }

                if (shareIntent != null) {
                    // アプリがインストールされている場合、起動
                    //Share menu
                    context.startActivity(Intent.createChooser(shareIntent, "共有する"));
                } else {
                    // 未インストールの場合、Playストアを開く
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + shareIntent.getPackage())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        // Playストアアプリがない場合、ブラウザで開く
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com" + shareIntent.getPackage())));
                    }
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}