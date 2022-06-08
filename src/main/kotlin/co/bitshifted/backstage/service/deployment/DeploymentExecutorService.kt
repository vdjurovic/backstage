/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.service.deployment

import co.bitshifted.backstage.BackstageConstants
import co.bitshifted.backstage.util.maxThreadPoolSize
import co.bitshifted.backstage.util.threadPoolCoreSize
import org.apache.tomcat.util.threads.ThreadPoolExecutor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DeploymentExecutorService(
    val taskMap : ConcurrentHashMap<Runnable, String> = ConcurrentHashMap()) :
    ThreadPoolExecutor(
        threadPoolCoreSize(),
    maxThreadPoolSize(),
    BackstageConstants.threadPoolKeepAliveMs,
    TimeUnit.MILLISECONDS,
    ArrayBlockingQueue(threadPoolCoreSize()) ) {

    override fun beforeExecute(t: Thread?, r: Runnable?) {
        super.beforeExecute(t, r)
    }

    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
    }
}