package com.base.foundation.db

/**
 * 所有的本地数据库的表的基类
 * 所有的继承类的类名都会作为表名存储在本地数据库
 */
interface Table {
	/**
	 * 创建表
	 */
	fun onCreate()

	/**
	 * 获得表名 即 表名即为类名
	 */
	fun getName():String {
		return this::class.java.simpleName
	}
}