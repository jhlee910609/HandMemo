package com.junhee.android.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    RadioGroup color;
    SeekBar stroke_bar;


    ImageView imageView;
    Board board;

    int selectedColor = Color.BLACK;
    float selectedStrokeWidth = 10f;
    int mode = Brush.MODE_DRAW;

    Bitmap captured = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.layout);
        color = (RadioGroup) findViewById(R.id.color);
        stroke_bar = (SeekBar) findViewById(R.id.stroke_bar);
        imageView = (ImageView) findViewById(R.id.imageView);

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureBorad();
            }
        });

        color.setOnCheckedChangeListener(colorChangedListener);

        stroke_bar.setProgress(10);
        stroke_bar.setOnSeekBarChangeListener(WidthChangedListener);
        board = new Board(getBaseContext());
        frameLayout.addView(board);
        setBrush(selectedColor, selectedStrokeWidth, mode);

    }

    private void captureBorad() {
        frameLayout.destroyDrawingCache();
        frameLayout.buildDrawingCache();
        captured = frameLayout.getDrawingCache();
        imageView.setImageBitmap(captured);

    }

    private void setBrush(int color, float width, int mode) {
        Brush brush = Brush.newInstance(color, width, mode);
        board.setBrush(brush);
    }

    private void setBrushColor(int color) {
        this.selectedColor = color;
        setBrush(selectedColor, selectedStrokeWidth, mode);
    }

    private void setStrokeWidth(float width) {
        this.selectedStrokeWidth = width;
        setBrush(selectedColor, selectedStrokeWidth, mode);

    }

    RadioGroup.OnCheckedChangeListener colorChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.btnBlack:
                    mode = Brush.MODE_DRAW;
                    setBrushColor(Color.BLACK);
                    break;

                case R.id.btnBlue:
                    mode = Brush.MODE_DRAW;
                    setBrushColor(Color.BLUE);
                    break;

                case R.id.btnGreen:
                    mode = Brush.MODE_DRAW;
                    setBrushColor(Color.GREEN);
                    break;

                case R.id.btnRed:
                    mode = Brush.MODE_DRAW;
                    setBrushColor(Color.RED);
                    break;

                case R.id.btnErase:
                    mode = Brush.MODE_ERASE;
                    setBrushColor(Color.TRANSPARENT);
                    break;
            }

        }
    };

    SeekBar.OnSeekBarChangeListener WidthChangedListener = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            selectedStrokeWidth = progress + 1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setStrokeWidth(selectedStrokeWidth);

        }
    };


    class Board extends View {
        Paint painter;
        Paint eraser;
        List<Brush> brushes;
        Path current_path;
        Brush current_brush;

        boolean newBrush = true;


        public Board(Context context) {
            super(context);
            setPaint();
            setEraser();
            brushes = new ArrayList<>();
        }

        private void setBrush(Brush brush) {
            current_brush = brush;
            newBrush = true;

        }

        private void setEraser() {
            eraser = new Paint();
            eraser.setColor(Color.TRANSPARENT);
            eraser.setStyle(Paint.Style.STROKE);
            eraser.setAntiAlias(true);
            eraser.setStrokeJoin(Paint.Join.ROUND);
            eraser.setStrokeCap(Paint.Cap.ROUND);
            // 투명으로 덧칠할 수 있게 수정할 수 있는 메소드
            eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        private void setPaint() {
            painter = new Paint();
            painter.setStyle(Paint.Style.STROKE);
            painter.setAntiAlias(true);
            painter.setStrokeJoin(Paint.Join.ROUND);
            painter.setStrokeCap(Paint.Cap.ROUND);
            painter.setXfermode(null);

        }


        private void createPath() {
            if (newBrush) {
                current_path = new Path();
                current_brush.addPath(current_path);
                brushes.add(current_brush);

                newBrush = false;
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            setLayerType(LAYER_TYPE_HARDWARE, null);
            for (Brush brush : brushes) {
                if (brush.erase) {
                    eraser.setStrokeWidth(brush.stroke_width);
                    canvas.drawPath(brush.path, eraser);

                } else {
                    painter.setStrokeWidth(brush.stroke_width);
                    painter.setColor(brush.color);
                    canvas.drawPath(brush.path, painter);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    createPath();
                    current_path.moveTo(x, y);
                    break;

                case MotionEvent.ACTION_MOVE:
                    current_path.lineTo(x, y);
                    break;

                case MotionEvent.ACTION_UP:
                    break;
            }
            invalidate();
            return true;
        }
    }
}

class Brush {

    public final static int MODE_DRAW = 100;
    public final static int MODE_ERASE = 200;

    Path path;
    int color;
    float stroke_width;

    boolean erase = false;

    public static Brush newInstance(int color, float width, int mode) {
        Brush brush = new Brush();
        brush.color = color;
        brush.stroke_width = width;
        switch (mode) {
            case MODE_DRAW:
                brush.erase = false;
                break;

            case MODE_ERASE:
                brush.erase = true;
                break;
        }
        return brush;
    }

    public void addPath(Path path) {
        this.path = path;
    }

}


