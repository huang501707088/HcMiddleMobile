/**
 * @author jrjin
 * @time 2015-11-24 上午10:40:47
 */
/**
 * IM模块角标说明
 * 1.IM内部的角标独立,不再总的树中
 * 2.IM的应用角标在树结构中,然后在里面添加一个叶节点,便于操作
 * 3.之后的操作都操作这个叶节点就可以了
 *
 * 方式二是把IM内部的东西也放入到树中,但这样会操作比较麻烦.
 * //// 2017.04.10
 * 4.增加用户认证登录时候传递token值到服务端{@link org.apache.qpid.management.common.sasl.PlainSaslClient#evaluateChallenge}
 */
package com.android.hcframe.im;