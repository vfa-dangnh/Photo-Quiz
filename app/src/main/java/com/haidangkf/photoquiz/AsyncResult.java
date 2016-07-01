package com.haidangkf.photoquiz;

import org.json.JSONObject;

interface AsyncResult
{
    void onFinishProcess(JSONObject object);
}