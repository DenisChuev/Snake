package dc.snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SnakeView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int SIZE = 45;
    private static final Paint paint = new Paint();
    private final Toast toast;
    private final int border;
    private final int background;
    private Game game;
    private DrawThread thread;
    private boolean isUpdatedSizes;
    private boolean playing;
    private int sizeCell;
    private int indent_h;
    private int indent_w;
    private int width;
    private int height;

    @SuppressLint("ShowToast")
    public SnakeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        toast = Toast.makeText(context, context.getString(R.string.win), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        background = ContextCompat.getColor(context, R.color.background);
        border = ContextCompat.getColor(context, R.color.border);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!isUpdatedSizes) updateSizes();
        thread = new DrawThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        thread.running = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (playing) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if ((game.xCoords[0] + 1) != game.xCoords[1] && (game.xCoords[0] - game.cols + 1) != game.xCoords[1] &&
                        (game.xCoords[0] - 1) != game.xCoords[1] && (game.xCoords[0] + game.cols - 1) != game.xCoords[1])
                    if (x >= getWidth() / 2) game.direction = Direction.RIGHT;
                    else game.direction = Direction.LEFT;
                else if (y >= getHeight() / 2) game.direction = Direction.DOWN;
                else game.direction = Direction.UP;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (playing && !game.move()) {
            toast.show();
        }
        drawBorder(canvas);
        drawBackground(canvas);
        drawSnake(canvas);
    }

    private void drawBorder(Canvas canvas) {
        paint.setColor(border);
        canvas.drawRect(0, 0, width, indent_h, paint);
        canvas.drawRect(width - indent_w, 0, width, height, paint);
        canvas.drawRect(width, height - indent_h, 0, height, paint);
        canvas.drawRect(0, height, indent_w, 0, paint);
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(background);
        canvas.drawRect(indent_w, indent_h, width - indent_w, height - indent_h, paint);
    }

    private void drawSnake(Canvas canvas) {
        drawBody(canvas);
        drawHead(canvas);
        drawFood(canvas);
    }

    private void drawHead(Canvas canvas) {
        paint.setColor(Color.RED);
        float dx = game.xCoords[0] * sizeCell + sizeCell / 2 + indent_w;
        float dy = game.yCoords[0] * sizeCell + sizeCell / 2 + indent_h;
        canvas.drawPoint(dx, dy, paint);
    }

    private void drawBody(Canvas canvas) {
        paint.setColor(Color.BLACK);
        float dx0, dy0, dx1, dy1;
        for (int z = 1; z < Game.getLength(); z++) {
            dx0 = game.xCoords[z] * sizeCell + sizeCell / 2 + indent_w;
            dy0 = game.yCoords[z] * sizeCell + sizeCell / 2 + indent_h;
            dx1 = game.xCoords[z - 1] * sizeCell + sizeCell / 2 + indent_w;
            dy1 = game.yCoords[z - 1] * sizeCell + sizeCell / 2 + indent_h;
            if ((game.xCoords[z] - game.cols + 1) != game.xCoords[z - 1] && (game.xCoords[z] + game.cols - 1) != game.xCoords[z - 1] &&
                    (game.yCoords[z] - game.rows + 1) != game.yCoords[z - 1] && (game.yCoords[z] + game.rows - 1) != game.yCoords[z - 1])
                canvas.drawLine(dx0, dy0, dx1, dy1, paint);
            float dx = game.xCoords[z] * sizeCell + sizeCell / 2 + indent_w;
            float dy = game.yCoords[z] * sizeCell + sizeCell / 2 + indent_h;
            canvas.drawPoint(dx, dy, paint);
        }
    }

    private void drawFood(Canvas canvas) {
        paint.setColor(Color.GREEN);
        float dx = Game.Food.getX() * sizeCell + sizeCell / 2 + indent_w;
        float dy = Game.Food.getY() * sizeCell + sizeCell / 2 + indent_h;
        canvas.drawPoint(dx, dy, paint);
    }

    private void updateSizes() {
        width = getWidth();
        height = getHeight();
        sizeCell = (width + height) / SIZE;
        int rows = height / sizeCell;
        int cols = width / sizeCell;
        indent_h = (height - rows * sizeCell) / 2;
        indent_w = (width - cols * sizeCell) / 2;
        paint.setStrokeWidth(sizeCell - sizeCell / 10);
        game = new Game(rows, cols);
        isUpdatedSizes = true;
        playing = true;
    }

    void pause() {
        playing = false;
    }

    void resume() {
        playing = true;
    }

    void refresh() {
        game.direction = Direction.NONE;
    }


    private class DrawThread extends Thread {

        private boolean running;

        public DrawThread() {
            running = true;
            start();
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    if (null == (canvas = getHolder().lockCanvas())) continue;
                    synchronized (getHolder()) {
                        onDraw(canvas);
                    }
                } finally {
                    if (canvas != null) getHolder().unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(1000 / Game.SPEED);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}