/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-13 下午1:50:22
*/
package com.android.hcframe.iterator;

import com.android.hcframe.HcLog;

import java.util.Iterator;
import java.util.Stack;

/**
 * 一个复合模式的遍历器
 * <p>事例</p>
 * <pre>
 * CompositeIterator iterator = new CompositeIterator(iterator());
 * while (iterator.hasNext()) {
 *	  ContactsInfo info = iterator.next();	
 * }
 * </pre>
 * 注意:这里遍历出的包括部门
 * @author jrjin
 * @time 2015-10-13 下午2:31:30
 */
public class CompositeIterator implements Iterator<TreeInfo> {

	private Stack<Iterator<TreeInfo>> mStack = new Stack<Iterator<TreeInfo>>();
	
	public CompositeIterator(Iterator<TreeInfo> iterator) {
		mStack.push(iterator);
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		if (mStack.empty()) {
			return false;
		} else {
			Iterator<TreeInfo> iterator = mStack.peek();
			if (!iterator.hasNext()) {
				mStack.pop();
				return hasNext();
			} else {
				return true;
			}
		}
		
	}

	@Override
	public TreeInfo next() {
		// TODO Auto-generated method stub
		if (hasNext()) {
			HcLog.D(" next stack size = "+mStack.size());
			Iterator<TreeInfo> iterator = mStack.peek();
			TreeInfo info = iterator.next();
			if (info instanceof NodeInfo) { // 这里需要测试
				mStack.push(info.getInfos().iterator());
				HcLog.D(" next push!");
			}
			return info;
		} else {
			return null;
		}
	}

	/**
	 * 这里只支持遍历，不支持删除
	 */
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("CompositeIterator remove is not supported!");
	}


}
