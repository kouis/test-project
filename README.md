#test-project

用于练习github使用

##测试抓取微博信息

####Tencent API

<font color="red">每个操作url，都要使用 Cookie</font>

账号检测：
检查账号是否要验证码
http://check.ptlogin2.qq.com/check?uin=:account&appid=1003903&r=" + Math.random()
参数：
	uin 账号
	appid 腾讯应用id，固定
	r 随机数，防缓存
返回值：
	ptui_checkVC('0','!RT1','\x00\x00\x00\x00\x00\x5e\xbb\x3d');
	第一个： 0 需要验证码， 1 不需要验证码
	第二个：如果 ！ 开头，直接为明文验证码用于登录加密密码，否则获取验证码
	第三个：登录密码加密串

-----------------------------

验证码：
http://captcha.qq.com/getimage?aid=1003903&r=" + Math.random() + "&uin=" + account
参数：
	uin 账号
返回值：
	InputStream,图片流

注：这个验证码将会代替为 账号检测第二个参数

-----------------------------

登录：
http://ptlogin2.qq.com/login?u=:account&p=:password&verifycode=:VCode&webqq_type=10&remember_uin=1&login2qq=1&aid=1003903&u1=:loginurl&h=1&ptredirect=0&ptlang=2052&from_ui=1&pttype=1&dumy=&fp=loginerroralert&action=7-24-1937704&mibao_css=m_webqq&t=1&g=1
参数：
	u 登录账号
	p 加密后的密码(参考 encodePass.js[passwordEncoding(password, \x00第三个, 验证码)])
	verifycode 验证码
	u1 登录产品的url, http://web.qq.com/loginproxy.html?login2qq=1&webqq_type=10
返回值：
	ptuiCB('0','0','http://web3.qq.com/loginproxy.html?login2qq=1&webqq_type=40','0','登录成功！', '承∮诺');
	第一个参数的意义:
		0：登录成功!
		1：系统繁忙，请稍后重试。
		2：已经过期的QQ号码。
		3：您输入的密码有误，请重试。
		4：您输入的验证码有误，请重试。es
		5：校验失败。
		6：密码错误。如果您刚修改过密码, 请稍后再登录.
		7：您的输入有误, 请重试。
		8：您的IP输入错误的次数过多，请稍后再试。

-----------------------------

获取微博信息
第一页使用：
http://api1.t.qq.com/asyn/home.php?&&apiType=8&apiHost=http://api.t.qq.com&_r=:_r1&_r=:_r2
参数：
	第一个_r1: 时间截 ， 不知是什么，猜想为当前时间的时间截
	第二个_r2: 时间截，猜想为当前时间的时间截加上6263
返回值：
	JSON形式的微博数据

-----------------------------

获取微博信息
下一页使用：
http://api1.t.qq.com/asyn/home.php?&time=:time&page=:page&id=:id&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=:r
参数：
	time 上一次请求返回的最后一条记录时间，对应JSON数据中的timestamp
	page 下页的页数
	id 上一次请求返回的最后一条记录id，对应JSON数据中的id
	r 时间截 ， 猜想为当前时间的时间截
返回值：
	JSON形式的微博数据

<font color="red">注意通过http://api1.t.qq.com/asyn/home.php抓取微博信息时，需要设置请求头Referer与User-Agent的信息。</font>