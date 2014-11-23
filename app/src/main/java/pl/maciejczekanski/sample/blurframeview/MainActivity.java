package pl.maciejczekanski.sample.blurframeview;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SeekBar;

import pl.maciejczekanski.sample.R;
import pl.maciejczekanski.view.BlurFrameLayout;


public class MainActivity extends ActionBarActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final BlurFrameLayout blurFrameLayout = (BlurFrameLayout) findViewById(R.id.blurFrameLayout);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new FooAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                blurFrameLayout.invalidate();
            }
        });

        SeekBar seekBar = (SeekBar)findViewById(R.id.blurRadius);
        seekBar.setProgress((int) blurFrameLayout.getBlurRadius());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurFrameLayout.setBlurRadius(Math.max(progress, 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
