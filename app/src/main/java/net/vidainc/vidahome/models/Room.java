package net.vidainc.vidahome.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by Ruan_Lopes on 15-08-14.
 */
public class Room {

    public long id;
    public String name;
    public Bitmap image;
    public Point initial, current;

    public int mDrawable;

    public int halfWidth;
    public int halfHeight;

    Boolean selected;
    public Rect r;

    public Room(){

    }

    public Room(Context c, Point point, int drawable)
    {
        this.mDrawable = drawable;
        this.image = BitmapFactory.decodeResource(c.getResources(), drawable);
        this.initial = new Point(point.x, point.y);
        this.current = new Point(point.x, point.y);

        this.halfHeight = image.getHeight() / 2;
        this.halfWidth = image.getWidth() / 2;

        r = new Rect(this.getLeft(), this.getTop(), this.getLeft() + this.getImageWidth(),this.getTop() + this.getImageHeight());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Point getInitial() {
        return initial;
    }

    public void setInitial(Point initial) {
        this.initial = initial;
    }

    public Point getCurrent() {
        return current;
    }

    public void setCurrent(Point current) {
        this.current = current;
    }

    public int getDrawable() {
        return mDrawable;
    }

    public void setDrawable(int drawable) {
        mDrawable = drawable;
    }

    public int getHalfWidth() {
        return halfWidth;
    }

    public void setHalfWidth(int halfWidth) {
        this.halfWidth = halfWidth;
    }

    public int getHalfHeight() {
        return halfHeight;
    }

    public void setHalfHeight(int halfHeight) {
        this.halfHeight = halfHeight;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Rect getRectangle() {
        return r;
    }

    public void setRectangle(Rect r) {
        this.r = r;
    }

    public int getImageWidth() {

        return this.getImage().getWidth();
    }

    public int getImageHeight() {
        return this.getImage().getHeight();
    }

    public void setTop(int y)
    {
        this.current.y = y;
    }

    public int getTop()
    {
        return this.current.y;
    }

    public void setLeft(int x)
    {
        this.current.x = x;
    }

    public int getLeft() { return this.current.x; }

    public void moveTo(int x, int y)
    {
        setLeft(x);
        setTop(y);
        //resetRectangle();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
