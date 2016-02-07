package dc.snake;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class SnakeActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private boolean running;
    private SharedPreferences pref;
    private Toolbar toolbar;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int score = Game.getScore();
            if (score > pref.getInt("high_score", 0))
                pref.edit().putInt("high_score", score).commit();
            int record = pref.getInt("high_score", 0);
            toolbar.setTitle(String.format("%s %d", getString(R.string.score), score));
            toolbar.setSubtitle(String.format("%s %d", getString(R.string.best_score), record));
            handler.postDelayed(runnable, 1000 / Game.SPEED);
        }
    };
    private SnakeView view;
    private MenuItem play_pause;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_snake);
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        view = (SnakeView) findViewById(R.id.SnakeView);
        running = true;
        runnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        running = false;
        play_pause.setIcon(R.drawable.ic_action_play);
        play_pause.setTitle(getString(R.string.play));
        view.pause();
        handler.removeCallbacks(runnable);
    }

    private void resume() {
        running = true;
        play_pause.setIcon(R.drawable.ic_action_pause);
        play_pause.setTitle(getString(R.string.pause));
        view.resume();
        runnable.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_snake, menu);
        play_pause = menu.findItem(R.id.action_play_pause);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                pause();
                view.refresh();
                resume();
                return true;
            case R.id.action_play_pause:
                if (running) pause();
                else resume();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}