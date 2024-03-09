package com.gooey.network.datasource

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
enum class Status {
    READY,
    SUCCESS,
    ERROR,
    LOADING,
    EMPTY,
    NOMORE,
    INSERTED,
    REMOVED,
    CLEAR,
    SET,
    CHANGE,
    BATCH
}

open class Resource<out T>(open val status: Status, open val data: T?, open val error: Throwable?) {

    open var code : Int = 0
    open var message : String? = null
    var rParam : Any? = null

    companion object {
        private const val CODE_SUCCESS = 200

        // 无param
        fun <T> success(data: T?): Resource<T> {
            return success(data, CODE_SUCCESS, null)
        }

        // 无param
        fun <T> success(data: T?, code: Int): Resource<T> {
            return success(data, code, null)
        }

        // 有param
        fun <T> success(data: T?, param: Any?): Resource<T> {
            return success(data, CODE_SUCCESS, param)
        }

        fun <T> success(data: T?, code: Int, param: Any?): Resource<T> {
            val resource = Resource(Status.SUCCESS, data, null)
            resource.code = code
            resource.rParam = param
            return resource
        }

        fun <T> error(error: Throwable?, data: T?, code : Int, msg : String?): Resource<T> {
            return error(error, data, code, msg, null)
        }

        fun <T> error(error: Throwable?, data: T?, code : Int, msg : String?, param : Any?): Resource<T> {
            val resource = Resource(Status.ERROR, data, error)
            resource.code = code
            resource.message = msg
            resource.rParam = param
            return resource
        }

        fun <T> loading(data: T?): Resource<T> {
            return loading(data, null)
        }

        fun <T> loading(data: T?, param: Any?): Resource<T> {
            val resource = Resource(Status.LOADING, data, null)
            resource.rParam = param
            return resource
        }

        fun <T> empty(): Resource<T> {
            return Resource(Status.EMPTY, null, null)
        }

        fun <T> nomore(): Resource<T> {
            return Resource(Status.NOMORE, null, null)
        }
    }

    fun isSuccess() : Boolean {
        return status == Status.SUCCESS
    }
    fun isLoading() : Boolean {
        return status == Status.LOADING
    }

    fun isError() : Boolean {
        return status == Status.ERROR
    }

}

open class ParamResource<P, R>(override val status: Status,
                               var param: P?,
                               override val data: R? = null,
                               override val error: Throwable? = null,
                               override var code: Int = 0,
                               override var message: String? = null) : Resource<R>(status, data, error) {
    /**
     * 接口返回的xtraceid
     */
    var xTraceId: String? = null

    companion object {
        fun <P, R> success(param: P?, data: R?): ParamResource<P, R> {
            return ParamResource(Status.SUCCESS, param, data)
        }

        fun <P, R> error(param: P?, data: R? = null, error: Throwable? = null,
                         code: Int = 0, message: String? = null): ParamResource<P, R> {
            return ParamResource(Status.ERROR, param, data, error, code, message)
        }

        fun <P, R> loading(param: P?): ParamResource<P, R> {
            return ParamResource(Status.LOADING, param)
        }
    }
}