package com.thebluecode.trxautophone;


import com.thebluecode.trxautophone.models.Step;

public interface OnStepSelectedListener {
    void onStepSelected(Step.Type type);
}