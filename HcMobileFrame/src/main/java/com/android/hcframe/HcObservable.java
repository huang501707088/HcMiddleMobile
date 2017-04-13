/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-13 上午10:04:30
*/
package com.android.hcframe;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

/**
 * 观察者的抽象实现
 * 注意：线程不安全的
 * @author jrjin
 * @time 2015-4-13 上午10:14:51
 */
public abstract class HcObservable implements HcSubject {

	private List<HcObserver> observers = new ArrayList<HcObserver>();
	
	public HcObservable() {}

	@Override
	public void addObserver(HcObserver o) {
		// TODO Auto-generated method stub
		if (o == null) {
            throw new NullPointerException();
        }
		if (!observers.contains(o))
            observers.add(o);
	}

	@Override
	public void removeObserver(HcObserver o) {
		// TODO Auto-generated method stub
		observers.remove(o);
	}

	@Override
	public void removeAll() {
		// TODO Auto-generated method stub
		observers.clear();
	}

	@Override
	public void notifyObservers(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		// TODO Auto-generated method stub
		for (HcObserver observer : observers) {
			observer.updateData(subject, data, request, response);
		}
	}

	@Override
	public void notifyObservers() {
		// TODO Auto-generated method stub
		notifyObservers(null);
	}

	@Override
	public void notifyObservers(Object data) {
		// TODO Auto-generated method stub
		notifyObservers(this, data, null, null);
	}

	@Override
	public void notifyObserver(HcObserver o, Object data) {
		// TODO Auto-generated method stub
		o.updateData(this, data, null, null);
	}
	
	public int countObservers() {
        return observers.size();
    }
}
