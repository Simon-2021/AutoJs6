package org.autojs.autojs.rhino

import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.UniqueTag

/**
 * Created by Stardust on 2017/5/17.
 * Modified by SuperMonster003 as of Apr 12, 2023.
 */
open class ProxyObject() : NativeObject() {

    private var mGetter: NativeFunction? = null
    private var mSetter: NativeFunction? = null

    constructor(scope: Scriptable, proxyObject: Any) : this() {
        val proxy = proxyObject as NativeObject
        val getter = proxy["get", scope]
        if (getter is NativeFunction) {
            mGetter = getter
        }
        val setter = proxy["set", scope]
        if (setter is NativeFunction) {
            mSetter = setter
        }
    }

    override fun put(name: String, start: Scriptable, value: Any?) {
        if (name == "__proxy__") {
            val proxy = value as NativeObject
            val getter = proxy["get", start]
            if (getter is NativeFunction) {
                mGetter = getter
            }
            val setter = proxy["set", start]
            if (setter is NativeFunction) {
                mSetter = setter
            }
        } else if (mSetter != null) {
            mSetter!!.call(Context.getCurrentContext(), start, start, arrayOf(name, value))
        } else {
            super.put(name, start, value)
        }
    }

    fun getWithoutProxy(name: String?, start: Scriptable?): Any {
        return super.get(name, start)
    }

    override fun get(name: String, start: Scriptable): Any {
        var value = super.get(name, start)
        if (value != null && value !== UniqueTag.NOT_FOUND) {
            return value
        }
        if (mGetter != null) {
            value = mGetter!!.call(Context.getCurrentContext(), start, start, arrayOf<Any>(name))
        }
        return value
    }

    override fun getDefaultValue(typeHint: Class<*>?) = toString()

}