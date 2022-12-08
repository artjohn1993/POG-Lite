package com.generator.poglite.presenter

import com.generator.poglite.api.GoogleSheetServices
import com.generator.poglite.model.GoogleSheet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class GoogleSheetPresenterClass(var view: GoogleSheetView, var api: GoogleSheetServices) : GoogleSheetPresenter {
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun getUrl(url: String) {
        println(url)
        compositeDisposable.add(
            api.getUrl(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ result ->
                    view.responseGetUrl(result)
                }, { error ->
                    view.responseGetUrlFailed("The caller does not have permission")
                })
        )
    }
}

interface GoogleSheetPresenter {
    fun getUrl(url: String)
}

interface GoogleSheetView {
    fun responseGetUrl(data: GoogleSheet.Result)
    fun responseGetUrlFailed(data: String)
}