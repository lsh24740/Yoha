package com.gooey.common.state;
import android.os.Looper


/**
 * @author lishihui01
 * @Date 2023/9/1
 * @Describe:
 */
abstract class CachedStateMachine(name: String) : StateMachine(name, Looper.getMainLooper()) {
    private val states = HashMap<Int, State>()

    operator fun get(state: Int): State {
        return states[state]!!
    }

    fun transitionTo(state: Int) {
        transitionTo(get(state))
    }

    final override fun addState(state: State?, parent: State?) {
        if (state is BaseState) {
            states[state.id] = state
        }
        if (parent is BaseState) {
            states[parent.id] = parent
        }
        super.addState(state, parent)
    }

    final override fun addState(state: State?) {
        if (state is BaseState) {
            states[state.id] = state
        }
        super.addState(state)
    }

    final override fun removeState(state: State?) {
        if (state is BaseState) {
            states.remove(state.id)
        }
        super.removeState(state)
    }
}

open class BaseState(val id: Int) : State()
