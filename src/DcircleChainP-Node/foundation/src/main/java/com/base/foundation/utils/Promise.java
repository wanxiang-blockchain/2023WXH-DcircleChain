package com.base.foundation.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;

import com.anywithyou.stream.FinalValue;

import org.jetbrains.annotations.NotNull;

import kotlin.Function;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

public class Promise<Value> {

    public interface Always {
        void run();
    }

    public interface Task<Value, Next>{
        @NonNull
        Promise<Next> run(Value value);
    }

    public interface PendTask<Value, Next> {
        void run(Value value, Resolve<Next> resolve, Reject reject);
    }

    public interface PendTaskByThread<Value, Next> {
        void run(Value value, Resolve<Next> resolve, Reject reject);
    }

    static public abstract class Resolve<Value> {
        public abstract void run(Value value);
        public void run() {run(null);}
    }

    static public abstract class Reject extends Resolve<Error>{
    }

    public interface Pend<Value> {
        void run(Resolve<Value> resolve, Reject reject);
    }

    public interface PendByThread<Value> {
        void run(Resolve<Value> resolve, Reject reject);
    }

    public Promise(@NonNull Pend<Value> task) {

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Promise must be called in mainThread");
        }

        final Promise<Value> promise = this;
        final FinalValue<Boolean> async = new FinalValue<>(false);

        try {
            task.run(new Resolve<>() {
                @Override
                public void run(Value value) {
                    promise.value_ = value;

                    // 必须异步，否则下一步的处理函数不会获取到；也就是必须先执行then，才能执行这里的逻辑
                    // 同时也满足Promise规范中的 每一步处理都必须是异步的
                    // promise已经是异步，则doThe就无需异步，否则，需要异步
                    if (async.value) {
                        promise.state_.resolve();
                    } else {
                        promise.state_.resolveAsync();
                    }
                }
            }, new Reject() {
                @Override
                public void run(Error error) {
                    promise.error_ = error;

                    // 必须异步，否则下一步的处理函数不会获取到；也就是必须先执行then，才能执行这里的逻辑
                    // 同时也满足Promise规范中的 每一步处理都必须是异步的
                    // promise已经是异步，则doThe就无需异步，否则，需要异步
                    if (async.value) {
                        promise.state_.reject();
                    } else {
                        promise.state_.rejectAsync();
                    }
                }
            });
        }catch (Exception e) {
            e.printStackTrace();

            promise.error_ = new Error(e);

            // 必须异步，否则下一步的处理函数不会获取到；也就是必须先执行then，才能执行这里的逻辑
            // 同时也满足Promise规范中的 每一步处理都必须是异步的
            // promise已经是异步，则doThe就无需异步，否则，需要异步
            if (async.value) {
                promise.state_.reject();
            } else {
                promise.state_.rejectAsync();
            }
        }

        async.value = true;
    }

    public Promise(@NonNull final PendByThread<Value> task) {
        this(new Pend<>() {
            @Override
            public void run(final Resolve<Value> resolve, final Reject reject) {
                //final Handler handler = new Handler();
                //替换为携程
                try {
                    task.run(new Resolve<>() {
                        @Override
                        public void run(final Value value) {
                            new PromiseKt().runCoroutine(resolve, value);
                         /*   handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    resolve.run(value);
                                }
                            });*/
                        }
                    }, new Reject() {
                        @Override
                        public void run(final Error error) {
                            new PromiseKt().runCoroutine(reject, error);
                       /*     handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reject.run(error);
                                }
                            });*/
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    new PromiseKt().runCoroutine(reject, new Error(e));
                 /*   handler.post(new Runnable() {
                        @Override
                        public void run() {
                            reject.run(new Error(e));
                        }
                    });*/
                }


           /*     new Thread(new Runnable() {
                    @Override
                    public void run() {


                    }
                }).start();*/
            }
        });
    }

    public <Next> Promise<Next> then(@NonNull Task<Value, Next> resolve) {
        return this.then(resolve, null);
    }

    public <Next> Promise<Next> then(@NonNull final PendTask<Value, Next> task) {
        return this.then(new Task<>() {
            @NonNull
            @Override
            public Promise<Next> run(final Value value) {
                return defaultPromise(new Pend<>() {
                    @Override
                    public void run(Resolve<Next> resolve, Reject reject) {
                        task.run(value, resolve, reject);
                    }
                });
            }
        });
    }

    public <Next> Promise<Next> then(@NonNull final PendTaskByThread<Value, Next> task) {
        return this.then(new PendTask<>() {
            @Override
            public void run(final Value value, final Resolve<Next> resolve, final Reject reject) {
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.run(value, new Resolve<>() {
                                @Override
                                public void run(final Next next) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            resolve.run(next);
                                        }
                                    });
                                }
                            }, new Reject() {
                                @Override
                                public void run(final Error error) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            reject.run(error);
                                        }
                                    });
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reject.run(new Error(e));
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    public Promise<Value> caught(@NonNull Task<Error, Value> reject) {
        return this.then(new Task<>() {
            @NonNull
            @Override
            public Promise<Value> run(Value o) {
                return Promise.resolve(o);
            }
        }, reject);
    }

    public Promise<Value> caught(@NonNull final PendTask<Error, Value> task) {
        return this.caught(new Task<>() {
            @NonNull
            @Override
            public Promise<Value> run(final Error error) {
                return defaultPromise(new Pend<>() {
                    @Override
                    public void run(Resolve<Value> resolve, Reject reject) {
                        task.run(error, resolve, reject);
                    }
                });
            }
        });
    }

    public Promise<Value> caught(@NonNull final PendTaskByThread<Error, Value> task) {
        return this.caught(new PendTask<>() {

            @Override
            public void run(final Error error, final Resolve<Value> resolve, final Reject reject) {
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.run(error, new Resolve<>() {
                                @Override
                                public void run(final Value value) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            resolve.run(value);
                                        }
                                    });
                                }
                            }, new Reject() {
                                @Override
                                public void run(final Error error) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            reject.run(error);
                                        }
                                    });
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reject.run(new Error(e));
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    public Promise<Value> always(final Always always) {

        this.resolve_ = new Task<Value, Value>() {
            @NonNull
            @Override
            public Promise<Value> run(Value o) {
                always.run();
                return Promise.resolve((Value)o);
            }
        };

        this.reject_ = new Task<Error, Value>() {
            @NonNull
            @Override
            public Promise<Value> run(Error error) {
                always.run();
                return Promise.reject(error);
            }
        };

        Promise<Value> ret = defaultPromise(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
            }
        });

        this.nextPromise_ = ret;

        return ret;

    }

    @NonNull
    public static <Value> Promise<Value> resolve(@NonNull final Value value) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
                resolve.run(value);
            }
        });
    }

    @NonNull
    public static Promise<Void> resolve() {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Void> resolve, Reject reject) {
                resolve.run((Void) null);
            }
        });
    }

    @NonNull
    public static <Value> Promise<Value> race(
            @NonNull final ArrayList<Promise<Value>> promises) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(final Resolve<Value> resolve, final Reject reject) {
                for (Promise<Value> promise : promises) {
                    //这里是同步
                    promise.then(new Task<Value, Value>() {
                        @NonNull
                        @Override
                        public Promise<Value> run(Value value) {
                            resolve.run(value);
                            return Promise.resolve(value);
                        }
                    }).caught(new Task<>() {
                        @NonNull
                        @Override
                        public Promise<Value> run(Error error) {
                            reject.run(error);
                            return Promise.over();
                        }
                    });
                }
            }
        });
    }

    @NonNull
    public static <Value> Promise<ArrayList<Value>> all(
            @NonNull final ArrayList<Promise<Value>> promises) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(final Resolve<ArrayList<Value>> resolve, final Reject reject) {

                if (promises.isEmpty()) {
                    resolve.run(new ArrayList<>());
                }

                final Object[] ret = new Object[promises.size()];
                final FinalValue<Integer> cnt = new FinalValue<>(promises.size());

                for (int i = 0; i < promises.size(); ++i) {
                    final int index = i;
                    promises.get(i).then(new Task<Value, Value>() {
                        @NonNull
                        @Override
                        public Promise<Value> run(Value value) {
                            ret[index] = value;
                            cnt.value--;
                            if (cnt.value == 0) {
                                ArrayList<Value> retTmp = new ArrayList<>(promises.size());
                                for (Object r : ret) {
                                    retTmp.add((Value) r);
                                }
                                resolve.run(retTmp);
                            }
                            return Promise.resolve(value);
                        }
                    }).caught(new Task<>() {
                        @NonNull
                        @Override
                        public Promise<Value> run(Error error) {
                            reject.run(error);
                            return Promise.over();
                        }
                    });
                }
            }
        });
    }

    @NonNull
    public static <Value> Promise<Value> reject(@NonNull final Error error) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
                reject.run(error);
            }
        });
    }

    @NonNull
    public static <Value> Promise<Value> reject(@NonNull final String msg) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
                reject.run(new Error(msg));
            }
        });
    }

    @NonNull
    public static <Value> Promise<Value> reject(@NonNull final Exception e) {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
                reject.run(new Error(e));
            }
        });
    }

    @NonNull
    public static <Value> Promise<Value> over() {
        return new Promise<>(new Pend<>() {
            @Override
            public void run(Resolve<Value> resolve, Reject reject) {
            }
        });
    }

    protected <Next> Promise<Next> defaultPromise(Pend<Next> pend) {
        return new Promise<>(pend);
    }

    protected void finish() {
    }

    protected void changeToPending() {
        this.state_ = new Pending(this);
    }

    protected void changeToFulfilled() {
        this.state_ = new Fulfilled();
    }

    protected void changeToRejected() {
        this.state_ = new Rejected();
    }

    @SuppressWarnings("unchecked")
    private <Next> Promise<Next> then(Task<Value, Next> resolve
            , Task<Error, Next> reject) {

        this.resolve_ = resolve != null? resolve : new Task<Value, Next>() {
            @NonNull
            @Override
            public Promise<Next> run(Value o) {
                return Promise.resolve((Next)o);
            }
        };

        this.reject_ = reject != null? reject : new Task<Error, Next>() {
            @NonNull
            @Override
            public Promise<Next> run(Error error) {
                return Promise.reject(error);
            }
        };

        Promise<Next> ret = defaultPromise(new Pend<>() {
            @Override
            public void run(Resolve<Next> resolve, Reject reject) {
            }
        });

        this.nextPromise_ = ret;

        return ret;
    }

    private void setNext(Promise next) {
        if (this.nextPromise_ == null || next == null) {
            return;
        }

        next.resolve_ = this.nextPromise_.resolve_;
        next.reject_ = this.nextPromise_.reject_;
        next.nextPromise_ = this.nextPromise_.nextPromise_;
    }

    private void doResolve() {
        if (this.resolve_ == null) {
            return;
        }

        Promise next = null;

        try {
            @SuppressWarnings("unchecked")
            Promise ret = this.resolve_.run(this.value_);
            next = ret;
        } catch (Exception e) {
            e.printStackTrace();

            next = Promise.reject(new Error("Exception: ", e));
        }

        finish();
        setNext(next);
    }

    private void doReject() {
        if (this.reject_ == null) {
            return;
        }

        Promise next = null;

        try {
            @SuppressWarnings("unchecked")
            Promise ret = this.reject_.run(this.error_);
            next = ret;
        } catch (Exception e) {
            e.printStackTrace();

            next = Promise.reject(new Error("Exception: ", e));
        }

        finish();
        setNext(next);
    }

    private Task resolve_ = null;
    private Task reject_ = null;

    private Promise nextPromise_ = null;
    private Value value_;
    private Error error_ = new Error("default");
    private State state_ = new Pending(this);

    // -------  state  --------------

    private class State {
        public void resolve(){}
        public void reject() {}

        public void resolveAsync(){
            final State that = this;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    that.resolve();
                }
            });
        }

        public void rejectAsync() {
            final State that = this;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    that.reject();
                }
            });
        }
    }

    private class Pending extends State {
        Pending(Promise<?> promise) {
            this.promise_ = promise;
        }

        public void resolve(){
            promise_.changeToFulfilled();
            promise_.doResolve();
        }

        public void reject() {
            promise_.changeToRejected();
            promise_.doReject();
        }

        Promise<?> promise_;
    }

    private class Fulfilled extends State { }

    private class Rejected extends State { }

}
