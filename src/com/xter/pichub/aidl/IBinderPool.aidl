package com.xter.pichub.aidl;

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}