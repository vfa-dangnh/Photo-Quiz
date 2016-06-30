package com.haidangkf.photoquiz;

import org.json.JSONObject;

interface AsyncResult
{
    void onResult(JSONObject object);
}