package androidx.lifecycle;

import android.annotation.SuppressLint;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.lifecycle.Lifecycle.State;

import com.gooey.common.utils.ReflectUtils;


public class LifeLiveData<T> extends MutableLiveData<T> {
    public LifeLiveData(T value) {
        super(value);
    }

    public LifeLiveData() {
    }

    @MainThread
    @SuppressLint({"RestrictedApi"})
    public void observeWithNoStick(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() != State.DESTROYED) {
            CustomerLifecycleBoundObserver wrapper = new CustomerLifecycleBoundObserver(owner, observer);

            try {
                SafeIterableMap mObserverss = (SafeIterableMap) ReflectUtils.getField(true, LiveData.class, this, "mObservers");
                Object existingObserverWrapper = mObserverss.putIfAbsent(observer, wrapper);
                if (existingObserverWrapper instanceof LifeLiveData.CustomerLifecycleBoundObserver) {
                    if (!((CustomerLifecycleBoundObserver) existingObserverWrapper).isAttachedTo(owner)) {
                        throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
                    }

                    return;
                }

                owner.getLifecycle().addObserver(wrapper);
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }
    }

    @MainThread
    @SuppressLint({"RestrictedApi"})
    public void observeForeverWithNoStick(@NonNull Observer<? super T> observer) {
        assertMainThread("observeForever");
        CustomerAlwaysActiveObserver wrapper = new CustomerAlwaysActiveObserver(observer);

        try {
            SafeIterableMap mObserverss = (SafeIterableMap) ReflectUtils.getField(true, LiveData.class, this, "mObservers");
            Object existingObserverWrapper = mObserverss.putIfAbsent(observer, wrapper);
            if (existingObserverWrapper instanceof LifeLiveData.CustomerLifecycleBoundObserver) {
                throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
            }

            if (existingObserverWrapper != null) {
                return;
            }

            wrapper.activeStateChanged(true);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    private class CustomerAlwaysActiveObserver extends CustomerLifecycleBoundObserver {
        CustomerAlwaysActiveObserver(Observer observer) {
            super((LifecycleOwner) null, observer);
        }

        boolean shouldBeActive() {
            return true;
        }

        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        }

        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        void detachObserver() {
        }
    }

    class CustomerLifecycleBoundObserver extends LiveData<T>.LifecycleBoundObserver {
        CustomerLifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer observer) {
            super(owner, observer);
            this.mLastVersion = LifeLiveData.this.getVersion();
        }
    }
}
