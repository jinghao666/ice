// **********************************************************************
//
// Copyright (c) 2003-2014 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package IceInternal;

public class RetryQueue
{
    RetryQueue(Instance instance)
    {
        _instance = instance;
    }

    synchronized public void
    add(OutgoingAsyncBase outAsync, int interval)
    {
        if(_instance == null)
        {
            throw new Ice.CommunicatorDestroyedException();
        }
        RetryTask task = new RetryTask(this, outAsync);
        task.setFuture(_instance.timer().schedule(task, interval, java.util.concurrent.TimeUnit.MILLISECONDS));
        _requests.add(task);
    }

    synchronized public void
    destroy()
    {
         java.util.HashSet<RetryTask> keep = new java.util.HashSet<RetryTask>();
        for(RetryTask task : _requests)
        {
            if(!task.destroy())
            {
                keep.add(task);
            }
        }
        _requests = keep;
        _instance = null;

        //
        // Wait for the tasks to be executed, it shouldn't take long
        // since they couldn't be canceled. If interrupted, we
        // preserve the interrupt.
        //
        boolean interrupted = false;
        while(!_requests.isEmpty())
        {
            try
            {
                wait();
            }
            catch(InterruptedException ex)
            {
                interrupted = true;
            }
        }
        if(interrupted)
        {
            Thread.currentThread().interrupt();
        }
    }

    synchronized void
    remove(RetryTask task)
    {
        _requests.remove(task);
        if(_instance == null && _requests.isEmpty())
        {
            notify();
        }
    }

    private Instance _instance;
    private java.util.HashSet<RetryTask> _requests = new java.util.HashSet<RetryTask>();
}
