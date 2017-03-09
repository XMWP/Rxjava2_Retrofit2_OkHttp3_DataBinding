package com.dsc.databindingdemo.presenter;

import android.content.Intent;

import com.dsc.databindingdemo.R;
import com.dsc.databindingdemo.api.GankApiService;
import com.dsc.databindingdemo.core.ServiceHelper;
import com.dsc.databindingdemo.model.GankData;
import com.dsc.databindingdemo.presenter.vm.FCViewModel;
import com.dsc.databindingdemo.ui.WebActivity;
import com.dsc.databindingdemo.utils.ToastUtil;
import com.reny.mvpvmlib.BasePresenter;
import com.reny.mvpvmlib.http.converter.ResultErrorException;

import cn.bingoogolapple.androidcommon.adapter.BGABindingViewHolder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by reny on 2017/1/4.
 */

public class FCPresenter extends BasePresenter<FCViewModel> {

    private String category = GankApiService.category_b;
    private int count;
    int page = 1;

    @Override
    public void onCreatePresenter() {
        viewModel.innerAdapter.setItemEventHandler(this);
        count = context.getResources().getInteger(R.integer.load_count);//每页加载条数
        loadData(true);
    }

    @Override
    public void loadData(final boolean isRefresh) {
        if(isRefresh) page = 1;

        addDisposable(ServiceHelper.getGankAS().getGankIoData(category, count, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<GankData>() {
                    @Override
                    public void onNext(GankData value) {
                        if(value.isError())throw new ResultErrorException(-1, "数据错误");
                        page++;
                        viewModel.setData(isRefresh, value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isRefresh && viewModel.firstLoadDataSuc){
                            ToastUtil.showShort(R.string.refresh_error);
                        }
                        onFailure(e);
                    }

                    @Override
                    public void onComplete() {}
                })
        );
    }

    //列表Item点击 与xml绑定
    public void onClickItem(BGABindingViewHolder holder, GankData.ResultsBean model) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("url", model.getUrl());
        context.startActivity(intent);
    }

}
