package com.sleepingbear.ennovel;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PatchActivity extends AppCompatActivity {
    private int fontSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        StringBuffer patch = new StringBuffer();

        patch.append("* 패치 내역" + CommConstants.sqlCR);
        patch.append("- UI 변경" + CommConstants.sqlCR);
        patch.append("- 데이타 복구시 이전 데이타를 초기화 하도록 수정" + CommConstants.sqlCR);
        patch.append("- 용량이 큰 파일의 경우 페이지로 나누어서 보이도록 수정" + CommConstants.sqlCR);
        patch.append("- 영어 소설 다운로드 사이트 추가" + CommConstants.sqlCR);
        patch.append("- 2017.06.20 : 영어소설 어플 개발" + CommConstants.sqlCR);

        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setText(patch.toString());

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setTextSize(fontSize);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
