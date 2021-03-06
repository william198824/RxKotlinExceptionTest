package com.william.rxkotlinexceptionoperatoritiontest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        val datas = resources.getStringArray(R.array.items)
        lv.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas)
        lv.setOnItemClickListener { _, _, _, id ->
            val realPosi = id.toInt()
            when (realPosi) {
                0 -> test0()
                1 -> test1()
                2 -> test2()
                3 -> test3()
                4 -> test4()
                5 -> test5()
                6 -> test6()
                7 -> test7()
                8 -> test8()
                9 -> test9()
                10 -> test10()
                11 -> test11()
                12 -> test12()
            }
        }
    }

    /**
     * 使用Single时，如果直接在onSuccess中抛出异常，不会回调onError，而是会崩溃。
     * 联想到success和error的单选规则就可以知道这样是必然的，既然已经回调了onSuccess，single就不会再捕获「检查异常」了。
     */
    private fun test0() {
        Single.just(0)
                .subscribe({
                    throw Exception("我擦")
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 使用Observable或Flowable时，如果直接在onNext中抛出异常会回调onError。
     */
    private fun test1() {
        Observable.just(0)
                .subscribe({
                    throw Exception("我擦")
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 如果map中直接返回Exception，该exception会被当作转换结果传递到onNext。
     */
    private fun test2() {
        Single.just(0)
                .map {
                    Exception("我擦")
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }


    /**
     * 如果map中抛出Exception，则会回调onError。那么在使用retrofit访问接口返回码判断的时候，就很推荐这样用！
     */
    private fun test3() {
        Single.just(0)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    1
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 在map中抛出异常，但后面使用了onErrorResumeNext，回调了onError,
     * 但是我发现onErrorResumeNext中需要返回跟源操作符一致的操作符，
     * 比如我用的single，那么也需要在onErrorResumeNext中返回一个single，不能返回其他。
     * 可以理解为专门用作错误处理的map，在一些场景，比如调用接口A失败就改调用B接口，或者用来自动切换环境的时候很有用。
     */
    private fun test4() {
        Single.just(0)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    1
                }
                .onErrorResumeNext {
                    Single.error(it)
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 在map中抛出异常，但后面使用了onErrorReturn,onErrorReturn在接收到错误的时候把元数据0转换成了2，
     * 这一点跟map很相似，他专门用作错误处理，可以理解为一旦接收到错误就返回一个默认值传递到onNext方法，然后调用onComplete终止发射。
     */
    private fun test5() {
        Observable.range(0, 100)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    1
                }
                .onErrorReturn {
                    2
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                }, {
                    Log.d("测试", "调用onComplete")
                })
    }

    /**
     * 在map中抛出异常，但后面使用了onErrorReturnItem(2),其实就是onErrorReturn的简写。
     */
    private fun test6() {
        Single.just(0)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    1
                }
                .onErrorReturnItem(2)
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 当map中抛出异常并回调onError后，可以取消订阅停止发送。
     */
    private fun test7() {
        Observable.range(0, 100)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    it * -1
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 当map中抛出异常,经过onErrorResumeNext回调onError后，可以取消订阅停止发送。
     */
    private fun test8() {
        Observable.range(0, 100)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    it * -1
                }
                .onErrorResumeNext(Function {
                    Observable.error(it)
                })
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 当使用Single时在onSuccess中调用onError。
     */
    private fun test9() {
        Single.just(0)
                .subscribe(object : SingleObserver<Int> {

                    private lateinit var d: Disposable

                    override fun onSubscribe(d: Disposable) {
                        this.d = d
                    }

                    override fun onSuccess(t: Int) {
                        onError(Exception("我擦"))
                    }

                    override fun onError(e: Throwable) {

                        Log.e("测试", e.message)
                        d.dispose()
                    }

                })
    }

    /**
     * 当使用Observable时在onNext中调用onError。
     */
    private fun test10() {
        Observable.just(0)
                .subscribe(object : Observer<Int> {

                    private lateinit var d: Disposable

                    override fun onSubscribe(d: Disposable) {
                        this.d = d
                    }

                    override fun onNext(t: Int) {
                        onError(Exception("我擦"))
                    }

                    override fun onError(e: Throwable) {

                        Log.e("测试", e.message)

                    }

                    override fun onComplete() {
                        d.dispose()
                    }
                })
    }

    /**
     *  onExceptionResumeNext只能捕获exception,而一旦遇到exception就会启用备用的Obervable
     */
    private fun test11() {
        Observable.range(0, 100)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    Log.d("测试","仍在发射${it}")
                    it * -1
                }
                .onExceptionResumeNext(Observable.range(1,99))
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }

    /**
     * 遇错重试，可以用来做轮询
     */
    private fun test12() {
        Observable.range(0, 100)
                .map {
                    if (it == 0) {
                        throw Exception("我擦")
                    }
                    Log.d("测试","仍在发射${it}")
                    it
                }
                .retry { t1, t2 ->
                    t1 != 50
                }
                .subscribe({
                    Log.d("测试", it.toString())
                }, {
                    Log.e("测试", it.message)
                })
    }
}
