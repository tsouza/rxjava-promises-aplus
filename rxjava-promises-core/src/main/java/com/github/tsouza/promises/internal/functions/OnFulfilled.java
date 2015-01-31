package com.github.tsouza.promises.internal.functions;

import com.github.tsouza.promises.internal.RxObserverAdapter;
import rx.functions.Func1;

public interface OnFulfilled<I, O> extends Func1<I, RxObserverAdapter<O>> {
}
