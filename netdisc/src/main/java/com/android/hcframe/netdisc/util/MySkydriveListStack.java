package com.android.hcframe.netdisc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.EmptyStackException;
import java.util.Vector;

/**
 * Created by pc on 2016/7/21.
 */
public class MySkydriveListStack<T extends Object> {
    Vector<T> vector;
    public MySkydriveListStack() {
        vector = new Vector();
    }

    /**
     * 弹出栈操作
     */
    public T pop() {
        T t = null;
        if (!isEmpty()) {
            t = vector.lastElement();
            int pos = vector.lastIndexOf(t);
            vector.removeElementAt(pos);
        }
        return t;
    }
    /**
     * 压入栈
     */
    public void push(T t) {
        vector.addElement(t);
    }

    public boolean isEmpty() {
        if (vector.isEmpty()) {
            return true;
        }
        return false;
    }
    /**
     * 获取栈顶元素
     */
    public T getTopObjcet() {
        if (isEmpty()) {
            return null;
        }
        return vector.get(vector.size() - 1);
    }
    /**
     * 获取栈中元素的数量
     */
    public int getStatckSize() {
        return vector.size();
    }
}
