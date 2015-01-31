package com.github.tsouza.promises.internal.functions;

import com.github.tsouza.promises.internal.RxObserverAdapter;
import rx.functions.Func1;

public interface OnRejected<O> extends Func1<Throwable, RxObserverAdapter<O>> {
}
