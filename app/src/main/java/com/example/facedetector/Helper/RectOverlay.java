package com.example.facedetector.Helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public class RectOverlay extends GraphicOverlay.Graphic {

    private Paint mRectPaint;
    private GraphicOverlay graphicOverlay;
    private FirebaseVisionFace face;



    public RectOverlay(GraphicOverlay graphicOverlay, FirebaseVisionFace face) {
        super(graphicOverlay);

        mRectPaint = new Paint();
        mRectPaint.setColor(Color.GREEN); // Color.GREEN - int type
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(4.0f);  // 4.0f - float type

        this.graphicOverlay = graphicOverlay;
        this.face = face;

        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {

        Rect rect = face.getBoundingBox();
        RectF rectF = new RectF(rect);
        rectF.left = translateX(rectF.left);
        rectF.right = translateX(rectF.right);
        rectF.top = translateX(rectF.top);
        rectF.bottom = translateX(rectF.bottom);

        canvas.drawRect(rectF, mRectPaint);
    }
}
