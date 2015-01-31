package com.github.tsouza.promises.internal.functions;

import com.github.tsouza.promises.internal.RxObserverAdapter;
import rx.functions.Func0;

public interface OnFinally<O> extends Func0<RxObserverAdapter<O>> {
}
