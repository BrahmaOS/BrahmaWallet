package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

public abstract class BaseService {

    protected Context context;
    protected abstract String tag();

    public boolean init(Context context) {
        this.context = context;
        return true;
    }

    public boolean destroy() {
        return true;
    }
}
