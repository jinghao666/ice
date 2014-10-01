// **********************************************************************
//
// Copyright (c) 2003-2014 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package IceInternal;

class RetryTask implements Runnable
{
    RetryTask(RetryQueue queue, OutgoingAsyncBase outAsync)
    {
        _queue = queue;
        _outAsync = outAsync;
    }

    @Override
    public void
    run()
    {
        try
        {
            _outAsync.processRetry();
        }
        catch(Ice.LocalException ex)
        {
            _outAsync.invokeExceptionAsync(ex);
        }
        
        //
        // NOTE: this must be called last, destroy() blocks until all task
        // are removed to prevent the client thread pool to be destroyed
        // (we still need the client thread pool at this point to call
        // exception callbacks with CommunicatorDestroyedException).
        //
        _queue.remove(this);
    }

    public boolean
    destroy()
    {
        if(_future.cancel(false))
        {
            _outAsync.invokeExceptionAsync(new Ice.CommunicatorDestroyedException());
            return true;
        }
        return false;
    }

    public void setFuture(java.util.concurrent.Future<?> future)
    {
        _future = future;
    }

    private final RetryQueue _queue;
    private final OutgoingAsyncBase _outAsync;
    private java.util.concurrent.Future<?> _future;
}
