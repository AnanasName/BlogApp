package com.codingwithmitch.openapi.repository

import kotlinx.coroutines.Job

open class JobManager(
    private val className: String
) {

    private val jobs: HashMap<String, Job> = HashMap()

    fun addJob(methodName: String, job: Job){
        cancelJob(methodName)
        jobs[methodName] = job
    }

    fun cancelJob(methodName: String){
        getJob(methodName)?.cancel()
    }

    fun getJob(methodName: String): Job?{
        if (jobs.containsKey(methodName)){
            jobs[methodName]?.let {
                return it
            }
        }
        return null
    }

    fun cancelActiveJobs(){
        for ((method, job) in jobs){
            if (job.isActive){
                job.cancel()
            }
        }
    }
}